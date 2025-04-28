package com.example.B2F.wrappers;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileOPResponse {
    @JsonProperty("fileId")
    String fileId;
    @JsonProperty("fileName")
    String fileName;
}
