package com.github.ykiselev.type.info.builder;

import java.util.Date;
import java.util.Objects;

/**
 * @author Yuriy Kiselev (uze@yandex.ru)
 * @since 01.05.2019
 */
@BuildTypeInfo
public class Item {

    private byte b;

    private short sh;

    private int i;

    private long l;

    private float fl;

    private double dbl;

    private boolean flag;

    private char ch;

    private String name;

    private Byte b2;

    private Short sh2;

    private Integer i2;

    private Long l2;

    private Float fl2;

    private Double dbl2;

    private Boolean flag2;

    private Character ch2;

    private Date date;

    private Object object;

    public byte getB() {
        return b;
    }

    public void setB(byte b) {
        this.b = b;
    }

    public short getSh() {
        return sh;
    }

    public void setSh(short sh) {
        this.sh = sh;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public long getL() {
        return l;
    }

    public void setL(long l) {
        this.l = l;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public char getCh() {
        return ch;
    }

    public void setCh(char ch) {
        this.ch = ch;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Byte getB2() {
        return b2;
    }

    public void setB2(Byte b2) {
        this.b2 = b2;
    }

    public Short getSh2() {
        return sh2;
    }

    public void setSh2(Short sh2) {
        this.sh2 = sh2;
    }

    public Integer getI2() {
        return i2;
    }

    public void setI2(Integer i2) {
        this.i2 = i2;
    }

    public Long getL2() {
        return l2;
    }

    public void setL2(Long l2) {
        this.l2 = l2;
    }

    public Boolean getFlag2() {
        return flag2;
    }

    public void setFlag2(Boolean flag2) {
        this.flag2 = flag2;
    }

    public Character getCh2() {
        return ch2;
    }

    public void setCh2(Character ch2) {
        this.ch2 = ch2;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public float getFl() {
        return fl;
    }

    public void setFl(float fl) {
        this.fl = fl;
    }

    public double getDbl() {
        return dbl;
    }

    public void setDbl(double dbl) {
        this.dbl = dbl;
    }

    public Float getFl2() {
        return fl2;
    }

    public void setFl2(Float fl2) {
        this.fl2 = fl2;
    }

    public Double getDbl2() {
        return dbl2;
    }

    public void setDbl2(Double dbl2) {
        this.dbl2 = dbl2;
    }

    @Override
    public String toString() {
        return "Item{" +
                "b=" + b +
                ", sh=" + sh +
                ", i=" + i +
                ", l=" + l +
                ", fl=" + fl +
                ", dbl=" + dbl +
                ", flag=" + flag +
                ", ch=" + ch +
                ", name='" + name + '\'' +
                ", b2=" + b2 +
                ", sh2=" + sh2 +
                ", i2=" + i2 +
                ", l2=" + l2 +
                ", fl2=" + fl2 +
                ", dbl2=" + dbl2 +
                ", flag2=" + flag2 +
                ", ch2=" + ch2 +
                ", date=" + date +
                ", object=" + object +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return b == item.b &&
                sh == item.sh &&
                i == item.i &&
                l == item.l &&
                Float.compare(item.fl, fl) == 0 &&
                Double.compare(item.dbl, dbl) == 0 &&
                flag == item.flag &&
                ch == item.ch &&
                Objects.equals(name, item.name) &&
                Objects.equals(b2, item.b2) &&
                Objects.equals(sh2, item.sh2) &&
                Objects.equals(i2, item.i2) &&
                Objects.equals(l2, item.l2) &&
                Objects.equals(fl2, item.fl2) &&
                Objects.equals(dbl2, item.dbl2) &&
                Objects.equals(flag2, item.flag2) &&
                Objects.equals(ch2, item.ch2) &&
                Objects.equals(date, item.date) &&
                Objects.equals(object, item.object);
    }

    @Override
    public int hashCode() {
        return Objects.hash(b, sh, i, l, fl, dbl, flag, ch, name, b2, sh2, i2, l2, fl2, dbl2, flag2, ch2, date, object);
    }
}
