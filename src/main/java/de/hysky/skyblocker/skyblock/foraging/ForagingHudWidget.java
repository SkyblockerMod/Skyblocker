package de.hysky.skyblocker.skyblock.foraging;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.utils.BazaarProduct;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Set;

@RegisterWidget
public class ForagingHudWidget extends ComponentBasedWidget {
	private static final MutableText TITLE = Text.literal("Foraging").formatted(Formatting.DARK_GREEN, Formatting.BOLD);
	private final MinecraftClient client = MinecraftClient.getInstance();

	private static ForagingHudWidget instance = null;

	public static ForagingHudWidget getInstance() {
		if (instance == null) instance = new ForagingHudWidget();
		return instance;
	}

	public ForagingHudWidget() {
		super(TITLE, Formatting.DARK_GREEN.getColorValue(), "hud_foraging");
		instance = this;
		update();
	}

	@Override
	protected boolean shouldUpdateBeforeRendering() {
		return true;
	}

	@Override
	public void updateContent() {
		if (client.player == null) {
			addComponent(new PlainTextComponent(Text.literal("No player :(")));
			return;
		}

		addSimpleIcoText(Ico.OAK_LOG, ForagingHud.counterText(), Formatting.GREEN, ForagingHud.NUMBER_FORMAT.format(ForagingHud.counter()));
		addSimpleIcoText(Ico.OAK_LOG, "Logs/min: ", Formatting.GREEN, ForagingHud.NUMBER_FORMAT.format((int) ForagingHud.logsPerMinute()));
		addSimpleIcoText(Ico.GOLD, "Coins/h: ", Formatting.GOLD, estimateCoinsPerHour());
		addSimpleIcoText(Ico.OAK_LOG, "Blocks/s: ", Formatting.GREEN, String.format("%.2f", ForagingHud.blockBreaks()));
		addComponent(Components.progressComponent(Ico.EXP_BOTTLE, Text.literal("Foraging Level:"), ForagingHud.foragingXpPercentProgress(), Formatting.GOLD.getColorValue()));
		addSimpleIcoText(Ico.LIME_DYE, "Foraging XP/h: ", Formatting.GREEN, ForagingHud.NUMBER_FORMAT.format((int) ForagingHud.foragingXpPerHour()));
	}

	private String estimateCoinsPerHour() {
		String lastId = ForagingHud.getLastLogSkyblockId();
		if (lastId == null) {
			return "—";
		}

		// ─── 1) NPC price lookup ─────────────────────────────────────────────────────────
		double npcPrice = 0.0;
		if (TooltipInfoType.NPC.hasOrNullWarning(lastId)) {
			npcPrice = TooltipInfoType.NPC.getData().getDouble(lastId);
		}

		// ─── 2) Bazaar price lookup ──────────────────────────────────────────────────────
		double bazaarPrice = 0.0;
		if (TooltipInfoType.BAZAAR.hasOrNullWarning(lastId)) {
			var bazaarMap = TooltipInfoType.BAZAAR.getData(); // Object2ObjectMap<String, BazaarProduct>
			BazaarProduct product = bazaarMap.get(lastId);
			if (product != null) {
				// Use sellPrice (i.e. price at which you can sell into Bazaar). Fallback to 0 if absent.
				bazaarPrice = product.sellPrice().orElse(0.0);
			}
		}

		// ─── 3) Auction House “Lowest BIN” lookup ───────────────────────────────────────
		double lowestBin = 0.0;
		if (TooltipInfoType.LOWEST_BINS.hasOrNullWarning(lastId)) {
			lowestBin = TooltipInfoType.LOWEST_BINS.getData().getDouble(lastId);
		}

		// ─── 4) Choose price: prefer Bazaar sellPrice > Auction House > NPC ─────────────
		double pricePerLog;
		if (bazaarPrice > 0) {
			pricePerLog = bazaarPrice;
		} else if (lowestBin > 0) {
			pricePerLog = lowestBin;
		} else {
			pricePerLog = npcPrice;
		}

		if (pricePerLog <= 0) {
			return "No Data";
		}

		// ─── 5) Calculate: logsPerMinute × pricePerLog × 60 = Coins/hour ──────────────
		double cph = ForagingHud.logsPerMinute() * pricePerLog * 60.0;
		return ForagingHud.NUMBER_FORMAT.format((long) cph);
	}


	@Override
	public boolean isEnabledIn(Location location) {
		return location == Location.THE_PARK && SkyblockerConfigManager.get().foraging.park.foragingHud.enableHud;
	}

	@Override
	public void setEnabledIn(Location location, boolean enabled) {
		if (location == Location.THE_PARK) {
			SkyblockerConfigManager.get().foraging.park.foragingHud.enableHud = enabled;
		}
	}

	@Override
	public Set<Location> availableLocations() {
		return Set.of(Location.THE_PARK);
	}

	@Override
	public Text getDisplayName() {
		return Text.literal("Foraging HUD");
	}
}
