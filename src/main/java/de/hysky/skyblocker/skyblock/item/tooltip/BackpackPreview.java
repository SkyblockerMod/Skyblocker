package de.hysky.skyblocker.skyblock.item.tooltip;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.item.ItemProtection;
import de.hysky.skyblocker.skyblock.item.background.ItemBackgroundManager;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BackpackPreview {
	private static final Logger LOGGER = LoggerFactory.getLogger(BackpackPreview.class);
	private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
	private static final Pattern ECHEST_PATTERN = Pattern.compile("Ender Chest.*\\((\\d+)/\\d+\\)");
	private static final Pattern BACKPACK_PATTERN = Pattern.compile("Backpack.*\\(Slot #(\\d+)\\)");
	private static final int STORAGE_SIZE = 27;
	private static final Storage[] storages = new Storage[STORAGE_SIZE];

	/**
	 * The profile id of the currently loaded backpack preview.
	 */
	private static String loaded;
	private static volatile Path saveDir;

	private BackpackPreview() {}

	@Init
	public static void init() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof AbstractContainerScreen<?> handledScreen) {
				ScreenEvents.remove(screen).register(screen1 -> updateStorage(handledScreen));
			}
		});
	}

	public static void tick() {
		if (Utils.isOnSkyblock()) {
			// save all dirty storages
			saveStorages();
			// update save dir based on sb profile id
			String id = Minecraft.getInstance().getUser().getProfileId().toString().replaceAll("-", "") + "/" + Utils.getProfileId();
			if (!id.equals(loaded)) {
				saveDir = SkyblockerMod.CONFIG_DIR.resolve("backpack-preview/" + id);

				CompletableFuture.runAsync(() -> {
					try {
						Files.createDirectories(saveDir);
					} catch (Exception e) {
						LOGGER.error("[Skyblocker] Failed to create the backpack preview save directory! Path: {}", saveDir, e);
					}
				}, Executors.newVirtualThreadPerTaskExecutor());

				// load storage again because profile id changed
				loaded = id;
				loadStorages();
			}
		}
	}

	private static void loadStorages() {
		for (int index = 0; index < STORAGE_SIZE; ++index) {
			storages[index] = null;
			//Copy variable since lambdas do not like when you use iteration variables (JDK-8300691)
			int index2 = index;

			CompletableFuture.supplyAsync(() -> {
				Path storageFile = saveDir.resolve(index2 + ".nbt");

				if (Files.isRegularFile(storageFile)) {
					try {
						return Storage.CODEC.parse(getOps(), NbtIo.read(storageFile)).getOrThrow();
					} catch (Exception e) {
						LOGGER.error("[Skyblocker] Failed to load backpack preview file: {}", storageFile.getFileName().toString(), e);
					}
				}

				return null;
			}, Executors.newVirtualThreadPerTaskExecutor()).thenAcceptAsync(storage -> storages[index2] = storage, Minecraft.getInstance());
		}
	}

	private static RegistryOps<Tag> getOps() {
		return Utils.getRegistryWrapperLookup().createSerializationContext(NbtOps.INSTANCE);
	}

	private static void saveStorages() {
		for (int index = 0; index < STORAGE_SIZE; ++index) {
			if (storages[index] != null && storages[index].dirty) {
				saveStorage(index);
			}
		}
	}

	private static void saveStorage(int index) {
		//Store desired storage in a variable to ensure that the instance cannot change during async execution
		Storage storage = storages[index];

		CompletableFuture.runAsync(() -> {
			Path storageFile = saveDir.resolve(index + ".nbt");
			try {
				NbtIo.write((CompoundTag) Storage.CODEC.encodeStart(getOps(), storage).getOrThrow(), storageFile);
			} catch (Exception e) {
				LOGGER.error("[Skyblocker] Failed to save backpack preview file: {}", storageFile.getFileName(), e);
			}
		}, Executors.newVirtualThreadPerTaskExecutor()).thenRunAsync(() -> storage.markClean(), Minecraft.getInstance());
	}

	private static void updateStorage(AbstractContainerScreen<?> handledScreen) {
		String title = handledScreen.getTitle().getString();
		int index = getStorageIndexFromTitle(title);
		if (index != -1) {
			storages[index] = new Storage(handledScreen.getMenu().slots.getFirst().container, title, true);
		}
	}

	public static boolean renderPreview(GuiGraphics context, Screen screen, int index, int mouseX, int mouseY) {
		if (index >= 9 && index < 18) index -= 9;
		else if (index >= 27 && index < 45) index -= 18;
		else return false;

		if (storages[index] == null) return false;
		int rows = (storages[index].size() - 9) / 9;

		int x = mouseX + 184 >= screen.width ? mouseX - 188 : mouseX + 8;
		int y = Math.max(0, mouseY - 16);

		context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, 176, rows * 18 + 17, 256, 256);
		context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y + rows * 18 + 17, 0, 215, 176, 7, 256, 256);

		Font textRenderer = Minecraft.getInstance().font;
		context.drawString(textRenderer, storages[index].name(), x + 8, y + 6, 0xFF404040, false);

		for (int i = 9; i < storages[index].size(); ++i) {
			ItemStack currentStack = storages[index].getStack(i);
			int itemX = x + (i - 9) % 9 * 18 + 8;
			int itemY = y + (i - 9) / 9 * 18 + 18;

			ItemBackgroundManager.drawBackgrounds(currentStack, context, itemX, itemY);

			if (ItemProtection.isItemProtected(currentStack)) {
				context.blit(RenderPipelines.GUI_TEXTURED, ItemProtection.ITEM_PROTECTION_TEX, itemX, itemY, 0, 0, 16, 16, 16, 16);
			}

			context.renderItem(currentStack, itemX, itemY);
			context.renderItemDecorations(textRenderer, currentStack, itemX, itemY);
			SlotTextManager.renderSlotText(context, textRenderer, null, currentStack, i, itemX, itemY);
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

	private static class Storage {
		private static final Codec<Storage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("name").forGetter(Storage::name),
				ItemUtils.EMPTY_ALLOWING_ITEMSTACK_CODEC.listOf().fieldOf("items").forGetter(Storage::getItemList)
				).apply(instance, Storage::create));
		private final Container inventory;
		private final String name;
		private boolean dirty;

		private Storage(Container inventory, String name, boolean dirty) {
			this.inventory = inventory;
			this.name = name;
			this.dirty = dirty;
		}

		private String name() {
			return name;
		}

		private int size() {
			return inventory.getContainerSize();
		}

		private ItemStack getStack(int index) {
			return inventory.getItem(index);
		}

		private void markClean() {
			dirty = false;
		}

		private static Storage create(String name, List<ItemStack> items) {
			SimpleContainer inventory = new SimpleContainer(items.toArray(ItemStack[]::new));
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
