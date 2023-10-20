package de.hysky.skyblocker.mixin.yacl;

import java.text.NumberFormat;
import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.gui.controllers.string.number.DoubleFieldController;
import dev.isxander.yacl3.gui.controllers.string.number.NumberFieldController;
import net.minecraft.text.Text;

@Mixin(value = DoubleFieldController.class, remap = false)
public abstract class DoubleFieldControllerMixin extends NumberFieldController<Double> {
	private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();


	public DoubleFieldControllerMixin(Option<Double> option, Function<Double, Text> displayFormatter) {
		super(option, displayFormatter);
	}

	@Overwrite
	public String getString() {
		return NUMBER_FORMAT.format(option().pendingValue());
	}
}
