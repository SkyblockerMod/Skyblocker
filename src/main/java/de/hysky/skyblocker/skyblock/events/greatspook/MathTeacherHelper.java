package de.hysky.skyblocker.skyblock.events.greatspook;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.Calculator;
import de.hysky.skyblocker.utils.Constants;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * <p>A helper class for the Math Teachers that can spawn after killing Primal Fears in the Great Spook event.</p>
 * <p>It only shows the result and allows for easily copying rather than sending the result to chat to not breach any hypixel rules.</p>
 */
public final class MathTeacherHelper {
	@Init
	public static void init() {
		ClientReceiveMessageEvents.MODIFY_GAME.register(MathTeacherHelper::onMessage);
	}

	/**
	 * Appends the result of the math expression to the message and a copy to clipboard text for, well, copying the result to the clipboard.
	 */
	public static Text onMessage(Text message, boolean overlay) {
		if (overlay) return message;
		List<Text> siblings = message.getSiblings();
		if (message.getContent() != PlainTextContent.EMPTY || siblings.size() != 3) return message;
		if (!siblings.getFirst().getString().equals("QUICK MATHS! ")) return message;

		String expression = siblings.get(2).getString().replace('x', '*'); // Hypixel uses x for multiplication while our calculator uses *
		try {
			String result = "%.0f".formatted(Calculator.calculate(expression));

			return message.copy()
			              .append(" = ")
			              .append(Text.literal(result)
			                          .formatted(Formatting.AQUA))
			              .append(ScreenTexts.SPACE)
			              .append(Text.translatable("text.skyblocker.clickToSuggest")
			                          .formatted(Formatting.GREEN)
			                          .styled(style ->
					                          style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, result))
					                               .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Constants.PREFIX.get().append(Text.translatable("text.skyblocker.clickToSuggest.@Tooltip"))))
			                          ));
		} catch (Exception e) {
			return message;
		}
	}
}
