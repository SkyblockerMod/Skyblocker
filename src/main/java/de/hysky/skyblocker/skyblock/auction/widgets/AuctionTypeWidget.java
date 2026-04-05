package de.hysky.skyblocker.skyblock.auction.widgets;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.auction.SlotClickHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

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
		ALL("all"),
		BIN("bin"),
		AUC("auctions");

		private final Identifier texture;
		private final Identifier hoverTexture;
		private static final String prefix = "auctions_gui/auction_type_widget/";
		private static final Identifier BACK_TEXTURE = SkyblockerMod.id(prefix + "back");

		Option(String textureName) {
			texture = SkyblockerMod.id(prefix + textureName);
			hoverTexture = SkyblockerMod.id(prefix + textureName + "_hover");
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
		public Identifier getOptionTexture() {
			return texture;
		}

		@Override
		public Identifier getBackTexture() {
			return BACK_TEXTURE;
		}

		@Override
		public Identifier getHoverTexture() {
			return hoverTexture;
		}
	}
}
