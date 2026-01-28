package org.rookies.zdme.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.io.Resource;

@Getter
@AllArgsConstructor
public class DownloadableFile {
    private Resource resource;
    private String filename;
}
