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
            saveStorage();
            // update save dir based on uuid and sb profile
            String uuid = MinecraftClient.getInstance().getSession().getUuidOrNull().toString().replaceAll("-", "");
            String profile = Utils.getProfile();
            if (!profile.isEmpty()) {
                save_dir = FabricLoader.getInstance().getConfigDir().resolve("skyblocker/backpack-preview/" + uuid + "/" + profile);
                //noinspection ResultOfMethodCallIgnored
                save_dir.toFile().mkdirs();
                if (loaded.equals(uuid + "/" + profile)) {
                    // mark currently opened storage as dirty
                    if (MinecraftClient.getInstance().currentScreen != null) {
                        String title = MinecraftClient.getInstance().currentScreen.getTitle().getString();
                        int index = getStorageIndexFromTitle(title);
                        if (index != -1) storages[index].markDirty();
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
            storages[index] = null;
            File file = save_dir.resolve(index + ".nbt").toFile();
            if (file.isFile()) {
                try {
                    NbtCompound root = NbtIo.read(file);
                    storages[index] = new Storage(new DummyInventory(Objects.requireNonNull(root)), root.getString("name"));
                } catch (Exception e) {
                    LOGGER.error("Failed to load backpack preview file: " + file.getName(), e);
                }
            }
        }
    }

    private static void saveStorage() {
        assert (save_dir != null);
        for (int index = 0; index < STORAGE_SIZE; ++index) {
            if (storages[index] != null && storages[index].dirty) {
                try {
                    NbtCompound root = new NbtCompound();
                    NbtList list = new NbtList();
                    for (int i = 9; i < storages[index].inventory.size(); ++i) {
                        ItemStack stack = storages[index].inventory.getStack(i);
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
                    root.put("size", NbtInt.of(storages[index].inventory.size() - 9));
                    root.putString("name", storages[index].name);
                    NbtIo.write(root, save_dir.resolve(index + ".nbt").toFile());
                    storages[index].markClean();
                } catch (Exception e) {
                    LOGGER.error("Failed to save backpack preview file: " + index + ".nbt", e);
                }
            }
        }
    }

    public static void updateStorage(HandledScreen<?> screen) {
        String title = screen.getTitle().getString();
        int index = getStorageIndexFromTitle(title);
        if (index != -1) {
            storages[index] = new Storage(screen.getScreenHandler().slots.get(0).inventory, title, true);
        }
    }

    public static boolean renderPreview(DrawContext context, int index, int mouseX, int mouseY) {
        if (index >= 9 && index < 18) index -= 9;
        else if (index >= 27 && index < 45) index -= 18;
        else return false;

        if (storages[index] == null) return false;
        int rows = (storages[index].inventory.size() - 9) / 9;

        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen == null) return false;
        int x = mouseX + 184 >= screen.width ? mouseX - 188 : mouseX + 8;
        int y = Math.max(0, mouseY - 16);

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(0f, 0f, 400f);

        RenderSystem.enableDepthTest();
        context.drawTexture(TEXTURE, x, y, 0, 0, 176, 7);
        context.drawTexture(TEXTURE, x, y + 7, 0, 17, 176, rows * 18);
        context.drawTexture(TEXTURE, x, y + rows * 18 + 7, 0, 215, 176, 7);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        context.drawText(textRenderer, storages[index].name, x + 8, y + 6, 0x404040, false);

        for (int i = 9; i < storages[index].inventory.size(); ++i) {
            ItemStack currentStack = storages[index].inventory.getStack(i);
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

    static class Storage {
        private final Inventory inventory;
        private final String name;
        private boolean dirty;

        public Storage(Inventory inventory, String name) {
            this(inventory, name, false);
        }

        public Storage(Inventory inventory, String name, boolean dirty) {
            this.inventory = inventory;
            this.name = name;
            this.dirty = dirty;
        }

        public void markDirty() {
            dirty = true;
        }

        public void markClean() {
            dirty = false;
        }
    }

    static class DummyInventory implements Inventory {
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
}
