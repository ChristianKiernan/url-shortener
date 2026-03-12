import http from 'k6/http';
import { check } from 'k6';

// Cache validation test.
//
// Purpose: demonstrate the difference between cold and warm cache performance.
//
// How to use:
//   1. Start the app fresh (or flush Redis with: docker exec <redis-container> redis-cli FLUSHALL)
//   2. Run this test:       k6 run cache-test.js
//   3. Watch Grafana — you will see cache misses drop and hits climb as the cache warms up
//   4. Run the test again immediately — almost all requests will be cache hits
//   5. Compare p99 latency between the two runs
//
// What to watch in Grafana:
//   - cache_gets_total{result="hit"} vs {result="miss"} — hit rate climbing over time
//   - http_server_requests_seconds (p99) — latency drop as cache warms
//   - hikaricp_connections_active — DB pressure falling on second run

export const options = {
    scenarios: {
        cache_validation: {
            executor: 'constant-vus',
            vus: 50,
            duration: '60s',
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<500'],
        http_req_failed: ['rate<0.01'],
    },
};

const BASE_URL = 'http://localhost:8080';
const API_URL = `${BASE_URL}/api/v1`;
const JSON_HEADERS = { headers: { 'Content-Type': 'application/json' } };

export function setup() {
    // Large enough pool that the cold run sustains DB activity long enough
    // for Prometheus (15s scrape interval) to capture it.
    const codes = [];
    for (let i = 0; i < 500; i++) {
        const res = http.post(
            `${API_URL}/shorten`,
            JSON.stringify({ url: `https://example.com/cache-test/${i}` }),
            JSON_HEADERS
        );
        const code = res.json('shortCode');
        if (code) codes.push(code);
    }
    return { codes };
}

export default function (data) {
    const code = data.codes[Math.floor(Math.random() * data.codes.length)];

    // Hit the redirect endpoint — this is the cached path.
    // redirects: 0 prevents k6 from following the 302 to example.com,
    // so we measure our app's response time only.
    const res = http.get(`${BASE_URL}/${code}`, { redirects: 0 });
    check(res, { 'status is 302': (r) => r.status === 302 });
}
