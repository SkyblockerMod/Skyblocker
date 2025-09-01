package de.hysky.skyblocker.skyblock.shortcut;

import com.google.common.collect.Comparators;
import com.mojang.serialization.Codec;
import de.hysky.skyblocker.annotations.GenEquals;
import de.hysky.skyblocker.annotations.GenHashCode;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Modified from {@link net.minecraft.client.option.KeyBinding}.
 */
public class ShortcutKeyBinding implements Comparable<ShortcutKeyBinding> {
	static final Codec<ShortcutKeyBinding> CODEC = Codec.STRING.xmap(
			s -> Arrays.stream(s.split(" \\+ ")).map(InputUtil::fromTranslationKey).toList(),
			l -> l.stream().map(InputUtil.Key::getTranslationKey).collect(Collectors.joining(" + "))
	).xmap(ShortcutKeyBinding::new, ShortcutKeyBinding::getBoundKeys);
	public static final Comparator<ShortcutKeyBinding> COMPARATOR = Comparator.comparing(ShortcutKeyBinding::getBoundKeysTranslationKey, Comparators.lexicographical(String::compareTo));
	private final List<InputUtil.Key> boundKeys;

	ShortcutKeyBinding(List<InputUtil.Key> boundKeys) {
		this.boundKeys = new ArrayList<>(boundKeys);
	}

	ShortcutKeyBinding copy() {
		return new ShortcutKeyBinding(boundKeys);
	}

	boolean isUnbound() {
		return boundKeys.isEmpty() || boundKeys.equals(List.of(InputUtil.UNKNOWN_KEY));
	}

	List<InputUtil.Key> getBoundKeys() {
		return boundKeys;
	}

	void clearBoundKeys() {
		boundKeys.clear();
	}

	void addBoundKey(InputUtil.Key boundKey) {
		if (!boundKeys.contains(boundKey)) {
			boundKeys.add(boundKey);
		}
	}

	Text getBoundKeysText() {
		MutableText boundKeysText = Text.empty();
		for (InputUtil.Key boundKey : boundKeys) {
			boundKeysText.append(boundKey.getLocalizedText()).append(" + ");
		}
		if (!boundKeysText.getSiblings().isEmpty()) boundKeysText.getSiblings().removeLast();
		return boundKeysText;
	}

	List<String> getBoundKeysTranslationKey() {
		return boundKeys.stream().map(InputUtil.Key::getTranslationKey).toList();
	}

	@Override
	@GenEquals
	public native boolean equals(Object o);

	@Override
	@GenHashCode
	public native int hashCode();

	@Override
	public int compareTo(@NotNull ShortcutKeyBinding shortcutKeyBinding) {
		return COMPARATOR.compare(this, shortcutKeyBinding);
	}
}
