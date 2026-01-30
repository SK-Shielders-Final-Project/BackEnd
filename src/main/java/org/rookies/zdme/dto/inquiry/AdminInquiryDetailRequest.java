package org.rookies.zdme.dto.inquiry;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminInquiryDetailRequest {
    private Long inquiry_id;
    private Integer admin_level;
}
