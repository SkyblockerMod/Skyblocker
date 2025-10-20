package de.hysky.skyblocker.skyblock.end;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TheEndBestiaryUpdater extends SimpleContainerSolver {
	private static final Pattern KILLS_PATTERN = Pattern.compile("Kills: ([0-9,]+)");

	public TheEndBestiaryUpdater() {
		super("The End ➜ Zealot");
	}

	@Override
	public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
		// Grab the total kills from the "title" entry (#4), and special zealot kills from its entry (#22)
		slots.int2ObjectEntrySet().stream().filter(entry -> entry.getIntKey() == 4 || entry.getIntKey() == 22)
				.map(Int2ObjectMap.Entry::getValue).filter(stack -> stack.isOf(Items.ENDER_CHEST)).forEach((stack) -> {
					Matcher matcher = ItemUtils.getLoreLineIfMatch(stack, KILLS_PATTERN);
					if (matcher == null) return;
					String killsStr = matcher.group(1).replace(",", "");
					if (!NumberUtils.isCreatable(killsStr)) return;

					int kills = NumberUtils.toInt(killsStr);
					var stats = TheEnd.PROFILES_STATS.computeIfAbsent(TheEnd.EndStats.EMPTY);
					assert stats != null;

					String name = stack.getName().getString();
					if (name.startsWith("Zealot")) {
						TheEnd.PROFILES_STATS.put(new TheEnd.EndStats(kills, stats.zealotsSinceLastEye(), stats.eyes()));
					} else if (name.contains("Special")) {
						TheEnd.PROFILES_STATS.put(new TheEnd.EndStats(stats.totalZealotKills(), stats.zealotsSinceLastEye(), kills));
					}
					EndHudWidget.getInstance().update();
				});
		return List.of();
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().otherLocations.end.zealotKillsEnabled;
	}
}
