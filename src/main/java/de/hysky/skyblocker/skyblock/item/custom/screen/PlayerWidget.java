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
		this.xRotation = MathHelper.clamp(this.xRotation - (float) deltaY * 2.5F, -50.0F, 50.0F);
		this.yRotation += (float) deltaX * 2.5F;
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
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {

	}

	@Override
	public void playDownSound(SoundManager soundManager) {}
}
