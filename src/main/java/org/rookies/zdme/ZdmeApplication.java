package org.rookies.zdme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.rookies.zdme.config.FileStorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableJpaAuditing
@SpringBootApplication
@EnableConfigurationProperties(FileStorageProperties.class)
public class ZdmeApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZdmeApplication.class, args);
    }
}

