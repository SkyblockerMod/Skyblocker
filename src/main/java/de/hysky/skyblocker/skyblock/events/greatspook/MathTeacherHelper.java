package de.hysky.skyblocker.skyblock.events.greatspook;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.Calculator;
import de.hysky.skyblocker.utils.Constants;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>A helper class for the Math Teachers that can spawn after killing Primal Fears in the Great Spook event.</p>
 * <p>It only shows the result and allows for easily copying rather than sending the result to chat to not breach any hypixel rules.</p>
 */
public final class MathTeacherHelper {
	private static final Pattern MATH_TEACHER_PATTERN = Pattern.compile("^QUICK MATHS! Solve: (.*)");

	@Init
	public static void init() {
		ClientReceiveMessageEvents.MODIFY_GAME.register(MathTeacherHelper::onMessage);
	}

	/**
	 * Appends the result of the math expression to the message and a copy to clipboard text for, well, copying the result to the clipboard.
	 */
	public static Text onMessage(Text message, boolean overlay) {
		if (overlay) return message;
		Matcher matcher = MATH_TEACHER_PATTERN.matcher(message.getString());
		if (!matcher.matches()) return message;
		String expression = matcher.group(1).replace("x", "*"); // Hypixel uses x for multiplication while our calculator uses *
		String result = "%.0f".formatted(Calculator.calculate(expression));
		return ((MutableText) message).append(" = ")
		                              .append(Text.literal(result)
		                                          .formatted(Formatting.AQUA))
		                              .append(ScreenTexts.SPACE)
		                              .append(Text.translatable("text.skyblocker.clickToCopy")
		                                          .formatted(Formatting.GREEN)
		                                          .styled(style ->
				                                          style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, result))
				                                               .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Constants.PREFIX.get().append(Text.translatable("text.skyblocker.clickToCopy.@Tooltip"))))
		                                          ));
	}
}
