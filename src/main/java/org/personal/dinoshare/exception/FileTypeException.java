package org.personal.dinoshare.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FileTypeException extends FileValidationException{

    private final String contentType;
    private final List<String> allowedTypes;

    public FileTypeException(String message, String contentType, List<String> allowedTypes) {

        super(message);

        this.contentType = contentType;
        this.allowedTypes = allowedTypes;
    }
}
