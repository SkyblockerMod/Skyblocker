package de.hysky.skyblocker.skyblock.auction.widgets;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.auction.SlotClickHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class AuctionTypeWidget extends SliderWidget<AuctionTypeWidget.Option> {

	/**
	 * @param x         x position
	 * @param y         y position
	 * @param slotClick IDK figure it out
	 */
	public AuctionTypeWidget(int x, int y, SlotClickHandler slotClick) {
		super(x, y, 17, 17, Component.literal("Auction Type Widget"), slotClick, Option.ALL);
	}

	public enum Option implements SliderWidget.OptionInfo {
		ALL("all.png"),
		BIN("bin.png"),
		AUC("auctions.png");

		private final ResourceLocation texture;
		private static final String prefix = "textures/gui/auctions_gui/auction_type_widget/";
		private static final ResourceLocation HOVER_TEXTURE = SkyblockerMod.id(prefix + "hover.png");
		private static final ResourceLocation BACK_TEXTURE = SkyblockerMod.id(prefix + "back.png");

		Option(String textureName) {
			texture = SkyblockerMod.id(prefix + textureName);
		}

		private static final AuctionTypeWidget.Option[] values = values();

		public static AuctionTypeWidget.Option get(int ordinal) {
			return values[Math.clamp(ordinal, 0, values.length - 1)];
		}

		@Override
		public boolean isVertical() {
			return true;
		}

		@Override
		public int getOffset() {
			return 4 * ordinal();
		}

		@Override
		public int[] getOptionSize() {
			return new int[]{17, 9};
		}

		@Override
		public ResourceLocation getOptionTexture() {
			return texture;
		}

		@Override
		public ResourceLocation getBackTexture() {
			return BACK_TEXTURE;
		}

		@Override
		public ResourceLocation getHoverTexture() {
			return HOVER_TEXTURE;
		}
	}
}
