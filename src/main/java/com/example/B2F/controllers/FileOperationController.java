package com.example.B2F.controllers;

import com.example.B2F.config.JwtService;
import com.example.B2F.service.FileOperation;
import com.example.B2F.wrappers.FileOPResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/B2F")
@RequiredArgsConstructor
public class FileOperationController {
    private final FileOperation fileOperation;
    private final JwtService jwtService;

    @PostMapping("/upload")
    ResponseEntity<?>uploadFile(@RequestParam MultipartFile[] file) throws Exception {
        List<CompletableFuture<FileOPResponse>> completableFutures = Arrays.stream(file)
                .map(f->fileOperation.uploadFileAsync(f))
                .toList();
        List<FileOPResponse> responses = completableFutures.stream()
                .map(CompletableFuture::join)
                .toList();
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/delete/{fileId}")
    ResponseEntity<?>deleteFile(@PathVariable String fileId) throws Exception{
        var res = fileOperation.deleteFile(fileId);
        if(res == null){
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping("/files")
    ResponseEntity<?>getAllFiles(@RequestHeader("Authorization") String header) throws Exception{
        if(header == null || !header.startsWith("Bearer ")){
            return new ResponseEntity<>("Invalid Token", HttpStatus.UNAUTHORIZED);
        }
        String jwtToken = header.substring(7);
        String userEmail = jwtService.extractUsername(jwtToken);
        return new ResponseEntity<>(fileOperation.getAllUploadedFiles(userEmail), HttpStatus.OK);
    }
}
