package de.hysky.skyblocker.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParser;
import com.mojang.util.UndashedUuid;

import de.hysky.skyblocker.utils.Http.ApiResponse;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

/*
 * Contains only basic helpers for using Http APIs
 */
public class ApiUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(ApiUtils.class);
	/** 
	 * Do not iterate over this map, it will be accessed and modified by multiple threads.
	 */
	private static final Object2ObjectOpenHashMap<String, String> NAME_2_UUID_CACHE = new Object2ObjectOpenHashMap<>();

	public static void init() {
		//Clear cache every 20 minutes
		Scheduler.INSTANCE.scheduleCyclic(NAME_2_UUID_CACHE::clear, 24_000, true);
	}

	/**
	 * Multithreading is to be handled by the method caller
	 */
	public static String name2Uuid(String name) {
		Session session = MinecraftClient.getInstance().getSession();

		if (session.getUsername().equals(name)) return UndashedUuid.toString(session.getUuidOrNull());
		if (NAME_2_UUID_CACHE.containsKey(name)) return NAME_2_UUID_CACHE.get(name);

		try (ApiResponse response = Http.sendName2UuidRequest(name)) {
			if (response.ok()) {
				String uuid = JsonParser.parseString(response.content()).getAsJsonObject().get("id").getAsString();

				NAME_2_UUID_CACHE.put(name, uuid);

				return uuid;
			}
		} catch (Exception e) {
			LOGGER.error("[Skyblocker] Name to uuid lookup failed! Name: {}", name, e);
		}

		return "";
	}
}
