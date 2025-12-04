package de.hysky.skyblocker.mixins.accessors;

import net.minecraft.client.gui.widget.CheckboxWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CheckboxWidget.class)
public interface CheckboxWidgetAccessor {
	@Accessor
	void setChecked(boolean checked);
}
