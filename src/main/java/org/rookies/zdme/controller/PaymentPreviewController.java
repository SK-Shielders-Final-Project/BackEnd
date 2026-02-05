package org.rookies.zdme.controller;

import lombok.RequiredArgsConstructor;
import org.rookies.zdme.dto.ReceiptPreviewRequest;
import org.rookies.zdme.exception.ForbiddenException;
import org.rookies.zdme.exception.NotFoundException;
import org.rookies.zdme.model.entity.Payment;
import org.rookies.zdme.model.entity.User;
import org.rookies.zdme.repository.PaymentRepository;
import org.rookies.zdme.repository.UserRepository;
import org.rookies.zdme.security.SecurityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/api/payment/receipt")
@RequiredArgsConstructor
public class PaymentPreviewController {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @PostMapping("/preview")
    public String previewReceipt(@RequestBody ReceiptPreviewRequest request, Model model) {
        // 1. Fetch Current User
        String username = SecurityUtil.getCurrentUsername();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));

        // 2. Fetch Payment
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new NotFoundException("Payment with ID " + request.getPaymentId() + " not found."));

        // 3. Validate Ownership
        if (!payment.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new ForbiddenException("Access denied: Payment does not belong to the current user.");
        }

        // Add payment and userMemo to model
        model.addAttribute("payment", payment);
        model.addAttribute("userMemo", request.getUserMemo());

        return "fragments/receipt-view";
    }
}
