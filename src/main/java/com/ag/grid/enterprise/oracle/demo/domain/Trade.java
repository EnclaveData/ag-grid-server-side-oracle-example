package com.ag.grid.enterprise.oracle.demo.domain;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class Trade implements Serializable {

    private static final long serialVersionUID = -860414236617960525L;

    private String product;

    private String portfolio;

    private String book;

    private long tradeId;

    private long submitterId;

    private long submitterDealId;

    private String dealType;

    private String bidType;

    private double currentValue;

    private double previousValue;

    private double pl1;

    private double pl2;

    private double gainDx;

    private double sxPx;

    private double x99Out;

    private long batch;

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

    public long getTradeId() {
        return tradeId;
    }

    public void setTradeId(long tradeId) {
        this.tradeId = tradeId;
    }

    public long getSubmitterId() {
        return submitterId;
    }

    public void setSubmitterId(long submitterId) {
        this.submitterId = submitterId;
    }

    public long getSubmitterDealId() {
        return submitterDealId;
    }

    public void setSubmitterDealId(long submitterDealId) {
        this.submitterDealId = submitterDealId;
    }

    public String getDealType() {
        return dealType;
    }

    public void setDealType(String dealType) {
        this.dealType = dealType;
    }

    public String getBidType() {
        return bidType;
    }

    public void setBidType(String bidType) {
        this.bidType = bidType;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    public double getPreviousValue() {
        return previousValue;
    }

    public void setPreviousValue(double previousValue) {
        this.previousValue = previousValue;
    }

    public double getPl1() {
        return pl1;
    }

    public void setPl1(double pl1) {
        this.pl1 = pl1;
    }

    public double getPl2() {
        return pl2;
    }

    public void setPl2(double pl2) {
        this.pl2 = pl2;
    }

    public double getGainDx() {
        return gainDx;
    }

    public void setGainDx(double gainDx) {
        this.gainDx = gainDx;
    }

    public double getSxPx() {
        return sxPx;
    }

    public void setSxPx(double sxPx) {
        this.sxPx = sxPx;
    }

    public double getX99Out() {
        return x99Out;
    }

    public void setX99Out(double x99Out) {
        this.x99Out = x99Out;
    }

    public long getBatch() {
        return batch;
    }

    public void setBatch(long batch) {
        this.batch = batch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trade trade = (Trade) o;
        return tradeId == trade.tradeId &&
                submitterId == trade.submitterId &&
                submitterDealId == trade.submitterDealId &&
                Double.compare(trade.currentValue, currentValue) == 0 &&
                Double.compare(trade.previousValue, previousValue) == 0 &&
                Double.compare(trade.pl1, pl1) == 0 &&
                Double.compare(trade.pl2, pl2) == 0 &&
                Double.compare(trade.gainDx, gainDx) == 0 &&
                Double.compare(trade.sxPx, sxPx) == 0 &&
                Double.compare(trade.x99Out, x99Out) == 0 &&
                batch == trade.batch &&
                Objects.equals(product, trade.product) &&
                Objects.equals(portfolio, trade.portfolio) &&
                Objects.equals(book, trade.book) &&
                Objects.equals(dealType, trade.dealType) &&
                Objects.equals(bidType, trade.bidType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(product, portfolio, book, tradeId, submitterId, submitterDealId, dealType, bidType, currentValue, previousValue, pl1, pl2, gainDx, sxPx, x99Out, batch);
    }

    @Override
    public String toString() {
        return "Trade{" +
                "product='" + product + '\'' +
                ", portfolio='" + portfolio + '\'' +
                ", book='" + book + '\'' +
                ", tradeId=" + tradeId +
                ", submitterId=" + submitterId +
                ", submitterDealId=" + submitterDealId +
                ", dealType='" + dealType + '\'' +
                ", bidType='" + bidType + '\'' +
                ", currentValue=" + currentValue +
                ", previousValue=" + previousValue +
                ", pl1=" + pl1 +
                ", pl2=" + pl2 +
                ", gainDx=" + gainDx +
                ", sxPx=" + sxPx +
                ", x99Out=" + x99Out +
                ", batch=" + batch +
                '}';
    }
}
