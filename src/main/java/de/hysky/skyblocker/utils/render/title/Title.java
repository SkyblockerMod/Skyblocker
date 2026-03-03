package de.hysky.skyblocker.utils.render.title;

import com.demonwav.mcdev.annotations.Translatable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Represents a title used for {@link TitleContainer}.
 *
 * @see TitleContainer
 */
public class Title {
	private MutableComponent text;
	protected float x = -1;
	protected float y = -1;

	/**
	 * Constructs a new title with the given translation key and formatting to be applied.
	 *
	 * @param textKey    the translation key
	 * @param formatting the formatting to be applied to the text
	 */
	public Title(@Translatable String textKey, ChatFormatting formatting) {
		this(Component.translatable(textKey).withStyle(formatting));
	}

	/**
	 * Constructs a new title with the given {@link MutableComponent}.
	 * Use {@link Component#literal(String)} or {@link Component#translatable(String)} to create a {@link MutableComponent}
	 *
	 * @param text the mutable text
	 */
	public Title(MutableComponent text) {
		this.text = text;
	}

	public MutableComponent getText() {
		return text;
	}

	public Title setText(MutableComponent text) {
		this.text = text;

		return this;
	}

	protected boolean isDefaultPos() {
		return x == -1 && y == -1;
	}

	protected void resetPos() {
		this.x = -1;
		this.y = -1;
	}
}
