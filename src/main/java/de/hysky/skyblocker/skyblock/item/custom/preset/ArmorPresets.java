package de.hysky.skyblocker.skyblock.item.custom.preset;

import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.skyblock.item.custom.CustomArmorTrims;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ArmorPresets {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final File PRESETS_FILE = new File(SkyblockerMod.CONFIG_DIR.toFile(), "armor_presets.json");

	private static final java.lang.reflect.Type LIST_TYPE = new TypeToken<List<ArmorPreset>>() {}.getType();

	private final List<ArmorPreset> presets = new ArrayList<>();

	private static ArmorPresets instance;

	private ArmorPresets() {
		load();
	}

	public static ArmorPresets getInstance() {
		if (instance == null) instance = new ArmorPresets();
		return instance;
	}

	public List<ArmorPreset> getPresets() {
		return presets;
	}

	public void addPreset(ArmorPreset preset) {
		presets.add(preset);
		save();
	}

	/**
	 * Remove the given preset from the list and persist the change.
	 */
	public void removePreset(ArmorPreset preset) {
		presets.remove(preset);
		save();
	}

	/**
	 * Rename the given preset and persist the change.
	 */
	public void renamePreset(ArmorPreset preset, String newName) {
		int idx = presets.indexOf(preset);
		if (idx >= 0) {
			presets.set(idx, new ArmorPreset(newName,
					preset.helmet(), preset.chestplate(), preset.leggings(), preset.boots()));
			save();
		}
	}

	/**
	 * Persist the current preset list to disk.
	 */
	public void savePresets() {
		save();
	}

	public void apply(ArmorPreset preset) {
		var player = net.minecraft.client.MinecraftClient.getInstance().player;
		if (player == null) return;
		var armorStacks = ItemUtils.getArmor(player);
		ArmorPreset.Piece[] pieces = new ArmorPreset.Piece[]{preset.helmet(), preset.chestplate(), preset.leggings(), preset.boots()};
		for (int i = 0; i < Math.min(armorStacks.size(), pieces.length); i++) {
			ItemStack playerStack = armorStacks.get(i);
			String uuid = ItemUtils.getItemUuid(playerStack);
			if (uuid.isEmpty()) continue;
			ArmorPreset.Piece piece = pieces[pieces.length - 1 - i];
			var cfg = SkyblockerConfigManager.get().general;
			if (piece.trim() != null) {
				cfg.customArmorTrims.put(uuid, new CustomArmorTrims.ArmorTrimId(
						Identifier.of(piece.trim().material()),
						Identifier.of(piece.trim().pattern())));
			} else
				cfg.customArmorTrims.remove(uuid);
			if (piece.dye() != null)
				cfg.customDyeColors.put(uuid, piece.dye());
			else
				cfg.customDyeColors.removeInt(uuid);
			if (piece.animation() != null)
				cfg.customAnimatedDyes.put(uuid, piece.animation());
			else
				cfg.customAnimatedDyes.remove(uuid);
			if (piece.texture() != null)
				cfg.customHelmetTextures.put(uuid, piece.texture());
			else
				cfg.customHelmetTextures.remove(uuid);
		}
		SkyblockerConfigManager.update(config -> {});
	}

	private void load() {
		presets.clear();
		Path file = PRESETS_FILE.toPath();
		if (!Files.exists(file)) return;
		try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
			@SuppressWarnings("unchecked")
			var loaded = (List<ArmorPreset>) SkyblockerMod.GSON.fromJson(reader, LIST_TYPE);
			if (loaded != null) presets.addAll(loaded);
		} catch (IOException e) {
			LOGGER.error("[Skyblocker] Failed to load armor presets", e);
		}
	}

	private void save() {
		Path file = PRESETS_FILE.toPath();
		try {
			Files.createDirectories(file.getParent());
			try (var writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
				SkyblockerMod.GSON.toJson(presets, LIST_TYPE, writer);
			}
		} catch (IOException e) {
			LOGGER.error("[Skyblocker] Failed to save armor presets", e);
		}
	}
}
