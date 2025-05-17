package org.personal.dinoshare.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.personal.dinoshare.config.PropsConfig;
import org.personal.dinoshare.exception.FileSizeException;
import org.personal.dinoshare.exception.FileTypeException;
import org.personal.dinoshare.exception.FileValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ValidationService {

    private final PropsConfig propsConfig;
    private List<String> allowedMimeTypes;

    @Value("${app.upload.max-file-size}")
    private long maxFileSize;

    @PostConstruct
    public void init() {
        this.allowedMimeTypes = propsConfig.getMimeTypes();
    }

    public void validateFile(MultipartFile file) throws FileValidationException {

        // 파일을 선택하지 않은 경우
        if (file == null || file.isEmpty()) {
            throw new FileValidationException("File is empty");
        }

        // 파일 크기 검증
        long maxFileB = maxFileSize * 1024 * 1024;
        if (file.getSize()>maxFileB) {
            throw new FileSizeException("File size exceeds limit: " + file.getName(), file.getSize(), maxFileSize);
        }

        // MIME 타입 검증
        String contentType = file.getContentType();

        if (contentType == null || contentType.trim().isEmpty()) {
            throw new FileTypeException("File content-type is empty: " + file.getName(), contentType, allowedMimeTypes);
        }

        if (!allowedMimeTypes.stream().anyMatch(allowedType -> allowedType.equalsIgnoreCase(contentType))) {
            throw new FileTypeException("File type not allowed: " + file.getName() + "\nAllowed types: " + String.join(", ", allowedMimeTypes), contentType, allowedMimeTypes);
        }
    }
}
