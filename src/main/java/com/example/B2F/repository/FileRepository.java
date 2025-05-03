package com.example.B2F.repository;

import com.example.B2F.entities.FileMetaData;
import com.example.B2F.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<FileMetaData, String> {
    List<FileMetaData> findByUploader(User user);
}
