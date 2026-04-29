package de.hysky.skyblocker.skyblock.tabhud.widget.element;


import com.demonwav.mcdev.annotations.Translatable;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public interface ElementCollector {
	<E extends Element> E addElement(E element);
	default void addSimpleIcoText(@Nullable FlexibleItemStack ico, String string, ChatFormatting fmt, int idx) {
		Component txt = simpleEntryText(idx, string, fmt);
		this.addElement(Elements.iconTextComponent(ico, txt));
	}

	default void addSimpleIcoText(@Nullable FlexibleItemStack ico, String string, ChatFormatting fmt, String content) {
		Component txt = simpleEntryText(content, string, fmt);
		this.addElement(Elements.iconTextComponent(ico, txt));
	}

	default void addSimpleIconTranslatableText(@Nullable FlexibleItemStack icon, @Translatable String translationKey, ChatFormatting formatting, String content) {
		Component text = simpleEntryTranslatableText(translationKey, content, formatting);
		this.addElement(Elements.iconTextComponent(icon, text));
	}

	default void addSimpleIconTranslatableText(FlexibleItemStack icon, @Translatable String translationKey, ChatFormatting formatting, Component content) {
		Component text = simpleEntryTranslatableText(translationKey, content, formatting);
		this.addElement(Elements.iconTextComponent(icon, text));
	}
	/**
	 * If the entry at idx has the format "[textA]: [textB]", the following is
	 * returned:
	 * [entryName] [textB.formatted(contentFmt)]
	 */
	static @Nullable Component simpleEntryText(int idx, String entryName, ChatFormatting contentFmt) {

		String src = PlayerListManager.strAt(idx);

		if (src == null) {
			return null;
		}

		int cidx = src.indexOf(':');
		if (cidx == -1) {
			return null;
		}

		src = src.substring(src.indexOf(':') + 1);
		return simpleEntryText(src, entryName, contentFmt);
	}

	/**
	 * @return [entryName] [entryContent.formatted(contentFmt)]
	 */
	static Component simpleEntryText(String entryContent, String entryName, ChatFormatting contentFmt) {
		return Component.literal(entryName).append(Component.literal(entryContent).withStyle(contentFmt));
	}

	static Component simpleEntryTranslatableText(String translationKey, String content, ChatFormatting contentFormatting) {
		return Component.translatable(translationKey, Component.literal(content).withStyle(contentFormatting));
	}

	static Component simpleEntryTranslatableText(String translationKey, Component content, ChatFormatting contentFormatting) {
		return Component.translatable(translationKey, content.copy().withStyle(contentFormatting));
	}


	class ElementCollection implements ElementCollector {
		private final List<Element> elements = new ArrayList<>();

		@Override
		public <E extends Element> E addElement(E element) {
			elements.add(element);
			return element;
		}

		public List<Element> getElements() {
			return elements;
		}
	}
}
