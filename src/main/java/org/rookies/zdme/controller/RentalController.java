package org.rookies.zdme.controller;

import lombok.RequiredArgsConstructor;
import org.rookies.zdme.model.dto.RentalRequestDto;
import org.rookies.zdme.model.dto.RentalResponseDto;
import org.rookies.zdme.service.RentalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/point")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    @PostMapping("")
    public ResponseEntity<?> startRental(@RequestBody RentalRequestDto reqDto) {
        RentalResponseDto resDto = rentalService.startRental(reqDto);
        return ResponseEntity.ok(resDto);
    }
}
