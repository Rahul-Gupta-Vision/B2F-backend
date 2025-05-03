package com.example.B2F.wrappers;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileDataResponse {
    @JsonProperty("file_id")
    String file_id;
    @JsonProperty("file_name")
    String file_name;
    @JsonProperty("b2f_file_id")
    String b2f_file_id;
    @JsonProperty("file_type")
    String file_type;
    @JsonProperty("file_size")
    String file_size;
    @JsonProperty("upload_time")
    String uploaded_time;
}
