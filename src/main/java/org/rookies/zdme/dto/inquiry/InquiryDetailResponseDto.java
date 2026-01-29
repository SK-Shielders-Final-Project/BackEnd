package org.rookies.zdme.dto.inquiry;

import lombok.Builder;
import lombok.Getter;
import org.rookies.zdme.dto.file.FileDetailResponseDto;
import org.rookies.zdme.model.entity.Inquiry;
import org.rookies.zdme.model.entity.User;

import java.time.LocalDateTime;

@Getter
@Builder
public class InquiryDetailResponseDto {
    private final Long inquiryId;
    private final String title;
    private final String content;
    private final String authorName;
    private final LocalDateTime createdAt;
    private final String adminReply;
    private final LocalDateTime updatedAt;
    private final FileDetailResponseDto attachment;

    public static InquiryDetailResponseDto from(Inquiry inquiry) {
        User user = inquiry.getUser();
        String authorName = (user != null) ? user.getName() : "Unknown";

        return InquiryDetailResponseDto.builder()
                .inquiryId(inquiry.getInquiryId())
                .title(inquiry.getTitle())
                .content(inquiry.getContent())
                .authorName(authorName)
                .createdAt(inquiry.getCreatedAt())
                .adminReply(inquiry.getAdminReply())
                .updatedAt(inquiry.getUpdatedAt())
                .attachment(FileDetailResponseDto.from(inquiry.getFile()))
                .build();
    }
}
