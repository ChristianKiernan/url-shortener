CREATE TABLE shortened_url (
    id          BIGSERIAL PRIMARY KEY,
    url         TEXT                     NOT NULL,
    short_code  VARCHAR(255)             NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE,
    updated_at  TIMESTAMP WITH TIME ZONE,
    access_count INTEGER                 NOT NULL DEFAULT 0,
    CONSTRAINT uq_shortened_url_short_code UNIQUE (short_code)
);
