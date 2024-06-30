package de.hysky.skyblocker.mixins.stp;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.widget.EntryListWidget;

@Mixin(EntryListWidget.class)
public interface EntryListWidgetInvoker {

	@Invoker("isScrollbarVisible")
	boolean invokeIsScrollbarVisible();
}
