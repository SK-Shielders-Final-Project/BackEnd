package org.rookies.zdme.dto.inquiry;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InquiryResponse {
    private Long inquiry_id;
    private Long user_id;
    private String title;
    private String content;
    private String image_url;
    private Long file_id;
    private Integer admin_level;
    private String admin_reply;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created_at;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updated_at;
}
