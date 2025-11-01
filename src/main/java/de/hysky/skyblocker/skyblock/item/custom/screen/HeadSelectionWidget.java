package de.hysky.skyblocker.skyblock.item.custom.screen;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.custom.CustomAnimatedHelmetTextures;
import de.hysky.skyblocker.skyblock.item.custom.CustomHelmetTextures;
import de.hysky.skyblocker.skyblock.profileviewer.utils.ProfileViewerUtils;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class HeadSelectionWidget extends ContainerWidget {
	private static final Identifier INNER_SPACE_TEXTURE = SkyblockerMod.id("menu_inner_space");

	private final List<HeadButton> allButtons = new ArrayList<>();
	private final List<HeadButton> visibleButtons = new ArrayList<>();
	private final TextFieldWidget searchField;
	private final HeadButton noneButton;
	private int buttonsPerRow = 1;

	private ItemStack currentItem;
	/**
	 * Null if a custom (animated or not) head wasn't selected.
	 */
	@Nullable
	private HeadButton selectedButton;

	public HeadSelectionWidget(int x, int y, int width, int height) {
		super(x, y, width, height, Text.of("HeadSelection"));
		this.searchField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, x + 3, y + 3, width - 6, 12, Text.translatable("gui.recipebook.search_hint"));
		this.searchField.setChangedListener(this::filterButtons);

		for (CustomHelmetTextures.NamedTexture tex : CustomHelmetTextures.getTextures()) {
			ItemStack head = ProfileViewerUtils.createSkull(tex.texture());
			HeadButton button = new HeadButton(tex.name(), tex.texture(), head, this::onClick);
			this.allButtons.add(button);
		}

		for (String id : CustomAnimatedHelmetTextures.getAnimatedHeadIds()) {
			AnimatedHeadButton button = new AnimatedHeadButton(id, this::onClick);
			this.allButtons.add(button);
		}

		this.noneButton = new HeadButton("", null, new ItemStack(Items.BARRIER), _ignored -> onClick(null));

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

	@Override
	public void setX(int x) {
		super.setX(x);
		searchField.setX(x + 3);
		layoutButtons();
	}

	@Override
	public void setY(int y) {
		super.setY(y);
		searchField.setY(y + 3);
		layoutButtons();
	}

	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		searchField.setWidth(width - 6);
		layoutButtons();
	}

	private void onClick(HeadButton button) {
		selectedButton = button;
		updateConfig();
		updateButtons();
	}

	private void updateConfig() {
		if (this.currentItem == null) return;
		String uuid = this.currentItem.getUuid();

		switch (this.selectedButton) {
			case null -> {
				SkyblockerConfigManager.get().general.customHelmetTextures.remove(uuid);
				SkyblockerConfigManager.get().general.customAnimatedHelmetTextures.remove(uuid);
			}
			case AnimatedHeadButton button -> {
				SkyblockerConfigManager.get().general.customAnimatedHelmetTextures.put(uuid, button.id);
				SkyblockerConfigManager.get().general.customHelmetTextures.remove(uuid);
			}
			case HeadButton button -> {
				SkyblockerConfigManager.get().general.customHelmetTextures.put(uuid, Objects.requireNonNull(button.texture));
				SkyblockerConfigManager.get().general.customAnimatedHelmetTextures.remove(uuid);
			}
		}
	}

	private void updateButtons() {
		// Check all buttons, whether one is selected depends on if it matches the selectedButton
		for (HeadButton b : this.allButtons) {
			b.selected = b.equals(this.selectedButton);
		}

		// If the selectedButton is null then set the noneButton as selected
		this.noneButton.selected = this.selectedButton == null;
	}

	private void filterButtons(String search) {
		setScrollY(0);
		String s = search.toLowerCase(Locale.ENGLISH);
		visibleButtons.clear();
		visibleButtons.add(noneButton);
		for (HeadButton b : allButtons) {
			if (b.name.toLowerCase(Locale.ENGLISH).contains(s)) {
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
		drawScrollbar(context, mouseX, mouseY);
		context.disableScissor();

		if (hovered != null && !hovered.name.isEmpty()) {
			context.drawTooltip(MinecraftClient.getInstance().textRenderer, Text.of(hovered.name), mouseX, mouseY);
		}
	}

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		if (searchField.mouseClicked(click, doubled)) {
			setFocused(searchField);
			return true;
		}

		double adjustedMouseY = click.y() + getScrollY();
		if (overflows()) {
			int scrollbarX = getScrollbarX();
			// Default scrollbar width is 6 pixels
			if (click.x() >= scrollbarX && click.x() < scrollbarX + 6) {
				int thumbY = getScrollbarThumbY();
				int thumbHeight = getScrollbarThumbHeight();
				if (click.y() >= thumbY && click.y() < thumbY + thumbHeight) {
					adjustedMouseY = click.y();
				}
			}
		}

		return super.mouseClicked(new Click(click.x(), adjustedMouseY, click.buttonInfo()), doubled);
	}

	@Override
	public boolean charTyped(CharInput input) {
		if (searchField.isFocused() && searchField.charTyped(input)) {
			return true;
		}
		return super.charTyped(input);
	}

	@Override
	public boolean keyPressed(KeyInput input) {
		if (searchField.isFocused() && searchField.keyPressed(input)) {
			return true;
		}
		return super.keyPressed(input);
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
		this.currentItem = item;
		String uuid = item.getUuid();

		String animatedHeadId = SkyblockerConfigManager.get().general.customAnimatedHelmetTextures.get(uuid);
		String customHeadTexture = SkyblockerConfigManager.get().general.customHelmetTextures.get(uuid);
		// The head button that should be selected (if any)
		HeadButton intendedSelected;

		// Search for the right button to select, defaulting to null if a suitable one cannot be found (e.g. texture changed, animated head removed)
		if (animatedHeadId != null) {
			intendedSelected = this.allButtons.stream()
					.filter(AnimatedHeadButton.class::isInstance)
					.map(AnimatedHeadButton.class::cast)
					.filter(animatedHead -> animatedHead.id.equals(animatedHeadId))
					.findFirst()
					.orElse(null);
		} else if (customHeadTexture != null) {
			intendedSelected = this.allButtons.stream()
					.filter(Predicate.not(AnimatedHeadButton.class::isInstance))
					.filter(head -> Objects.requireNonNull(head.texture).equals(customHeadTexture))
					.findFirst()
					.orElse(null);
		} else {
			intendedSelected = null;
		}

		this.selectedButton = intendedSelected;

		updateButtons();
		filterButtons(this.searchField.getText());
	}

	private static class HeadButton extends ClickableWidget {
		private final String name;
		/**
		 * Only null if this is an animated head.
		 */
		@UnknownNullability
		private final String texture;
		/**
		 * Only null if this is an animated head.
		 */
		@UnknownNullability
		private final ItemStack head;
		private final Consumer<HeadButton> onPress;
		private boolean selected = false;

		HeadButton(String name, @UnknownNullability String texture, @UnknownNullability ItemStack head, Consumer<HeadButton> onPress) {
			super(0, 0, 20, 20, Text.empty());
			this.name = name;
			this.texture = texture;
			this.head = head;
			this.onPress = onPress;
		}

		/**
		 * Retrieves the underlying {@link ItemStack} for displaying the head, required for animated heads.
		 *
		 * Will never return null.
		 */
		protected ItemStack getHead() {
			return this.head;
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
			context.drawItem(this.getHead(), getX() + 2, getY() + 2);
			if (this.selected) {
				context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x3000FF00);
			}
			if (isHovered()) {
				context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x20FFFFFF);
			}
		}

		@Override
		public void onClick(Click click, boolean doubled) {
			this.onPress.accept(this);
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
	}

	private static class AnimatedHeadButton extends HeadButton {
		private final String id;

		AnimatedHeadButton(String id, Consumer<HeadButton> onPress) {
			// The head stack is initially passed as null but initialized
			// The texture is not needed so we leave it as null too as this is done by id
			super(CustomAnimatedHelmetTextures.formatName(id), null, null, onPress);
			this.id = id;
		}

		/**
		 * Creates the item stack dynamically for each entry as the underlying stack will need to change.
		 */
		@Override
		protected ItemStack getHead() {
			ProfileComponent profile = CustomAnimatedHelmetTextures.animateHeadTexture(this.id);

			if (profile != null) {
				ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
				stack.set(DataComponentTypes.PROFILE, profile);

				return stack;
			}

			return Ico.BARRIER;
		}
	}
}
