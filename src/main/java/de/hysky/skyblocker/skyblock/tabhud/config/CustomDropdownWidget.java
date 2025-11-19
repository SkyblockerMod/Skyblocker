package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.render.gui.DropdownWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Style;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import java.util.List;
import java.util.function.Consumer;

class CustomDropdownWidget<T> extends DropdownWidget<T> {
	private static final Identifier TEXTURE = SkyblockerMod.id("menu_outer_space");
	private final T firstEntry;

	CustomDropdownWidget(int x, int y, int width, int maxHeight, List<T> entries, Consumer<T> selectCallback, T selected) {
		super(MinecraftClient.getInstance(), x, y, width, maxHeight, 12, entries, selectCallback, selected, opened -> {});
		headerHeight = 15;
		firstEntry = entries.getFirst();
	}


	@Override
	protected void renderHeader(DrawContext context, int mouseX, int mouseY, float partialTicks) {
		int y = getY() - 1;
		int y2 = y + headerHeight;
		TopBarWidget.drawButtonBorder(context, getX(), y, y2);
		TopBarWidget.drawButtonBorder(context, getRight(), y, y2);

		if (isHovered() && mouseY < y2) {
			context.fill(getX(), y, getRight() + 1, y2, ColorHelper.withAlpha(100, 0));
		} else {
			context.fill(getX(), y, getRight() + 1, y2, ColorHelper.withAlpha(50, 0));
		}
		context.drawText(client.textRenderer, ">", getX() + 4, getY() + (headerHeight - client.textRenderer.fontHeight) / 2 + 1, Colors.ALTERNATE_WHITE, true); // +1 on the y coordinate cuz drawScrollableText does so too
		drawScrollableText(context, client.textRenderer, formatter.apply(selected),
				getX() + getWidth() / 2,
				getX() + 4 + 6,
				getY() + 2,
				getRight() - 2,
				getY() + headerHeight - 2,
				-1);
	}

	@Override
	protected DropdownWidget<T>.DropdownList createDropdown() {
		return new CustomDropdownList(client);
	}

	@Override
	protected DropdownWidget<T>.Entry createEntry(T element) {
		return new CustomEntry(element);
	}

	class CustomDropdownList extends DropdownList {
		protected CustomDropdownList(MinecraftClient minecraftClient) {
			super(minecraftClient);
		}

		@Override
		protected void drawMenuListBackground(DrawContext context) {
			context.enableScissor(this.getX(), this.getY() - 1, this.getRight(), this.getBottom() + 2);
			context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX(), this.getY() - 3, this.getWidth(), this.getHeight() + 5);
			context.disableScissor();
		}
	}

	class CustomEntry extends Entry {
		protected CustomEntry(T element) {
			super(element);
		}

		@Override
		public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			if (entry != firstEntry) {
				context.drawHorizontalLine(this.getX(), this.getX() + this.getWidth(), this.getY(), ColorHelper.withAlpha(15, 0));
			}
			drawScrollableText(context, client.textRenderer, formatter.apply(entry).copy().fillStyle(Style.EMPTY.withUnderline(hovered)), this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + 11, -1);

		}
	}
}
