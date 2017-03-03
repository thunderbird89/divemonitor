package com.example.zmeggyesi.divemonitor.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<DummyItem> ITEMS = new ArrayList<DummyItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    private static void addItem(DummyItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static DummyItem createDummyItem(int position) {
        return new DummyItem(position, "Item " + position);
    }

    private static String makeDetails(int position, List<Double> depths) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }

        builder.append(depths.get(ThreadLocalRandom.current().nextInt(0, position)));
        builder.append(depths.size());
        return builder.toString();
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        public final String id;
        public final String content;
        public final String details;
        public final List<Double> depths = new ArrayList<>();

        public DummyItem(int id, String content) {
            this.id = String.valueOf(id);
            this.content = content;

            int i = 0;
            while (i < 25) {
                Double rand = ThreadLocalRandom.current().nextDouble(0, 20);
                this.depths.add(rand);
                i++;
            }
            this.details = makeDetails(id, depths);
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
