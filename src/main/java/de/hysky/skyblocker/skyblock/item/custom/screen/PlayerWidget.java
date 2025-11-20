package de.hysky.skyblocker.skyblock.item.custom.screen;

import de.hysky.skyblocker.SkyblockerMod;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PlayerWidget extends ClickableWidget {
	private static final Identifier INNER_SPACE_TEXTURE = SkyblockerMod.id("menu_inner_space");
	private static final float FLIP_ROTATION = (float) Math.PI;
	private final AbstractClientPlayerEntity player;

	private float xRotation = -10;
	private float yRotation = 225;

	public PlayerWidget(int x, int y, int width, int height, AbstractClientPlayerEntity player) {
		super(x, y, width, height, Text.literal(""));
		this.player = player;
		this.player.headYaw = this.player.lastHeadYaw = 0;
	}

	@Override
	protected void onDrag(Click click, double offsetX, double offsetY) {
		super.onDrag(click, offsetX, offsetY);
		this.xRotation = MathHelper.clamp(this.xRotation - (float) offsetY * 2.5F, -50.0F, 50.0F);
		this.yRotation += (float) offsetX * 2.5F;
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, INNER_SPACE_TEXTURE, getX(), getY(), getWidth(), getHeight());

		float size = 64f;
		Vector3f translation = new Vector3f(0, player.getHeight() / 2f + 0.0625f, 0);
		Quaternionf rotation = new Quaternionf().rotationXYZ(-xRotation * MathHelper.RADIANS_PER_DEGREE, -yRotation * MathHelper.RADIANS_PER_DEGREE, FLIP_ROTATION);

		InventoryScreen.drawEntity(
				context,
				getX(),
				getY(),
				this.getRight(),
				this.getBottom(),
				size,
				translation,
				rotation,
				null,
				player
		);
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

	@Override
	public void playDownSound(SoundManager soundManager) {}
}
