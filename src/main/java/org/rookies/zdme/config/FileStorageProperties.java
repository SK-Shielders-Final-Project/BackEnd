package org.rookies.zdme.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.file")
public class FileStorageProperties {

    private String baseDir;
    private long maxSizeBytes;
    private List<String> allowedExt;

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public long getMaxSizeBytes() {
        return maxSizeBytes;
    }

    public void setMaxSizeBytes(long maxSizeBytes) {
        this.maxSizeBytes = maxSizeBytes;
    }

    public List<String> getAllowedExt() {
        return allowedExt;
    }

    public void setAllowedExt(List<String> allowedExt) {
        this.allowedExt = allowedExt;
    }
}
