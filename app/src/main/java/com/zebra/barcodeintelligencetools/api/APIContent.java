package com.zebra.barcodeintelligencetools.api;

import java.util.HashMap;

/**
 * Helper class for providing API content for user interfaces created by
 * Android template wizards.
 * <p>
 */
public class APIContent extends HashMap<String, ApiItem> {
    public void addItem(String content, String details, int icon) {
        ApiItem item = new ApiItem(Integer.toString(size() + 1), content, details, icon);
        put(item.id, item);
    }
}