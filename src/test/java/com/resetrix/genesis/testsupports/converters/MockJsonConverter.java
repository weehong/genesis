package com.resetrix.genesis.testsupports.converters;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;

import java.util.List;

/**
 * Mock converter class with "Json" but not "Jackson" in the name to test the first branch
 * of the isConverterTypeJackson condition.
 */
public class MockJsonConverter implements HttpMessageConverter<Object> {

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return List.of();
    }

    @Override
    public Object read(Class<? extends Object> clazz, HttpInputMessage inputMessage) {
        return null;
    }

    @Override
    public void write(Object o, MediaType contentType, HttpOutputMessage outputMessage) {
    }
}
