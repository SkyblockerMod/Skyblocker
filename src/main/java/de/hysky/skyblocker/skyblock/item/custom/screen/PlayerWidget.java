package de.hysky.skyblocker.skyblock.item.custom.screen;

import de.hysky.skyblocker.SkyblockerMod;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PlayerWidget extends ClickableWidget {

	private static final Identifier INNER_SPACE_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "menu_inner_space");
	private static final Quaternionf FLIP_ROTATION = new Quaternionf().rotationXYZ(0, 0.0F, (float) Math.PI);
	private final AbstractClientPlayerEntity player;

	private float xRotation = -10;
	private float yRotation = 225;

	public PlayerWidget(int x, int y, int width, int height, AbstractClientPlayerEntity player) {
		super(x, y, width, height, Text.literal(""));
		this.player = player;
		this.player.headYaw = this.player.lastHeadYaw = 0;
	}

	@Override
	protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
		super.onDrag(mouseX, mouseY, deltaX, deltaY);
		this.xRotation = MathHelper.clamp(this.xRotation - (float)deltaY * 2.5F, -50.0F, 50.0F);
		this.yRotation += (float)deltaX * 2.5F;
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		context.drawGuiTexture(RenderLayer::getGuiTextured, INNER_SPACE_TEXTURE, getX(), getY(), getWidth(), getHeight());
		MatrixStack matrices = context.getMatrices();
		matrices.push();
		float size = 64f;
		matrices.translate(getX() + getWidth() / 2f, getY() + getHeight() / 2f, size);
		Quaternionf quaternion = new Quaternionf().rotationXYZ(xRotation * MathHelper.PI / 180, yRotation * MathHelper.PI / 180, 0);
		matrices.multiply(quaternion);
		matrices.translate(0, 0, -50);
		Vector3f vector3f = new Vector3f(0, player.getHeight() / 2.0F + .0625f, 0);
		InventoryScreen.drawEntity(
				context,
				0,
				0,
				size,
				vector3f,
				FLIP_ROTATION,
				null,
				player
		);
		matrices.pop();
			/*
			Failed attempt at selecting which armor piece you are editing directly on the 3D preview, pretty close but looks stupid and x is flipped or some reason
			And I don't know why :(

			matrices.translate(0,0, 50);
			matrices.scale(size, size, -size);
			matrices.translate(vector3f.x, vector3f.y, vector3f.z);
			matrices.multiply(SILLY_ROTATION);
			matrices.scale(-1.0F, -1.0F, 1.0F);
			matrices.scale(0.9375F, 0.9375F, 0.9375F);
			matrices.translate(0.0F, -1.501F, 0.0F);

			Matrix4f projInv = new Matrix4f(RenderSystem.getProjectionMatrix()).invert();
			Matrix4f viewInv = new Matrix4f(RenderSystem.getModelViewMatrix()).mul(matrices.peek().getPositionMatrix()).invert();

			float x = 2f * mouseX / width - 1;
			float y = 1 - (2f * mouseY / height);
			Vector4f mouseNear = new Vector4f(x, y, -1, 1f).mul(projInv).mul(viewInv);
			Vector4f mouseFar = new Vector4f(x, y, 1, 1f).mul(projInv).mul(viewInv);
			Vector4f mouseDiff = new Vector4f(mouseFar).sub(mouseNear);

			matrices.pop();
			Vector3f nearVec3 = new Vector3f(-mouseNear.x(), mouseNear.y(), mouseNear.z());
			Vector3f farVec3 = new Vector3f(-mouseFar.x(), mouseFar.y(), mouseFar.z());
			Vector3f diffVec3 = new Vector3f(-mouseDiff.x(), mouseDiff.y(), mouseDiff.z()).normalize();
			context.drawText(textRenderer, nearVec3.toString(), 1, 1, -1, true);
			context.drawText(textRenderer, farVec3.toString(), 1, 10, -1, true);
			context.drawText(textRenderer, diffVec3.toString(), 1, 19, -1, true);
			//context.fill(width/2 - 1, height/2 - 1, width/2 + 1, height/2 + 1, 200, 0xFF_00_FF_00);
			*/
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {

	}

	@Override
	public void playDownSound(SoundManager soundManager) {}
}
