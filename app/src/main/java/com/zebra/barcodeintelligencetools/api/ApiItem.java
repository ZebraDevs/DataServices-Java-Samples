package com.zebra.barcodeintelligencetools.api;

import androidx.annotation.NonNull;

/**
 * An item representing a piece of API content.
 */
public class ApiItem {
    public final String id;
    public final String content;
    public final String details;
    public final int icon;

    ApiItem(@NonNull String id, @NonNull String content, String details, int icon) {
        this.id = id;
        this.content = content;
        this.details = details;
        this.icon = icon;
    }

    @NonNull
    @Override
    public String toString() {
        return content;
    }
}