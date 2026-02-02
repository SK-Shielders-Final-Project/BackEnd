package org.rookies.zdme.controller;

import lombok.RequiredArgsConstructor;
import org.rookies.zdme.dto.RentalRequestDto;
import org.rookies.zdme.dto.RentalResponseDto;
import org.rookies.zdme.dto.RentalsDto;
import org.rookies.zdme.service.RentalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/point")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    @PostMapping("")
    public ResponseEntity<?> startRental(@RequestBody RentalRequestDto reqDto) {
        try {
            RentalResponseDto resDto = rentalService.startRental(reqDto);
            return ResponseEntity.ok(resDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

    }
    @GetMapping("")
    public ResponseEntity<?> getRentals() {
        List<RentalsDto> rentals = rentalService.getRentals();
        return ResponseEntity.ok(rentals);
    }
}

