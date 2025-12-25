package de.hysky.skyblocker.skyblock.item;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.compatibility.ResourcePackCompatibility;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.mixins.accessors.AbstractContainerScreenAccessor;
import de.hysky.skyblocker.mixins.accessors.ScreenAccessor;
import de.hysky.skyblocker.mixins.accessors.SlotAccessor;
import de.hysky.skyblocker.utils.hoveredItem.HoveredItemStackProvider;
import de.hysky.skyblocker.skyblock.item.wikilookup.WikiLookupManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * <p>Adds equipment slots to the inventory screen and moves the offhand slot.</p>
 * <p>Opened here {@link de.hysky.skyblocker.mixins.MinecraftMixin#skyblocker$skyblockInventoryScreen MinecraftClientMixin#skyblocker$skyblockInventoryScreen}</p>
 * <p>Book button is moved here {@link de.hysky.skyblocker.mixins.InventoryScreenMixin#skyblocker$moveButton InventoryScreenMixin#skyblocker$moveButton}</p>
 */
public class SkyblockInventoryScreen extends InventoryScreen implements HoveredItemStackProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger("Equipment");
	private static final Supplier<ItemStack[]> EMPTY_EQUIPMENT = () -> new ItemStack[]{ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
	public static final ItemStack[] equipment = EMPTY_EQUIPMENT.get();
	public static final ItemStack[] equipment_rift = EMPTY_EQUIPMENT.get();
	private static final Codec<ItemStack[]> CODEC = ItemUtils.EMPTY_ALLOWING_ITEMSTACK_CODEC.listOf(4, 8) // min size at 4 for backwards compat
			.xmap(itemStacks -> itemStacks.toArray(ItemStack[]::new), List::of).fieldOf("items").codec();

	private static final ResourceLocation SLOT_TEXTURE = ResourceLocation.withDefaultNamespace("container/slot");
	private static final ResourceLocation EMPTY_SLOT = SkyblockerMod.id("equipment/empty_icon");
	private static final Path FOLDER = SkyblockerMod.CONFIG_DIR.resolve("equipment");

	private final Slot[] equipmentSlots = new Slot[4];
	private ItemStack hoveredItem;

	private static void save(String profileId) {
		try {
			Files.createDirectories(FOLDER);
		} catch (IOException e) {
			LOGGER.error("[Skyblocker] Failed to create folder for equipment!", e);
		}
		Path resolve = FOLDER.resolve(profileId + ".nbt");

		try {
			NbtIo.writeUnnamedTagWithFallback(CODEC.encodeStart(NbtOps.INSTANCE, ArrayUtils.addAll(equipment, equipment_rift)).getOrThrow(), new DataOutputStream(Files.newOutputStream(resolve)));
		} catch (Exception e) {
			LOGGER.error("[Skyblocker] Failed to save Equipment data", e);
		}
	}

	private static void load(String profileId) {
		Path resolve = FOLDER.resolve(profileId + ".nbt");
		CompletableFuture.supplyAsync(() -> {
			if (!Files.exists(resolve)) return EMPTY_EQUIPMENT.get();
			try {
				return CODEC.parse(NbtOps.INSTANCE, NbtIo.read(resolve)).getOrThrow();
			} catch (NoSuchFileException ignored) {
			} catch (Exception e) {
				LOGGER.error("[Skyblocker] Failed to load Equipment data", e);
			}
			return EMPTY_EQUIPMENT.get();
			// Schedule on main thread to avoid any async weirdness
		}).thenAccept(itemStacks -> Minecraft.getInstance().execute(() -> {
			System.arraycopy(itemStacks, 0, equipment, 0, Math.min(itemStacks.length, 4));
			if (itemStacks.length <= 4) return;
			System.arraycopy(itemStacks, 4, equipment_rift, 0, Math.clamp(itemStacks.length - 4, 0, 4));
		}));
	}

	@Override
	public void added() {
		Slot slot = menu.slots.get(45);
		((SlotAccessor) slot).setX(slot.x + 21);
	}

	@Init
	public static void initEquipment() {
		SkyblockEvents.PROFILE_CHANGE.register(((prevProfileId, profileId) -> {
			if (!prevProfileId.isEmpty()) CompletableFuture.runAsync(() -> save(prevProfileId)).thenRun(() -> load(profileId));
			else load(profileId);
		}));

		ClientLifecycleEvents.CLIENT_STOPPING.register(client1 -> {
			String profileId = Utils.getProfileId();
			if (!profileId.isBlank()) {
				CompletableFuture.runAsync(() -> save(profileId));
			}
		});
	}

	public SkyblockInventoryScreen(Player player) {
		super(player);
		if (ResourcePackCompatibility.options.renameInventoryScreen().orElse(false)) {
			((ScreenAccessor) this).setTitle(Component.literal(SkyblockerConfigManager.get().quickNav.enableQuickNav ? "InventoryScreenEquipmentQuickNavSkyblocker" : "InventoryScreenEquipmentSkyblocker"));
		}
		SimpleContainer inventory = new SimpleContainer(Utils.isInTheRift() ? equipment_rift : equipment);
		for (int i = 0; i < 4; i++) {
			equipmentSlots[i] = new EquipmentSlot(inventory, i, 77, 8 + i * 18);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		for (Slot equipmentSlot : equipmentSlots) {
			if (isHovering(equipmentSlot.x, equipmentSlot.y, 16, 16, click.x(), click.y())) {
				MessageScheduler.INSTANCE.sendMessageAfterCooldown("/equipment", true);
				return true;
			}
		}
		return super.mouseClicked(click, doubled);
	}

	/**
	 * Draws the equipment slots in the foreground layer after vanilla slots are drawn
	 * in {@link net.minecraft.client.gui.screens.inventory.AbstractContainerScreen#render(GuiGraphics, int, int, float) HandledScreen#render(DrawContext, int, int, float)}.
	 */
	@Override
	protected void renderLabels(GuiGraphics context, int mouseX, int mouseY) {
		for (Slot equipmentSlot : equipmentSlots) {
			boolean hovered = isHovering(equipmentSlot.x, equipmentSlot.y, 16, 16, mouseX, mouseY);

			if (hovered) context.blitSprite(RenderPipelines.GUI_TEXTURED, AbstractContainerScreenAccessor.getSLOT_HIGHLIGHT_BACK_SPRITE(), equipmentSlot.x - 4, equipmentSlot.y - 4, 24, 24);

			renderSlot(context, equipmentSlot);

			if (hovered) context.blitSprite(RenderPipelines.GUI_TEXTURED, AbstractContainerScreenAccessor.getSLOT_HIGHLIGHT_FRONT_SPRITE(), equipmentSlot.x - 4, equipmentSlot.y - 4, 24, 24);
		}

		super.renderLabels(context, mouseX, mouseY);
	}

	@Override
	protected void renderTooltip(GuiGraphics context, int x, int y) {
		super.renderTooltip(context, x, y);

		hoveredItem = null;
		if (!menu.getCarried().isEmpty()) return;

		for (Slot equipmentSlot : equipmentSlots) {
			if (isHovering(equipmentSlot.x, equipmentSlot.y, 16, 16, x, y) && equipmentSlot.hasItem()) {
				ItemStack itemStack = equipmentSlot.getItem();
				context.setTooltipForNextFrame(this.font, this.getTooltipFromContainerItem(itemStack), itemStack.getTooltipImage(), x, y);
				hoveredItem = itemStack;
			}
		}
	}

	@Override
	public void removed() {
		super.removed();
		// put the handler back how it was, the handler is the same while the player is alive/in the same world
		Slot slot = menu.slots.get(45);
		((SlotAccessor) slot).setX(slot.x - 21);
	}

	@Override
	protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
		super.renderBg(context, delta, mouseX, mouseY);
		for (int i = 0; i < 3; i++) {
			context.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_TEXTURE, leftPos + 76, topPos + 7 + i * 18, 18, 18);
		}
		Slot slot = menu.slots.get(45);
		context.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_TEXTURE, leftPos + slot.x - 1, topPos + slot.y - 1, 18, 18);
	}

	@Override
	protected void renderSlot(GuiGraphics context, Slot slot) {
		super.renderSlot(context, slot);
		if (slot instanceof EquipmentSlot && !slot.hasItem()) {
			context.blitSprite(RenderPipelines.GUI_TEXTURED, EMPTY_SLOT, slot.x, slot.y, 16, 16);
		}
	}

	@Override
	public @Nullable ItemStack getFocusedItem() {
		return hoveredItem;
	}

	@Override
	public boolean keyPressed(KeyEvent input) {
		Minecraft client = Minecraft.getInstance();
		if (client.isWindowActive()) {
			var mouse = client.mouseHandler;
			var window = client.getWindow();
			var mouseX = MouseHandler.getScaledXPos(window, mouse.xpos());
			var mouseY = MouseHandler.getScaledYPos(window, mouse.ypos());

			for (Slot equipmentSlot : equipmentSlots) {
				if (isHovering(equipmentSlot.x, equipmentSlot.y, 16, 16, mouseX, mouseY)) {
					if (WikiLookupManager.handleWikiLookup(Either.left(equipmentSlot), client.player, input)) {
						return true;
					}
				}
			}
		}
		return super.keyPressed(input);
	}

	private static class EquipmentSlot extends Slot {

		private EquipmentSlot(Container inventory, int index, int x, int y) {
			super(inventory, index, x, y);
		}

		@Override
		public boolean mayPickup(Player playerEntity) {
			return false;
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return false;
		}
	}
}
