package de.hysky.skyblocker.skyblock.item;

import com.mojang.blaze3d.systems.RenderSystem;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BackpackPreview {
    private static final Logger LOGGER = LoggerFactory.getLogger(BackpackPreview.class);
    private static final Identifier TEXTURE = new Identifier("textures/gui/container/generic_54.png");
    private static final Pattern ECHEST_PATTERN = Pattern.compile("Ender Chest.*\\((\\d+)/\\d+\\)");
    private static final Pattern BACKPACK_PATTERN = Pattern.compile("Backpack.*\\(Slot #(\\d+)\\)");
    private static final int STORAGE_SIZE = 27;

    private static final Inventory[] storage = new Inventory[STORAGE_SIZE];
    private static final boolean[] dirty = new boolean[STORAGE_SIZE];

    private static String loaded = ""; // uuid + sb profile currently loaded
    private static Path save_dir = null;

    public static void init() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof HandledScreen<?> handledScreen) {
                updateStorage(handledScreen);
            }
        });
    }

    public static void tick() {
        Utils.update(); // force update isOnSkyblock to prevent crash on disconnect
        if (Utils.isOnSkyblock()) {
            // save all dirty storages
            saveStorage();
            // update save dir based on uuid and sb profile
            String uuid = MinecraftClient.getInstance().getSession().getUuidOrNull().toString().replaceAll("-", "");
            String profile = Utils.getProfile();
            if (!profile.isEmpty()) {
                save_dir = FabricLoader.getInstance().getConfigDir().resolve("skyblocker/backpack-preview/" + uuid + "/" + profile);
                save_dir.toFile().mkdirs();
                if (loaded.equals(uuid + "/" + profile)) {
                    // mark currently opened storage as dirty
                    if (MinecraftClient.getInstance().currentScreen != null) {
                        String title = MinecraftClient.getInstance().currentScreen.getTitle().getString();
                        int index = getStorageIndexFromTitle(title);
                        if (index != -1) dirty[index] = true;
                    }
                } else {
                    // load storage again because uuid/profile changed
                    loaded = uuid + "/" + profile;
                    loadStorage();
                }
            }
        }
    }

    public static void loadStorage() {
        assert (save_dir != null);
        for (int index = 0; index < STORAGE_SIZE; ++index) {
            storage[index] = null;
            dirty[index] = false;
            File file = save_dir.resolve(index + ".nbt").toFile();
            if (file.isFile()) {
                try {
                    NbtCompound root = NbtIo.read(file);
                    storage[index] = new DummyInventory(Objects.requireNonNull(root));
                } catch (Exception e) {
                    LOGGER.error("Failed to load backpack preview file: " + file.getName(), e);
                }
            }
        }
    }

    private static void saveStorage() {
        assert (save_dir != null);
        for (int index = 0; index < STORAGE_SIZE; ++index) {
            if (dirty[index]) {
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
                        NbtIo.write(root, save_dir.resolve(index + ".nbt").toFile());
                        dirty[index] = false;
                    } catch (Exception e) {
                        LOGGER.error("Failed to save backpack preview file: " + index + ".nbt", e);
                    }
                }
            }
        }
    }

    public static void updateStorage(HandledScreen<?> screen) {
        String title = screen.getTitle().getString();
        int index = getStorageIndexFromTitle(title);
        if (index != -1) {
            storage[index] = screen.getScreenHandler().slots.get(0).inventory;
            dirty[index] = true;
        }
    }

    public static boolean renderPreview(DrawContext context, int index, int mouseX, int mouseY) {
        if (index >= 9 && index < 18) index -= 9;
        else if (index >= 27 && index < 45) index -= 18;
        else return false;

        if (storage[index] == null) return false;
        int rows = (storage[index].size() - 9) / 9;

        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen == null) return false;
        int x = mouseX + 184 >= screen.width ? mouseX - 188 : mouseX + 8;
        int y = Math.max(0, mouseY - 16);

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(0f, 0f, 400f);

        RenderSystem.enableDepthTest();
        context.drawTexture(TEXTURE, x, y, 0, 0, 176, rows * 18 + 17);
        context.drawTexture(TEXTURE, x, y + rows * 18 + 17, 0, 126, 176, 96);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        for (int i = 9; i < storage[index].size(); ++i) {
            ItemStack currentStack = storage[index].getStack(i);
            int itemX = x + (i - 9) % 9 * 18 + 8;
            int itemY = y + (i - 9) / 9 * 18 + 8;

            if (SkyblockerConfigManager.get().general.itemInfoDisplay.itemRarityBackgrounds) {
                ItemRarityBackgrounds.tryDraw(currentStack, context, itemX, itemY);
            }

            matrices.push();
            matrices.translate(0f, 0f, 200f);
            context.drawItem(currentStack, itemX, itemY);
            context.drawItemInSlot(textRenderer, currentStack, itemX, itemY);
            matrices.pop();
        }

        matrices.pop();

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
    private final List<ItemStack> stacks;

    public DummyInventory(NbtCompound root) {
        stacks = new ArrayList<>(root.getInt("size") + 9);
        for (int i = 0; i < 9; ++i) stacks.add(ItemStack.EMPTY);
        root.getList("list", NbtCompound.COMPOUND_TYPE).forEach(item ->
                stacks.add(ItemStack.fromNbt((NbtCompound) item))
        );
    }

    @Override
    public int size() {
        return stacks.size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ItemStack getStack(int slot) {
        return stacks.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return null;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return null;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        stacks.set(slot, stack);
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return false;
    }

    @Override
    public void clear() {
    }
}
