package com.mediamarktsaturn.azubi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "market", schema = "relin_uni2")
public class Market {
    @Id
    @Column(name = "marketid")
    private Integer marketId;  // ← Feldname korrigiert

    @Column(name = "marketname")
    private String marketName;

    @Column(name = "location")
    private String location;

    @Column(name = "postalcode")
    private String postalCode;  // ← Feldname korrigiert

    @Column(name = "created")
    private LocalDateTime created;

    @Column(name = "updated")
    private LocalDateTime updated;

    public Market() {
    }

    public Market(String marketName, String location, String postalCode) {
        this.marketName = marketName;
        this.location = location;
        this.postalCode = postalCode;
        this.created = LocalDateTime.now();
        this.updated = LocalDateTime.now();
    }

    //Getter and Setter
    public Integer getMarketId() {
        return marketId;
    }

    public void setMarketId(Integer marketId) {
        this.marketId = marketId;
    }

    public String getMarketName() {
        return marketName;
    }

    public void setMarketName(String marketName) {
        this.marketName = marketName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }
}
