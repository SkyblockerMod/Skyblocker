package de.hysky.skyblocker.skyblock.quicknav;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.mixins.accessors.AbstractContainerScreenAccessor;
import de.hysky.skyblocker.mixins.accessors.PopupScreenAccessor;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.render.gui.AbstractPopupScreen;
import de.hysky.skyblocker.utils.render.texture.FallbackedTexture;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.PopupScreen;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import java.time.Duration;

import org.jspecify.annotations.Nullable;

@Environment(value = EnvType.CLIENT)
public class QuickNavButton extends AbstractWidget {
	private static final long TOGGLE_DURATION = 1000;
	@SuppressWarnings("unchecked")
	private static final @Nullable FallbackedTexture<Identifier>[] TAB_TEXTURES = new FallbackedTexture[14];

	private final int index;
	private final boolean toggled;
	private final String command;
	private final ItemStack icon;
	protected final Tooltip tooltip;

	private boolean temporaryToggled = false;
	private long toggleTime;

	// Stores whether the button is currently rendering in front of the main inventory background.
	private boolean renderInFront;
	private int alpha = 255;

	/**
	 * Checks if the current tab is a top tab based on its index.
	 *
	 * @return true if the index is less than 7, false otherwise.
	 */
	private boolean isTopTab() {
		return index < 7;
	}

	public boolean toggled() {
		return toggled || temporaryToggled;
	}

	public void setRenderInFront(boolean renderInFront) {
		this.renderInFront = renderInFront;
	}

	public float getAlpha() {
		return alpha / 255f;
	}

	/**
	 * Constructs a new QuickNavButton with the given parameters.
	 *
	 * @param index   the index of the button.
	 * @param toggled the toggled state of the button.
	 * @param command the command to execute when the button is clicked.
	 * @param icon    the icon to display on the button.
	 * @param tooltip the tooltip to show when hovered
	 */
	public QuickNavButton(int index, boolean toggled, String command, ItemStack icon, String tooltip) {
		super(0, 0, 26, 32, Component.empty());
		this.index = index;
		this.toggled = toggled;
		this.command = command;
		this.icon = icon;
		this.toggleTime = 0;
		if (tooltip == null || tooltip.isEmpty()) {
			this.tooltip = null;
			return;
		}
		Tooltip tip;
		try {
			setTooltip(tip = Tooltip.create(ComponentSerialization.CODEC.decode(JsonOps.INSTANCE, SkyblockerMod.GSON.fromJson(tooltip, JsonElement.class)).getOrThrow().getFirst()));
		} catch (Exception e) {
			setTooltip(tip = Tooltip.create(Component.literal(tooltip)));
		}
		this.tooltip = tip;
		setTooltipDelay(Duration.ofMillis(100));
	}

	private void updateCoordinates() {
		Screen screen = Minecraft.getInstance().screen;
		while (screen instanceof PopupScreen || screen instanceof AbstractPopupScreen) {
			if (screen instanceof PopupScreen) {
				if (!(screen instanceof PopupScreenAccessor popup)) {
					throw new IllegalStateException(
							"Current PopupScreen does not support AccessorPopupBackground"
					);
				}
				screen = popup.getUnderlyingScreen();
			} else if (screen instanceof AbstractPopupScreen abstractPopupScreen) {
				screen = abstractPopupScreen.backgroundScreen;
			}
		}
		if (screen instanceof AbstractContainerScreen<?> handledScreen) {
			var accessibleScreen = (AbstractContainerScreenAccessor) handledScreen;
			int x = accessibleScreen.getX();
			int y = accessibleScreen.getY();
			int h = accessibleScreen.getImageHeight();
			if (handledScreen instanceof ContainerScreen) h--; // they messed up the height on these.
			int w = accessibleScreen.getImageWidth();
			this.setX(x + this.index % 7 * 25 + w / 2 - 176 / 2);
			this.setY(this.index < 7 ? y - 28 : y + h - 4);
		}
	}

	/**
	 * Handles click events. If the button is not currently toggled,
	 * it sets the toggled state to true and sends a message with the command after cooldown.
	 */
	@Override
	public void onClick(MouseButtonEvent click, boolean doubled) {
		if (!this.temporaryToggled) {
			this.temporaryToggled = true;
			this.toggleTime = System.currentTimeMillis();
			if (command == null || command.isEmpty()) {
				Minecraft.getInstance().player.displayClientMessage(Constants.PREFIX.get().append(Component.literal("Quick Nav button index " + (index + 1) + " has no command!").withStyle(ChatFormatting.RED)), false);
			} else {
				MessageScheduler.INSTANCE.sendMessageAfterCooldown(command, true);
			}
			this.alpha = 0;
		}
	}

	/**
	 * As of 1.21.8, vanilla's creative inventory tabs aren't tab navigable due to them not being proper GUI buttons and instead they're
	 * manually drawn and the click logic is manual as well. If that ever changes, this should be adjusted to match the new vanilla behaviour.
	 */
	@Override
	public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent navigation) {
		return null;
	}

	/**
	 * Renders the button on screen. This includes both its texture and its icon.
	 * The method first updates the coordinates of the button,
	 * then calculates appropriate values for rendering based on its current state,
	 * and finally draws both the background and icon of the button on screen.
	 *
	 * @param context the context in which to render the button
	 * @param mouseX  the x-coordinate of the mouse cursor
	 * @param mouseY  the y-coordinate of the mouse cursor
	 */
	@Override
	public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
		this.updateCoordinates();

		// Note that this changes the return value of `toggled()`, so do not call it after this point.
		// Instead, use `renderInFront` to determine whether the button is currently rendering in front of the main inventory background.
		if (this.temporaryToggled && System.currentTimeMillis() - this.toggleTime >= TOGGLE_DURATION) {
			this.temporaryToggled = false; // Reset toggled state
		}
		//"animation"
		if (alpha < 255) {
			alpha = Math.min(alpha + 10, 255);
		}

		Identifier tabTexture = getTexture();

		// Render the button texture, always with full alpha if it's not rendering in front
		context.blitSprite(RenderPipelines.GUI_TEXTURED, tabTexture, this.getX(), this.getY(), this.width, this.height, renderInFront ? ARGB.color(alpha, -1) : -1);
		// Render the button icon
		int yOffset = this.index < 7 ? 1 : -1;
		context.renderItem(this.icon, this.getX() + 5, this.getY() + 8 + yOffset);

		this.handleCursor(context);
	}

	private Identifier getTexture() {
		FallbackedTexture<Identifier> texture = TAB_TEXTURES[index];
		if (texture != null) return texture.get();
		// Construct the texture identifier based on the index and toggled state
		return (TAB_TEXTURES[index] = FallbackedTexture.ofGuiSprite(
				SkyblockerMod.id("quick_nav/tab_" + (isTopTab() ? "top" : "bottom") + "_" + (renderInFront ? "selected" : "unselected") + "_" + (index % 7 + 1)),
				Identifier.withDefaultNamespace("container/creative_inventory/tab_" + (isTopTab() ? "top" : "bottom") + "_" + (renderInFront ? "selected" : "unselected") + "_" + (index % 7 + 1))
		)).get();
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {}
}
