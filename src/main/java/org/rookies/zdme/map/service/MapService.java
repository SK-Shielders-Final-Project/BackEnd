package org.rookies.zdme.map.service;

import org.rookies.zdme.domain.BikeLocationData; // 새로운 엔티티 임포트
import org.rookies.zdme.repository.BikeLocationDataRepository; // 새로운 레포지토리 임포트
import org.rookies.zdme.map.dto.BikeLocationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MapService {

    @Autowired
    private BikeLocationDataRepository bikeLocationDataRepository; // 레포지토리 타입 변경

    /**
     * WARNING: This method is intentionally vulnerable to XML External Entity (XXE) attacks.
     * DO NOT USE THIS IN A PRODUCTION ENVIRONMENT.
     * It is configured to allow external entities for educational/testing purposes only.
     */
    public String getLatitudeFromXml(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        // Intentionally enabling features that allow XXE
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", true);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xml)));
        
        return doc.getElementsByTagName("lat").item(0).getTextContent();
    }
    
    public List<BikeLocationDTO> getBikeLocationsNearby(String lat) {
        // We received 'lat' from the insecure XML, now we fetch bikes from DB
        System.out.println("Latitude requested via insecure XML: " + lat);

        List<BikeLocationData> bikeEntities = bikeLocationDataRepository.findAll(); // 레포지토리 사용

        return bikeEntities.stream()
                           .map(entity -> new BikeLocationDTO(entity.getId(), entity.getLatitude(), entity.getLongitude()))
                           .collect(Collectors.toList());
    }
}
