package me.xmrvizzy.skyblocker.skyblock.api.records.dungeons;

import com.google.gson.annotations.SerializedName;

public record Journals(
        @SerializedName("pages_completed") int pagesCompleted,
        @SerializedName("journals_completed") int journalsCompleted,
        @SerializedName("total_pages") Integer totalPages,
        boolean maxed,
        @SerializedName("journal_entries") Entry[] journalEntries

){
    public record Entry(
            String name,
            @SerializedName("pages_collected") int pagesCollected,
            @SerializedName("total_pages") Integer totalPages
    ){}
}
