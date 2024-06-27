package de.hysky.skyblocker.skyblock.item;

import com.mojang.serialization.Codec;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.mixins.accessors.SlotAccessor;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
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
import java.nio.file.NoSuchFileException;
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

    private static final Identifier SLOT_TEXTURE = Identifier.ofVanilla("container/slot");
    private static final Identifier EMPTY_SLOT = Identifier.of(SkyblockerMod.NAMESPACE, "equipment/empty_icon");

    private final Slot[] equipmentSlots = new Slot[4];

    public static void initEquipment() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client1 -> {
            Path resolve = SkyblockerMod.CONFIG_DIR.resolve("equipment.nbt");
            try (BufferedReader reader = Files.newBufferedReader(resolve)) {
                ItemStack[] array = CODEC.parse(
                                NbtOps.INSTANCE, StringNbtReader.parse(reader.lines().collect(Collectors.joining())))
                        .getOrThrow();
                System.arraycopy(array, 0, equipment, 0, Math.min(array.length, 4));
            } catch (NoSuchFileException ignored) {
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
            equipmentSlots[i] = new EquipmentSlot(inventory, i, 77, 8 + i * 18);
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

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(this.x, this.y, 0.0F);
        for (Slot equipmentSlot : equipmentSlots) {
            drawSlot(context, equipmentSlot);
            if (isPointWithinBounds(equipmentSlot.x, equipmentSlot.y, 16, 16, mouseX, mouseY)) drawSlotHighlight(context, equipmentSlot.x, equipmentSlot.y, 0);
        }
        matrices.pop();
    }

    @Override
    protected void drawMouseoverTooltip(DrawContext context, int x, int y) {
        super.drawMouseoverTooltip(context, x, y);
        if (!handler.getCursorStack().isEmpty()) return;
        for (Slot equipmentSlot : equipmentSlots) {
            if (isPointWithinBounds(equipmentSlot.x, equipmentSlot.y, 16, 16, x, y)) {
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
