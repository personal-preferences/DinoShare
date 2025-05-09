package org.personal.dinoshare.controller;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.personal.dinoshare.config.PropsConfig;
import org.personal.dinoshare.dto.FileDetailsDTO;
import org.personal.dinoshare.dto.UploadForm;
import org.personal.dinoshare.service.IpLimitService;
import org.personal.dinoshare.service.LinkService;
import org.personal.dinoshare.service.UploadService;
import org.personal.dinoshare.service.ValidationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class UploadController {

    private final IpLimitService ipLimitService;
    private final ValidationService validationService;
    private final UploadService uploadService;
    private final LinkService linkService;
    private final PropsConfig propsConfig;

    private List<Integer> allowedExpMinutes;

    @Value("${app.upload.max-file-count}")
    private int maxFileCount;

    @PostConstruct
    public void init() {
        this.allowedExpMinutes = propsConfig.getExpirationMinutes();
    }

    // upload-form.html
    @GetMapping
    public String showUploadForm(Model model) {
        UploadForm defaultForm = new UploadForm();

        // 리스트의 마지막 값을 기본으로 설정
        if (allowedExpMinutes !=null && !allowedExpMinutes.isEmpty()) {
            defaultForm.setExpMinutes(allowedExpMinutes.get(allowedExpMinutes.size()-1));
        } else {
            defaultForm.setExpMinutes(1440);
        }

        model.addAttribute("uploadForm", defaultForm);

        return "upload-form";
    }

    // 파일 업로드
    @PostMapping("/upload")
    public String handleUpload(
            @ModelAttribute("uploadForm") @Valid UploadForm uploadForm,
            BindingResult bindingResult,
            @RequestParam("files")List<MultipartFile> files,
            HttpServletRequest request,
            Model model) {

        // 로드 밸런서, CDN, 웹 방화벽, 리버스 프록시(Nginx, Apache 등) 같은 중간 장비가 존재하는 경우 -> 최초 클라이언트의 IP 주소 반환 X
        String clientIp = getClientIp(request);

        // IP 기반 활성 링크 수 제한
        if (ipLimitService.isOverLimit(clientIp)) {
            bindingResult.reject("ip.limit", "동일 IP의 생성 가능 활성 공유 링크 수 초과(최대 5개)");

            return "upload-form";
        }

        // @Valid 검증 결과
        if (bindingResult.hasErrors()) {

            return "upload-form";
        }

        // 파일을 선택하지 않은 경우
        if (files==null || files.isEmpty() || files.stream().allMatch(MultipartFile::isEmpty)) {
            bindingResult.reject("files.empty", "파일 부재");

            return "upload-form";
        }

        // 선택한 파일이 3개 초과한 경우
        if (files.size()>maxFileCount) {
            bindingResult.reject("files.too.many", "선택 파일 개수 초과(최대 3개)");

            return "upload-form";
        }

        // 파일 유효성 검증
        try {
            for (MultipartFile file : files) {
                validationService.validateFile(file);
            }
        } catch (ValidationException e) {
            bindingResult.reject("validation.failed", e.getMessage());

            return "upload-form";
        }

        Integer expMinutes = uploadForm.getExpMinutes();

        // 만료 시간 검증
        if (!allowedExpMinutes.contains(expMinutes)) {
            bindingResult.reject("expiration.not.allowed", "만료 시간 설정 불가: " + expMinutes + "분");

            return "upload-form";
        }

        Duration expDuration = Duration.ofMinutes(expMinutes);

        List<FileDetailsDTO> uploadFiles;
        String createdLink = "";

        // 파일 업로드
        try {
            uploadFiles = uploadService.uploadFiles(files);
        } catch (IOException e) {
            model.addAttribute("globalError", "파일 저장 중 오류 발생: " + e.getMessage());

            return "upload-form";
        } catch (RuntimeException e) {
            model.addAttribute("globalError", "파일 처리 중 오류 발생: " + e.getMessage());

            return "upload-form";
        }

        // 링크 생성 및 처리
        try {
            createdLink = linkService.createLink(uploadFiles, expDuration, clientIp);
        } catch (Exception e) {
            bindingResult.reject("link.created.failed", e.getMessage());

            return "upload-form";
        }

        model.addAttribute("shareUrl", "/share/" + createdLink);
        model.addAttribute("expirationTime", LocalDateTime.now().plus(expDuration));

        return "upload-success";
    }

    private String getClientIp(HttpServletRequest request) {

        String xfwd = request.getHeader("x-forwarded-for");

        if (xfwd!=null && !xfwd.isEmpty() && !"unknown".equalsIgnoreCase(xfwd)) {
            return xfwd.split(",")[0];
        }

        return request.getRemoteAddr();
    }
}
