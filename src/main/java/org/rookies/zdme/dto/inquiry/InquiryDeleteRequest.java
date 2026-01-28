package org.rookies.zdme.dto.inquiry;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InquiryDeleteRequest {
    private Long user_id;
    private Long inquiry_id;
    private Integer admin_level;
}
