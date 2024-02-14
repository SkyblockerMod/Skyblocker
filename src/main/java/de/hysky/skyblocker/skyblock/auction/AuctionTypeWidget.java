package de.hysky.skyblocker.skyblock.auction;

import de.hysky.skyblocker.SkyblockerMod;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class AuctionTypeWidget extends SliderWidget<AuctionTypeWidget.Option> {

    /**
     * @param x             x position
     * @param y             y position
     * @param width         width
     * @param height        height
     * @param message       probably useless, just put the widget name
     * @param parent        the parent AuctionsBrowser
     * @param defaultOption the default option <strong>should be the one at ordinal 0</strong>
     */
    public AuctionTypeWidget(int x, int y, AuctionsBrowserScreen parent) {
        super(x, y, 17, 17, Text.literal("Auction Type Widget"), parent, Option.ALL);
    }

    public enum Option implements OptionInfo {
        ALL("all.png"),
        BIN("bin.png"),
        AUC("auctions.png");

        private final Identifier texture;
        private static final String prefix = "textures/gui/auctions_gui/auction_type_widget/";
        private static final Identifier HOVER_TEXTURE = new Identifier(SkyblockerMod.NAMESPACE, prefix + "hover.png");
        private static final Identifier BACK_TEXTURE = new Identifier(SkyblockerMod.NAMESPACE,prefix + "back.png");

        Option(String textureName) {
            texture = new Identifier(SkyblockerMod.NAMESPACE, prefix + textureName);
        }
        private static final AuctionTypeWidget.Option[] values = values();
        public static AuctionTypeWidget.Option get(int ordinal) {return values[MathHelper.clamp(ordinal, 0, values.length-1)];}

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
            return HOVER_TEXTURE;
        }
    }
}
