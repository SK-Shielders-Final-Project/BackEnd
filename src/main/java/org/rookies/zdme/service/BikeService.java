package org.rookies.zdme.service;

import java.time.LocalDateTime; // LocalDateTime 임포트 추가
import java.util.List;
import java.util.stream.Collectors;

import org.rookies.zdme.dto.bike.*;
import org.rookies.zdme.exception.BadRequestException; // BadRequestException 임포트 추가
import org.rookies.zdme.exception.NotFoundException;
import org.rookies.zdme.model.entity.Bike;
import org.rookies.zdme.model.entity.File; // File 임포트 추가
import org.rookies.zdme.model.entity.User;
import org.rookies.zdme.model.enums.BikeStatus; // BikeStatus 임포트 추가
import org.rookies.zdme.repository.BikeRepository;
import org.rookies.zdme.repository.FileRepository; // FileRepository 임포트 추가
import org.rookies.zdme.repository.UserRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Paths;

@Service
public class BikeService {

    private final BikeRepository bikeRepository;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final FileService fileService; // FileService 의존성 추가

    public BikeService(BikeRepository bikeRepository, UserRepository userRepository, FileRepository fileRepository, FileService fileService) {
        this.bikeRepository = bikeRepository;
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
        this.fileService = fileService; // FileService 주입
    }

    @Transactional(readOnly = true)
    public List<BikeResponse> listByStatusCode(Integer statusCode) {
        BikeStatus dbStatus = toDbStatusFromCode(statusCode);
        return bikeRepository.findAllByStatusOrderByBikeIdAsc(dbStatus)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BikeStatusUpdateResponse updateStatus(Long bikeId, String statusKorean) {
        Bike bike = bikeRepository.findById(bikeId)
                .orElseThrow(() -> new NotFoundException("bike not found"));

        BikeStatus newStatus = toDbStatusFromKorean(statusKorean);
        bike.setStatus(newStatus);
        Bike saved = bikeRepository.save(bike);

        return BikeStatusUpdateResponse.builder()
                .bike_id(saved.getBikeId())
                .status(toKoreanStatus(saved.getStatus()))
                .updated_at(saved.getUpdatedAt())
                .build();
    }

    @Transactional
    public void rentBike(String serialNumber) {
        Bike bike = bikeRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new NotFoundException("해당 시리얼 넘버의 자전거를 찾을 수 없습니다."));

        switch (bike.getStatus()) {
            case AVAILABLE:
                bike.setStatus(BikeStatus.IN_USE);
                bikeRepository.save(bike);
                break;
            case REPAIRING:
                throw new BadRequestException("현재 수리중인 자전거는 대여할 수 없습니다.");
            case IN_USE:
                throw new BadRequestException("이미 사용중인 자전거입니다.");
        }
    }

    @Transactional
    public BikeReturnResponseDto returnBike(BikeReturnRequestDto requestDto) {
        // 1. 자전거 조회
        Bike bike = bikeRepository.findBySerialNumber(requestDto.getSerialNumber())
                .orElseThrow(() -> new NotFoundException("해당 시리얼 넘버의 자전거를 찾을 수 없습니다."));

        // 2. 상태 검증
        if (bike.getStatus() != BikeStatus.IN_USE) {
            throw new BadRequestException("현재 반납할 수 있는 상태의 자전거가 아닙니다.");
        }

        // 3. 파일 엔티티 조회 (반납 사진)
        File returnPhoto = fileRepository.findById(requestDto.getFileId())
                .orElseThrow(() -> new NotFoundException("제공된 파일 ID에 해당하는 사진을 찾을 수 없습니다."));

        // 4. 자전거 정보 업데이트
        bike.setStatus(BikeStatus.AVAILABLE); // 상태를 AVAILABLE로 변경
        bike.setLatitude(requestDto.getLatitude());
        bike.setLongitude(requestDto.getLongitude());
        bike.setReturnPhoto(returnPhoto); // 반납 사진 연결
        bike.setUpdatedAt(LocalDateTime.now()); // updatedAt 필드 갱신

        // 5. 저장
        Bike savedBike = bikeRepository.save(bike);

        // 6. 응답 DTO 생성 및 반환
        return new BikeReturnResponseDto(
                savedBike.getLatitude(),
                savedBike.getLongitude(),
                savedBike.getUpdatedAt()
        );
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

    private BikeStatus toDbStatusFromCode(Integer code) {
        if (code == null) throw new IllegalArgumentException("status is required");
        if (code == 0) return BikeStatus.IN_USE;
        if (code == 1) return BikeStatus.AVAILABLE;
        if (code == 2) return BikeStatus.REPAIRING;
        throw new IllegalArgumentException("invalid status code: " + code);
    }

    private int toCodeFromDbStatus(BikeStatus db) {
        if (BikeStatus.IN_USE.equals(db)) return 0;
        if (BikeStatus.AVAILABLE.equals(db)) return 1;
        if (BikeStatus.REPAIRING.equals(db)) return 2;
        return -1;
    }

    private BikeStatus toDbStatusFromKorean(String kor) {
        if (kor == null) throw new IllegalArgumentException("status is required");
        String s = kor.trim();
        if ("사용중".equals(s)) return BikeStatus.IN_USE;
        if ("가용".equals(s) || "가용 가능".equals(s)) return BikeStatus.AVAILABLE;
        if ("고장".equals(s) || "수리중".equals(s)) return BikeStatus.REPAIRING;
        throw new IllegalArgumentException("invalid status: " + kor);
    }

    private String toKoreanStatus(BikeStatus db) {
        if (BikeStatus.IN_USE.equals(db)) return "사용중";
        if (BikeStatus.AVAILABLE.equals(db)) return "가용";
        if (BikeStatus.REPAIRING.equals(db)) return "고장";
        return "알수없음";
    }
    @Transactional(readOnly = true)
    public List<BikeResponse> listAll() {
        return bikeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DownloadableFile getBikeReturnPhoto(String serialNumber) {
        // 1. 시리얼 넘버로 자전거 조회
        Bike bike = bikeRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new NotFoundException("해당 시리얼 넘버의 자전거를 찾을 수 없습니다."));

        // 2. 자전거에 연결된 반납 사진 정보(File 엔티티)를 가져옴
        File returnPhoto = bike.getReturnPhoto();
        if (returnPhoto == null) {
            throw new NotFoundException("자전거에 반납된 사진이 존재하지 않습니다.");
        }

        // 3. File 엔티티의 경로, 파일명, 확장자를 조합하여 전체 파일 경로 생성
        String filePath = Paths.get(returnPhoto.getPath(), returnPhoto.getFileName() + "." + returnPhoto.getExt()).toString();

        // 4. FileService를 이용해 실제 파일 리소스를 로드
        Resource resource = fileService.loadAsResource(filePath);

        // 5. 다운로드 가능한 파일 객체로 감싸서 반환
        return new DownloadableFile(resource, returnPhoto.getOriginalName());
    }
}
