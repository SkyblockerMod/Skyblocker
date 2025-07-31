package de.hysky.skyblocker.skyblock.radialMenu;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextManager;
import de.hysky.skyblocker.skyblock.item.tooltip.BackpackPreview;
import de.hysky.skyblocker.utils.render.gui.state.HorizontalGradientGuiElementRenderState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class RadialButton implements Drawable, Element, Widget, Selectable {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	private final float startAngle;
	private final float arcLength;
	private final float internalRadius;
	private final float externalRadius;
	private final ItemStack icon;
	private final BooleanSupplier getHovered;
	private final int linkedSlot;

	public RadialButton(float startAngle, float arcLength, float internalRadius, float externalRadius, ItemStack icon, BooleanSupplier getHovered, int linkedSlot) {
		super();
		this.startAngle = (float) (startAngle - (Math.PI / 2));//start at the top
		this.arcLength = arcLength;
		this.internalRadius = internalRadius;
		this.externalRadius = externalRadius;
		this.icon = icon;
		this.getHovered = getHovered;
		this.linkedSlot = linkedSlot;
	}

	public String getName() {
		Text customName = icon.getCustomName();
		if (customName == null) return "null";
		return icon.getCustomName().getString();
	}

	protected int getLinkedSlot() {
		return linkedSlot;
	}

	@Override
	public void setFocused(boolean focused) {

	}

	@Override
	public boolean isFocused() {
		return false;
	}

	@Override
	public ScreenRect getNavigationFocus() {
		return Element.super.getNavigationFocus();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		//change color when hovered
		boolean hovered = getHovered.getAsBoolean();
		int color = hovered ? 0xFE000000 : 0x77000000;
		float internal = internalRadius;
		float external = hovered ? externalRadius + 5 : externalRadius;

		//get bounding box
		Vector2i center = new Vector2i(context.getScaledWindowWidth() / 2, context.getScaledWindowHeight() / 2);
		List<Vector2f> vertices = new ArrayList<>();
		//first rectangle
		vertices.add(getPos(center, startAngle, internal));
		vertices.add(getPos(center, startAngle + arcLength / 2, internal));
		vertices.add(getPos(center, startAngle + arcLength / 2, external));
		vertices.add(getPos(center, startAngle, external));
		//second rectangle
		vertices.add(getPos(center, startAngle + arcLength / 2, internal));
		vertices.add(getPos(center, startAngle + arcLength, internal));
		vertices.add(getPos(center, startAngle + arcLength, external));
		vertices.add(getPos(center, startAngle + arcLength / 2, external));

		context.state.addSimpleElement(new CustomShapeGuiElementRenderState(RenderPipelines.GUI, TextureSetup.empty(), new Matrix3x2f(context.getMatrices()),vertices,  color, context.scissorStack.peekLast()));
		//render background
		//context.draw(provider -> {
		//	VertexConsumer vertexConsumer = provider.getBuffer(RenderLayer.getGui());
		//	for (Vector3f vertex : vertices) {
		//		vertexConsumer.vertex(vertex).color(color);
		//	}
		//});

		//render icon
		float iconAngle = startAngle + (arcLength / 2);
		Vector2f iconPos = getPos(center, iconAngle, (internal + external) / 2);
		iconPos.sub(8, 8);


		context.drawItem(icon, (int) iconPos.x, (int) iconPos.y);
		context.drawStackOverlay(CLIENT.textRenderer, icon, (int) iconPos.x, (int) iconPos.y);
		SlotTextManager.renderSlotText(context, CLIENT.textRenderer, null, icon, linkedSlot, (int) iconPos.x, (int) iconPos.y);

		//render tooltip
		if (hovered && (Screen.hasShiftDown() || SkyblockerConfigManager.get().uiAndVisuals.radialMenu.tooltipsWithoutShift)) {
			// Backpack Preview
			if (CLIENT.currentScreen.getTitle().getString().equals("Storage")) {
				BackpackPreview.renderPreview(context, CLIENT.currentScreen, linkedSlot, mouseX, mouseY);
			} else {
				//normal tooltips
				context.drawItemTooltip(CLIENT.textRenderer, icon, mouseX, mouseY);
			}
		}
	}

	/**
	 * Get Screen position for a given angle and radius around the center
	 * @param center center of screen
	 * @param angle angle around center clockwise from top
	 * @param radius radius
	 * @return the screen position
	 */
	private static Vector2f getPos(Vector2i center, float angle, float radius) {
		return new Vector2f((float) (center.x + (radius * Math.cos(angle))), (float) (center.y + (radius * Math.sin(angle))));
	}

	@Override
	public SelectionType getType() {
		if (this.isFocused()) {
			return Selectable.SelectionType.FOCUSED;
		} else {
			return this.getHovered.getAsBoolean() ? Selectable.SelectionType.HOVERED : Selectable.SelectionType.NONE;
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
		builder.put(NarrationPart.TITLE, getName());
	}

	@Override
	public void setX(int x) {}

	@Override
	public void setY(int y) {}

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
	public void forEachChild(Consumer<ClickableWidget> consumer) {}
}
