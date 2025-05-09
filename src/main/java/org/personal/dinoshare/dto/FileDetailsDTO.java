package org.personal.dinoshare.dto;

import lombok.*;

import java.nio.file.Path;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class FileDetailsDTO {

    private String orginalFileName;
    private String fileName;
    private Path filePath;
    private long fileSize;
    private String contentType;
}
