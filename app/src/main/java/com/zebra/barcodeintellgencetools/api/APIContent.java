package com.zebra.barcodeintellgencetools.api;

import android.content.Context;

import com.zebra.barcodeintellgencetools.R;

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
    Context context;
    public APIContent(Context context){
        this.context = context;

        // Add some API items.
        addItem(new ApiItem("1", context.getString(R.string.create_barcode), context.getString(R.string.create_barcode_details)));
        addItem(new ApiItem("2", context.getString(R.string.fda_recall), context.getString(R.string.fda_recall_details)));
        addItem(new ApiItem("3", context.getString(R.string.upc_lookup), context.getString(R.string.upc_lookup_details)));
    }

    /**
     * An array of API items.
     */
    public static final List<ApiItem> ITEMS = new ArrayList<ApiItem>();

    /**
     * A map of API items, by ID.
     */
    public static final Map<String, ApiItem> ITEM_MAP = new HashMap<String, ApiItem>();

    private static void addItem(ApiItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    /**
     * An item representing a piece of API content.
     */
    public static class ApiItem {
        public final String id;
        public final String content;
        public final String details;

        public ApiItem(String id, String content, String details) {
            this.id = id;
            this.content = content;
            this.details = details;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
