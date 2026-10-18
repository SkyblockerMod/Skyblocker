package de.hysky.skyblocker.mixins.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.MultiLineTextWidget;

@Mixin(Checkbox.class)
public interface CheckboxAccessor {
	@Accessor
	void setSelected(boolean checked);

	@Accessor
	MultiLineTextWidget getTextWidget();
}
