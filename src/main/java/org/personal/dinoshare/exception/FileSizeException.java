package org.personal.dinoshare.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileSizeException extends FileValidationException{

    private final long fileSize;
    private final long maxFileSize;

    public FileSizeException(String message, long fileSize, long maxFileSize) {

        super(message);

        this.fileSize = fileSize;
        this.maxFileSize = maxFileSize;
    }
}
