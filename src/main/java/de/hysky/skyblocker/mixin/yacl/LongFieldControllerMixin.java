package de.hysky.skyblocker.mixin.yacl;

import java.text.NumberFormat;
import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.gui.controllers.string.number.LongFieldController;
import dev.isxander.yacl3.gui.controllers.string.number.NumberFieldController;
import net.minecraft.text.Text;

@Mixin(value = LongFieldController.class, remap = false)
public abstract class LongFieldControllerMixin extends NumberFieldController<Long> {
	private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

	public LongFieldControllerMixin(Option<Long> option, Function<Long, Text> displayFormatter) {
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
