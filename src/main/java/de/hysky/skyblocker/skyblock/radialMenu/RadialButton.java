package de.hysky.skyblocker.skyblock.radialMenu;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextManager;
import de.hysky.skyblocker.skyblock.item.tooltip.BackpackPreview;
import de.hysky.skyblocker.utils.render.HudHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class RadialButton implements Renderable, GuiEventListener, LayoutElement, NarratableEntry {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	private final float startAngle;
	private final float arcLength;
	private final float internalRadius;
	private final float externalRadius;
	private final ItemStack icon;
	private final BooleanSupplier getHovered;
	private final int linkedSlot;

	public RadialButton(float startAngle, float arcLength, float internalRadius, float externalRadius, ItemStack icon, BooleanSupplier getHovered, int linkedSlot) {
		super();
		this.startAngle = (float) (startAngle - (Math.PI / 2)); //start at the top
		this.arcLength = arcLength;
		this.internalRadius = internalRadius;
		this.externalRadius = externalRadius;
		this.icon = icon;
		this.getHovered = getHovered;
		this.linkedSlot = linkedSlot;
	}

	public String getName() {
		Component customName = icon.getCustomName();
		if (customName == null) return "null";
		return icon.getCustomName().getString();
	}

	protected int getLinkedSlot() {
		return linkedSlot;
	}

	@Override
	public ScreenRectangle getRectangle() {
		return GuiEventListener.super.getRectangle();
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
		//change color  and radius when hovered
		boolean hovered = getHovered.getAsBoolean();
		int color = hovered ? 0xFE000000 : 0x77000000; //darker when hovered
		float internal = internalRadius;
		float external = hovered ? externalRadius + 5 : externalRadius;

		//get bounding box
		Vector2i center = new Vector2i(context.guiWidth() / 2, context.guiHeight() / 2);
		List<Vector2f> vertices = new ArrayList<>();
		//first background rectangle
		vertices.add(getPos(center, startAngle, internal));
		vertices.add(getPos(center, startAngle + arcLength / 2, internal));
		vertices.add(getPos(center, startAngle + arcLength / 2, external));
		vertices.add(getPos(center, startAngle, external));
		//second background rectangle
		vertices.add(getPos(center, startAngle + arcLength / 2, internal));
		vertices.add(getPos(center, startAngle + arcLength, internal));
		vertices.add(getPos(center, startAngle + arcLength, external));
		vertices.add(getPos(center, startAngle + arcLength / 2, external));

		//draw background
		HudHelper.drawCustomShape(context, vertices, color);

		//render icon
		float iconAngle = startAngle + (arcLength / 2);
		Vector2f iconPos = getPos(center, iconAngle, (internal + external) / 2);
		iconPos.sub(8, 8);
		context.renderItem(icon, (int) iconPos.x, (int) iconPos.y);
		context.renderItemDecorations(CLIENT.font, icon, (int) iconPos.x, (int) iconPos.y);
		SlotTextManager.renderSlotText(context, CLIENT.font, null, icon, linkedSlot, (int) iconPos.x, (int) iconPos.y);

		//render tooltip
		if (hovered && (HudHelper.hasShiftDown() || SkyblockerConfigManager.get().uiAndVisuals.radialMenu.tooltipsWithoutShift)) {
			// Backpack Preview
			if (CLIENT.screen != null && CLIENT.screen.getTitle().getString().equals("Storage")) {
				BackpackPreview.renderPreview(context, CLIENT.screen, linkedSlot, mouseX, mouseY);
			} else {
				//normal tooltips
				context.setTooltipForNextFrame(CLIENT.font, icon, mouseX, mouseY);
			}
		}
	}

	/**
	 * Get Screen position for a given angle and radius around the center
	 *
	 * @param center center of screen
	 * @param angle  angle around center clockwise from top
	 * @param radius radius
	 * @return the screen position
	 */
	private static Vector2f getPos(Vector2i center, float angle, float radius) {
		return new Vector2f((float) (center.x + (radius * Math.cos(angle))), (float) (center.y + (radius * Math.sin(angle))));
	}

	@Override
	public NarrationPriority narrationPriority() {
		if (this.isFocused()) {
			return NarratableEntry.NarrationPriority.FOCUSED;
		} else {
			return this.getHovered.getAsBoolean() ? NarratableEntry.NarrationPriority.HOVERED : NarratableEntry.NarrationPriority.NONE;
		}
	}

	@Override
	public boolean isActive() {
		return NarratableEntry.super.isActive();
	}

	@Override
	public Collection<? extends NarratableEntry> getNarratables() {
		return NarratableEntry.super.getNarratables();
	}

	@Override
	public void updateNarration(NarrationElementOutput builder) {
		builder.add(NarratedElementType.TITLE, getName());
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
	public void visitWidgets(Consumer<AbstractWidget> consumer) {}

	@Override
	public void setFocused(boolean focused) {

	}

	@Override
	public boolean isFocused() {
		return false;
	}
}
