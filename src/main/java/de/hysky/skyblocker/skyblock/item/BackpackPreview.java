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
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BackpackPreview {
    private static final Logger LOGGER = LoggerFactory.getLogger(BackpackPreview.class);
    private static final Identifier TEXTURE = new Identifier("textures/gui/container/generic_54.png");
    private static final Pattern ECHEST_PATTERN = Pattern.compile("Ender Chest.*\\((\\d+)/\\d+\\)");
    private static final Pattern BACKPACK_PATTERN = Pattern.compile("Backpack.*\\(Slot #(\\d+)\\)");
    private static final int STORAGE_SIZE = 27;

    private static final Storage[] storages = new Storage[STORAGE_SIZE];

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
            saveStorages();
            // update save dir based on uuid and sb profile
            String uuid = MinecraftClient.getInstance().getSession().getUuidOrNull().toString().replaceAll("-", "");
            String profile = Utils.getProfile(); //TODO switch to profile id
            if (!profile.isEmpty()) {
                String loading = uuid + "/" + profile;
                save_dir = FabricLoader.getInstance().getConfigDir().resolve("skyblocker/backpack-preview/" + loading);
                //noinspection ResultOfMethodCallIgnored
                save_dir.toFile().mkdirs();
                if (loaded.equals(loading)) {
                    // mark currently opened storage as dirty
                    if (MinecraftClient.getInstance().currentScreen != null) {
                        String title = MinecraftClient.getInstance().currentScreen.getTitle().getString();
                        int index = getStorageIndexFromTitle(title);
                        if (index != -1) storages[index].markDirty();
                    }
                } else {
                    // load storage again because uuid/profile changed
                    loaded = loading;
                    loadStorages();
                }
            }
        }
    }

    public static void loadStorages() {
        assert (save_dir != null);
        for (int index = 0; index < STORAGE_SIZE; ++index) {
            File file = save_dir.resolve(index + ".nbt").toFile();
            if (file.isFile()) {
                try {
                    storages[index] = Storage.fromNbt(Objects.requireNonNull(NbtIo.read(file)));
                } catch (Exception e) {
                    LOGGER.error("Failed to load backpack preview file: " + file.getName(), e);
                }
            }
        }
    }

    private static void saveStorages() {
        assert (save_dir != null);
        for (int index = 0; index < STORAGE_SIZE; ++index) {
            if (storages[index] != null && storages[index].dirty) {
                saveStorage(index);
            }
        }
    }

    private static void saveStorage(int index) {
        try {
            NbtIo.write(storages[index].toNbt(), save_dir.resolve(index + ".nbt").toFile());
            storages[index].markClean();
        } catch (Exception e) {
            LOGGER.error("Failed to save backpack preview file: " + index + ".nbt", e);
        }
    }

    public static void updateStorage(HandledScreen<?> screen) {
        String title = screen.getTitle().getString();
        int index = getStorageIndexFromTitle(title);
        if (index != -1) {
            storages[index] = new Storage(screen.getScreenHandler().slots.get(0).inventory, title, true);
        }
    }

    public static boolean renderPreview(DrawContext context, Screen screen, int index, int mouseX, int mouseY) {
        if (index >= 9 && index < 18) index -= 9;
        else if (index >= 27 && index < 45) index -= 18;
        else return false;

        if (storages[index] == null) return false;
        int rows = (storages[index].size() - 9) / 9;

        int x = mouseX + 184 >= screen.width ? mouseX - 188 : mouseX + 8;
        int y = Math.max(0, mouseY - 16);

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(0f, 0f, 400f);

        RenderSystem.enableDepthTest();
        context.drawTexture(TEXTURE, x, y, 0, 0, 176, rows * 18 + 17);
        context.drawTexture(TEXTURE, x, y + rows * 18 + 17, 0, 215, 176, 7);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        context.drawText(textRenderer, storages[index].name, x + 8, y + 6, 0x404040, false);

        matrices.translate(0f, 0f, 200f);
        for (int i = 9; i < storages[index].size(); ++i) {
            ItemStack currentStack = storages[index].getStack(i);
            int itemX = x + (i - 9) % 9 * 18 + 8;
            int itemY = y + (i - 9) / 9 * 18 + 18;

            if (SkyblockerConfigManager.get().general.itemInfoDisplay.itemRarityBackgrounds) {
                ItemRarityBackgrounds.tryDraw(currentStack, context, itemX, itemY);
            }

            context.drawItem(currentStack, itemX, itemY);
            context.drawItemInSlot(textRenderer, currentStack, itemX, itemY);
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

    static class Storage {
        private final Inventory inventory;
        private final String name;
        private boolean dirty;

        private Storage(Inventory inventory, String name) {
            this(inventory, name, false);
        }

        private Storage(Inventory inventory, String name, boolean dirty) {
            this.inventory = inventory;
            this.name = name;
            this.dirty = dirty;
        }

        private int size() {
            return inventory.size();
        }

        private ItemStack getStack(int index) {
            return inventory.getStack(index);
        }

        private void markDirty() {
            dirty = true;
        }

        private void markClean() {
            dirty = false;
        }

        @NotNull
        private static Storage fromNbt(NbtCompound root) {
            SimpleInventory inventory = new SimpleInventory(root.getInt("size"));
            inventory.readNbtList(root.getList("list", NbtCompound.COMPOUND_TYPE));
            return new Storage(inventory, root.getString("name"));
        }

        @NotNull
        private NbtCompound toNbt() {
            NbtCompound root = new NbtCompound();
            NbtList list = new NbtList();
            for (int i = 0; i < size(); ++i) {
                list.add(getStack(i).writeNbt(new NbtCompound()));
            }
            root.put("list", list);
            root.put("size", NbtInt.of(size()));
            root.putString("name", name);
            return root;
        }
    }
}
