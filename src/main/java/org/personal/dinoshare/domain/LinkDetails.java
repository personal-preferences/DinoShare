package org.personal.dinoshare.domain;

import lombok.*;
import org.personal.dinoshare.dto.FileDetailsDTO;

import java.time.Duration;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class LinkDetails {

    private String link;
    private List<FileDetailsDTO> files;
    private Duration expMinutes;
    private String clientIp;
}