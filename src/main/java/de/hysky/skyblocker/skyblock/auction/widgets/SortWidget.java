package de.hysky.skyblocker.skyblock.auction.widgets;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.auction.SlotClickHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SortWidget extends SliderWidget<SortWidget.Option> {

    /**
     * @param x         x position
     * @param y         y position
     * @param clickSlot the parent AuctionsBrowser
     */
    public SortWidget(int x, int y, SlotClickHandler clickSlot) {
        super(x, y, 36, 9, Text.literal("Sort Widget"), clickSlot, Option.HIGH);
    }

    public enum Option implements SliderWidget.OptionInfo {
        HIGH("high.png"),
        LOW("low.png"),
        SOON("soon.png"),
        RAND("rand.png");

        private final Identifier texture;
        private static final String prefix = "textures/gui/auctions_gui/sort_widget/";
        private static final Identifier HOVER_TEXTURE = new Identifier(SkyblockerMod.NAMESPACE, prefix + "hover.png");
        private static final Identifier BACK_TEXTURE = new Identifier(SkyblockerMod.NAMESPACE, prefix + "back.png");

        Option(String textureName) {
            texture = new Identifier(SkyblockerMod.NAMESPACE, prefix + textureName);
        }

        public Identifier getOptionTexture() {
            return texture;
        }

        private static final Option[] values = values();

        public static Option get(int ordinal) {
            return values[Math.clamp(ordinal, 0, values.length - 1)];
        }

        @Override
        public boolean isVertical() {
            return false;
        }

        @Override
        public int getOffset() {
            return 5 * ordinal();
        }

        @Override
        public int[] getOptionSize() {
            return new int[]{21, 9};
        }

        @Override
        public Identifier getBackTexture() {
            return BACK_TEXTURE;
        }

        @Override
        public Identifier getHoverTexture() {
            return HOVER_TEXTURE;
        }
    }
}
