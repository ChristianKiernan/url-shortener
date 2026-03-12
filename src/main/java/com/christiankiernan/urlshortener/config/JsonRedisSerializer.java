package com.christiankiernan.urlshortener.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.IOException;

public class JsonRedisSerializer<T> implements RedisSerializer<T> {

    private final ObjectMapper mapper;
    private final Class<T> targetType;

    public JsonRedisSerializer(ObjectMapper mapper, Class<T> targetType) {
        this.mapper = mapper;
        this.targetType = targetType;
    }

    @Override
    public byte[] serialize(T value) throws SerializationException {
        if (value == null) return null;
        try {
            return mapper.writeValueAsBytes(value);
        } catch (IOException e) {
            throw new SerializationException("Could not serialize value", e);
        }
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null) return null;
        try {
            return mapper.readValue(bytes, targetType);
        } catch (IOException e) {
            throw new SerializationException("Could not deserialize value", e);
        }
    }
}
