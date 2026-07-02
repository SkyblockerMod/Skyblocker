package de.hysky.skyblocker.config;

import net.minecraft.network.chat.Component;

public final class CommonTags {
	public static final Component ADDED_IN_5_9_0 = Component.nullToEmpty("v5.9.0");
	public static final Component ADDED_IN_5_10_0 = Component.nullToEmpty("v5.10.0");
	public static final Component ADDED_IN_5_11_0 = Component.nullToEmpty("v5.11.0");
	public static final Component ADDED_IN_6_0_0 = Component.nullToEmpty("v6.0.0");
	public static final Component ADDED_IN_6_2_0 = Component.nullToEmpty("v6.2.0");
	public static final Component ADDED_IN_6_3_0 = Component.nullToEmpty("v6.3.0");
	public static final Component ADDED_IN_6_4_0 = Component.nullToEmpty("v6.4.0");
	public static final Component ADDED_IN_6_5_0 = Component.nullToEmpty("v6.5.0");
	public static final Component ADDED_IN_6_5_1 = Component.nullToEmpty("v6.5.1");
	public static final Component ADDED_IN_6_6_0 = Component.literal("v6.6.0");

	public static final Component LATEST_VERSION_TAG = ADDED_IN_6_6_0;

	/// Common tags for {@link net.minecraft.client.KeyMapping KeyMappings} to make them all easily searchable.
	public static final Component[] KEY_MAPPING = { Component.translatable("skyblocker.config.tag.keyBind"), Component.translatable("skyblocker.config.tag.keyMapping") };
}
