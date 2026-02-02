package org.rookies.zdme.map.controller;

import org.rookies.zdme.map.dto.BikeLocationsDTO;
import org.rookies.zdme.map.service.MapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/map")
public class MapController {

    @Autowired
    private MapService mapService;

    @PostMapping(value = "/bikes-nearby", 
                 consumes = MediaType.APPLICATION_XML_VALUE, 
                 produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<BikeLocationsDTO> getBikesNearby(@RequestBody String xmlRequest) {
        try {
            // This service method is intentionally vulnerable to XXE
            String lat = mapService.getLatitudeFromXml(xmlRequest); 
            
            BikeLocationsDTO response = new BikeLocationsDTO();
            response.setBikes(mapService.getBikeLocationsNearby(lat));
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
