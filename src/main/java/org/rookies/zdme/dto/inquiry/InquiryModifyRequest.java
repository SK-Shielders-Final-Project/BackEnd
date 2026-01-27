package org.rookies.zdme.dto.inquiry;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InquiryModifyRequest {
    private Long inquiry_id;
    private String title;
    private String content;
    private Long file_id; // null이면 첨부 제거, 값 있으면 교체
}
