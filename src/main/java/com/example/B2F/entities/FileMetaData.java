package com.example.B2F.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "files")
public class FileMetaData {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @PrePersist
    public void generateId() {
        this.id = UUID.randomUUID().toString();
    }

    @Column(nullable = false)
    private String filename;

    @ManyToOne
    @JoinColumn(name = "uploader_id", referencedColumnName = "id", nullable = false)
    private User uploader;

    @Column(name = "b2_file_id", nullable = false)
    private String b2FileId;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    @Column(nullable = false)
    private long size;

    @Column(name = "file_type", nullable = false)
    private String fileType;
}
