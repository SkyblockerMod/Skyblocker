package de.hysky.skyblocker.mixins.accessors;

import net.minecraft.client.gui.components.Checkbox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Checkbox.class)
public interface CheckboxWidgetAccessor {
	@Accessor
	void setSelected(boolean checked);
}
