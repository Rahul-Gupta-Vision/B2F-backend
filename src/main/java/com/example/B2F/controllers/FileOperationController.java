package com.example.B2F.controllers;

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

@RestController
@RequestMapping("/api/B2F")
@RequiredArgsConstructor
public class FileOperationController {
    private final FileOperation fileOperation;

    @PostMapping("/upload")
    ResponseEntity<?>uploadFile(@RequestParam MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename().split("\\.")[0];
        String filetype = file.getOriginalFilename().split("\\.")[1];
        FileOPResponse fileRes = null;
        if(file.getSize() > 100){
            fileRes = fileOperation.uploadLargeFile(fileName, file.getInputStream(), file.getSize(), file.getContentType(), filetype);
        }else{
            fileRes = fileOperation.uploadFile(fileName, file.getInputStream(), file.getSize(), file.getContentType(), filetype);
        }
        return new ResponseEntity<>(fileRes, HttpStatus.OK);
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
}
