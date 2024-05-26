package de.hysky.skyblocker.skyblock.item;

import com.mojang.serialization.Codec;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.mixins.accessors.SlotAccessor;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
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
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Opened here {@code de.hysky.skyblocker.mixins.MinecraftClientMixin#skyblocker$skyblockInventoryScreen}
 * <br>
 * Book button is moved here {@code de.hysky.skyblocker.mixins.InventoryScreenMixin#skyblocker}
 */
public class SkyblockInventoryScreen extends InventoryScreen {
    private static final Logger LOGGER = LoggerFactory.getLogger("Equipment");

    public static final ItemStack[] equipment = new ItemStack[]{ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
    private static final Codec<ItemStack[]> CODEC = ItemUtils.EMPTY_ALLOWING_ITEMSTACK_CODEC.listOf(4,4)
            .xmap(itemStacks -> itemStacks.toArray(ItemStack[]::new), List::of).fieldOf("items").codec();

    private static final Identifier SLOT_TEXTURE = new Identifier("container/slot");
    private static final Identifier EMPTY_SLOT = new Identifier(SkyblockerMod.NAMESPACE, "equipment/empty_icon");

    public static void initEquipment() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client1 -> {
            Path resolve = SkyblockerMod.CONFIG_DIR.resolve("equipment.nbt");
            try (BufferedReader reader = Files.newBufferedReader(resolve)) {
                ItemStack[] array = CODEC.parse(
                                NbtOps.INSTANCE, StringNbtReader.parse(reader.lines().collect(Collectors.joining())))
                        .getOrThrow();
                System.arraycopy(array, 0, equipment, 0, Math.min(array.length, 4));
            } catch (Exception e) {
                LOGGER.error("[Skyblocker] Failed to load Equipment data", e);
            }
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client1 -> {
            Path resolve = SkyblockerMod.CONFIG_DIR.resolve("equipment.nbt");
            try (BufferedWriter writer = Files.newBufferedWriter(resolve)) {
                writer.write(new StringNbtWriter().apply(CODEC.encodeStart(NbtOps.INSTANCE, equipment).getOrThrow()));
            } catch (Exception e) {
                LOGGER.error("[Skyblocker] Failed to save Equipment data", e);
            }
        });
    }

    public SkyblockInventoryScreen(PlayerEntity player) {
        super(player);
        SimpleInventory inventory = new SimpleInventory(equipment);

        Slot slot = handler.slots.get(45);
        ((SlotAccessor) slot).setX(slot.x + 21);
        for (int i = 0; i < 4; i++) {
            handler.slots.add(new EquipmentSlot(inventory, i, 77, 8 + i * 18));
        }
    }

    @Override
    protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
        if (slot instanceof EquipmentSlot && handler.getCursorStack().isEmpty()) {
            MessageScheduler.INSTANCE.sendMessageAfterCooldown("/equipment");
        }
        super.onMouseClick(slot, slotId, button, actionType);
    }

    @Override
    public void removed() {
        super.removed();
        // put the handler back how it was, the handler is the same while the player is alive/in the same world
        Slot slot = handler.slots.get(45);
        ((SlotAccessor) slot).setX(slot.x - 21);
        handler.slots.removeIf(slot1 -> slot1 instanceof EquipmentSlot);
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
        if (slot instanceof EquipmentSlot && !slot.hasStack()) {
            context.drawGuiTexture(EMPTY_SLOT, slot.x, slot.y, 16, 16);
        }
    }

    private static class EquipmentSlot extends Slot {

        public EquipmentSlot(Inventory inventory, int index, int x, int y) {
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
