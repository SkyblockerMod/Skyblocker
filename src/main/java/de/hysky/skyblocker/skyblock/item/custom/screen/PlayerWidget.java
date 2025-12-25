package de.hysky.skyblocker.skyblock.item.custom.screen;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.mixins.accessors.InventoryScreenInvoker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PlayerWidget extends AbstractWidget {
	private static final Identifier INNER_SPACE_TEXTURE = SkyblockerMod.id("menu_inner_space");
	private static final float FLIP_ROTATION = (float) Math.PI;
	private final AbstractClientPlayer player;

	private float xRotation = -10;
	private float yRotation = 225;

	public PlayerWidget(int x, int y, int width, int height, AbstractClientPlayer player) {
		super(x, y, width, height, Component.literal(""));
		this.player = player;
		this.player.yHeadRot = this.player.yHeadRotO = 0;
	}

	@Override
	protected void onDrag(MouseButtonEvent click, double offsetX, double offsetY) {
		super.onDrag(click, offsetX, offsetY);
		this.xRotation = Mth.clamp(this.xRotation - (float) offsetY * 2.5F, -50.0F, 50.0F);
		this.yRotation += (float) offsetX * 2.5F;
	}

	@Override
	protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
		context.blitSprite(RenderPipelines.GUI_TEXTURED, INNER_SPACE_TEXTURE, getX(), getY(), getWidth(), getHeight());

		float size = 64f;
		Vector3f translation = new Vector3f(0, player.getBbHeight() / 2f + 0.0625f, 0);
		Quaternionf rotation = new Quaternionf().rotationXYZ(-xRotation * Mth.DEG_TO_RAD, -yRotation * Mth.DEG_TO_RAD, FLIP_ROTATION);

		EntityRenderState renderState = InventoryScreenInvoker.invokeExtractRenderState(this.player);
		context.submitEntityRenderState(renderState, size, translation, rotation, null, getX(), getY(), this.getRight(), this.getBottom());
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {}

	@Override
	public void playDownSound(SoundManager soundManager) {}
}
