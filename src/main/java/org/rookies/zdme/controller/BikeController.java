package org.rookies.zdme.controller;

import java.util.List;

import org.rookies.zdme.dto.bike.BikeListRequest;
import org.rookies.zdme.dto.bike.BikeResponse;
import org.rookies.zdme.service.BikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class BikeController {

    private final BikeService bikeService;

    public BikeController(BikeService bikeService) {
        this.bikeService = bikeService;
    }

    @PostMapping({"/bikes", "/bikes/"})
    public ResponseEntity<List<BikeResponse>> list() {
        return ResponseEntity.ok(bikeService.listAll());
    }


    @PutMapping("/admin/bike")
    public ResponseEntity<?> updateStatus(
            @RequestBody org.rookies.zdme.dto.bike.BikeStatusUpdateRequest request // response body
    ) {
        return ResponseEntity.ok(
                bikeService.updateStatus(request.getBike_id(), request.getStatus())
        );
    }
}
