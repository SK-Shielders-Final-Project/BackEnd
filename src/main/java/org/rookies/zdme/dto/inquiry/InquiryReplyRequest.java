package org.rookies.zdme.dto.inquiry;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InquiryReplyRequest {
    private Long inquiry_id;
    private String admin_reply;
}
