package com.ag.grid.enterprise;

import com.ag.grid.enterprise.oracle.demo.domain.Trade;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class TradeDumpGeneratorApp {

    public static void main(String[] args) throws IOException {
        new TradeDumpGeneratorApp().run();
    }

    private void run() throws IOException {
        System.out.println("Generating trades...");
        final Map<String, Map<Long, Trade>> result = generate(2_000_000, 7, 200_000);
        System.out.println("Generated trade map (" + result.size() + "):");
        String stats = result.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(e -> e.getValue().size()))
                .map(e -> "  " + e.getKey() + ": " + e.getValue().size())
                .collect(Collectors.joining("\n"));
        System.out.println(stats);
        System.out.println("Dumping...");
        Files.write(Paths.get("trades-stats.txt"), stats.getBytes(StandardCharsets.UTF_8));
        dump(Paths.get("trades.jser"), result);
        System.out.println("Done.");
    }

    private void dump(Path path, Object object) throws IOException {
        try (OutputStream os = Files.newOutputStream(path);
             OutputStream bos = new BufferedOutputStream(os);
             ObjectOutputStream oos = new ObjectOutputStream(bos)
        ) {
            oos.writeObject(object);
        }
    }

    private Map<String, Map<Long, Trade>> generate(int totalTrades, int minSize, int maxSize) {
        final Map<String, Map<Long, Trade>> map = new HashMap<>();
        int left = totalTrades;
        final ThreadLocalRandom rnd = ThreadLocalRandom.current();
        int i = 1, p = 1;
        while (left > minSize && (maxSize / minSize) > 1_000) {
            final int size = maxSize;
            final String portfolio = "portfolio_" + p;
            final Map<Long, Trade> trades = createPortfolio(portfolio, i, size);
            map.put(portfolio, trades);
            left -= size;
            i += size;
            p++;
            maxSize = (maxSize * 3) / 4;
        }
        while (left > minSize) {
            final int size = rnd.nextInt(minSize, Math.min(maxSize, left));
            final String portfolio = "portfolio_" + p;
            final Map<Long, Trade> trades = createPortfolio(portfolio, i, size);
            map.put(portfolio, trades);
            left -= size;
            i += size;
            p++;
        }
        return map;
    }

    private Map<Long, Trade> createPortfolio(String portfolio, int fromId, int size) {
        final ThreadLocalRandom rnd = ThreadLocalRandom.current();
        final Map<Long, Trade> map = new HashMap<>();
        for (int i = 1; i <= size; i++) {
            Trade t = new Trade();
            t.setTradeId(fromId + i);
            t.setPortfolio(portfolio);
            t.setProduct("product_" + rnd.nextInt(1, 100));
            t.setBook("book_" + rnd.nextInt(1, 5_000));
            t.setSubmitterId(rnd.nextInt(1, 3_000));
            t.setSubmitterDealId(i);
            t.setDealType("dealType_" + rnd.nextInt(1, 50));
            t.setBidType("bidType_" + rnd.nextInt(1, 250));
            t.setCurrentValue(rnd.nextDouble(0, 1_000_000));
            t.setPreviousValue(rnd.nextDouble(0, 1_100_000));
            t.setPl1(rnd.nextDouble());
            t.setPl2(rnd.nextDouble());
            t.setGainDx(rnd.nextDouble());
            t.setSxPx(rnd.nextDouble());
            t.setX99Out(rnd.nextDouble());
            t.setBatch(rnd.nextInt(1, 15_000));
            map.put(t.getTradeId(), t);
        }
        return map;
    }
}
