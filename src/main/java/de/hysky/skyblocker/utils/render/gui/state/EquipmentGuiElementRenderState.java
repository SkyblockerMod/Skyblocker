package de.hysky.skyblocker.utils.render.gui.state;

import org.jetbrains.annotations.Nullable;

import de.hysky.skyblocker.utils.render.gui.special.EquipmentGuiElementRenderer;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.registry.RegistryKey;

public record EquipmentGuiElementRenderState(
		EquipmentRenderer equipmentRenderer,
		EquipmentModel.LayerType layerType,
		RegistryKey<EquipmentAsset> assetKey,
		Model model,
		ItemStack stack,
		int x1,
		int y1,
		int x2,
		int y2,
		float rotation,
		float scale,
		float offset,
		@Nullable ScreenRect scissorArea,
		@Nullable ScreenRect bounds
		) implements InstancedGuiElementRenderState {
	public EquipmentGuiElementRenderState(
			EquipmentRenderer equipmentRenderer,
			EquipmentModel.LayerType layerType,
			RegistryKey<EquipmentAsset> assetKey,
			Model model,
			ItemStack stack,
			int x1,
			int y1,
			int x2,
			int y2,
			float rotation,
			float scale,
			float offset,
			@Nullable ScreenRect scissorArea
			) {
		this(equipmentRenderer, layerType, assetKey, model, stack, x1, y1, x2, y2, rotation, scale, offset, scissorArea, SpecialGuiElementRenderState.createBounds(x1, y1, x2, y2, scissorArea));
	}

	@Override
	public EquipmentGuiElementRenderer newRenderer(Immediate vertexConsumers) {
		return new EquipmentGuiElementRenderer(vertexConsumers);
	}
}
