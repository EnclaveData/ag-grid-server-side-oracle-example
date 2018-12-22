package com.ag.grid.enterprise.oracle.demo.dao;

import java.io.StringWriter;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
class LimitedStringWriter extends StringWriter {

    public LimitedStringWriter() {
        super(2_000);
    }

    private int storageLeft() {
        return getBuffer().capacity() - getBuffer().length();
    }

    @Override
    public void write(int c) {
        if (storageLeft() < 1) {
            return;
        }
        super.write(c);
    }

    @Override
    public void write(char[] cbuf, int off, int len) {
        final int left = storageLeft();
        if (left < len) {
            len = left;
        }
        if (len <= 0) {
            return;
        }
        super.write(cbuf, off, len);
    }

    @Override
    public void write(String str) {
        final int left = storageLeft();
        if (str == null) {
            if (left < 4) {
                return;
            }
        } else if (str.length() > left) {
            super.write(str, 0, left);
            return;
        }
        super.write(str);
    }

    @Override
    public void write(String str, int off, int len) {
        final int left = storageLeft();
        if (str == null) {
            if (left < 4) {
                return;
            }
        } else if (len > left) {
            len = left;
        }
        if (len <= 0) {
            return;
        }
        super.write(str, off, len);
    }
}
