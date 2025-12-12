package de.hysky.skyblocker.utils.render.gui.state;

import org.jspecify.annotations.Nullable;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;

public record EquipmentGuiElementRenderState<S>(
		EquipmentLayerRenderer equipmentRenderer,
		EquipmentClientInfo.LayerType layerType,
		ResourceKey<EquipmentAsset> assetKey,
		Model<S> model,
		S state,
		ItemStack stack,
		int x1,
		int y1,
		int x2,
		int y2,
		float rotation,
		float scale,
		float offset,
		@Nullable ScreenRectangle scissorArea,
		@Nullable ScreenRectangle bounds
		) implements PictureInPictureRenderState {
	public EquipmentGuiElementRenderState(
			EquipmentLayerRenderer equipmentRenderer,
			EquipmentClientInfo.LayerType layerType,
			ResourceKey<EquipmentAsset> assetKey,
			Model<S> model,
			S state,
			ItemStack stack,
			int x1,
			int y1,
			int x2,
			int y2,
			float rotation,
			float scale,
			float offset,
			@Nullable ScreenRectangle scissorArea
			) {
		this(equipmentRenderer, layerType, assetKey, model, state, stack, x1, y1, x2, y2, rotation, scale, offset, scissorArea, PictureInPictureRenderState.getBounds(x1, y1, x2, y2, scissorArea));
	}
}
