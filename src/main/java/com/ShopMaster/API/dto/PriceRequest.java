package com.ShopMaster.API.dto;

public class PriceRequest {
    private String product;
    private Integer startYear; // optional; if null, server will use current year
    private int years; // horizon in years (e.g., 3)

    public PriceRequest() {
    }

    public PriceRequest(String product, Integer startYear, int years) {
        this.product = product;
        this.startYear = startYear;
        this.years = years;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public Integer getStartYear() {
        return startYear;
    }

    public void setStartYear(Integer startYear) {
        this.startYear = startYear;
    }

    public int getYears() {
        return years;
    }

    public void setYears(int years) {
        this.years = years;
    }
}
