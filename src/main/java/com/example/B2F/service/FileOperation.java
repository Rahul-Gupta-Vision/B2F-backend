package com.example.B2F.service;

import com.backblaze.b2.client.exceptions.B2Exception;
import com.example.B2F.entities.User;
import com.example.B2F.wrappers.FileDataResponse;
import com.example.B2F.wrappers.FileOPResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface FileOperation {
    FileOPResponse uploadFile(String filename, InputStream inputStream, long contentLength, String contentType, String fileType) throws Exception;
    FileOPResponse deleteFile(String fileId) throws B2Exception;
    FileOPResponse uploadLargeFile(String filename, InputStream inputStream, long contentLength, String contentType, String fileType) throws Exception;
    @Async
    CompletableFuture<FileOPResponse> uploadFileAsync(MultipartFile file);
    List<FileDataResponse> getAllUploadedFiles(String userId) throws Exception;
}
