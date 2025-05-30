package org.personal.dinoshare.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.personal.dinoshare.domain.LinkDetails;
import org.personal.dinoshare.dto.FileDetailsDTO;
import org.personal.dinoshare.service.LinkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Controller
@RequestMapping("/share")
@RequiredArgsConstructor
public class DownloadController {

    @Value("${app.upload.dir}")
    private String uploadDir;

    private final LinkService linkService;
    private static final Logger logger = LoggerFactory.getLogger(DownloadController.class);

    @GetMapping("/{link}")
    public String showFileList(@PathVariable String link, Model model) {
        LinkDetails linkDetails = linkService.getLink(link);

        if (linkDetails == null) {
            model.addAttribute("errorMessage", "링크가 존재하지 않습니다.");
        } else if (linkDetails.getFiles() == null || linkDetails.getFiles().isEmpty()) {
            model.addAttribute("errorMessage", "파일이 존재하지 않습니다.");
        }

        model.addAttribute("linkDetails", linkDetails);
        return "download-list";
    }

    @GetMapping("/download/{link}/{fileName}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String link,
            @PathVariable String fileName,
            HttpServletRequest request) {

        LinkDetails linkDetails = linkService.getLink(link);

        if (linkDetails == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "공유 링크가 유효하지 않습니다.: " + link);
        }

        Optional<FileDetailsDTO> target = linkDetails.getFiles().stream()
                .filter(dto -> dto.getFileName().equals(fileName))
                .findFirst();

        FileDetailsDTO targetDTO = target.orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "파일이 존재하지 않습니다.")
        );

        try {
            // 파일 저장 경로;
            Path filePath = Paths.get(uploadDir).resolve(targetDTO.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            // 파일 유효성 검증
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found: " + link);
            }

            // MIME 타입 검증
            String contentType = null;

            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException e) {
                logger.error("Fail to determine file type: {} - {}", link, e.getMessage());
            } catch (NullPointerException e) {
                logger.error("File is null: {} - {}", link, e.getMessage());
            }

            // 기본값 사용
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            String originalFileName = resource.getFilename();

            if (originalFileName == null) {
                originalFileName = link;
            }

            // 헤더 설정
            String encodedFileName = URLEncoder.encode(originalFileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            String headerValue = "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName;

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                    .body(resource);

        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid URL: " + link);
        }
    }

//    @GetMapping("/{link}")
//    public ResponseEntity<Resource> downloadFile(@PathVariable String link, HttpServletRequest request) {
//
//        try {
//            // 파일 저장 경로
//            FileDetailsDTO fileDetail = linkService.getLink(link).getFiles().get(0);
//            Path filePath = Paths.get(uploadDir).resolve(fileDetail.getFilePath()).normalize();
//            Resource resource = new UrlResource(filePath.toUri());
//
//            // 파일 유효성 검증
//            if (!resource.exists() || !resource.isReadable()) {
//                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found: " + link);
//            }
//
//            // MIME 타입 검증
//            String contentType = null;
//
//            try {
//                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
//            } catch (IOException e) {
//                System.err.println("Fail to determine file type: " + link + " - " + e.getMessage());
//            } catch (NullPointerException e) {
//                System.err.println("File is null: " + link + " - " + e.getMessage());
//            }
//
//            // 기본값 사용
//            if (contentType == null) {
//                contentType = "application/octet-stream";
//            }
//
//            String originalFilename = resource.getFilename();
//
//            if (originalFilename == null) {
//                originalFilename = link;
//            }
//
//            // 헤더 설정
//            String encodedFileName = URLEncoder.encode(originalFilename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
//            String headerValue = "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName;
//
//            return ResponseEntity.ok()
//                    .contentType(MediaType.parseMediaType(contentType))
//                    .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
//                    .body(resource);
//
//        } catch (MalformedURLException e) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid URL: " + link);
//        }
//    }
}
