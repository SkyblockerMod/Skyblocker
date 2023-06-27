package me.xmrvizzy.skyblocker.skyblock.rift;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class TheRift {
	public static final String LOCATION = "rift";

	public static void init() {
		WorldRenderEvents.AFTER_TRANSLUCENT.register(MirrorverseWaypoints::render);
	}
}
