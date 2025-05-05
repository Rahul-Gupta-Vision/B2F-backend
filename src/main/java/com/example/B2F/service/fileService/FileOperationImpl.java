package com.example.B2F.service.fileService;

import com.backblaze.b2.client.B2StorageClient;
import com.backblaze.b2.client.contentSources.B2ContentSource;
import com.backblaze.b2.client.contentSources.B2FileContentSource;
import com.backblaze.b2.client.exceptions.B2Exception;
import com.backblaze.b2.client.exceptions.B2UnauthorizedException;
import com.backblaze.b2.client.structures.B2Bucket;
import com.backblaze.b2.client.structures.B2FileVersion;
import com.backblaze.b2.client.structures.B2UploadFileRequest;
import com.example.B2F.entities.FileMetaData;
import com.example.B2F.entities.User;
import com.example.B2F.repository.FileRepository;
import com.example.B2F.repository.UserRepository;
import com.example.B2F.service.FileOperation;
import com.example.B2F.wrappers.FileDataResponse;
import com.example.B2F.wrappers.FileOPResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class FileOperationImpl implements FileOperation {

    @Value("${backblaze.bucketName}")
    private String bucketName;

    private final B2StorageClient b2StorageClient;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    @Override
    public FileOPResponse uploadFile(String filename, InputStream inputStream, long contentLength, String contentType, String fileType) throws B2Exception, UsernameNotFoundException {
        B2Bucket b2Bucket = b2StorageClient.getBucketOrNullByName(bucketName);
        if(b2Bucket == null){
            throw new IllegalStateException("Bucket Not found");
        }
        B2ContentSource source = new InputStreamB2ContentSource(inputStream, contentLength);
        B2UploadFileRequest request = B2UploadFileRequest
                                    .builder(b2Bucket.getBucketId(), filename, contentType, source).build();
        B2FileVersion fileVersion = b2StorageClient.uploadSmallFile(request);
        var user = getCurrentUser();
        var fData = FileMetaData.builder()
                .fileType(fileType)
                .filename(filename)
                .size(contentLength)
                .uploader(user)
                .b2FileId(fileVersion.getFileId())
                .uploadedAt(Instant.now()).build();
        fileRepository.save(fData);
        return FileOPResponse.builder().fileId(fileVersion.getFileId()).fileName(filename).build();
    }

    @Override
    public FileOPResponse deleteFile(String fileId) throws B2Exception {
        var file  = fileRepository.findById(fileId);
        if(file.isEmpty()){
            return null;
        }
        String b2FileID = file.get().getB2FileId();
        String fileName = file.get().getFilename();
        b2StorageClient.deleteFileVersion(fileName, b2FileID);
        fileRepository.deleteById(fileId);
        return FileOPResponse.builder().fileName(fileName).fileId(b2FileID).build();
    }

    @Override
    public FileOPResponse uploadLargeFile(String filename, InputStream inputStream, long contentLength, String contentType, String fileType) throws Exception {
        B2Bucket b2Bucket = b2StorageClient.getBucketOrNullByName(bucketName);
        if(b2Bucket == null){
            throw new IllegalStateException("Bucket Not found");
        }
        File tempFile = File.createTempFile("b2f-upload-large",".tmp");
        try(FileOutputStream fos = new FileOutputStream(tempFile)){
            inputStream.transferTo(fos);
        }
        B2FileContentSource source = B2FileContentSource.builder(tempFile).build();
        ExecutorService executor = Executors.newFixedThreadPool(4);
        B2UploadFileRequest request = B2UploadFileRequest
                .builder(b2Bucket.getBucketId(), filename, contentType, source).build();
        B2FileVersion fileVersion;
        try {
            fileVersion = b2StorageClient.uploadLargeFile(request, executor);
        } finally {
            executor.shutdown();
            tempFile.delete();
        }
        var user = getCurrentUser();
        var fData = FileMetaData.builder()
                .fileType(fileType)
                .filename(filename)
                .size(contentLength)
                .uploader(user)
                .b2FileId(fileVersion.getFileId())
                .uploadedAt(Instant.now()).build();
        fileRepository.save(fData);
        return FileOPResponse.builder().fileId(fileVersion.getFileId()).fileName(filename).build();
    }

    @Async
    @Override
    public CompletableFuture<FileOPResponse> uploadFileAsync(MultipartFile file) {
        try{
            int idx = file.getOriginalFilename().lastIndexOf('.');
            String fileName = file.getOriginalFilename().substring(0, idx);
            String filetype = file.getOriginalFilename().substring(idx+1);
            FileOPResponse response = (file.getSize() > 100 * 1024 * 1024)
                    ? uploadLargeFile(fileName, file.getInputStream(), file.getSize(), file.getContentType(), filetype)
                    : uploadFile(fileName, file.getInputStream(), file.getSize(), file.getContentType(), filetype);
            return CompletableFuture.completedFuture(response);
        }catch (Exception e){
            throw new RuntimeException("Failed to Upload file: "+e.getMessage());
        }
    }

    @Override
    public List<FileDataResponse> getAllUploadedFiles(String userId) throws Exception {
        User uploader = userRepository.findUserByUsername(userId).orElseThrow();
        List<FileMetaData> files = fileRepository.findByUploader(uploader);
        List<FileDataResponse> responses = new ArrayList<>();
        for(FileMetaData file:files){
            responses.add(FileDataResponse.builder().file_size(String.valueOf(file.getSize()))
                    .b2f_file_id(file.getB2FileId())
                    .file_name(file.getFilename())
                    .file_id(file.getId())
                    .file_type(file.getFileType())
                    .uploaded_time(convertInstantTimeToDateAndDay(file.getUploadedAt().toString())).build());
        }
        return responses;
    }


    private User getCurrentUser(){
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(principal instanceof UserDetails){
                String userName = ((UserDetails) principal).getUsername();
                return userRepository.findUserByUsername(userName).orElseThrow(()->new UsernameNotFoundException("Current User: User not found"));
            }else {
                throw new RuntimeException("Unauthorized user");
            }
    }

    private String convertInstantTimeToDateAndDay(String time){
        Instant instant = Instant.parse(time);
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
        String date = zonedDateTime.toLocalDate().toString();
        String day = zonedDateTime.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        return date+", "+day;
    }
}
