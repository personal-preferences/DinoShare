package org.personal.dinoshare.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties("app.upload")
public class PropsConfig {
    private List<Integer> expirationMinutes;
    private List<String> mimeTypes;
}
