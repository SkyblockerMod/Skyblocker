package de.hysky.skyblocker.skyblock.dwarven.fossil;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;
import de.hysky.skyblocker.utils.chat.ChatPatternListener;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

public class FossilMuncher extends ChatPatternListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(FossilMuncher.class);

	private static final Map<String, String> answers;

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
		String answer = answers.getOrDefault(riddle, riddle);
		client.player.sendSystemMessage(Component.nullToEmpty("§e[NPC] §6Fossil Muncher§f: " + answer));
		return true;
	}

	static {
		answers = new HashMap<>();

		answers.put("lived underground and dug tunnels", "Claw Fossil");
		answers.put("had a really fancy tail", "Clubbed Fossil");
		answers.put("lived underwater", "Helix Fossil");
		answers.put("was kinda spiny u know", "Spine Fossil");
		answers.put("lived in herds and was quite woolly", "Tusk Fossil");
		answers.put("is pretty rough to look at", "Ugly Fossil");
		answers.put("had a really pointy beak", "Webbed Fossil");
	}
}
