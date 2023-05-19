package me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// widget component that consists of an icon and a line of text

public class IcoTextComponent extends Component {

    private ItemStack ico;
    private Text text;

    public IcoTextComponent(ItemStack ico, Text txt) {
        this.ico = (ico == null) ? Ico.BARRIER : ico;
        this.text = txt;

        if (txt == null) {
            this.ico = Ico.BARRIER;
            this.text = Text.literal("No data").formatted(Formatting.GRAY);
        }

        this.width = ICO_DIM + PAD_L + txtRend.getWidth(this.text);
        this.height = ICO_DIM;
    }

    public IcoTextComponent() {
        this(null, null);
    }

    @Override
    public void render(MatrixStack ms, int x, int y) {
        itmRend.renderGuiItemIcon(ms, ico, x, y);
        txtRend.draw(ms, text, x + ICO_DIM + PAD_L, y + 5, 0xffffffff);
    }

}
