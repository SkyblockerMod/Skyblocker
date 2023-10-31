package de.hysky.skyblocker.mixin.yacl;

import java.text.NumberFormat;
import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.gui.controllers.string.number.FloatFieldController;
import dev.isxander.yacl3.gui.controllers.string.number.NumberFieldController;
import net.minecraft.text.Text;

@Mixin(value = FloatFieldController.class, remap = false)
public abstract class FloatFieldControllerMixin extends NumberFieldController<Float> {
	private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

	
	public FloatFieldControllerMixin(Option<Float> option, Function<Float, Text> displayFormatter) {
		super(option, displayFormatter);
	}

	@Overwrite
	public String getString() {
		return NUMBER_FORMAT.format(option().pendingValue());
	}
}
