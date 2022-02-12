package me.xmrvizzy.skyblocker.skyblock;

import com.mojang.blaze3d.systems.RenderSystem;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.Identifier;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BackpackPreview extends DrawableHelper {
    private static final Identifier TEXTURE = new Identifier(SkyblockerMod.NAMESPACE, "textures/gui/inventory_background.png");
    private static final BackpackPreview instance = new BackpackPreview();
    private static final Pattern ECHEST_PATTERN = Pattern.compile("Ender Chest.*\\((\\d+)/\\d+\\)");
    private static final Pattern BACKPACK_PATTERN = Pattern.compile("Backpack.*\\((\\d+)/\\d+\\)");

    private static final Inventory[] storage = new Inventory[27];
    private static final boolean[] dirty = new boolean[27];
    private static boolean loaded = false;
    private static int counter = 0;

    private static File getSaveDir() {
        String uuid = MinecraftClient.getInstance().player.getUuidAsString();
        File dir = new File("./config/skyblocker/backpack-preview/" + uuid + "/");
        dir.mkdirs();
        return dir;
    }

    public static void loadStorage(HandledScreen screen) {
        if (!loaded) {
            String title = screen.getTitle().getString();
            if (title.equals("Storage")) {
                for (int index = 0; index < storage.length; ++index) {
                    File file = new File(getSaveDir().getPath(), index + ".nbt");
                    if (file.isFile()) {
                        try {
                            NbtCompound root = NbtIo.read(file);
                            storage[index] = new DummyInventory(root);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                loaded = true;
            }
        }
    }

    private static void saveStorage(int index) {
        File file = new File(getSaveDir().getPath(), index + ".nbt");
        if (storage[index] != null) {
            try {
                NbtCompound root = new NbtCompound();
                NbtList list = new NbtList();
                for (int i = 9; i < storage[index].size(); ++i) {
                    ItemStack stack = storage[index].getStack(i);
                    NbtCompound item = new NbtCompound();
                    if (stack.isEmpty()) {
                        item.put("id", NbtString.of("minecraft:air"));
                        item.put("Count", NbtInt.of(1));
                    } else {
                        item.put("id", NbtString.of(stack.getItem().toString()));
                        item.put("Count", NbtInt.of(stack.getCount()));
                        item.put("tag", stack.getNbt());
                    }
                    list.add(item);
                }
                root.put("list", list);
                root.put("size", NbtInt.of(storage[index].size() - 9));
                NbtIo.write(root, file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateStorage(HandledScreen screen) {
        String title = screen.getTitle().getString();
        int index = getStorageIndexFromTitle(title);
        if (index != -1) {
            storage[index] = ((HandledScreen<?>)screen).getScreenHandler().slots.get(0).inventory;
            dirty[index] = true;
        }
    }

    public static void tick() {
        if (++counter == 100) {
            counter = 0;
            for (int i = 0; i < dirty.length; ++i)
                if (dirty[i]) {
                    saveStorage(i);
                    String title = MinecraftClient.getInstance().currentScreen.getTitle().getString();
                    int index = getStorageIndexFromTitle(title);
                    dirty[i] = i == index;
                }
        }
    }

    public static boolean renderPreview(MatrixStack matrices, int index, int mouseX, int mouseY) {
        if (index >= 9 && index < 18) index -= 9;
        else if (index >= 27 && index < 45) index -= 18;
        else return false;

        if (storage[index] == null) return false;
        int rows = (storage[index].size() - 9) / 9;

        Screen screen = MinecraftClient.getInstance().currentScreen;
        int x = mouseX + 184 >= screen.width ? mouseX - 188 : mouseX + 8;
        int y = Math.max(0, mouseY - 16);

        RenderSystem.disableDepthTest();
        RenderSystem.setShaderTexture(0, TEXTURE);
        instance.drawTexture(matrices, x, y, 0, 0, 176, 7);
        for (int i = 0; i < rows; ++i)
            instance.drawTexture(matrices, x, y + i * 18 + 7, 0, 7, 176, 18);
        instance.drawTexture(matrices, x, y + rows * 18 + 7, 0, 25, 176, 7);
        RenderSystem.enableDepthTest();

        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        for (int i = 9; i < storage[index].size(); ++i) {
            int itemX = x + (i - 9) % 9 * 18 + 8;
            int itemY = y + (i - 9) / 9 * 18 + 8;
            itemRenderer.zOffset = 200.0F;
            itemRenderer.renderInGui(storage[index].getStack(i), itemX, itemY);
            itemRenderer.renderGuiItemOverlay(textRenderer, storage[index].getStack(i), itemX, itemY);
            itemRenderer.zOffset = 0.0F;
        }

        return true;
    }

    private static int getStorageIndexFromTitle(String title) {
        Matcher echest = ECHEST_PATTERN.matcher(title);
        if (echest.find()) return Integer.parseInt(echest.group(1)) - 1;
        Matcher backpack = BACKPACK_PATTERN.matcher(title);
        if (backpack.find()) return Integer.parseInt(backpack.group(1)) + 8;
        return -1;
    }
}

class DummyInventory implements Inventory {
    private List<ItemStack> stacks;

    public DummyInventory(NbtCompound root) {
        stacks = new ArrayList<>(root.getInt("size") + 9);
        for (int i = 0; i < 9; ++i) stacks.add(ItemStack.EMPTY);
        root.getList("list", NbtCompound.COMPOUND_TYPE).forEach(item ->
                stacks.add(ItemStack.fromNbt((NbtCompound)item))
        );
    }

    @Override
    public int size() { return stacks.size(); }

    @Override
    public boolean isEmpty() { return false; }

    @Override
    public ItemStack getStack(int slot) { return stacks.get(slot); }

    @Override
    public ItemStack removeStack(int slot, int amount) { return null; }

    @Override
    public ItemStack removeStack(int slot) { return null; }

    @Override
    public void setStack(int slot, ItemStack stack) { stacks.set(slot, stack); }

    @Override
    public void markDirty() {}

    @Override
    public boolean canPlayerUse(PlayerEntity player) { return false; }

    @Override
    public void clear() {}
}