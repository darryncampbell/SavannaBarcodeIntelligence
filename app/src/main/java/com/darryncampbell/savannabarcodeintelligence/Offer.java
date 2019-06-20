package com.darryncampbell.savannabarcodeintelligence;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Offer {

    @SerializedName("merchant")
    @Expose
    private String merchant;
    @SerializedName("domain")
    @Expose
    private String domain;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("currency")
    @Expose
    private String currency;
    @SerializedName("list_price")
    @Expose
    private String listPrice;
    @SerializedName("price")
    @Expose
    private Double price;
    @SerializedName("shipping")
    @Expose
    private String shipping;
    @SerializedName("condition")
    @Expose
    private String condition;
    @SerializedName("availability")
    @Expose
    private String availability;
    @SerializedName("link")
    @Expose
    private String link;
    @SerializedName("updated_t")
    @Expose
    private Integer updatedT;

    public String getMerchant() {
        return merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getListPrice() {
        return listPrice;
    }

    public void setListPrice(String listPrice) {
        this.listPrice = listPrice;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getShipping() {
        return shipping;
    }

    public void setShipping(String shipping) {
        this.shipping = shipping;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Integer getUpdatedT() {
        return updatedT;
    }

    public void setUpdatedT(Integer updatedT) {
        this.updatedT = updatedT;
    }

}