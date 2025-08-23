package de.hysky.skyblocker.skyblock.shortcut;

import com.mojang.serialization.Codec;
import de.hysky.skyblocker.annotations.GenEquals;
import de.hysky.skyblocker.annotations.GenHashCode;
import net.minecraft.client.util.InputUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Modified from {@link net.minecraft.client.option.KeyBinding}.
 */
public class ShortcutKeyBinding implements Comparable<ShortcutKeyBinding> {
	static final Codec<ShortcutKeyBinding> CODEC = Codec.STRING
			.xmap(InputUtil::fromTranslationKey, InputUtil.Key::getTranslationKey)
			.xmap(ShortcutKeyBinding::new, ShortcutKeyBinding::getBoundKey);
	private InputUtil.Key boundKey;

	ShortcutKeyBinding(InputUtil.Key boundKey) {
		this.boundKey = boundKey;
	}

	boolean isUnbound() {
		return boundKey.equals(InputUtil.UNKNOWN_KEY);
	}

	InputUtil.Key getBoundKey() {
		return boundKey;
	}

	void setBoundKey(InputUtil.Key boundKey) {
		this.boundKey = boundKey;
	}

	@Override
	@GenEquals
	public native boolean equals(Object o);

	@Override
	@GenHashCode
	public native int hashCode();

	@Override
	public int compareTo(@NotNull ShortcutKeyBinding shortcutKeyBinding) {
		return boundKey.getTranslationKey().compareTo(shortcutKeyBinding.boundKey.getTranslationKey());
	}
}
