package de.hysky.skyblocker.skyblock.profileviewer.inventory;

import de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerPage;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class PaginationButton extends ClickableWidget {
    private final ProfileViewerPage screen;
    private final boolean isNextButton;
    private final Identifier TEXTURE;
    private final Identifier HIGHLIGHT;

    public PaginationButton(ProfileViewerPage screen, int x, int y, boolean isNextButton) {
        super(x, y, 12, 17, Text.empty());
        this.screen = screen;
        this.isNextButton = isNextButton;
        if (isNextButton) {
            TEXTURE = Identifier.of("minecraft", "textures/gui/sprites/recipe_book/page_forward.png");
            HIGHLIGHT = Identifier.of("minecraft", "textures/gui/sprites/recipe_book/page_forward_highlighted.png");
        } else {
            TEXTURE = Identifier.of("minecraft", "textures/gui/sprites/recipe_book/page_backward.png");
            HIGHLIGHT = Identifier.of("minecraft", "textures/gui/sprites/recipe_book/page_backward_highlighted.png");
        }
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX(), this.getY(), 0, 0, 12, 17, 12, 17);
        if (isMouseOver(mouseX, mouseY)) context.drawTexture(RenderPipelines.GUI_TEXTURED, HIGHLIGHT, this.getX(), this.getY(), 0, 0, 12, 17, 12, 17);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (isNextButton) {
            screen.nextPage();
        } else {
            screen.previousPage();
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}
