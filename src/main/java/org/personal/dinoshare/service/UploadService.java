package org.personal.dinoshare.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.personal.dinoshare.dto.FileDetailsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    private Path uploadPath;

    private static final Logger logger = LoggerFactory.getLogger(UploadService.class);

    @PostConstruct
    public void init() {

        try {
            uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Fail to create directory!");
        }
    }

    public List<FileDetailsDTO> uploadFiles(List<MultipartFile> files) throws IOException {

        List<FileDetailsDTO> fileDetails = new ArrayList<>();

        for (MultipartFile file : files) {
            String originalFilename = file.getOriginalFilename();

            // 파일 이름이 없는 경우
            if (originalFilename == null || originalFilename.trim().isEmpty()) {
                throw new FileNotFoundException("File name is empty!");
            }

            // 파일 확장자 추출
            String fileExtension = "";
            int dot = originalFilename.lastIndexOf(".");

            if (0<dot && dot<originalFilename.length()-1) {
                fileExtension = originalFilename.substring(dot+1);
            }

            // UUID를 사용한 파일 이름으로 경로 생성
            String fileName = UUID.randomUUID().toString() + "." + fileExtension;
            Path targetPath = uploadPath.resolve(fileName);

            System.out.println(targetPath);

            try {
                // 파일 저장
                Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                // 파일 객체 정보 생성
                FileDetailsDTO detail = new FileDetailsDTO(
                        originalFilename,
                        fileName,
                        targetPath,
                        file.getSize(),
                        file.getContentType()
                );

                fileDetails.add(detail);
            } catch (IOException e) {
                throw new IOException("Fail to upload file: " + originalFilename);
            } catch (Exception e) {
                throw new RuntimeException("Unexpected error while uploading file: " + originalFilename);
            }
        }
            return fileDetails;
    }

    public boolean deleteFiles(Path filePath) {

        try {
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            logger.error("Fail to delete file {}: {}", e.getMessage(), filePath);
//            System.err.println("Fail to delete file: " + filePath + " - " + e.getMessage());

            return false;
        }
    }

//    public FileDetailsDTO getFileDetails(String link) {
//        return file
//    }
}
