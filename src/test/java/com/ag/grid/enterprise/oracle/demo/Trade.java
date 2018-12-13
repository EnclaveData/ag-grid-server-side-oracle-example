package com.ag.grid.enterprise.oracle.demo;

public class Trade {

    private String product;

    private String portfolio;

    private String book;

    private Long tradeId;

    private double value;

    public Trade() {
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(String portfolio) {
        this.portfolio = portfolio;
    }

    public String getBook() {
        return book;
    }

    public void setBook(String book) {
        this.book = book;
    }

    public Long getTradeId() {
        return tradeId;
    }

    public void setTradeId(Long tradeId) {
        this.tradeId = tradeId;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Trade{" +
                "product='" + product + '\'' +
                ", portfolio='" + portfolio + '\'' +
                ", book='" + book + '\'' +
                ", tradeId=" + tradeId +
                '}';
    }
}