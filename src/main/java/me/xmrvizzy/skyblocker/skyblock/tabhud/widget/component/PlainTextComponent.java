package me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class PlainTextComponent extends Component {

    private Text text;

    public PlainTextComponent(Text text) {
        this.text = text;

        this.width = PAD_S + txtRend.getWidth(text) + PAD_S;
        this.height = txtRend.fontHeight;
    }

    @Override
    public void render(MatrixStack ms, int x, int y) {
        txtRend.draw(ms, text, x + PAD_S, y, 0xffffffff);
    }

}
