package com.ag.grid.enterprise;

import com.ag.grid.enterprise.oracle.demo.domain.Trade;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * @author Yuriy Kiselev (uze@yandex.ru)
 * @since 23.12.2018
 */
public final class TradeDumpLoader {

    public static Map<String, Map<Long, Trade>> load() {
        try (InputStream is = Files.newInputStream(Paths.get(System.getProperty("tradesDumpFile", "trades.jser")));
             InputStream bis = new BufferedInputStream(is);
             ObjectInputStream ois = new ObjectInputStream(bis)
        ) {
            return (Map<String, Map<Long, Trade>>) ois.readObject();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
