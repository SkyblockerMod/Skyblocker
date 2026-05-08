package de.hysky.skyblocker.skyblock.dwarven.fossil;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;
import de.hysky.skyblocker.utils.chat.ChatPatternListener;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.regex.Matcher;

public class FossilMuncher extends ChatPatternListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(FossilMuncher.class);

	private static final Map<String, String> ANSWERS = Map.of(
		"lived underground and dug tunnels", "Claw Fossil",
		"had a really fancy tail", "Clubbed Fossil",
		"was the king of his kind", "Footprint Fossil",
		"lived underwater", "Helix Fossil",
		"was kinda spiny u know", "Spine Fossil",
		"lived in herds and was quite woolly", "Tusk Fossil",
		"is pretty rough to look at", "Ugly Fossil",
		"had a really pointy beak", "Webbed Fossil"
	);

	public FossilMuncher() {
		super("^\\[NPC] Fossil Muncher: the fossil i want ([a-zA-Z, \\-]*)$");
	}

	@Override
	public ChatFilterResult state() {
		return SkyblockerConfigManager.get().mining.glacite.solveFossilMuncher ? ChatFilterResult.FILTER : ChatFilterResult.PASS;
	}

	@Override
	public boolean onMatch(Component message, Matcher matcher) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) return false;
		LOGGER.info("Original Fossil Muncher message: {}", message.getString());
		String riddle = matcher.group(1);
		String answer = ANSWERS.getOrDefault(riddle, riddle);
		client.player.sendSystemMessage(Component.nullToEmpty("§e[NPC] §6Fossil Muncher§f: " + answer));
		return true;
	}
}
