package me.xmrvizzy.skyblocker.chat;

public enum ChatFilterResult {
    // Skip this one / no action
    PASS,
    // Filter
    FILTER,
    // Move to action bar
    ACTION_BAR;
    // Skip remaining checks, don't filter
    // null
}
