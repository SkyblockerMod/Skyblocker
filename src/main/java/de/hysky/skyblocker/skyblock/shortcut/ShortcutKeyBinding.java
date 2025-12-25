package de.hysky.skyblocker.skyblock.shortcut;

import com.google.common.collect.Comparators;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.Codec;
import de.hysky.skyblocker.annotations.GenEquals;
import de.hysky.skyblocker.annotations.GenHashCode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Modified from {@link net.minecraft.client.KeyMapping}.
 */
public class ShortcutKeyBinding implements Comparable<ShortcutKeyBinding> {
	static final Codec<ShortcutKeyBinding> CODEC = Codec.STRING.xmap(
			s -> Arrays.stream(s.split(" \\+ ")).map(InputConstants::getKey).toList(),
			l -> l.stream().map(InputConstants.Key::getName).collect(Collectors.joining(" + "))
	).xmap(ShortcutKeyBinding::new, ShortcutKeyBinding::getBoundKeys);
	public static final Comparator<ShortcutKeyBinding> COMPARATOR = Comparator.comparing(ShortcutKeyBinding::getBoundKeysTranslationKey, Comparators.lexicographical(String::compareTo));
	private final List<InputConstants.Key> boundKeys;

	ShortcutKeyBinding(List<InputConstants.Key> boundKeys) {
		this.boundKeys = new ArrayList<>(boundKeys);
	}

	ShortcutKeyBinding copy() {
		return new ShortcutKeyBinding(boundKeys);
	}

	boolean isUnbound() {
		return boundKeys.isEmpty() || boundKeys.equals(List.of(InputConstants.UNKNOWN));
	}

	List<InputConstants.Key> getBoundKeys() {
		return boundKeys;
	}

	void clearBoundKeys() {
		boundKeys.clear();
	}

	void addBoundKey(InputConstants.Key boundKey) {
		if (!boundKeys.contains(boundKey)) {
			boundKeys.add(boundKey);
		}
	}

	Component getBoundKeysText() {
		MutableComponent boundKeysText = Component.empty();
		for (InputConstants.Key boundKey : boundKeys) {
			boundKeysText.append(boundKey.getDisplayName()).append(" + ");
		}
		if (!boundKeysText.getSiblings().isEmpty()) boundKeysText.getSiblings().removeLast();
		return boundKeysText;
	}

	List<String> getBoundKeysTranslationKey() {
		return boundKeys.stream().map(InputConstants.Key::getName).toList();
	}

	@Override
	@GenEquals
	public native boolean equals(Object o);

	@Override
	@GenHashCode
	public native int hashCode();

	@Override
	public int compareTo(ShortcutKeyBinding shortcutKeyBinding) {
		return COMPARATOR.compare(this, shortcutKeyBinding);
	}
}
