package de.hysky.skyblocker.skyblock.item.custom.screen;

import de.hysky.skyblocker.mixins.accessors.EntityRenderDispatcherAccessor;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.client.render.entity.model.ArmorEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.item.equipment.trim.ArmorTrimMaterial;
import net.minecraft.item.equipment.trim.ArmorTrimMaterials;
import net.minecraft.item.equipment.trim.ArmorTrimPattern;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public abstract sealed class TrimElementButton extends PressableWidget permits TrimElementButton.Pattern, TrimElementButton.Material {
	private static final ItemStack BARRIER = new ItemStack(Items.BARRIER);
	protected final @Nullable Identifier element;
	protected ItemStack stack;
	private final Consumer<TrimElementButton> onPress;

	public TrimElementButton(@Nullable Identifier element, Text name, Consumer<TrimElementButton> onPress) {
		super(0, 0, 20, 20, name);
		this.element = element;
		this.onPress = onPress;
		setTooltip(Tooltip.of(getMessage()));
	}

	public @Nullable Identifier getElement() {
		return element;
	}

	@Override
	public void setMessage(Text message) {
		super.setMessage(message);
		setTooltip(Tooltip.of(getMessage()));
	}

	@Override
	public void drawMessage(DrawContext context, TextRenderer textRenderer, int color) {
		draw(context);
	}

	abstract void draw(DrawContext context);

	public static final class Pattern extends TrimElementButton {

		private static final int DEFAULT_ROTATION = 195;

		private static ArmorEntityModel<BipedEntityRenderState> OUTER_MODEL = null;
		private static ArmorEntityModel<BipedEntityRenderState> INNER_MODEL = null;
		private static EquipmentRenderer EQUIPMENT_RENDERER = null;

		private final ArmorTrim trim;
		private EquippableComponent equippableComponent;

		private float rotation = DEFAULT_ROTATION;

		public Pattern(@Nullable Identifier element, @Nullable ArmorTrimPattern pattern, Consumer<TrimElementButton> onPress) {
			super(element, pattern == null ? Text.translatable("gui.none") : pattern.description(), onPress);
			if (element == null) {
				trim = null;
				return;
			}
			if (OUTER_MODEL == null) {
				OUTER_MODEL = new ArmorEntityModel<>(MinecraftClient.getInstance().getLoadedEntityModels().getModelPart(EntityModelLayers.PLAYER_OUTER_ARMOR));
				INNER_MODEL = new ArmorEntityModel<>(MinecraftClient.getInstance().getLoadedEntityModels().getModelPart(EntityModelLayers.PLAYER_INNER_ARMOR));
				EQUIPMENT_RENDERER = new EquipmentRenderer(
						((EntityRenderDispatcherAccessor) MinecraftClient.getInstance().getEntityRenderDispatcher()).getEquipmentModelLoader(),
						MinecraftClient.getInstance().getBlockRenderManager().getModels().getModelManager().getAtlas(TexturedRenderLayers.ARMOR_TRIMS_ATLAS_TEXTURE));
			}
			trim = new ArmorTrim(
					Utils.getRegistryWrapperLookup().getOrThrow(RegistryKeys.TRIM_MATERIAL).getOrThrow(ArmorTrimMaterials.QUARTZ),
					RegistryEntry.of(pattern));
		}

		public void setStack(@NotNull ItemStack newStack) {
			newStack = newStack.copy();
			// Remove the uuid so it doesn't render with the selected trim
			NbtCompound nbtCopy = ItemUtils.getCustomData(newStack).copy();
			nbtCopy.remove(ItemUtils.UUID);
			newStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbtCopy));
			newStack.set(DataComponentTypes.TRIM, trim);

			equippableComponent = newStack.get(DataComponentTypes.EQUIPPABLE);
			if (equippableComponent == null) throw new IllegalArgumentException("Trimmed stack must contain an equippable component");

			stack = newStack;
		}

		@Override
		void draw(DrawContext context) {
			if (trim == null) {
				context.drawItem(BARRIER, getX() + getWidth() / 2 - 8, getY() + getHeight() / 2 - 8);
				return;
			}
			if (isHovered()) {
				rotation += MinecraftClient.getInstance().getRenderTickCounter().getDynamicDeltaTicks() * 0.05f * 90;
				rotation %= 360;
			} else rotation = DEFAULT_ROTATION;

			EquipmentSlot slot = equippableComponent.slot();
			ArmorEntityModel<BipedEntityRenderState> model = slot == EquipmentSlot.LEGS ? INNER_MODEL : OUTER_MODEL;
			float offset = setVisibleAndGetOffset(model, slot);

			MatrixStack matrices = context.getMatrices();
			matrices.push();
			matrices.translate(getX() + getWidth() / 2f, getY() + getHeight() / 2f, 200);
			matrices.translate(0, offset, 0);
			matrices.scale(14, 14, 14);
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-5));
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
			DiffuseLighting.enableGuiShaderLighting();
			context.draw(vertexConsumerProvider -> EQUIPMENT_RENDERER.render(
					slot == EquipmentSlot.LEGS ? EquipmentModel.LayerType.HUMANOID_LEGGINGS : EquipmentModel.LayerType.HUMANOID,
					equippableComponent.assetId().orElse(EquipmentAssetKeys.IRON),
					model,
					stack,
					matrices,
					vertexConsumerProvider,
					15
			));
			DiffuseLighting.enableGuiDepthLighting();
			matrices.pop();
		}

		private static float setVisibleAndGetOffset(ArmorEntityModel<?> bipedModel, EquipmentSlot slot) {
			bipedModel.setVisible(false);
			switch (slot) {
				case HEAD:
					bipedModel.head.visible = true;
					bipedModel.hat.visible = true;
					return 4;
				case CHEST:
					bipedModel.body.visible = true;
					bipedModel.rightArm.visible = true;
					bipedModel.leftArm.visible = true;
					return -6;
				case LEGS:
					bipedModel.body.visible = true;
					bipedModel.rightLeg.visible = true;
					bipedModel.leftLeg.visible = true;
					return -14;
				case FEET:
					bipedModel.rightLeg.visible = true;
					bipedModel.leftLeg.visible = true;
					return -20;
			}
			return 0;
		}
	}

	public static final class Material extends TrimElementButton {
		public Material(Identifier element, ArmorTrimMaterial material, Consumer<TrimElementButton> onPress) {
			super(element, material.description(), onPress);

			// Find item that provides given material
			stack = Registries.ITEM.stream()
					.filter(item -> Optional.ofNullable(item.getComponents().get(DataComponentTypes.PROVIDES_TRIM_MATERIAL))
							.flatMap(c -> c.getMaterial(Utils.getRegistryWrapperLookup()))
							.map(provided -> provided.matchesId(element))
							.orElse(false)
					)
					.findAny()
					.map(ItemStack::new)
					.orElse(BARRIER);
		}

		@Override
		void draw(DrawContext context) {
			context.drawItem(stack, getX() + getWidth() / 2 - 8, getY() + getHeight() / 2 - 8);
		}
	}

	@Override
	public void onPress() {
		onPress.accept(this);
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}
