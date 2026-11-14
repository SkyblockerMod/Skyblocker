package de.hysky.skyblocker.skyblock.chat;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jspecify.annotations.Nullable;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.ChatConfig;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.Formatters;

public final class SackMessageToToast {

	@Init
	public static void init() {
		ClientReceiveMessageEvents.ALLOW_GAME.register(SackMessageToToast::onChatMessage);
	}

	private static boolean onChatMessage(Component message, boolean overlay) {
		ChatConfig.SackToast.EnableState enableState = SkyblockerConfigManager.get().chat.sackToast.enabled;
		if (enableState == ChatConfig.SackToast.EnableState.OFF) return true;
		if (!message.getString().startsWith("[Sacks] ")) return true;

		Object2IntMap<String> addedItems = new Object2IntArrayMap<>();
		Object2IntMap<String> removedItems = new Object2IntArrayMap<>();

		message.visit(((style, contents) -> {
			int indexOfPlus = contents.indexOf('+');
			int indexOfMinus = contents.indexOf('-');
			if (indexOfMinus < 0 && indexOfPlus < 0) return Optional.empty();
			if (!(style.getHoverEvent() instanceof HoverEvent.ShowText(Component value))) return Optional.empty();
			if (!SackMessagePrice.isSackHover(value)) return Optional.empty();

			Object2IntArrayMap<String> map = SackMessagePrice.parseItems(value.getSiblings());
			if (indexOfPlus >= 0) {
				addedItems.putAll(map);
				return Optional.empty();
			} else {
				if (SkyblockerConfigManager.get().chat.sackToast.mergeAddedRemoved) {
					map.forEach((s, v) -> addedItems.mergeInt(s, v, Integer::sum));
				} else {
					removedItems.putAll(map);
				}
				return Optional.of(""); // short circuit, removed items is last
			}
		}), Style.EMPTY);

		if (addedItems.isEmpty() && removedItems.isEmpty()) return true;


		List<ItemWithCount> items = Stream.concat(addedItems.object2IntEntrySet().stream(), removedItems.object2IntEntrySet().stream())
				.map(entry -> new ItemWithCount(
						Optional.ofNullable(SackMessagePrice.getNeuId(entry.getKey())).map(ItemRepository::getItemStack).map(FlexibleItemStack::getStack).orElse(null),
						Component.literal(entry.getKey()),
						Component.literal(Formatters.DIFF_NUMBERS.format(entry.getIntValue())).withColor(entry.getIntValue() < 0 ? TextColor.RED : TextColor.GREEN)
				)).toList();
		if (items.isEmpty()) return true;
		Minecraft.getInstance().gui.toastManager().addToast(new SackToast(items));
		return enableState == ChatConfig.SackToast.EnableState.KEEP_MESSAGE;
	}

	private static class SackToast implements Toast {
		private static final int LEFT_PADDING = 8;
		private static final int SCISSOR_PADDING = 5;
		private static final int ITEM_AREA_SIZE = 20;
		private static final int TEXT_SPACING = 4;
		private static final int ROW_HEIGHT_WITH_SPACING = 20;
		private static final int MINIMUM_WIDTH = 150;
		private static final long MINIMUM_DURATION = 3000;
		private static final Identifier TEXTURE = SkyblockerMod.id("notification");

		private final List<ItemWithCount> items;
		private Visibility visibility = Visibility.SHOW;
		private int width = -1;
		/**
		 * Scroll offset for each millisecond since the toast is fully visible
		 */
		private final float movementPerMillisecond;
		private final long millisPerItem;

		private SackToast(List<ItemWithCount> items) {
			this.items = items;
			millisPerItem = Math.max((long) (SkyblockerConfigManager.get().chat.sackToast.itemDisplayDuration * 1000), MINIMUM_DURATION / items.size());
			// -12 to still have it a bit visible when the toast starts disappearing
			movementPerMillisecond = ((ROW_HEIGHT_WITH_SPACING * items.size() - 12) / (items.size() * (float) millisPerItem));
		}

		@Override
		public Visibility getWantedVisibility() {
			return visibility;
		}

		@Override
		public void update(ToastManager manager, long fullyVisibleForMs) {
			if (width == -1) {
				Font font = Minecraft.getInstance().font;
				width = items.stream().mapToInt(item -> {
					final int nameWidth = font.width(item.stack() != null ? item.stack().getHoverName() : item.name());
					final int countWidth = font.width(item.count());
					return nameWidth + TEXT_SPACING + countWidth;
				}).max().orElse(0) + LEFT_PADDING * 2 + ITEM_AREA_SIZE;
				width = Math.max(width, MINIMUM_WIDTH);
			}
			if (fullyVisibleForMs > items.size() * millisPerItem) {
				visibility = Visibility.HIDE;
			}
		}

		private void extractItem(GuiGraphicsExtractor graphics, Font font, int y, ItemWithCount item) {
			if (item.stack() != null) {
				graphics.item(item.stack(), LEFT_PADDING, y);
			}
			Component name = item.stack() != null ? item.stack().getHoverName() : item.name();
			final int nameWidth = font.width(name);
			graphics.text(font, name, LEFT_PADDING + ITEM_AREA_SIZE, y + 4, -1);
			graphics.text(font, item.count(), LEFT_PADDING + ITEM_AREA_SIZE + nameWidth + TEXT_SPACING, y + 4, -1);
		}

		@Override
		public void extractRenderState(GuiGraphicsExtractor graphics, Font font, long fullyVisibleForMs) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, width(), height());
			if (items.size() == 1) {
				extractItem(graphics, font, 8, items.getFirst());
			} else if (items.size() == 2) {
				extractItem(graphics, font, 6, items.getFirst());
				extractItem(graphics, font, 6 + ROW_HEIGHT_WITH_SPACING, items.getLast());
			} else {
				graphics.enableScissor(SCISSOR_PADDING, SCISSOR_PADDING, width() - SCISSOR_PADDING, height() - SCISSOR_PADDING);
				int startY = height() - SCISSOR_PADDING * 2 - (int) (fullyVisibleForMs * movementPerMillisecond) - ROW_HEIGHT_WITH_SPACING / 2;
				for (ItemWithCount item : items) {
					if (startY + ROW_HEIGHT_WITH_SPACING >= SCISSOR_PADDING && startY <= height() - SCISSOR_PADDING) {
						extractItem(graphics, font, startY, item);
					}
					startY += ROW_HEIGHT_WITH_SPACING;
				}
				graphics.disableScissor();
			}
		}

		@Override
		public int height() {
			return switch (items.size()) {
				case 1 -> 32;
				case 2 -> 48;
				default -> 40;
			};
		}

		@Override
		public int width() {
			return width;
		}
	}
	private record ItemWithCount(@Nullable ItemStack stack, Component name, Component count) {}
}
