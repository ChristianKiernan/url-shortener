package com.christiankiernan.urlshortener.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.IOException;

@NullMarked
public record JsonRedisSerializer<T>(ObjectMapper mapper, Class<T> targetType) implements RedisSerializer<T> {

    @Override
    public byte[] serialize(@Nullable T value) throws SerializationException {
        try {
            return mapper.writeValueAsBytes(value);
        } catch (IOException e) {
            throw new SerializationException("Could not serialize value", e);
        }
    }

    @Override
    public T deserialize(byte @Nullable [] bytes) throws SerializationException {
        try {
            return mapper.readValue(bytes, targetType);
        } catch (IOException e) {
            throw new SerializationException("Could not deserialize value", e);
        }
    }
}
