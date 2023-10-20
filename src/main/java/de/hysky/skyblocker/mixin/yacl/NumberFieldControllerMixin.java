package de.hysky.skyblocker.mixin.yacl;

import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import dev.isxander.yacl3.gui.controllers.slider.ISliderController;
import dev.isxander.yacl3.gui.controllers.string.number.NumberFieldController;
import dev.isxander.yacl3.impl.utils.YACLConstants;
import net.minecraft.util.math.MathHelper;

@Mixin(value = NumberFieldController.class, remap = false)
public abstract class NumberFieldControllerMixin<T extends Number> implements ISliderController<T> {
	private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();
	private static final DecimalFormatSymbols DECIMAL_FORMAT_SYMBOLS = DecimalFormatSymbols.getInstance();
	
	@Overwrite
	public void setFromString(String value) {
		try {
			setPendingValue(MathHelper.clamp(NUMBER_FORMAT.parse(value).doubleValue(), min(), max()));
		} catch (ParseException ignore) {
			YACLConstants.LOGGER.warn("Failed to parse number: {}", value);
		}
	}
	
	@Overwrite
	public boolean isInputValid(String input) {
		input = input.replace(DECIMAL_FORMAT_SYMBOLS.getGroupingSeparator() + "", "");
		ParsePosition parsePosition = new ParsePosition(0);
		NUMBER_FORMAT.parse(input, parsePosition);
		
		return parsePosition.getIndex() == input.length();
	}
	
	@Overwrite
	protected String cleanupNumberString(String number) {
		throw new UnsupportedOperationException("This method should no longer be called.");
	}
}
