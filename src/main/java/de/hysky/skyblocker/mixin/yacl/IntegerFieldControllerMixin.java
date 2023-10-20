package de.hysky.skyblocker.mixin.yacl;

import java.text.NumberFormat;
import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.gui.controllers.string.number.IntegerFieldController;
import dev.isxander.yacl3.gui.controllers.string.number.NumberFieldController;
import net.minecraft.text.Text;

@Mixin(value = IntegerFieldController.class, remap = false)
public abstract class IntegerFieldControllerMixin extends NumberFieldController<Integer> {
	private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();
	
	public IntegerFieldControllerMixin(Option<Integer> option, Function<Integer, Text> displayFormatter) {
		super(option, displayFormatter);
	}
	
	@Overwrite
	public String getString() {
		return NUMBER_FORMAT.format(option().pendingValue());
	}
	
	@Overwrite
	public boolean isInputValid(String input) {
		return super.isInputValid(input);
	}
}
