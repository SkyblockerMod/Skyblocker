package de.hysky.skyblocker.skyblock.item.custom.screen;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.custom.CustomAnimatedHelmetTextures;
import de.hysky.skyblocker.skyblock.item.custom.CustomHelmetTextures;
import de.hysky.skyblocker.skyblock.profileviewer.utils.ProfileViewerUtils;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;

public class HeadSelectionWidget extends AbstractContainerWidget {
	private static final Identifier INNER_SPACE_TEXTURE = SkyblockerMod.id("menu_inner_space");

	private final List<HeadButton> allButtons = new ArrayList<>();
	private final List<HeadButton> visibleButtons = new ArrayList<>();
	private final EditBox searchField;
	private final HeadButton noneButton;
	private int buttonsPerRow = 1;

	private ItemStack currentItem;
	/**
	 * Null if a custom (animated or not) head wasn't selected.
	 */
	private @Nullable HeadButton selectedButton;

	public HeadSelectionWidget(int x, int y, int width, int height) {
		super(x, y, width, height, Component.nullToEmpty("HeadSelection"));
		this.searchField = new EditBox(Minecraft.getInstance().font, x + 3, y + 3, width - 6, 12, Component.translatable("gui.recipebook.search_hint"));
		this.searchField.setResponder(this::filterButtons);

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

		SkyblockerConfigManager.updateOnly(config -> {
			switch (this.selectedButton) {
				case null -> {
					config.general.customHelmetTextures.remove(uuid);
					config.general.customAnimatedHelmetTextures.remove(uuid);
				}
				case AnimatedHeadButton button -> {
					config.general.customAnimatedHelmetTextures.put(uuid, button.id);
					config.general.customHelmetTextures.remove(uuid);
				}
				case HeadButton button -> {
					config.general.customHelmetTextures.put(uuid, Objects.requireNonNull(button.texture));
					config.general.customAnimatedHelmetTextures.remove(uuid);
				}
			}
		});
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
		setScrollAmount(0);
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
	public List<? extends GuiEventListener> children() {
		int startY = searchField.getBottom() + 3;
		int endY = getY() + getHeight() - 2;
		int scrollY = (int) scrollAmount();
		List<GuiEventListener> list = new ArrayList<>();
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
	protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
		context.blitSprite(RenderPipelines.GUI_TEXTURED, INNER_SPACE_TEXTURE, getX(), getY(), getWidth(), getHeight());

		searchField.render(context, mouseX, mouseY, delta);

		int startY = searchField.getBottom() + 3;
		int startX = getX() + 2;
		int endX = getX() + getWidth() - 2;
		int endY = getY() + getHeight() - 2;
		context.enableScissor(startX, startY, endX, endY);
		int scrollY = (int) scrollAmount();
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
		renderScrollbar(context, mouseX, mouseY);
		context.disableScissor();

		if (hovered != null && !hovered.name.isEmpty()) {
			context.setTooltipForNextFrame(Minecraft.getInstance().font, Component.nullToEmpty(hovered.name), mouseX, mouseY);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (searchField.mouseClicked(click, doubled)) {
			setFocused(searchField);
			return true;
		}

		double adjustedMouseY = click.y() + scrollAmount();
		if (scrollbarVisible()) {
			int scrollbarX = scrollBarX();
			// Default scrollbar width is 6 pixels
			if (click.x() >= scrollbarX && click.x() < scrollbarX + 6) {
				int thumbY = scrollBarY();
				int thumbHeight = scrollerHeight();
				if (click.y() >= thumbY && click.y() < thumbY + thumbHeight) {
					adjustedMouseY = click.y();
				}
			}
		}

		return super.mouseClicked(new MouseButtonEvent(click.x(), adjustedMouseY, click.buttonInfo()), doubled);
	}

	@Override
	public boolean charTyped(CharacterEvent input) {
		if (searchField.isFocused() && searchField.charTyped(input)) {
			return true;
		}
		return super.charTyped(input);
	}

	@Override
	public boolean keyPressed(KeyEvent input) {
		if (searchField.isFocused() && searchField.keyPressed(input)) {
			return true;
		}
		return super.keyPressed(input);
	}

	@Override
	protected int contentHeight() {
		int rows = Math.ceilDiv(visibleButtons.size(), buttonsPerRow);
		// 3px top padding + search bar height + 3px gap before the grid +
		// button rows + 3px bottom padding
		return rows * 20 + searchField.getHeight() + 9;
	}

	@Override
	protected double scrollRate() {
		return 10;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {}

	public void setCurrentItem(ItemStack item) {
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
		filterButtons(this.searchField.getValue());
	}

	private static class HeadButton extends AbstractWidget {
		private final String name;
		/**
		 * Only null if this is an animated head.
		 */
		private final @Nullable String texture;
		/**
		 * Only null if this is an animated head.
		 */
		private final @Nullable ItemStack head;
		private final Consumer<HeadButton> onPress;
		private boolean selected = false;

		HeadButton(String name, @Nullable String texture, @Nullable ItemStack head, Consumer<HeadButton> onPress) {
			super(0, 0, 20, 20, Component.empty());
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
			return Objects.requireNonNull(this.head);
		}

		@Override
		protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
			context.renderItem(this.getHead(), getX() + 2, getY() + 2);
			if (this.selected) {
				context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x3000FF00);
			}
			if (this.isHovered()) {
				context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x20FFFFFF);
			}
			this.handleCursor(context);
		}

		@Override
		public void onClick(MouseButtonEvent click, boolean doubled) {
			this.onPress.accept(this);
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput builder) {}
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
			ResolvableProfile profile = CustomAnimatedHelmetTextures.animateHeadTexture(this.id);

			if (profile != null) {
				ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
				stack.set(DataComponents.PROFILE, profile);

				return stack;
			}

			return Ico.BARRIER;
		}
	}
}
