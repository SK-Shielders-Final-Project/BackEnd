package org.rookies.zdme.map.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "mapResponse") // XML 루트 엘리먼트 이름
@XmlAccessorType(XmlAccessType.FIELD)
public class MapResponseDTO {

    @XmlElement(name = "exploitedLatitude") // XXE 결과가 담길 필드
    private String exploitedLatitude;

    @XmlElement(name = "bikes") // 자전거 목록을 담을 엘리먼트
    private List<BikeLocationDTO> bikes;

    public MapResponseDTO() {}

    public MapResponseDTO(String exploitedLatitude, List<BikeLocationDTO> bikes) {
        this.exploitedLatitude = exploitedLatitude;
        this.bikes = bikes;
    }

    // Getters and Setters
    public String getExploitedLatitude() {
        return exploitedLatitude;
    }

    public void setExploitedLatitude(String exploitedLatitude) {
        this.exploitedLatitude = exploitedLatitude;
    }

    public List<BikeLocationDTO> getBikes() {
        return bikes;
    }

    public void setBikes(List<BikeLocationDTO> bikes) {
        this.bikes = bikes;
    }
}
