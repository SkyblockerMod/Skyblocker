package de.hysky.skyblocker.skyblock.item;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.compatibility.ResourcePackCompatibility;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.mixins.accessors.HandledScreenAccessor;
import de.hysky.skyblocker.mixins.accessors.ScreenAccessor;
import de.hysky.skyblocker.mixins.accessors.SlotAccessor;
import de.hysky.skyblocker.utils.hoveredItem.HoveredItemStackProvider;
import de.hysky.skyblocker.skyblock.item.wikilookup.WikiLookupManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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
 * <p>Opened here {@link de.hysky.skyblocker.mixins.MinecraftClientMixin#skyblocker$skyblockInventoryScreen MinecraftClientMixin#skyblocker$skyblockInventoryScreen}</p>
 * <p>Book button is moved here {@link de.hysky.skyblocker.mixins.InventoryScreenMixin#skyblocker$moveButton InventoryScreenMixin#skyblocker$moveButton}</p>
 */
public class SkyblockInventoryScreen extends InventoryScreen implements HoveredItemStackProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger("Equipment");
    private static final Supplier<ItemStack[]> EMPTY_EQUIPMENT = () -> new ItemStack[]{ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
    public static final ItemStack[] equipment = EMPTY_EQUIPMENT.get();
    public static final ItemStack[] equipment_rift = EMPTY_EQUIPMENT.get();
    private static final Codec<ItemStack[]> CODEC = ItemUtils.EMPTY_ALLOWING_ITEMSTACK_CODEC.listOf(4, 8) // min size at 4 for backwards compat
            .xmap(itemStacks -> itemStacks.toArray(ItemStack[]::new), List::of).fieldOf("items").codec();

    private static final Identifier SLOT_TEXTURE = Identifier.ofVanilla("container/slot");
    private static final Identifier EMPTY_SLOT = SkyblockerMod.id("equipment/empty_icon");
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
            NbtIo.write(CODEC.encodeStart(NbtOps.INSTANCE, ArrayUtils.addAll(equipment, equipment_rift)).getOrThrow(), new DataOutputStream(Files.newOutputStream(resolve)));
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
        }).thenAccept(itemStacks -> MinecraftClient.getInstance().execute(() -> {
            System.arraycopy(itemStacks, 0, equipment, 0, Math.min(itemStacks.length, 4));
            if (itemStacks.length <= 4) return;
            System.arraycopy(itemStacks, 4, equipment_rift, 0, Math.clamp(itemStacks.length - 4, 0, 4));
        }));
    }

	@Override
	public void onDisplayed() {
		Slot slot = handler.slots.get(45);
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

    public SkyblockInventoryScreen(PlayerEntity player) {
        super(player);
		if (ResourcePackCompatibility.options.renameInventoryScreen().orElse(false)) {
			((ScreenAccessor) this).setTitle(Text.literal(SkyblockerConfigManager.get().quickNav.enableQuickNav ? "InventoryScreenEquipmentQuickNavSkyblocker" : "InventoryScreenEquipmentSkyblocker"));
		}
	    SimpleInventory inventory = new SimpleInventory(Utils.isInTheRift() ? equipment_rift : equipment);
	    for (int i = 0; i < 4; i++) {
		    equipmentSlots[i] = new EquipmentSlot(inventory, i, 77, 8 + i * 18);
	    }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        for (Slot equipmentSlot : equipmentSlots) {
            if (isPointWithinBounds(equipmentSlot.x, equipmentSlot.y, 16, 16, click.x(), click.y())) {
                MessageScheduler.INSTANCE.sendMessageAfterCooldown("/equipment", true);
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    /**
     * Draws the equipment slots in the foreground layer after vanilla slots are drawn
     * in {@link net.minecraft.client.gui.screen.ingame.HandledScreen#render(DrawContext, int, int, float) HandledScreen#render(DrawContext, int, int, float)}.
     */
    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        for (Slot equipmentSlot : equipmentSlots) {
            boolean hovered = isPointWithinBounds(equipmentSlot.x, equipmentSlot.y, 16, 16, mouseX, mouseY);

            if (hovered) context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HandledScreenAccessor.getSLOT_HIGHLIGHT_BACK_TEXTURE(), equipmentSlot.x - 4, equipmentSlot.y - 4, 24, 24);

            drawSlot(context, equipmentSlot);

            if (hovered) context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HandledScreenAccessor.getSLOT_HIGHLIGHT_FRONT_TEXTURE(), equipmentSlot.x - 4, equipmentSlot.y - 4, 24, 24);
        }

        super.drawForeground(context, mouseX, mouseY);
    }

    @Override
    protected void drawMouseoverTooltip(DrawContext context, int x, int y) {
        super.drawMouseoverTooltip(context, x, y);

		hoveredItem = null;
        if (!handler.getCursorStack().isEmpty()) return;

        for (Slot equipmentSlot : equipmentSlots) {
            if (isPointWithinBounds(equipmentSlot.x, equipmentSlot.y, 16, 16, x, y) && equipmentSlot.hasStack()) {
                ItemStack itemStack = equipmentSlot.getStack();
                context.drawTooltip(this.textRenderer, this.getTooltipFromItem(itemStack), itemStack.getTooltipData(), x, y);
				hoveredItem = itemStack;
            }
        }
    }

    @Override
    public void removed() {
        super.removed();
        // put the handler back how it was, the handler is the same while the player is alive/in the same world
        Slot slot = handler.slots.get(45);
        ((SlotAccessor) slot).setX(slot.x - 21);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        super.drawBackground(context, delta, mouseX, mouseY);
        for (int i = 0; i < 3; i++) {
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, SLOT_TEXTURE, x + 76, y + 7 + i * 18, 18, 18);
        }
		Slot slot = handler.slots.get(45);
		context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, SLOT_TEXTURE, x + slot.x - 1, y + slot.y - 1, 18, 18);
    }

    @Override
    protected void drawSlot(DrawContext context, Slot slot) {
        super.drawSlot(context, slot);
        if (slot instanceof EquipmentSlot && !slot.hasStack()) {
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, EMPTY_SLOT, slot.x, slot.y, 16, 16);
        }
    }

	@Override
	public @Nullable ItemStack getFocusedItem() {
		return hoveredItem;
	}

	@Override
	public boolean keyPressed(KeyInput input) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.isWindowFocused()) {
			var mouse = client.mouse;
			var window = client.getWindow();
			var mouseX = Mouse.scaleX(window, mouse.getX());
			var mouseY = Mouse.scaleY(window, mouse.getY());

			for (Slot equipmentSlot : equipmentSlots) {
				if (isPointWithinBounds(equipmentSlot.x, equipmentSlot.y, 16, 16, mouseX, mouseY)) {
					if (WikiLookupManager.handleWikiLookup(Either.left(equipmentSlot), client.player, input)) {
						return true;
					}
				}
			}
		}
		return super.keyPressed(input);
	}

    private static class EquipmentSlot extends Slot {

        private EquipmentSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canTakeItems(PlayerEntity playerEntity) {
            return false;
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }
    }
}
