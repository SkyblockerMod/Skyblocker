package de.hysky.skyblocker.skyblock.item.custom.screen;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.skyblock.item.custom.CustomHelmetTextures;
import de.hysky.skyblocker.skyblock.profileviewer.utils.ProfileViewerUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HeadSelectionWidget extends ContainerWidget {

	private static final Identifier INNER_SPACE_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "menu_inner_space");


	private final List<HeadButton> allButtons = new ArrayList<>();
	private final List<HeadButton> visibleButtons = new ArrayList<>();
	private final TextFieldWidget searchField;
	private final HeadButton noneButton;
	private int buttonsPerRow = 1;

	private ItemStack currentItem;
	private String selectedTexture;

	public HeadSelectionWidget(int x, int y, int width, int height) {
		super(x, y, width, height, Text.of("HeadSelection"));
		searchField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, x + 3, y + 3, width - 6, 12, Text.translatable("gui.recipebook.search_hint"));
		searchField.setChangedListener(this::filterButtons);

		for (CustomHelmetTextures.NamedTexture tex : CustomHelmetTextures.getTextures()) {
			ItemStack head = ProfileViewerUtils.createSkull(tex.texture());
			HeadButton button = new HeadButton(tex.name(), tex.texture(), head, () -> onClick(tex.texture()));
			allButtons.add(button);
		}
		noneButton = new HeadButton("", null, new ItemStack(Items.BARRIER), () -> onClick(null));

		filterButtons("");
	}

	private void layoutButtons() {
		buttonsPerRow = Math.max(1, (getWidth() - 6) / 20);
		int startY = searchField.getBottom() + 3;
		for (int i = 0; i < visibleButtons.size(); i++) {
			HeadButton button = visibleButtons.get(i);
			button.setPosition(getX() + 3 + (i % buttonsPerRow) * 20, startY + (i / buttonsPerRow) * 20);
		}
	}

	private void onClick(String texture) {
		selectedTexture = texture;
		updateConfig();
		updateButtons();
	}

	private void updateConfig() {
		if (currentItem == null) return;
		String uuid = ItemUtils.getItemUuid(currentItem);
		if (selectedTexture == null) {
			SkyblockerConfigManager.get().general.customHelmetTextures.remove(uuid);
		} else {
			SkyblockerConfigManager.get().general.customHelmetTextures.put(uuid, selectedTexture);
		}
	}

	private void updateButtons() {
		for (HeadButton b : allButtons) {
			b.selected = Objects.equals(b.texture, selectedTexture);
		}
		noneButton.selected = selectedTexture == null;
	}

	private void filterButtons(String search) {
		setScrollY(0);
		String s = search.toLowerCase();
		visibleButtons.clear();
		visibleButtons.add(noneButton);
		for (HeadButton b : allButtons) {
			if (b.name.toLowerCase().contains(s)) {
				visibleButtons.add(b);
			}
		}
		layoutButtons();
		updateButtons();
	}

	@Override
	public List<? extends Element> children() {
		int startY = searchField.getBottom() + 3;
		int endY = getY() + getHeight() - 2;
		int scrollY = (int) getScrollY();
		List<Element> list = new ArrayList<>();
		for (HeadButton b : visibleButtons) {
			int y = b.getY() - scrollY;
			if (y + b.getHeight() > startY && y < endY) {
				list.add(b);
			}
		}
		list.add(searchField);
		return list;
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, INNER_SPACE_TEXTURE, getX(), getY(), getWidth(), getHeight());

		searchField.render(context, mouseX, mouseY, delta);

		int startY = searchField.getBottom() + 3;
		int startX = getX() + 2;
		int endX = getX() + getWidth() - 2;
		int endY = getY() + getHeight() - 2;
		context.enableScissor(startX, startY, endX, endY);
		int scrollY = (int) getScrollY();
		HeadButton hovered = null;
		for (HeadButton b : visibleButtons) {
			int originalY = b.getY();
			int y = originalY - scrollY;
			if (y + b.getHeight() <= startY || y >= endY) {
				continue;
			}
			b.setY(y);
			b.render(context, mouseX, mouseY, delta);
			if (b.isMouseOver(mouseX, mouseY) && mouseX >= startX && mouseX < endX && mouseY >= startY && mouseY < endY) {
				hovered = b;
			}
			b.setY(originalY);
		}
		drawScrollbar(context);
		context.disableScissor();

		if (hovered != null && !hovered.name.isEmpty()) {
			context.drawTooltip(MinecraftClient.getInstance().textRenderer, Text.of(hovered.name), mouseX, mouseY);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (searchField.mouseClicked(mouseX, mouseY, button)) {
			setFocused(searchField);
			return true;
		}

		double adjustedMouseY = mouseY + getScrollY();
		if (overflows()) {
			int scrollbarX = getScrollbarX();
			// Default scrollbar width is 6 pixels
			if (mouseX >= scrollbarX && mouseX < scrollbarX + 6) {
				int thumbY = getScrollbarThumbY();
				int thumbHeight = getScrollbarThumbHeight();
				if (mouseY >= thumbY && mouseY < thumbY + thumbHeight) {
					adjustedMouseY = mouseY;
				}
			}
		}

		return super.mouseClicked(mouseX, adjustedMouseY, button);
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		if (searchField.isFocused() && searchField.charTyped(chr, modifiers)) {
			return true;
		}
		return super.charTyped(chr, modifiers);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (searchField.isFocused() && searchField.keyPressed(keyCode, scanCode, modifiers)) {
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	protected int getContentsHeightWithPadding() {
		int rows = Math.ceilDiv(visibleButtons.size(), buttonsPerRow);
		// 3px top padding + search bar height + 3px gap before the grid +
		// button rows + 3px bottom padding
		return rows * 20 + searchField.getHeight() + 9;
	}

	@Override
	protected double getDeltaYPerScroll() {
		return 10;
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

	public void setCurrentItem(@NotNull ItemStack item) {
		currentItem = item;
		String uuid = ItemUtils.getItemUuid(item);
		selectedTexture = SkyblockerConfigManager.get().general.customHelmetTextures.get(uuid);
		updateButtons();
		filterButtons(searchField.getText());
	}

	private static class HeadButton extends ClickableWidget {
		private final String name;
		private final String texture;
		private final ItemStack head;
		private boolean selected = false;

		HeadButton(String name, String texture, ItemStack head, Runnable onPress) {
			super(0, 0, 20, 20, Text.empty());
			this.name = name;
			this.texture = texture;
			this.head = head;
			this.onPress = onPress;
		}

		private final Runnable onPress;

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
			context.drawItem(head, getX() + 2, getY() + 2);
			if (selected) {
				context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x3000FF00);
			}
			if (isHovered()) {
				context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x20FFFFFF);
			}
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			onPress.run();
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
	}
}
