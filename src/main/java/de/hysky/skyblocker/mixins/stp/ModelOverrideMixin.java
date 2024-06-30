package de.hysky.skyblocker.mixins.stp;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import de.hysky.skyblocker.injected.SkyblockerModelOverrides;
import de.hysky.skyblocker.stp.predicates.SkyblockerTexturePredicate;
import net.minecraft.client.render.model.json.ModelOverride;
import net.minecraft.client.render.model.json.ModelOverrideList;

@Mixin(value = { ModelOverride.class, ModelOverrideList.BakedOverride.class })
public class ModelOverrideMixin implements SkyblockerModelOverrides {
	@Unique
	private SkyblockerTexturePredicate[] predicates;

	@Override
	public void setItemPredicates(SkyblockerTexturePredicate[] predicates) {
		this.predicates = predicates;
	}

	@Override
	@Nullable
	public SkyblockerTexturePredicate[] getItemPredicates() {
		return this.predicates;
	}
}
