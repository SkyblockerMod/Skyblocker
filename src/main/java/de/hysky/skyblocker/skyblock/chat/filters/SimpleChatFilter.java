package de.hysky.skyblocker.skyblock.chat.filters;

import de.hysky.skyblocker.utils.chat.ChatPatternListener;
import org.intellij.lang.annotations.Language;

import java.util.regex.Matcher;
import net.minecraft.network.chat.Component;

public abstract class SimpleChatFilter extends ChatPatternListener {
	protected SimpleChatFilter(@Language("RegExp") String pattern) {
		super(pattern);
	}

	@Override
	protected final boolean onMatch(Component message, Matcher matcher) {
		return true;
	}
}
