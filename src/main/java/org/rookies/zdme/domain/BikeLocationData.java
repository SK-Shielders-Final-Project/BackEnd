package org.rookies.zdme.domain; // 패키지 경로 변경

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table; // @Table 어노테이션 추가

@Entity
@Table(name = "bikes") // 데이터베이스 테이블 이름이 'bikes'라고 명시
public class BikeLocationData { // 클래스명 변경

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BIKE_ID") // 실제 DB 컬럼 이름으로 매핑
    private Long id;

    @Column(name = "LATITUDE", nullable = false)
    private double latitude;

    @Column(name = "LONGITUDE", nullable = false)
    private double longitude;
    
    // 기본 생성자 (JPA 필수)
    public BikeLocationData() {}

    // 필드 생성자 (옵션)
    public BikeLocationData(Long id, double latitude, double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
