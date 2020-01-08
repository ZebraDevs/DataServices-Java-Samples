package com.zebra.barcodeintelligencetools.api;

import android.content.Context;

import androidx.annotation.NonNull;

import com.zebra.barcodeintelligencetools.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing API content for user interfaces created by
 * Android template wizards.
 * <p>
 */
public class APIContent {
    /**
     * An array of API items.
     */
    public static final List<ApiItem> ITEMS = new ArrayList<>();
    /**
     * A map of API items, by ID.
     */
    public static final Map<String, ApiItem> ITEM_MAP = new HashMap<>();

    public APIContent(Context context) {

        // Add some API items.
        ITEMS.clear();
        addItem(new ApiItem("1", context.getString(R.string.create_barcode), context.getString(R.string.create_barcode_details)));
        addItem(new ApiItem("2", context.getString(R.string.fda_recall), context.getString(R.string.fda_recall_details)));
        addItem(new ApiItem("3", context.getString(R.string.upc_lookup), context.getString(R.string.upc_lookup_details)));
    }

    private static void addItem(ApiItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    /**
     * An item representing a piece of API content.
     */
    public class ApiItem {
        public final String id;
        public final String content;
        public final String details;

        ApiItem(@NonNull String id, @NonNull String content, String details) {
            this.id = id;
            this.content = content;
            this.details = details;
        }

        @NonNull
        @Override
        public String toString() {
            return content;
        }
    }
}
