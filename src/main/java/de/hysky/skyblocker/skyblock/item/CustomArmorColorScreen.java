package de.hysky.skyblocker.skyblock.item;

import com.mojang.blaze3d.systems.RenderSystem;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import org.joml.*;
import org.lwjgl.glfw.GLFW;

import java.lang.Math;

public class CustomArmorColorScreen extends Screen {

	private static final Quaternionf SILLY_ROTATION = new Quaternionf().rotationXYZ(0, 0.0F, (float) Math.PI);
	private static final ModelData PLAYER_MODEL = PlayerEntityModel.getTexturedModelData(Dilation.NONE, false);

	private final OtherClientPlayerEntity player = new OtherClientPlayerEntity(MinecraftClient.getInstance().world, MinecraftClient.getInstance().getGameProfile()){
		@Override
		public boolean isInvisibleTo(PlayerEntity player) {
			return true;
		}
	};

	@Init
	public static void initCommand() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
				ClientCommandManager.literal("skyblocker").then(ClientCommandManager.literal("teehee").executes(context -> {
					Scheduler.queueOpenScreen(new CustomArmorColorScreen());
					return 1;
				}))));
	}

	protected CustomArmorColorScreen() {
		super(Text.literal("Custom Color Screen"));
		/*this.player.bodyYaw = 210.0F;
		this.player.setPitch(25.0F);
		this.player.headYaw = this.player.getBodyYaw();
		this.player.prevHeadYaw = this.player.getBodyYaw();*/
		this.player.headYaw = this.player.prevHeadYaw =  0;
	}

	@Override
	public void tick() {
		player.age++;
	}

	private float rotation = 0;

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_RIGHT) {
			rotation += MathHelper.PI / 16.f;
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		//rotation += delta / 20.f;
		rotation %= 2.f*MathHelper.PI;
		MatrixStack matrices = context.getMatrices();
		matrices.push();
		float size = 128f;
		matrices.translate(width/2f, height/2f, size);
		Quaternionf quaternion = new Quaternionf().rotationXYZ(rotation, 0, 0);
		matrices.multiply(quaternion);
		matrices.translate(0, 0, -50);
		Vector3f vector3f = new Vector3f(0, player.getHeight() / 2.0F + .0625f, 0);
		InventoryScreen.drawEntity(
				context,
				0,
				0,
				size,
				vector3f,
				SILLY_ROTATION,
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
}
