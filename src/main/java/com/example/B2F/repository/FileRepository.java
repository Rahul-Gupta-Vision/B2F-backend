package com.example.B2F.repository;

import com.example.B2F.entities.FileMetaData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<FileMetaData, String> {
}
