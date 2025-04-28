package com.example.B2F.service.fileService;

import com.backblaze.b2.client.contentSources.B2ContentSource;
import com.backblaze.b2.client.exceptions.B2Exception;

import java.io.IOException;
import java.io.InputStream;


public class InputStreamB2ContentSource implements B2ContentSource {
    private final InputStream inputStream;
    private final Long contentLength;

    public InputStreamB2ContentSource(InputStream inputStream, Long contentLength){
        this.inputStream = inputStream;
        this.contentLength = contentLength;
    }
    @Override
    public long getContentLength() throws IOException {
        return contentLength;
    }

    @Override
    public String getSha1OrNull() throws IOException {
        return null;
    }

    @Override
    public Long getSrcLastModifiedMillisOrNull() throws IOException {
        return null;
    }

    @Override
    public InputStream createInputStream() throws IOException, B2Exception {
        return inputStream;
    }

}
