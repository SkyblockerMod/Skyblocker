package de.hysky.skyblocker.skyblock.item;

import com.mojang.serialization.Codec;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.mixins.accessors.SlotAccessor;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.nbt.visitor.StringNbtWriter;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Opened here {@code de.hysky.skyblocker.mixins.MinecraftClientMixin#skyblocker$skyblockInventoryScreen}
 * <br>
 * Book button is moved here {@code de.hysky.skyblocker.mixins.InventoryScreenMixin#skyblocker}
 */
public class SkyblockInventoryScreen extends InventoryScreen {
    private static final Logger LOGGER = LoggerFactory.getLogger("Equipment");
    private static final Supplier<ItemStack[]> EMPTY_EQUIPMENT = () -> new ItemStack[]{ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
    public static final ItemStack[] equipment = EMPTY_EQUIPMENT.get();
    public static final ItemStack[] equipment_rift = EMPTY_EQUIPMENT.get();
    private static final Codec<ItemStack[]> CODEC = ItemUtils.EMPTY_ALLOWING_ITEMSTACK_CODEC.listOf(4, 8) // min size at 4 for backwards compat
            .xmap(itemStacks -> itemStacks.toArray(ItemStack[]::new), List::of).fieldOf("items").codec();

    private static final Identifier SLOT_TEXTURE = Identifier.ofVanilla("container/slot");
    private static final Int2ObjectMap<Identifier> SLOT_TEXTURES = Util.make(new Int2ObjectOpenHashMap<>(), map -> {
        map.put(0, Identifier.of(SkyblockerMod.NAMESPACE, "equipment/necklace"));
        map.put(1, Identifier.of(SkyblockerMod.NAMESPACE, "equipment/cloak"));
        map.put(2, Identifier.of(SkyblockerMod.NAMESPACE, "equipment/belt"));
        map.put(3, Identifier.of(SkyblockerMod.NAMESPACE, "equipment/gloves"));
    });
    private static final Path FOLDER = SkyblockerMod.CONFIG_DIR.resolve("equipment");

    private final Slot[] equipmentSlots = new Slot[4];

    private static void save(String profileId) {
        try {
            Files.createDirectories(FOLDER);
        } catch (IOException e) {
            LOGGER.error("[Skyblocker] Failed to create folder for equipment!", e);
        }
        Path resolve = FOLDER.resolve(profileId + ".nbt");

        try (BufferedWriter writer = Files.newBufferedWriter(resolve)) {

            writer.write(new StringNbtWriter().apply(CODEC.encodeStart(NbtOps.INSTANCE, ArrayUtils.addAll(equipment, equipment_rift)).getOrThrow()));
        } catch (Exception e) {
            LOGGER.error("[Skyblocker] Failed to save Equipment data", e);
        }
    }

    private static void load(String profileId) {
        Path resolve = FOLDER.resolve(profileId + ".nbt");
        CompletableFuture.supplyAsync(() -> {
            try (BufferedReader reader = Files.newBufferedReader(resolve)) {
                return CODEC.parse(NbtOps.INSTANCE, StringNbtReader.parse(reader.lines().collect(Collectors.joining()))).getOrThrow();
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
        SimpleInventory inventory = new SimpleInventory(Utils.isInTheRift() ? equipment_rift: equipment);

        Slot slot = handler.slots.get(45);
        ((SlotAccessor) slot).setX(slot.x + 21);

        for (int i = 0; i < 4; i++) {
            equipmentSlots[i] = new EquipmentSlot(inventory, i, 77, 8 + i * 18, SLOT_TEXTURES.get(i));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Slot equipmentSlot : equipmentSlots) {
            if (isPointWithinBounds(equipmentSlot.x, equipmentSlot.y, 16, 16, mouseX, mouseY)) {
                MessageScheduler.INSTANCE.sendMessageAfterCooldown("/equipment");
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * Draws the equipment slots in the foreground layer after vanilla slots are drawn
     * in {@link net.minecraft.client.gui.screen.ingame.HandledScreen#render(DrawContext, int, int, float) HandledScreen#render(DrawContext, int, int, float)}.
     */
    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        for (Slot equipmentSlot : equipmentSlots) {
            drawSlot(context, equipmentSlot);
            if (isPointWithinBounds(equipmentSlot.x, equipmentSlot.y, 16, 16, mouseX, mouseY)) drawSlotHighlight(context, equipmentSlot.x, equipmentSlot.y, 0);
        }

        super.drawForeground(context, mouseX, mouseY);
    }

    @Override
    protected void drawMouseoverTooltip(DrawContext context, int x, int y) {
        super.drawMouseoverTooltip(context, x, y);
        if (!handler.getCursorStack().isEmpty()) return;
        for (Slot equipmentSlot : equipmentSlots) {
            if (isPointWithinBounds(equipmentSlot.x, equipmentSlot.y, 16, 16, x, y) && equipmentSlot.hasStack()) {
                ItemStack itemStack = equipmentSlot.getStack();
                context.drawTooltip(this.textRenderer, this.getTooltipFromItem(itemStack), itemStack.getTooltipData(), x, y);
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
        for (int i = 0; i < 4; i++) {
            context.drawGuiTexture(SLOT_TEXTURE, x + 76 + (i == 3 ? 21 : 0), y + 7 + i * 18, 18, 18);
        }
    }

    @Override
    protected void drawSlot(DrawContext context, Slot slot) {
        super.drawSlot(context, slot);
        if (slot instanceof EquipmentSlot equipmentSlot && !slot.hasStack()) {
            context.drawGuiTexture(equipmentSlot.slotTexture, slot.x, slot.y, 16, 16);
        }
    }

    private static class EquipmentSlot extends Slot {
    	private final Identifier slotTexture;

        public EquipmentSlot(Inventory inventory, int index, int x, int y, Identifier texture) {
            super(inventory, index, x, y);
            this.slotTexture = texture;
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
