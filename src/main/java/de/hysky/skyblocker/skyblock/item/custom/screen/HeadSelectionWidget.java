package de.hysky.skyblocker.skyblock.item.custom.screen;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.custom.CustomAnimatedHelmetTextures;
import de.hysky.skyblocker.skyblock.item.custom.CustomHelmetTextures;
import de.hysky.skyblocker.skyblock.profileviewer.utils.ProfileViewerUtils;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.render.gui.SearchableGridWidget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jspecify.annotations.Nullable;

public class HeadSelectionWidget extends SearchableGridWidget {
	private static final Identifier INNER_SPACE_TEXTURE = SkyblockerMod.id("menu_inner_space");

	private final List<HeadButton> allButtons = new ArrayList<>();
	private final List<HeadButton> visibleButtons = new ArrayList<>();
	private final HeadButton noneButton;

	private @Nullable ItemStack currentItem;
	/**
	 * Null if a custom (animated or not) head wasn't selected.
	 */
	private HeadButton selectedButton;

	public HeadSelectionWidget(int x, int y, int width, int height) {
		super(x + 2, y + 2, width - 4, height - 4, Component.nullToEmpty("HeadSelection"), 20);

		for (CustomHelmetTextures.NamedTexture tex : CustomHelmetTextures.getTextures()) {
			ItemStack head = ProfileViewerUtils.createSkull(tex.texture()).getStackOrThrow();
			HeadButton button = new HeadButton(tex.name(), tex.texture(), head, this::onClick);
			this.allButtons.add(button);
		}

		for (String id : CustomAnimatedHelmetTextures.getAnimatedHeadIds()) {
			AnimatedHeadButton button = new AnimatedHeadButton(id, this::onClick);
			this.allButtons.add(button);
		}

		this.noneButton = new HeadButton("", this::onClick);
		this.selectedButton = this.noneButton;

		setSearch("");
	}

	@Override
	public void setX(int x) {
		super.setX(x + 2);
	}

	@Override
	public void setY(int y) {
		super.setY(y + 2);
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
				case HeadButton button when button == noneButton -> {
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
		this.noneButton.selected = this.selectedButton == this.noneButton;
	}

	@Override
	protected Collection<? extends AbstractWidget> filterWidgets(String search) {
		setScrollAmount(0);
		String s = search.toLowerCase(Locale.ENGLISH);
		visibleButtons.clear();
		visibleButtons.add(noneButton);
		for (HeadButton b : allButtons) {
			if (b.name.toLowerCase(Locale.ENGLISH).contains(s)) {
				visibleButtons.add(b);
			}
		}
		updateButtons();
		return visibleButtons;
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, INNER_SPACE_TEXTURE, getX() - 2, getY() - 2, getWidth() + 4, getHeight() + 4);
		super.extractWidgetRenderState(graphics, mouseX, mouseY, a);
	}

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
					.map(HeadButton.class::cast)
					.orElse(noneButton);
		} else if (customHeadTexture != null) {
			intendedSelected = this.allButtons.stream()
					.filter(Predicate.not(AnimatedHeadButton.class::isInstance))
					.filter(head -> Objects.requireNonNull(head.texture).equals(customHeadTexture))
					.findFirst()
					.orElse(noneButton);
		} else {
			intendedSelected = noneButton;
		}

		this.selectedButton = intendedSelected;

		updateButtons();
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
		private final ItemStack head;
		private final Consumer<HeadButton> onPress;
		private boolean selected = false;

		private HeadButton(String name, Consumer<HeadButton> onPress) {
			this(name, null, Ico.BARRIER.getStackOrThrow(), onPress);
		}

		private HeadButton(String name, @Nullable String texture, ItemStack head, Consumer<HeadButton> onPress) {
			super(0, 0, 20, 20, Component.empty());
			this.name = name;
			this.texture = texture;
			this.head = head;
			this.onPress = onPress;

			if (!name.isEmpty()) {
				setTooltip(Tooltip.create(Component.nullToEmpty(name)));
			}
		}

		/**
		 * Retrieves the underlying {@link ItemStack} for displaying the head, required for animated heads.
		 */
		protected ItemStack getHead() {
			return this.head;
		}

		@Override
		protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
			graphics.item(this.getHead(), getX() + 2, getY() + 2);
			if (this.selected) {
				graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x3000FF00);
			}
			if (this.isHovered()) {
				graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x20FFFFFF);
			}
			this.handleCursor(graphics);
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

		private AnimatedHeadButton(String id, Consumer<HeadButton> onPress) {
			super(CustomAnimatedHelmetTextures.formatName(id), onPress);
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

			return Ico.BARRIER.getStackOrThrow();
		}
	}
}
