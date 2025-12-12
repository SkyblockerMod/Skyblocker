package de.hysky.skyblocker.skyblock.profileviewer.inventory;

import de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerPage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class PaginationButton extends AbstractWidget {
	private final ProfileViewerPage screen;
	private final boolean isNextButton;
	private final Identifier TEXTURE;
	private final Identifier HIGHLIGHT;

	public PaginationButton(ProfileViewerPage screen, int x, int y, boolean isNextButton) {
		super(x, y, 12, 17, Component.empty());
		this.screen = screen;
		this.isNextButton = isNextButton;
		if (isNextButton) {
			TEXTURE = Identifier.withDefaultNamespace("textures/gui/sprites/recipe_book/page_forward.png");
			HIGHLIGHT = Identifier.withDefaultNamespace("textures/gui/sprites/recipe_book/page_forward_highlighted.png");
		} else {
			TEXTURE = Identifier.withDefaultNamespace("textures/gui/sprites/recipe_book/page_backward.png");
			HIGHLIGHT = Identifier.withDefaultNamespace("textures/gui/sprites/recipe_book/page_backward_highlighted.png");
		}
	}

	@Override
	protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
		context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX(), this.getY(), 0, 0, 12, 17, 12, 17);
		if (isMouseOver(mouseX, mouseY)) context.blit(RenderPipelines.GUI_TEXTURED, HIGHLIGHT, this.getX(), this.getY(), 0, 0, 12, 17, 12, 17);
	}

	@Override
	public void onClick(MouseButtonEvent click, boolean doubled) {
		if (isNextButton) {
			screen.nextPage();
		} else {
			screen.previousPage();
		}
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {}
}
