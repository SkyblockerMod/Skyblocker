package de.hysky.skyblocker.skyblock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.FunUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.render.LevelRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.utils.render.state.EmptyRenderState;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.CommonColors;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.animal.feline.CatSoundVariants;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.mixins.accessors.MinecraftAccessor;
import de.hysky.skyblocker.utils.Utils;
import org.jspecify.annotations.Nullable;

import java.util.Random;

public class CatPicture {
	private static @Nullable Vec3 renderPosition = new Vec3(-3, 79, 3);
	private static final Random RANDOM = new Random();
	//private static final Box CULLING_BOX = new Box(RENDER_POSITION.x, RENDER_POSITION.y, RENDER_POSITION.z, RENDER_POSITION.x + 1, RENDER_POSITION.y + 1, RENDER_POSITION.z + 1/16d);
	private static final Identifier TEXTURE = SkyblockerMod.id("textures/cat.png");
	private static float rotation = 180;
	private static boolean recipeBookEasterEgg = false;
	private static boolean moveAround = FunUtils.shouldEnableFun();

	@Init
	public static void init() {
		LevelRenderExtractionCallback.EVENT.register(CatPicture::extractRendering);
		Scheduler.INSTANCE.scheduleCyclic(CatPicture::update, 61);
		SkyblockEvents.LOCATION_CHANGE.register(_ -> resetPosition());
		SkyblockEvents.LEAVE.register(CatPicture::resetPosition);
	}

	public static void recipeBook() {
		if (recipeBookEasterEgg) return;
		recipeBookEasterEgg = true;
		moveAround = true;
		renderPosition = null;
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.CAT_SOUNDS.get(CatSoundVariants.SoundSet.CLASSIC).adultSounds().ambientSound(), 1));
	}

	private static void extractRendering(PrimitiveCollector collector) {
		// TODO Bring back culling eventually, maybe just include more context in the collector
		if (moveAround || (SkyblockerConfigManager.get().misc.cat && Utils.getLocation() == Location.HUB)) {
			collector.submitVanilla(EmptyRenderState.INSTANCE, CatPicture::extractRenderState);
		}
	}

	private static void resetPosition() {
		if (moveAround) renderPosition = null;
	}

	private static void update() {
		if (!Utils.isOnSkyblock()) return;
		if (!moveAround) return;
		ClientLevel level = Minecraft.getInstance().level;
		LocalPlayer player = Minecraft.getInstance().player;
		if (level == null || player == null) return;

		Vec3 lookAngle = player.getLookAngle();
		Vec3 eyePosition = player.getEyePosition();
		if (renderPosition != null && renderPosition.subtract(eyePosition).normalize().dot(lookAngle) > 0.3) return; // is looking at kitty

		Vec3 randomVector = new Vec3(RANDOM.nextDouble(-1, 1), RANDOM.nextDouble(-0.2, 0.2), RANDOM.nextDouble(-1, 1));
		if (lookAngle.dot(randomVector) > -0.1) randomVector = randomVector.scale(-1); // make sure it is behind the player

		BlockHitResult result = level.clip(new ClipContext(eyePosition, eyePosition.add(randomVector.scale(15)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
		if (result.getType() == HitResult.Type.MISS || result.getDirection().getAxis() == Direction.Axis.Y) return;
		if (!level.getBlockState(result.getBlockPos()).isSolidRender()) return; // only place on full blocks
		BlockPos relative = result.getBlockPos().relative(result.getDirection());
		if (!level.getBlockState(relative).isAir()) return; // only place in empty spots

		renderPosition = new Vec3(relative);
		rotation = 180f - result.getDirection().toYRot();
	}

	private static void extractRenderState(EmptyRenderState state, LevelRenderState levelState, SubmitNodeCollector submitNodeCollector) {
		if (renderPosition == null) return;
		ItemFrameRenderState itemFrameState = new ItemFrameRenderState();
		((MinecraftAccessor) Minecraft.getInstance()).getBlockModelResolver().updateForItemFrame(itemFrameState.frameModel, false, true);

		PoseStack matrices = new PoseStack();
		matrices.pushPose();
		matrices.translate(-levelState.cameraRenderState.pos.x + renderPosition.x + 0.5, -levelState.cameraRenderState.pos.y + renderPosition.y + 0.5, -levelState.cameraRenderState.pos.z + renderPosition.z + 0.5);
		matrices.mulPose(Axis.YP.rotationDegrees(rotation));

		// Render Item Frame
		matrices.translate(-0.5, -0.5, -0.5);
		itemFrameState.frameModel.submitWithZOffset(matrices, submitNodeCollector, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE);

		// Render Kitty
		matrices.translate(1, 1, 0);
		matrices.mulPose(Axis.ZP.rotationDegrees(180.0F));

		submitNodeCollector.submitCustomGeometry(matrices, RenderTypes.text(TEXTURE), (matricesEntry, buffer) -> {
			float z = 1F - 1 / 16f - 1 / 2048f;
			buffer.addVertex(matricesEntry, 0.0F, 1, z).setColor(CommonColors.WHITE).setUv(0.0F, 1.0F).setLight(LightCoordsUtil.FULL_BRIGHT);
			buffer.addVertex(matricesEntry, 1, 1, z).setColor(CommonColors.WHITE).setUv(1.0F, 1.0F).setLight(LightCoordsUtil.FULL_BRIGHT);
			buffer.addVertex(matricesEntry, 1, 0.0F, z).setColor(CommonColors.WHITE).setUv(1.0F, 0.0F).setLight(LightCoordsUtil.FULL_BRIGHT);
			buffer.addVertex(matricesEntry, 0.0F, 0.0F, z).setColor(CommonColors.WHITE).setUv(0.0F, 0.0F).setLight(LightCoordsUtil.FULL_BRIGHT);
		});

		matrices.popPose();
	}
}
