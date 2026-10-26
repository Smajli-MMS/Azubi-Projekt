package com.mediamarktsaturn.azubi.core.market.model;

import java.time.LocalDateTime;

public class Market {
    private Integer marketId;
    private String marketName;
    private String location;
    private String postalCode;
    private LocalDateTime created;
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
