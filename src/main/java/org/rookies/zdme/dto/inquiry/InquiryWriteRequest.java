package org.rookies.zdme.dto.inquiry;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InquiryWriteRequest {
    private String title;
    private String content;
    private Long file_id; // optional
}
