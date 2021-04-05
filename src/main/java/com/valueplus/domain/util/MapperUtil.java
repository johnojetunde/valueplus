package com.valueplus.domain.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import java.io.IOException;

public class MapperUtil {
    public static final ObjectMapper MAPPER;

    private MapperUtil() {
    }

    static {
        MAPPER = (new ObjectMapper()).disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
                .registerModule(new ParameterNamesModule());
    }

    public static <T> T copy(T original, Class<T> objClass) {
        return copy(MAPPER, original, objClass);
    }

    public static <T> T copy(ObjectMapper mapper, T original, Class<T> objClass) {
        TokenBuffer intermediateTokenBuffer = new TokenBuffer(mapper, false);

        try {
            mapper.writeValue(intermediateTokenBuffer, original);
            return mapper.readValue(intermediateTokenBuffer.asParser(), objClass);
        } catch (IOException var5) {
            throw new IllegalArgumentException(var5);
        }
    }
}
