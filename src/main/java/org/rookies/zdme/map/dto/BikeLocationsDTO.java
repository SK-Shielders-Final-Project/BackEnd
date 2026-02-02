package org.rookies.zdme.map.dto;

import org.rookies.zdme.map.dto.BikeLocationDTO;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "bikes")
@XmlAccessorType(XmlAccessType.FIELD)
public class BikeLocationsDTO {

    @XmlElement(name = "bike")
    private List<BikeLocationDTO> bikes;

    public List<BikeLocationDTO> getBikes() {
        return bikes;
    }

    public void setBikes(List<BikeLocationDTO> bikes) {
        this.bikes = bikes;
    }
}
