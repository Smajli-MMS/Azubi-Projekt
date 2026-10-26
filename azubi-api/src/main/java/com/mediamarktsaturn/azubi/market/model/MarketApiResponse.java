package com.mediamarktsaturn.azubi.market.model;

public class MarketApiResponse {
    private Integer marketId;
    private String marketName;
    private String location;
    private String postalCode;

    public MarketApiResponse() {
    }

    public MarketApiResponse(Integer marketId, String marketName, String location, String postalCode) {
        this.marketId = marketId;
        this.marketName = marketName;
        this.location = location;
        this.postalCode = postalCode;
    }

    // Getters und Setters
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
}
