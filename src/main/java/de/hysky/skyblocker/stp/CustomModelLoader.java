package de.hysky.skyblocker.stp;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public abstract class CustomModelLoader implements PreparableModelLoadingPlugin<Collection<Identifier>>, ModelModifier.OnLoad {
	protected CustomModelLoader() {
		PreparableModelLoadingPlugin.register(this::prepareModelsInternal, this);
	}

	@Override
	public void onInitializeModelLoader(Collection<Identifier> data, ModelLoadingPlugin.Context pluginContext) {
		pluginContext.addModels(data);
		pluginContext.modifyModelOnLoad().register(this);
	}

	private CompletableFuture<Collection<Identifier>> prepareModelsInternal(ResourceManager manager, Executor executor) {
		return CompletableFuture.supplyAsync(() -> prepareModels(manager), executor);
	}

	protected abstract Collection<Identifier> prepareModels(ResourceManager manager);

	@Override
	public UnbakedModel modifyModelOnLoad(UnbakedModel model, ModelModifier.OnLoad.Context context) {
		return model;
	}
}
