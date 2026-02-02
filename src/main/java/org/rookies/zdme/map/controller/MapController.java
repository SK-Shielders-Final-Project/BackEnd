package org.rookies.zdme.map.controller;

import org.rookies.zdme.map.dto.MapResponseDTO; // 변경된 DTO 임포트
import org.rookies.zdme.map.dto.BikeLocationDTO; // BikeLocationDTO도 필요
import org.rookies.zdme.map.service.MapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List; // List 임포트

@RestController
@RequestMapping("/api/map")
public class MapController {

    @Autowired
    private MapService mapService;

    @PostMapping(value = "/bikes-nearby", 
                 consumes = MediaType.APPLICATION_XML_VALUE, 
                 produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<MapResponseDTO> getBikesNearby(@RequestBody String xmlRequest) { // 반환 타입 변경
        try {
            // This service method is intentionally vulnerable to XXE
            String lat = mapService.getLatitudeFromXml(xmlRequest); 
            
            List<BikeLocationDTO> bikeList = mapService.getBikeLocationsNearby(lat); // 자전거 리스트 가져오기
            MapResponseDTO response = new MapResponseDTO(lat, bikeList); // 새로운 DTO 생성 및 값 설정
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build(); // 500으로 변경하여 내부 서버 오류임을 명확히 함
        }
    }
}
