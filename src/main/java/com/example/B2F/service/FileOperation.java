package com.example.B2F.service;

import com.backblaze.b2.client.exceptions.B2Exception;
import com.example.B2F.wrappers.FileOPResponse;

import java.io.InputStream;

public interface FileOperation {
    FileOPResponse uploadFile(String filename, InputStream inputStream, long contentLength, String contentType, String fileType) throws Exception;
    FileOPResponse deleteFile(String fileId) throws B2Exception;
    FileOPResponse uploadLargeFile(String filename, InputStream inputStream, long contentLength, String contentType, String fileType) throws Exception;
}
