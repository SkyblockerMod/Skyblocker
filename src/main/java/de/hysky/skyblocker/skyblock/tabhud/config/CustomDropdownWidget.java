package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.render.gui.DropdownWidget;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;

class CustomDropdownWidget<T> extends DropdownWidget<T> {
	private static final Identifier TEXTURE = SkyblockerMod.id("menu_outer_space");
	private final T firstEntry;

	CustomDropdownWidget(int x, int y, int width, int maxHeight, List<T> entries, Consumer<T> selectCallback, T selected) {
		super(Minecraft.getInstance(), x, y, width, maxHeight, 12, entries, selectCallback, selected, _ -> {});
		headerHeight = 15;
		firstEntry = entries.getFirst();
	}


	@Override
	protected void extractHeader(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		int y = getY() - 1;
		int y2 = y + headerHeight;
		TopBarWidget.drawButtonBorder(graphics, getX(), y, y2);
		TopBarWidget.drawButtonBorder(graphics, getRight(), y, y2);

		if (isHovered() && mouseY < y2) {
			graphics.fill(getX(), y, getRight() + 1, y2, ARGB.color(100, 0));
		} else {
			graphics.fill(getX(), y, getRight() + 1, y2, ARGB.color(50, 0));
		}
		graphics.text(client.font, ">", getX() + 4, getY() + (headerHeight - client.font.lineHeight) / 2 + 1, CommonColors.LIGHTER_GRAY, true); // +1 on the y coordinate cuz drawScrollableText does so too
		graphics.textRenderer().acceptScrolling(formatter.apply(selected),
				getX() + getWidth() / 2,
				getX() + 4 + 6,
				getRight() - 2,
				getY() + 2,
				getY() + headerHeight - 2
		);
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
		protected CustomDropdownList(Minecraft minecraftClient) {
			super(minecraftClient);
		}

		@Override
		protected void extractListBackground(GuiGraphicsExtractor context) {
			context.enableScissor(this.getX(), this.getY() - 1, this.getRight(), this.getBottom() + 2);
			context.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX(), this.getY() - 3, this.getWidth(), this.getHeight() + 5);
			context.disableScissor();
		}
	}

	class CustomEntry extends Entry {
		protected CustomEntry(T element) {
			super(element);
		}

		@Override
		public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			if (entry != firstEntry) {
				graphics.horizontalLine(this.getX(), this.getX() + this.getWidth(), this.getY(), ARGB.color(15, 0));
			}
			graphics.textRenderer().acceptScrollingWithDefaultCenter(
					formatter.apply(entry).copy().withStyle(Style.EMPTY.withUnderlined(hovered)),
					this.getX(),
					this.getX() + this.getWidth(),
					this.getY(),
					this.getY() + 11
			);

		}
	}
}
