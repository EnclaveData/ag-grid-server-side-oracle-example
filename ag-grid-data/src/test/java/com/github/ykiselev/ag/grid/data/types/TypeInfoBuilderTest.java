package com.github.ykiselev.ag.grid.data.types;

import org.junit.Before;
import org.junit.Test;

import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.function.UnaryOperator;

import static org.junit.Assert.assertEquals;

/**
 * @author Yuriy Kiselev (uze@yandex.ru)
 * @since 2018-12-22
 */
public class TypeInfoBuilderTest {

    private A obj;

    @Before
    public void setUp() {
        obj = new A();

        C c = new C();
        c.setA(1);
        c.setB(2L);
        c.setC(3d);
        c.setName("C");

        B b = new B();
        b.setInfo(c);

        obj.setId(10L);
        obj.setIndex(11);
        obj.setB(b);
        obj.setValue(Math.PI);
        obj.setName("A");
    }

    private Attribute<A> attr(UnaryOperator<TypeInfoBuilder<A>> op, String name) {
        return op.apply(new TypeInfoBuilder<>())
                .build()
                .getAttribute(name);
    }

    private ToIntFunction<A> intGetter(UnaryOperator<TypeInfoBuilder<A>> op, String name) {
        return attr(op, name).getIntGetter();
    }

    private ToLongFunction<A> longGetter(UnaryOperator<TypeInfoBuilder<A>> op, String name) {
        return attr(op, name).getLongGetter();
    }

    private ToDoubleFunction<A> doubleGetter(UnaryOperator<TypeInfoBuilder<A>> op, String name) {
        return attr(op, name).getDoubleGetter();
    }

    private <V> Function<A, V> objGetter(UnaryOperator<TypeInfoBuilder<A>> op, String name) {
        return (Function<A, V>) attr(op, name).getObjectGetter();
    }

    @Test
    public void shouldBuildIntAttribute() {
        assertEquals(11,
                intGetter(b ->
                        b.withInt("index", A::getIndex), "index").applyAsInt(obj)
        );
    }

    @Test
    public void shouldBuildLongAttribute() {
        assertEquals(10L,
                longGetter(b ->
                        b.withLong("id", A::getId), "id").applyAsLong(obj)
        );
    }

    @Test
    public void shouldBuildDoubleAttribute() {
        assertEquals(Math.PI,
                doubleGetter(b ->
                        b.withDouble("value", A::getValue), "value").applyAsDouble(obj),
                0.0001d
        );
    }

    @Test
    public void shouldBuildObjectAttribute() {
        assertEquals("A",
                objGetter(b ->
                        b.with("name", String.class, A::getName), "name").apply(obj)
        );
    }

    @Test
    public void shouldBuildIntChainAttribute() {
        assertEquals(1, intGetter(b ->
                        b.withChain("a", A::getB).and(B::getInfo).andInt(C::getA),
                "a").applyAsInt(obj));
    }

    @Test
    public void shouldBuildLongChainAttribute() {
        assertEquals(2L, longGetter(b ->
                        b.withChain("b", A::getB).and(B::getInfo).andLong(C::getB),
                "b").applyAsLong(obj));
    }

    @Test
    public void shouldBuildDoubleChainAttribute() {
        assertEquals(3d, doubleGetter(b ->
                        b.withChain("c", A::getB).and(B::getInfo).andDouble(C::getC),
                "c").applyAsDouble(obj),
                0.0001d
        );
    }

    @Test
    public void shouldBuildObjectChainAttribute() {
        assertEquals("C", objGetter(b ->
                        b.withChain("name", A::getB).and(B::getInfo).andObject(String.class, C::getName),
                "name").apply(obj)
        );
    }
}

class C {

    private int a;

    private long b;

    private double c;

    private String name;

    int getA() {
        return a;
    }

    void setA(int a) {
        this.a = a;
    }

    long getB() {
        return b;
    }

    void setB(long b) {
        this.b = b;
    }

    double getC() {
        return c;
    }

    void setC(double c) {
        this.c = c;
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }
}

class B {

    private C info;

    C getInfo() {
        return info;
    }

    void setInfo(C info) {
        this.info = info;
    }
}

class A {

    private B b;

    private int index;

    private long id;

    private double value;

    private String name;

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    B getB() {
        return b;
    }

    void setB(B b) {
        this.b = b;
    }

    int getIndex() {
        return index;
    }

    void setIndex(int index) {
        this.index = index;
    }

    long getId() {
        return id;
    }

    void setId(long id) {
        this.id = id;
    }

    double getValue() {
        return value;
    }

    void setValue(double value) {
        this.value = value;
    }
}