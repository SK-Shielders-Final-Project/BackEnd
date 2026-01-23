package org.rookies.zdme.service;

import java.util.List;
import java.util.stream.Collectors;

import org.rookies.zdme.dto.bike.*;
import org.rookies.zdme.exception.ForbiddenException;
import org.rookies.zdme.exception.NotFoundException;
import org.rookies.zdme.model.entity.Bike;
import org.rookies.zdme.model.entity.User;
import org.rookies.zdme.repository.BikeRepository;
import org.rookies.zdme.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BikeService {

    private final BikeRepository bikeRepository;
    private final UserRepository userRepository;

    public BikeService(BikeRepository bikeRepository, UserRepository userRepository) {
        this.bikeRepository = bikeRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<BikeResponse> listByStatusCode(Integer statusCode) {
        String dbStatus = toDbStatusFromCode(statusCode);
        return bikeRepository.findAllByStatusOrderByBikeIdAsc(dbStatus)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BikeStatusUpdateResponse updateStatus(Long adminId, Long bikeId, String statusKorean) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("admin not found"));

        if (admin.getAdminLevel() == null || admin.getAdminLevel() < 1) {
            throw new ForbiddenException("admin permission required");
        }

        Bike bike = bikeRepository.findById(bikeId)
                .orElseThrow(() -> new NotFoundException("bike not found"));

        bike.setStatus(toDbStatusFromKorean(statusKorean));
        Bike saved = bikeRepository.save(bike);

        return BikeStatusUpdateResponse.builder()
                .bike_id(saved.getBikeId())
                .status(toKoreanStatus(saved.getStatus()))
                .updated_at(saved.getUpdatedAt())
                .build();
    }

    private BikeResponse toResponse(Bike b) {
        int code = toCodeFromDbStatus(b.getStatus());
        return BikeResponse.builder()
                .bike_id(b.getBikeId())
                .serial_number(b.getSerialNumber())
                .model_name(b.getModelName())
                .status_code(code)
                .status(toKoreanStatus(b.getStatus()))
                .latitude(b.getLatitude())
                .longitude(b.getLongitude())
                .build();
    }

    private String toDbStatusFromCode(Integer code) {
        if (code == null) throw new IllegalArgumentException("status is required");
        if (code == 0) return "IN_USE";
        if (code == 1) return "AVAILABLE";
        if (code == 2) return "REPAIRING";
        throw new IllegalArgumentException("invalid status code: " + code);
    }

    private int toCodeFromDbStatus(String db) {
        if ("IN_USE".equals(db)) return 0;
        if ("AVAILABLE".equals(db)) return 1;
        if ("REPAIRING".equals(db)) return 2;
        return -1;
    }

    private String toDbStatusFromKorean(String kor) {
        if (kor == null) throw new IllegalArgumentException("status is required");
        String s = kor.trim();
        if ("사용중".equals(s)) return "IN_USE";
        if ("가용".equals(s) || "가용 가능".equals(s)) return "AVAILABLE";
        if ("고장".equals(s) || "수리중".equals(s)) return "REPAIRING";
        throw new IllegalArgumentException("invalid status: " + kor);
    }

    private String toKoreanStatus(String db) {
        if ("IN_USE".equals(db)) return "사용중";
        if ("AVAILABLE".equals(db)) return "가용";
        if ("REPAIRING".equals(db)) return "고장";
        return "알수없음";
    }
    @Transactional(readOnly = true)
    public List<BikeResponse> listAll() {
        return bikeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

}
