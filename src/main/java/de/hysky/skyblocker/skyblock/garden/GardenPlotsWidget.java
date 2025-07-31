package de.hysky.skyblocker.skyblock.garden;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.injected.RecipeBookHolder;
import de.hysky.skyblocker.mixins.accessors.HandledScreenAccessor;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.gui.ItemButtonWidget;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import it.unimi.dsi.fastutil.ints.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import org.joml.Matrix3x2fStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class GardenPlotsWidget extends ContainerWidget {

	private static final Logger LOGGER = LoggerFactory.getLogger("Garden Plots");
	private static final Path FOLDER = SkyblockerMod.CONFIG_DIR.resolve("garden_plots");

	//////////////////////////
	// STATIC SHENANIGANS
	//////////////////////////

	public static final Int2IntMap GARDEN_PLOT_TO_SLOT = Int2IntMaps.unmodifiable(new Int2IntOpenHashMap(Map.ofEntries(
			Map.entry(1, 7),
			Map.entry(2, 11),
			Map.entry(3, 13),
			Map.entry(4, 17),
			Map.entry(5, 6),
			Map.entry(6, 8),
			Map.entry(7, 16),
			Map.entry(8, 18),
			Map.entry(9, 2),
			Map.entry(10, 10),
			Map.entry(11, 14),
			Map.entry(12, 22),
			Map.entry(13, 1),
			Map.entry(14, 3),
			Map.entry(15, 5),
			Map.entry(16, 9),
			Map.entry(17, 15),
			Map.entry(18, 19),
			Map.entry(19, 21),
			Map.entry(20, 23),
			Map.entry(21, 0),
			Map.entry(22, 4),
			Map.entry(23, 20),
			Map.entry(24, 24)
	)));

	private static final GardenPlot[] gardenPlots = new GardenPlot[25];

	@Init
	public static void init() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof GenericContainerScreen containerScreen && screen.getTitle().getString().trim().equals("Configure Plots")) {
				ScreenEvents.remove(screen).register(ignored -> {
					GenericContainerScreenHandler screenHandler = containerScreen.getScreenHandler();
					// Take plot icons and names
					for (int row = 0; row < 5; row++) for (int i = row * 9 + 2; i < row * 9 + 7; i++) {
						if (i == 22) continue; // Barn icon
						Slot slot = screenHandler.slots.get(i);
						ItemStack stack = slot.getStack();
						if (stack.isEmpty() || stack.isOf(Items.RED_STAINED_GLASS_PANE) || stack.isOf(Items.OAK_BUTTON) || stack.isOf(Items.BLACK_STAINED_GLASS_PANE))
							continue;
						gardenPlots[(i / 9) * 5 + (i % 9 - 2)] = new GardenPlot(stack.getItem(), stack.getName().getString().split("-", 2)[1].trim());
					}

				});
			} else if (screen instanceof InventoryScreen inventoryScreen && Utils.getLocation().equals(Location.GARDEN) && SkyblockerConfigManager.get().farming.garden.gardenPlotsWidget) {
				GardenPlotsWidget widget = new GardenPlotsWidget(
						((HandledScreenAccessor) inventoryScreen).getX() + ((HandledScreenAccessor) inventoryScreen).getBackgroundWidth() + 4,
						((HandledScreenAccessor) inventoryScreen).getY());
				Screens.getButtons(inventoryScreen).add(widget);

				((RecipeBookHolder) inventoryScreen).registerRecipeBookToggleCallback(() -> widget.setPosition(
						((HandledScreenAccessor) inventoryScreen).getX() + ((HandledScreenAccessor) inventoryScreen).getBackgroundWidth() + 4,
						((HandledScreenAccessor) inventoryScreen).getY()
				));
			}
		});

		SkyblockEvents.PROFILE_CHANGE.register(((prevProfileId, profileId) -> {
			if (!prevProfileId.isEmpty())
				CompletableFuture.runAsync(() -> save(prevProfileId)).thenRun(() -> load(profileId));
			else load(profileId);
		}));

		ClientLifecycleEvents.CLIENT_STOPPING.register(client1 -> {
			String profileId = Utils.getProfileId();
			if (!profileId.isBlank()) {
				CompletableFuture.runAsync(() -> save(profileId));
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
			Arrays.stream(gardenPlots).map(gardenPlot -> {
				if (gardenPlot == null) return null;
				JsonObject jsonObject = new JsonObject();
				jsonObject.add("icon", Item.ENTRY_CODEC.encodeStart(JsonOps.INSTANCE, gardenPlot.item.getRegistryEntry()).getOrThrow());
				jsonObject.addProperty("name", gardenPlot.name);
				return jsonObject;
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
							JsonObject jsonObject = jsonElement.getAsJsonObject();
							return new GardenPlot(Item.ENTRY_CODEC.decode(JsonOps.INSTANCE, jsonObject.get("icon")).getOrThrow().getFirst().value(), jsonObject.get("name").getAsString());
						}
				).toArray(GardenPlot[]::new);
			} catch (NoSuchFileException ignored) {
			} catch (Exception e) {
				LOGGER.error("[Skyblocker] Failed to load Equipment data", e);
			}
			return new GardenPlot[25];
			// Schedule on main thread to avoid any async weirdness
		}).thenAccept(newPlots -> MinecraftClient.getInstance().execute(() -> System.arraycopy(newPlots, 0, gardenPlots, 0, Math.min(newPlots.length, 25))));
	}

	/////////////////////////////
	// THE WIDGET ITSELF
	/////////////////////////////

	private static final Identifier BACKGROUND_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/garden_plots.png");
	private static final MutableText GROSS_PEST_TEXT = Text.translatable("skyblocker.gardenPlots.pests").formatted(Formatting.RED, Formatting.BOLD);
	private static final MutableText TP_TEXT = Text.translatable("skyblocker.gardenPlots.tp").formatted(Formatting.YELLOW, Formatting.BOLD);

	private final ItemStack[] items;
	private int hoveredSlot = -1;
	private long updateFromTabTime = System.currentTimeMillis();
	private final IntList infectedPlots = new IntArrayList(8);

	private final ItemButtonWidget[] widgets;

	public GardenPlotsWidget(int x, int y) {
		super(x, y, 104, 132, Text.translatable("skyblocker.gardenPlots"));
		items = Arrays.stream(gardenPlots).map(gardenPlot -> {
			if (gardenPlot == null) return null;
			ItemStack itemStack = new ItemStack(gardenPlot.item());
			itemStack.set(DataComponentTypes.ITEM_NAME, Text.literal(gardenPlot.name()).formatted(Formatting.GREEN, Formatting.BOLD));
			return itemStack;
		}).toArray(ItemStack[]::new);
		items[12] = new ItemStack(Items.LODESTONE);
		items[12].set(DataComponentTypes.ITEM_NAME, Text.literal("The Barn"));
		updateInfestedFromTab();

		// Inner widgets
		ItemButtonWidget deskButton = new ItemButtonWidget(
				getX() + 7, getBottom() - 24,
				new ItemStack(Items.BOOK), Text.translatable("skyblocker.gardenPlots.openDesk"),
				button -> MessageScheduler.INSTANCE.sendMessageAfterCooldown("/desk", true)
		);
		ItemButtonWidget spawnButton = new ItemButtonWidget(
				getRight() - 7 - 40 - 2, getBottom() - 24,
				new ItemStack(Items.ENDER_EYE), Text.translatable("skyblocker.gardenPlots.spawn"),
				button -> MessageScheduler.INSTANCE.sendMessageAfterCooldown("/warp garden", true)
		);
		ItemButtonWidget setSpawnButton = new ItemButtonWidget(
				getRight() - 7 - 20, getBottom() - 24,
				new ItemStack(Math.random() < 0.001 ? Items.PINK_BED : Items.RED_BED), Text.translatable("skyblocker.gardenPlots.setSpawn"),
				button -> MessageScheduler.INSTANCE.sendMessageAfterCooldown("/setspawn", true)
		);
		widgets = new ItemButtonWidget[]{deskButton, spawnButton, setSpawnButton};
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		Matrix3x2fStack matrices = context.getMatrices();
		matrices.pushMatrix();
		matrices.translate(getX(), getY());

		context.drawTexture(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, 0, 0, 0, 0, getWidth(), getHeight(), getWidth(), getHeight());

		context.drawText(textRenderer, getMessage(), 8, 6, Colors.DARK_GRAY, false);

		hoveredSlot = -1;
		long timeMillis = System.currentTimeMillis();
		for (int i = 0; i < items.length; i++) {
			ItemStack item = items[i];
			if (item == null) continue;


			int slotX = 7 + (i % 5) * 18;
			int slotY = 17 + (i / 5) * 18;
			boolean hovered = slotX + getX() <= mouseX && mouseX < slotX + getX() + 18 && slotY + getY() <= mouseY && mouseY < slotY + getY() + 18;

			if (hovered) {
				context.fill(slotX + 1, slotY + 1, slotX + 17, slotY + 17, 0xAA_FF_FF_FF);
				matrices.pushMatrix();
				matrices.translate(slotX, slotY);
				matrices.scale(1.125f, 1.125f);
				context.drawItem(item, 0, 0);
				matrices.popMatrix();
				hoveredSlot = i;
			} else
				context.drawItem(item, slotX + 1, slotY + 1);

			boolean infested = infectedPlots.contains(i);
			if (infested && (timeMillis & 512) != 0) {
				context.drawBorder(slotX + 1, slotY + 1, 16, 16, Colors.RED);
			}

			// tooltip
			if (hovered) {
				List<Text> tooltip = infested ?
						List.of(
								Text.translatable("skyblocker.gardenPlots.plot", item.getName()),
								GROSS_PEST_TEXT,
								Text.empty(),
								TP_TEXT) :

						i == 12 ?
								List.of(
										item.getName(),
										Text.empty(),
										TP_TEXT) :

								List.of(
										Text.translatable("skyblocker.gardenPlots.plot", item.getName()),
										Text.empty(),
										TP_TEXT
								);
				context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
			}
		}

		matrices.popMatrix();

		for (ItemButtonWidget widget : widgets) {
			widget.render(context, mouseX, mouseY, delta);
		}



		if (timeMillis - updateFromTabTime > 3000) {
			updateFromTabTime = timeMillis;
			updateInfestedFromTab();
		}
	}

	private void updateInfestedFromTab() {
		infectedPlots.clear();
		for (int i = 0; i < PlayerListManager.getPlayerStringList().size(); i++) {
			String string = PlayerListManager.getPlayerStringList().get(i);
			if (string.startsWith("Plots:")) {
				String[] split = string.split(":")[1].split(",");
				for (String s : split) {
					try {
						infectedPlots.add(GARDEN_PLOT_TO_SLOT.getOrDefault(Integer.parseInt(s.strip()), -1));
					} catch (NumberFormatException ignored) {}
				}
				break;
			}
		}
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		super.onClick(mouseX, mouseY);
		if (hoveredSlot == -1) return;

		if (SkyblockerConfigManager.get().farming.garden.closeScreenOnPlotClick && MinecraftClient.getInstance().currentScreen != null)
			MinecraftClient.getInstance().currentScreen.close();

		if (hoveredSlot == 12) MessageScheduler.INSTANCE.sendMessageAfterCooldown("/plottp barn", true);
		else MessageScheduler.INSTANCE.sendMessageAfterCooldown("/plottp " + gardenPlots[hoveredSlot].name, true);
	}

	@Override
	protected boolean isValidClickButton(int button) {
		return super.isValidClickButton(button) && hoveredSlot != -1;
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
	}

	@Override
	public List<? extends Element> children() {
		return List.of(widgets);
	}

	@Override
	protected int getContentsHeightWithPadding() {
		return getHeight();
	}

	@Override
	protected double getDeltaYPerScroll() {
		return 0;
	}

	@Override
	public void setX(int x) {
		int prevX = getX();
		super.setX(x);
		int diff = x - prevX;
		for (ItemButtonWidget widget : widgets) {
			widget.setX(widget.getX() + diff);
		}
	}

	@Override
	public void setY(int y) {
		int prevY = getY();
		super.setY(y);
		int diff = y - prevY;
		for (ItemButtonWidget widget : widgets) {
			widget.setY(widget.getY() + diff);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (isMouseOver(mouseX, mouseY) && isValidClickButton(button)) {
			onClick(mouseX, mouseY);
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	private record GardenPlot(Item item, String name) {
	}
}
