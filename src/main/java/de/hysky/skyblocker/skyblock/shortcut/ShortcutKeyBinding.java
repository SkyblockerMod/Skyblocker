package de.hysky.skyblocker.skyblock.shortcut;

import com.mojang.serialization.Codec;
import net.minecraft.client.util.InputUtil;

public class ShortcutKeyBinding {
	static final Codec<ShortcutKeyBinding> CODEC = Codec.STRING
			.xmap(InputUtil::fromTranslationKey, InputUtil.Key::getTranslationKey)
			.xmap(ShortcutKeyBinding::new, ShortcutKeyBinding::getBoundKey);
	private final InputUtil.Key boundKey;

	private ShortcutKeyBinding(InputUtil.Key boundKey) {
		this.boundKey = boundKey;
	}

	private InputUtil.Key getBoundKey() {
		return boundKey;
	}
}
