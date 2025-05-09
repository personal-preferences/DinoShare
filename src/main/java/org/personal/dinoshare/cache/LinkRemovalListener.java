package org.personal.dinoshare.cache;

import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import lombok.RequiredArgsConstructor;
import org.personal.dinoshare.domain.LinkDetails;
import org.personal.dinoshare.dto.FileDetailsDTO;
import org.personal.dinoshare.service.IpLimitService;
import org.personal.dinoshare.service.UploadService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LinkRemovalListener implements RemovalListener<String, LinkDetails> {

    private final UploadService uploadService;
    private final IpLimitService ipLimitService;


    @Override
    public void onRemoval(String link, LinkDetails details, RemovalCause cause) {

        if (details==null) {
            return;
        }

        List<FileDetailsDTO> files = details.getFiles();

        if (files!=null) {
            for (FileDetailsDTO file : files) {
                try {
                    uploadService.deleteFiles(file.getFilePath());
                    System.out.println("File deleted: " + file.getFilePath());
                } catch (Exception e) {
                    System.err.println("Fail to delete " + file.getFilePath() + ": " + e.getMessage());
                }
            }
        } else {
            System.out.println("No file found");
        }

        String clientIp = details.getClientIp();

        if (clientIp!=null && !clientIp.trim().isEmpty()) {

            try {
                ipLimitService.decrementCount(clientIp);
                System.out.println("Decremented IP count: " + clientIp);
            } catch (Exception e) {
                System.err.println("Fail to decrement IP count " + clientIp + ": " + e.getMessage());
            }
        } else {
            System.out.println("No client IP found");
        }
    }
}
