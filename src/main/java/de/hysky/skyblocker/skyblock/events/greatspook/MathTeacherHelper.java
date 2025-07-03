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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * <p>A helper class for the Math Teachers that can spawn after killing Primal Fears in the Great Spook event.</p>
 */
public final class MathTeacherHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(MathTeacherHelper.class);

	@Init
	public static void init() {
		ClientReceiveMessageEvents.MODIFY_GAME.register(MathTeacherHelper::onMessage);
	}

	/**
	 * Appends the result of the math expression to the message and a send in chat text that, well, sends the result in chat.
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
			              .append(Text.translatable("text.skyblocker.clickToSend")
			                          .formatted(Formatting.GREEN)
			                          .styled(style ->
					                          style.withClickEvent(new ClickEvent.RunCommand("/ac " + result))
					                               .withHoverEvent(new HoverEvent.ShowText(Constants.PREFIX.get().append(Text.translatable("text.skyblocker.clickToSend.@Tooltip"))))
			                          ));
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Math Teacher Helper] Failed to calculate math expression: {}", expression, e);
			return message;
		}
	}
}
