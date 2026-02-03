package org.rookies.zdme.controller;

import lombok.RequiredArgsConstructor;
import org.rookies.zdme.dto.RentalRequestDto;
import org.rookies.zdme.dto.RentalResponseDto;
import org.rookies.zdme.dto.RentalsDto;
import org.rookies.zdme.service.RentalService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @GetMapping("/search")
    public ResponseEntity<?> searchRentals(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "bikeId", required = false) String bikeId
            ) {
        List<RentalsDto> rentals = rentalService.searchRentals(startDate, endDate, bikeId);
        return ResponseEntity.ok(rentals);
    }
}

