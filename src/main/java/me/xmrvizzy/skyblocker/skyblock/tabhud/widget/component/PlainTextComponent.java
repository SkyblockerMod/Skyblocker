package me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

// widget component that consists of a line of text

public class PlainTextComponent extends Component {

    private Text text;

    public PlainTextComponent(Text text) {
        this.text = text;

        this.width = PAD_S + txtRend.getWidth(text); // looks off without padding
        this.height = txtRend.fontHeight;
    }

    @Override
    public void render(MatrixStack ms, int x, int y) {
        txtRend.draw(ms, text, x, y, 0xffffffff);
    }

}
