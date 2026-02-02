package org.rookies.zdme.service;

import lombok.RequiredArgsConstructor;
import org.rookies.zdme.dto.GiftHistoryResponseDto;
import org.rookies.zdme.model.entity.GiftHistory;
import org.rookies.zdme.model.entity.User;
import org.rookies.zdme.repository.GiftHistoryRepository;
import org.rookies.zdme.repository.UserRepository;
import org.rookies.zdme.security.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserRepository userRepository;
    private final GiftHistoryRepository giftHistoryRepository;

    @Transactional
    public void sendGift(String senderName, String receiverName, Long amount) {
        User sender = userRepository.findByUsername(senderName)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자(sender)입니다."));

        User receiver = userRepository.findByUsername(receiverName)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자(receiver)입니다."));

        sender.updatePoint(-amount);
        receiver.updatePoint(amount);

        GiftHistory history = GiftHistory.builder()
                .amount(amount)
                .receiver(receiver)
                .sender(sender)
                .build();

        giftHistoryRepository.save(history);
    }

    public List<GiftHistoryResponseDto> getGiftHistoryList() {
        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        List<GiftHistory> historyList = giftHistoryRepository.findBySenderOrReceiverOrderByCreatedAtDesc(user, user);

        return historyList.stream()
                .map(h -> GiftHistoryResponseDto.builder()
                        .senderName(h.getSender().getUsername())
                        .receiverName(h.getReceiver().getUsername())
                        .amount(h.getAmount())
                        .createdAt(h.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
