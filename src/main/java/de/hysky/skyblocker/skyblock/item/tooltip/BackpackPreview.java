package de.hysky.skyblocker.skyblock.item.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.ItemProtection;
import de.hysky.skyblocker.skyblock.item.ItemRarityBackgrounds;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.nbt.visitor.StringNbtWriter;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.RegistryOps;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BackpackPreview {
    private static final Logger LOGGER = LoggerFactory.getLogger(BackpackPreview.class);
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/container/generic_54.png");
    private static final Pattern ECHEST_PATTERN = Pattern.compile("Ender Chest.*\\((\\d+)/\\d+\\)");
    private static final Pattern BACKPACK_PATTERN = Pattern.compile("Backpack.*\\(Slot #(\\d+)\\)");
    private static final int STORAGE_SIZE = 27;

    private static final Storage[] storages = new Storage[STORAGE_SIZE];

    private BackpackPreview() {}

    /**
     * The profile id of the currently loaded backpack preview.
     */
    private static String loaded;
    private static Path saveDir;

    @Init
    public static void init() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof HandledScreen<?> handledScreen) {
                ScreenEvents.remove(screen).register(screen1 -> updateStorage(handledScreen));
            }
        });
    }

    public static void tick() {
        if (Utils.isOnSkyblock()) {
            // save all dirty storages
            saveStorages();
            // update save dir based on sb profile id
            String id = MinecraftClient.getInstance().getSession().getUuidOrNull().toString().replaceAll("-", "") + "/" + Utils.getProfileId();
            if (!id.equals(loaded)) {
                saveDir = SkyblockerMod.CONFIG_DIR.resolve("backpack-preview/" + id);

                try {
                    Files.createDirectories(saveDir);
                } catch (Exception e) {
                    LOGGER.error("[Skyblocker] Failed to create the backpack preview save directory! Path: {}", saveDir, e);
                }

                // load storage again because profile id changed
                loaded = id;
                loadStorages();
            }
        }
    }

    private static void loadStorages() {
        for (int index = 0; index < STORAGE_SIZE; ++index) {
            storages[index] = null;
            Path storageFile = saveDir.resolve(index + ".nbt");
            if (Files.isRegularFile(storageFile)) {
                try (BufferedReader reader = Files.newBufferedReader(storageFile)) {
                    storages[index] = Storage.CODEC.parse(getOps(), StringNbtReader.parse(reader.lines().collect(Collectors.joining()))).getOrThrow();
                } catch (Exception e) {
                    LOGGER.error("Failed to load backpack preview file: {}", storageFile.getFileName().toString(), e);
                }
            }
        }
    }

    private static RegistryOps<NbtElement> getOps() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.getNetworkHandler() != null && client.getNetworkHandler().getRegistryManager() != null ? client.getNetworkHandler().getRegistryManager().getOps(NbtOps.INSTANCE) : BuiltinRegistries.createWrapperLookup().getOps(NbtOps.INSTANCE);
    }

    private static void saveStorages() {
        for (int index = 0; index < STORAGE_SIZE; ++index) {
            if (storages[index] != null && storages[index].dirty) {
                saveStorage(index);
            }
        }
    }

    private static void saveStorage(int index) {
        Path storageFile = saveDir.resolve(index + ".nbt");
        try (BufferedWriter writer = Files.newBufferedWriter(storageFile)) {
            writer.write(new StringNbtWriter().apply(Storage.CODEC.encodeStart(getOps(), storages[index]).getOrThrow()));
            storages[index].markClean();
        } catch (Exception e) {
            LOGGER.error("Failed to save backpack preview file: {}", storageFile.getFileName(), e);
        }
    }

    private static void updateStorage(HandledScreen<?> handledScreen) {
        String title = handledScreen.getTitle().getString();
        int index = getStorageIndexFromTitle(title);
        if (index != -1) {
            storages[index] = new Storage(handledScreen.getScreenHandler().slots.getFirst().inventory, title, true);
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
        context.drawText(textRenderer, storages[index].name(), x + 8, y + 6, 0x404040, false);

        matrices.translate(0f, 0f, 200f);
        for (int i = 9; i < storages[index].size(); ++i) {
            ItemStack currentStack = storages[index].getStack(i);
            int itemX = x + (i - 9) % 9 * 18 + 8;
            int itemY = y + (i - 9) / 9 * 18 + 18;

            if (SkyblockerConfigManager.get().general.itemInfoDisplay.itemRarityBackgrounds) {
                ItemRarityBackgrounds.tryDraw(currentStack, context, itemX, itemY);
            }

            if (ItemProtection.isItemProtected(currentStack)) {
                RenderSystem.enableBlend();
                context.drawTexture(ItemProtection.ITEM_PROTECTION_TEX, itemX, itemY, 0, 0, 16, 16, 16, 16);
                RenderSystem.disableBlend();
            }

            context.drawItem(currentStack, itemX, itemY);
            context.drawItemInSlot(textRenderer, currentStack, itemX, itemY);
            SlotTextManager.renderSlotText(context, textRenderer, null, currentStack, i, itemX, itemY);
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
        private static final Codec<Storage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("name").forGetter(Storage::name),
                ItemUtils.EMPTY_ALLOWING_ITEMSTACK_CODEC.listOf().fieldOf("items").forGetter(Storage::getItemList)
        ).apply(instance, Storage::create));
        private final Inventory inventory;
        private final String name;
        private boolean dirty;

        private Storage(Inventory inventory, String name, boolean dirty) {
            this.inventory = inventory;
            this.name = name;
            this.dirty = dirty;
        }

        private String name() {
            return name;
        }

        private int size() {
            return inventory.size();
        }

        private ItemStack getStack(int index) {
            return inventory.getStack(index);
        }

        private void markClean() {
            dirty = false;
        }

        private static Storage create(String name, List<ItemStack> items) {
            SimpleInventory inventory = new SimpleInventory(items.toArray(ItemStack[]::new));
            return new Storage(inventory, name, false);
        }

        private List<ItemStack> getItemList() {
            List<ItemStack> items = new ArrayList<>();

            for (int i = 0; i < size(); ++i) {
                items.add(getStack(i));
            }

            return items;
        }
    }
}
