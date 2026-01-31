package de.hysky.skyblocker.skyblock.garden;

import com.google.gson.JsonArray;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.mixins.accessors.AbstractContainerScreenAccessor;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.Holder;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public final class GardenPlots {
	private static final Logger LOGGER = LoggerFactory.getLogger("Garden Plots");
	private static final Path FOLDER = SkyblockerMod.CONFIG_DIR.resolve("garden_plots");

	public static final @Nullable GardenPlot[] GARDEN_PLOTS = new GardenPlot[25];

	@Init
	public static void init() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof ContainerScreen containerScreen && screen.getTitle().getString().trim().equals("Configure Plots")) {
				ScreenEvents.remove(screen).register(ignored -> {
					ChestMenu screenHandler = containerScreen.getMenu();
					// Take plot icons and names
					for (int row = 0; row < 5; row++)
						for (int i = row * 9 + 2; i < row * 9 + 7; i++) {
							if (i == 22) continue; // Barn icon
							Slot slot = screenHandler.slots.get(i);
							ItemStack stack = slot.getItem();
							if (stack.isEmpty() || stack.is(Items.RED_STAINED_GLASS_PANE) || stack.is(Items.OAK_BUTTON) || stack.is(Items.BLACK_STAINED_GLASS_PANE))
								continue;
							// SkyHanni adds formatting codes to the plot names when using their custom plot icons.
							String name = ChatFormatting.stripFormatting(stack.getHoverName().getString());
							String[] parts = name.split("-", 2);
							if (parts.length < 2) {
								LOGGER.warn("Invalid plot name: {}", name);
								continue;
							}
							int index = (i / 9) * 5 + (i % 9 - 2);
							Either<Item, String> icon = stack.getSkyblockId().isBlank() ? Either.left(stack.getItem()) : Either.right(stack.getSkyblockId());
							String plotName = parts[1].trim();
							GardenPlot plot = GARDEN_PLOTS[index];
							if (plot == null) {
								GARDEN_PLOTS[index] = new GardenPlot(icon, plotName);
							} else {
								GARDEN_PLOTS[index] = plot.withIconName(icon, plotName);
							}
						}

				});
			} else if (screen instanceof InventoryScreen inventoryScreen && Utils.getLocation().equals(Location.GARDEN) && SkyblockerConfigManager.get().farming.garden.gardenPlotsWidget) {
				GardenPlotsWidget widget = new GardenPlotsWidget(
						((AbstractContainerScreenAccessor) inventoryScreen).getX() + ((AbstractContainerScreenAccessor) inventoryScreen).getImageWidth() + 4,
						((AbstractContainerScreenAccessor) inventoryScreen).getY());
				Screens.getButtons(inventoryScreen).add(widget);

				inventoryScreen.registerRecipeBookToggleCallback(() -> widget.setPosition(
						((AbstractContainerScreenAccessor) inventoryScreen).getX() + ((AbstractContainerScreenAccessor) inventoryScreen).getImageWidth() + 4,
						((AbstractContainerScreenAccessor) inventoryScreen).getY()
				));
			}
		});

		SkyblockEvents.PROFILE_CHANGE.register(((prevProfileId, profileId) -> {
			if (!prevProfileId.isEmpty())
				CompletableFuture.runAsync(() -> save(prevProfileId), Executors.newVirtualThreadPerTaskExecutor()).thenRun(() -> load(profileId));
			else load(profileId);
		}));

		ClientLifecycleEvents.CLIENT_STOPPING.register(client1 -> {
			String profileId = Utils.getProfileId();
			if (!profileId.isBlank()) {
				CompletableFuture.runAsync(() -> save(profileId), Executors.newVirtualThreadPerTaskExecutor());
			}
		});
	}

	private static void save(String profileId) {
		try {
			Files.createDirectories(FOLDER);
		} catch (IOException e) {
			LOGGER.error("[Skyblocker] Failed to create folder for garden plots!", e);
		}
		Path resolve = FOLDER.resolve(profileId + ".json");

		try (BufferedWriter writer = Files.newBufferedWriter(resolve)) {
			JsonArray elements = new JsonArray();
			Arrays.stream(GARDEN_PLOTS).map(gardenPlot -> {
				if (gardenPlot == null) return null;
				return GardenPlot.CODEC.encodeStart(JsonOps.INSTANCE, gardenPlot).result().orElse(null);
			}).forEach(elements::add);

			SkyblockerMod.GSON.toJson(elements, writer);
		} catch (Exception e) {
			LOGGER.error("[Skyblocker] Failed to save Garden Plots data", e);
		}
	}

	private static void load(String profileId) {
		Path resolve = FOLDER.resolve(profileId + ".json");
		CompletableFuture.supplyAsync(() -> {
			try (BufferedReader reader = Files.newBufferedReader(resolve)) {
				return SkyblockerMod.GSON.fromJson(reader, JsonArray.class).asList().stream().map(jsonElement -> {
							if (jsonElement == null || jsonElement.isJsonNull()) return null;
							return GardenPlot.CODEC.decode(JsonOps.INSTANCE, jsonElement).result().map(Pair::getFirst).orElseThrow();
						}
				).toArray(GardenPlot[]::new);
			} catch (NoSuchFileException ignored) {
			} catch (Exception e) {
				LOGGER.error("[Skyblocker] Failed to load Equipment data", e);
			}
			return new GardenPlot[25];
			// Schedule on main thread to avoid any async weirdness
		}, Executors.newVirtualThreadPerTaskExecutor()).thenAccept(newPlots -> Minecraft.getInstance().execute(() -> System.arraycopy(newPlots, 0, GARDEN_PLOTS, 0, Math.min(newPlots.length, 25))));
	}

	public record GardenPlot(Either<Item, String> icon, String name, Optional<String> customIcon) {
		@SuppressWarnings("deprecation")
		public static final Codec<GardenPlot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
								Codec.either(Item.CODEC.xmap(Holder::value, Item::builtInRegistryHolder), Codec.STRING)
										.fieldOf("icon").forGetter(GardenPlot::icon),
								Codec.STRING.fieldOf("name").forGetter(GardenPlot::name),
								Codec.STRING.optionalFieldOf("customIcon").forGetter(GardenPlot::customIcon)
						)
						.apply(instance, GardenPlot::new)
		);

		public GardenPlot(Either<Item, String> icon, String name) {
			this(icon, name, Optional.empty());
		}

		public GardenPlot withIconName(Either<Item, String> icon, String name) {
			return new GardenPlot(icon, name, customIcon);
		}

		public GardenPlot withCustomIcon(@Nullable String customIcon) {
			return new GardenPlot(icon, name, Optional.ofNullable(customIcon));
		}
	}
}
