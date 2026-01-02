package de.hysky.skyblocker.skyblock.item.custom.screen;

import de.hysky.skyblocker.mixins.accessors.EntityRenderDispatcherAccessor;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.HudHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.AtlasIds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.minecraft.world.item.equipment.trim.TrimPattern;

public abstract sealed class TrimElementButton extends AbstractButton permits TrimElementButton.Pattern, TrimElementButton.Material {
	private static final ItemStack BARRIER = new ItemStack(Items.BARRIER);
	protected final @Nullable Identifier element;
	protected ItemStack stack;
	private final Consumer<TrimElementButton> onPress;

	public TrimElementButton(@Nullable Identifier element, Component name, Consumer<TrimElementButton> onPress) {
		super(0, 0, 20, 20, name);
		this.element = element;
		this.onPress = onPress;
		setTooltip(Tooltip.create(getMessage()));
	}

	public @Nullable Identifier getElement() {
		return element;
	}

	@Override
	public void setMessage(Component message) {
		super.setMessage(message);
		setTooltip(Tooltip.create(getMessage()));
	}

	@Override
	public void renderString(GuiGraphics context, Font textRenderer, int color) {
		draw(context);
	}

	abstract void draw(GuiGraphics context);

	public static final class Pattern extends TrimElementButton {
		private static final int DEFAULT_ROTATION = 15;
		private static ArmorModelSet<? extends HumanoidModel<?>> equipmentModelData = null;
		private static EquipmentLayerRenderer equipmentRenderer = null;

		private final ArmorTrim trim;
		private Equippable equippableComponent;
		private float rotation = DEFAULT_ROTATION;

		public Pattern(@Nullable Identifier element, @Nullable TrimPattern pattern, Consumer<TrimElementButton> onPress) {
			super(element, pattern == null ? Component.translatable("gui.none") : pattern.description(), onPress);
			if (element == null) {
				trim = null;
				return;
			}
			if (equipmentModelData == null) {
				equipmentModelData = ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, Minecraft.getInstance().getEntityModels(), modelPart -> new PlayerModel(modelPart, false));
				equipmentRenderer = new EquipmentLayerRenderer(
						((EntityRenderDispatcherAccessor) Minecraft.getInstance().getEntityRenderDispatcher()).getEquipmentAssets(),
						Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.ARMOR_TRIMS));
			}

			trim = new ArmorTrim(
					Utils.getRegistryWrapperLookup().lookupOrThrow(Registries.TRIM_MATERIAL).getOrThrow(TrimMaterials.QUARTZ),
					Holder.direct(pattern));
		}

		public void setStack(ItemStack newStack) {
			newStack = newStack.copy();
			// Remove the uuid so it doesn't render with the selected trim
			CompoundTag nbtCopy = ItemUtils.getCustomData(newStack).copy();
			nbtCopy.remove(ItemUtils.UUID);
			newStack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbtCopy));
			newStack.set(DataComponents.TRIM, trim);

			equippableComponent = newStack.get(DataComponents.EQUIPPABLE);
			if (equippableComponent == null) throw new IllegalArgumentException("Trimmed stack must contain an equippable component");

			stack = newStack;
		}

		@Override
		void draw(GuiGraphics context) {
			if (trim == null) {
				context.renderItem(BARRIER, getX() + getWidth() / 2 - 8, getY() + getHeight() / 2 - 8);
				return;
			}
			if (isHovered()) {
				rotation += Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks() * 0.05f * 90;
				rotation %= 360;
			} else rotation = DEFAULT_ROTATION;

			EquipmentSlot slot = equippableComponent.slot();
			@SuppressWarnings("unchecked")
			HumanoidModel<AvatarRenderState> model = (HumanoidModel<AvatarRenderState>) equipmentModelData.get(slot);
			AvatarRenderState state = new AvatarRenderState();
			EquipmentClientInfo.LayerType layerType = slot == EquipmentSlot.LEGS ? EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS : EquipmentClientInfo.LayerType.HUMANOID;
			float offset = setVisibleAndGetOffset(model, slot);

			HudHelper.drawEquipment(context, equipmentRenderer, layerType, equippableComponent.assetId().orElse(EquipmentAssets.IRON), model, state, stack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), rotation, 14, offset);
		}

		@SuppressWarnings("incomplete-switch")
		private static float setVisibleAndGetOffset(HumanoidModel<?> bipedModel, EquipmentSlot slot) {
			bipedModel.setAllVisible(false);
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
		public Material(Identifier element, TrimMaterial material, Consumer<TrimElementButton> onPress) {
			super(element, material.description(), onPress);

			// Find item that provides given material
			stack = BuiltInRegistries.ITEM.stream()
					.filter(item -> Optional.ofNullable(item.components().get(DataComponents.PROVIDES_TRIM_MATERIAL))
							.flatMap(c -> c.unwrap(Utils.getRegistryWrapperLookup()))
							.map(provided -> provided.is(element))
							.orElse(false)
					)
					.findAny()
					.map(ItemStack::new)
					.orElse(BARRIER);
		}

		@Override
		void draw(GuiGraphics context) {
			context.renderItem(stack, getX() + getWidth() / 2 - 8, getY() + getHeight() / 2 - 8);
		}
	}

	@Override
	public void onPress(InputWithModifiers input) {
		onPress.accept(this);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {}
}
