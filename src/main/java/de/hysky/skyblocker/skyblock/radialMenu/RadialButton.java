package de.hysky.skyblocker.skyblock.radialMenu;

import de.hysky.skyblocker.skyblock.item.slottext.SlotTextManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.navigation.NavigationDirection;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallbackI;

import java.util.Collection;
import java.util.function.Consumer;

public class RadialButton implements Drawable, Element, Widget, Selectable {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	float startAngle;
	float arcLength;
	float internalRadius;
	float externalRadius;
	ItemStack icon;
	boolean focused;
	boolean hovered;
	protected final RadialButton.PressAction onPress;
	int linkedSlot;

	public RadialButton(float startAngle, float arcLength, float internalRadius, float externalRadius, ItemStack icon, RadialButton.PressAction onPress, int linkedSlot) {
		super();
		this.startAngle = (float) (startAngle - (Math.PI / 2));//start at the top
		this.arcLength = arcLength;
		this.internalRadius = internalRadius;
		this.externalRadius = externalRadius;
		this.icon = icon;
		focused = false;
		this.onPress = onPress;
		this.linkedSlot = linkedSlot;
	}
	public String getName() {
		Text customName = icon.getCustomName();
		if (customName == null) return null;
		return icon.getCustomName().getString();
	}


	/**
	 * Callback for when a mouse move event has been captured.
	 *
	 * @param mouseX the X coordinate of the mouse
	 * @param mouseY the Y coordinate of the mouse
	 */
	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		Element.super.mouseMoved(mouseX, mouseY);
	}

	/**
	 * Callback for when a mouse button down event
	 * has been captured.
	 * <p>
	 * The button number is identified by the constants in
	 * {@link GLFW GLFW} class.
	 *
	 * @param mouseX the X coordinate of the mouse
	 * @param mouseY the Y coordinate of the mouse
	 * @param button the mouse button number
	 * @return {@code true} to indicate that the event handling is successful/valid
	 * @see GLFW#GLFW_MOUSE_BUTTON_1
	 */
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (hovered) {
			this.onPress.onPress(this);
		}
		return Element.super.mouseClicked(mouseX, mouseY, button);
	}

	/**
	 * Callback for when a mouse button release event
	 * has been captured.
	 * <p>
	 * The button number is identified by the constants in
	 * {@link GLFW GLFW} class.
	 *
	 * @param mouseX the X coordinate of the mouse
	 * @param mouseY the Y coordinate of the mouse
	 * @param button the mouse button number
	 * @return {@code true} to indicate that the event handling is successful/valid
	 * @see GLFW#GLFW_MOUSE_BUTTON_1
	 */
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		return Element.super.mouseReleased(mouseX, mouseY, button);
	}

	/**
	 * Callback for when a mouse button drag event
	 * has been captured.
	 * <p>
	 * The button number is identified by the constants in
	 * {@link GLFW GLFW} class.
	 *
	 * @param mouseX the current X coordinate of the mouse
	 * @param mouseY the current Y coordinate of the mouse
	 * @param button the mouse button number
	 * @param deltaX the difference of the current X with the previous X coordinate
	 * @param deltaY the difference of the current Y with the previous Y coordinate
	 * @return {@code true} to indicate that the event handling is successful/valid
	 * @see GLFW#GLFW_MOUSE_BUTTON_1
	 */
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		return Element.super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	/**
	 * Callback for when a mouse button scroll event
	 * has been captured.
	 *
	 * @param mouseX           the X coordinate of the mouse
	 * @param mouseY           the Y coordinate of the mouse
	 * @param horizontalAmount the horizontal scroll amount
	 * @param verticalAmount   the vertical scroll amount
	 * @return {@code true} to indicate that the event handling is successful/valid
	 */
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		return Element.super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	/**
	 * Callback for when a key down event has been captured.
	 * <p>
	 * The key code is identified by the constants in
	 * {@link GLFW GLFW} class.
	 *
	 * @param keyCode   the named key code of the event as described in the {@link GLFW GLFW} class
	 * @param scanCode  the unique/platform-specific scan code of the keyboard input
	 * @param modifiers a GLFW bitfield describing the modifier keys that are held down (see <a href="https://www.glfw.org/docs/3.3/group__mods.html">GLFW Modifier key flags</a>)
	 * @return {@code true} to indicate that the event handling is successful/valid
	 * @see Keyboard#onKey(long, int, int, int, int)
	 * @see GLFW#GLFW_KEY_Q
	 * @see GLFWKeyCallbackI#invoke(long, int, int, int, int)
	 */
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return Element.super.keyPressed(keyCode, scanCode, modifiers);
	}

	/**
	 * Callback for when a key down event has been captured.
	 * <p>
	 * The key code is identified by the constants in
	 * {@link GLFW GLFW} class.
	 *
	 * @param keyCode   the named key code of the event as described in the {@link GLFW GLFW} class
	 * @param scanCode  the unique/platform-specific scan code of the keyboard input
	 * @param modifiers a GLFW bitfield describing the modifier keys that are held down (see <a href="https://www.glfw.org/docs/3.3/group__mods.html">GLFW Modifier key flags</a>)
	 * @return {@code true} to indicate that the event handling is successful/valid
	 * @see Keyboard#onKey(long, int, int, int, int)
	 * @see GLFW#GLFW_KEY_Q
	 * @see GLFWKeyCallbackI#invoke(long, int, int, int, int)
	 */
	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		return Element.super.keyReleased(keyCode, scanCode, modifiers);
	}

	/**
	 * Callback for when a character input has been captured.
	 * <p>
	 * The key code is identified by the constants in
	 * {@link GLFW GLFW} class.
	 *
	 * @param chr       the captured character
	 * @param modifiers a GLFW bitfield describing the modifier keys that are held down (see <a href="https://www.glfw.org/docs/3.3/group__mods.html">GLFW Modifier key flags</a>)
	 * @return {@code true} to indicate that the event handling is successful/valid
	 * @see GLFW#GLFW_KEY_Q
	 * @see GLFWKeyCallbackI#invoke(long, int, int, int, int)
	 */
	@Override
	public boolean charTyped(char chr, int modifiers) {
		return Element.super.charTyped(chr, modifiers);
	}

	@Override
	public @Nullable GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
		return Element.super.getNavigationPath(navigation);
	}

	/**
	 * Checks if the mouse position is within the bound
	 * of the element.
	 *
	 * @param mouseX the X coordinate of the mouse
	 * @param mouseY the Y coordinate of the mouse
	 * @return {@code true} if the mouse is within the bound of the element, otherwise {@code false}
	 */
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		if (CLIENT.currentScreen == null) return false;
		float actualX = (float) (mouseX * 2) - CLIENT.currentScreen.width;
		float actualY = (float) (mouseY * 2) - CLIENT.currentScreen.height;

		//get angle of mouse and adjust to use same starting point and direction as buttons and see if its within bounds
		double angle = -Math.atan2(actualX, actualY) + Math.PI / 2;
		return angle > startAngle && angle < startAngle + arcLength - 0.001; // make sure there is no overlap
	}


	@Override
	public void setFocused(boolean focused) {
		this.focused = focused;
	}

	@Override
	public boolean isFocused() {
		return this.focused;
	}

	@Override
	public @Nullable GuiNavigationPath getFocusedPath() {
		return Element.super.getFocusedPath();
	}

	@Override
	public ScreenRect getNavigationFocus() {
		return Element.super.getNavigationFocus();
	}

	@Override
	public void setPosition(int x, int y) {
		Widget.super.setPosition(x, y);
	}

	@Override
	public ScreenRect getBorder(NavigationDirection direction) {
		return Element.super.getBorder(direction);
	}


	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		//change color when hovered
		this.hovered = this.isMouseOver(mouseX, mouseY);
		int color = hovered ? 0xFE000000 : 0x77000000;
		float internal = internalRadius;
		float external = hovered ? externalRadius + 5 : externalRadius;

		//get bounding box
		Vector2i center = new Vector2i(context.getScaledWindowWidth() / 2, context.getScaledWindowHeight() / 2);
		Vector3f pos1 = getPos(center, startAngle, internal);
		Vector3f pos2 = getPos(center, startAngle, external);
		Vector3f pos3 = getPos(center, startAngle + arcLength, internal);
		Vector3f pos4 = getPos(center, startAngle + arcLength, external);


		//render background
		context.draw(provider -> {
			VertexConsumer vertexConsumer = provider.getBuffer(RenderLayer.getGui());
			Matrix4f positionMatrix = context.getMatrices().peek().getPositionMatrix();
			vertexConsumer.vertex(positionMatrix, pos1.x, pos1.y, pos1.z).color(color);
			vertexConsumer.vertex(positionMatrix, pos3.x, pos3.y, pos3.z).color(color);
			vertexConsumer.vertex(positionMatrix, pos4.x, pos4.y, pos4.z).color(color);
			vertexConsumer.vertex(positionMatrix, pos2.x, pos2.y, pos2.z).color(color);
		});

		//render icon
		float iconAngle = startAngle + (arcLength / 2);
		Vector3f iconPos = getPos(center, iconAngle, (internal + external) / 2);
		iconPos.sub(8, 8, 0);


		context.drawItem(icon, (int) iconPos.x, (int) iconPos.y, (int) iconPos.z);
		context.drawStackOverlay(CLIENT.textRenderer, icon, (int) iconPos.x, (int) iconPos.y);
		SlotTextManager.renderSlotText(context, CLIENT.textRenderer, null, icon, linkedSlot, (int) iconPos.x, (int) iconPos.y);

		//render tooltip
		if (hovered && Screen.hasShiftDown()) { //todo config for shift
			context.drawItemTooltip(CLIENT.textRenderer, icon, mouseX, mouseY);
		}
	}

	private static Vector3f getPos(Vector2i center, float angle, float radius) {
		return new Vector3f((float) (center.x + (radius * Math.cos(angle))), (float) (center.y + (radius * Math.sin(angle))), 0);
	}

	@Override
	public SelectionType getType() {
		if (this.isFocused()) {
			return Selectable.SelectionType.FOCUSED;
		} else {
			return this.hovered ? Selectable.SelectionType.HOVERED : Selectable.SelectionType.NONE;
		}
	}

	@Override
	public boolean isNarratable() {
		return Selectable.super.isNarratable();
	}

	@Override
	public Collection<? extends Selectable> getNarratedParts() {
		return Selectable.super.getNarratedParts();
	}

	@Override
	public void appendNarrations(NarrationMessageBuilder builder) {

	}

	@Override
	public void setX(int x) {

	}

	@Override
	public void setY(int y) {

	}

	@Override
	public int getX() {
		return 0;
	}

	@Override
	public int getY() {
		return 0;
	}

	@Override
	public int getWidth() {
		return 0;
	}

	@Override
	public int getHeight() {
		return 0;
	}

	@Override
	public void forEachChild(Consumer<ClickableWidget> consumer) {

	}

	@Override
	public int getNavigationOrder() {
		return Element.super.getNavigationOrder();
	}

	@Environment(EnvType.CLIENT)
	public interface PressAction {
		void onPress(RadialButton button);
	}
}
