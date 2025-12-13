package de.hysky.skyblocker.skyblock.garden;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Location;
import it.unimi.dsi.fastutil.doubles.DoubleBooleanPair;
import java.util.Map;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

@RegisterWidget
public class FarmingHudWidget extends ComponentBasedWidget {
	private static final MutableComponent TITLE = Component.literal("Farming").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD);
	private static final Set<Location> AVAILABLE_LOCATIONS = Set.of(Location.GARDEN);
	public static final Map<String, String> FARMING_TOOLS = Map.ofEntries(
			Map.entry("THEORETICAL_HOE_WHEAT_1", "WHEAT"),
			Map.entry("THEORETICAL_HOE_WHEAT_2", "WHEAT"),
			Map.entry("THEORETICAL_HOE_WHEAT_3", "WHEAT"),
			Map.entry("THEORETICAL_HOE_CARROT_1", "CARROT_ITEM"),
			Map.entry("THEORETICAL_HOE_CARROT_2", "CARROT_ITEM"),
			Map.entry("THEORETICAL_HOE_CARROT_3", "CARROT_ITEM"),
			Map.entry("THEORETICAL_HOE_POTATO_1", "POTATO_ITEM"),
			Map.entry("THEORETICAL_HOE_POTATO_2", "POTATO_ITEM"),
			Map.entry("THEORETICAL_HOE_POTATO_3", "POTATO_ITEM"),
			Map.entry("THEORETICAL_HOE_CANE_1", "SUGAR_CANE"),
			Map.entry("THEORETICAL_HOE_CANE_2", "SUGAR_CANE"),
			Map.entry("THEORETICAL_HOE_CANE_3", "SUGAR_CANE"),
			Map.entry("THEORETICAL_HOE_WARTS_1", "NETHER_STALK"),
			Map.entry("THEORETICAL_HOE_WARTS_2", "NETHER_STALK"),
			Map.entry("THEORETICAL_HOE_WARTS_3", "NETHER_STALK"),
			Map.entry("FUNGI_CUTTER", "RED_MUSHROOM"),
			Map.entry("CACTUS_KNIFE", "CACTUS"),
			Map.entry("MELON_DICER", "MELON"),
			Map.entry("MELON_DICER_2", "MELON"),
			Map.entry("MELON_DICER_3", "MELON"),
			Map.entry("PUMPKIN_DICER", "PUMPKIN"),
			Map.entry("PUMPKIN_DICER_2", "PUMPKIN"),
			Map.entry("PUMPKIN_DICER_3", "PUMPKIN"),
			Map.entry("COCO_CHOPPER", "INK_SACK:3"),
			Map.entry("BASIC_GARDENING_HOE", ""),
			Map.entry("ADVANCED_GARDENING_HOE", "")
	);
	private static FarmingHudWidget instance = null;

	public static FarmingHudWidget getInstance() {
		if (instance == null) instance = new FarmingHudWidget();
		return instance;
	}

	private final Minecraft client = Minecraft.getInstance();

	public FarmingHudWidget() {
		super(TITLE, ChatFormatting.YELLOW.getColor(), "hud_farming");
		instance = this;
		update();
	}

	@Override
	public boolean shouldUpdateBeforeRendering() {
		return true;
	}

	@Override
	public void updateContent() {
		if (client.player == null) {
			addComponent(new PlainTextComponent(Component.literal("Nothing to show :p")));
			return;
		}
		ItemStack farmingToolStack = client.player.getMainHandItem();
		if (farmingToolStack == null) return;
		String itemId = farmingToolStack.getSkyblockId();
		String cropItemId = FARMING_TOOLS.getOrDefault(itemId, "");
		ItemStack cropStack = ItemRepository.getItemStack(cropItemId.replace(":", "-")); // Hacky conversion to neu id since ItemUtils.getNeuId requires an item stack.

		String counterText = FarmingHud.counterText();
		String counterNumber = FarmingHud.NUMBER_FORMAT.format(FarmingHud.counter());
		if (FarmingHud.CounterType.NONE.matchesText(counterText)) counterNumber = "";
		addSimpleIcoText(cropStack, counterText, ChatFormatting.YELLOW, counterNumber);
		float cropsPerMinute = FarmingHud.cropsPerMinute();
		addSimpleIconTranslatableText(cropStack, "skyblocker.config.farming.general.cropsPerMin", ChatFormatting.YELLOW, FarmingHud.NUMBER_FORMAT.format((int) cropsPerMinute / 10 * 10));
		addSimpleIconTranslatableText(Ico.GOLD, "skyblocker.config.farming.general.coinsPerHour", ChatFormatting.GOLD, getPriceText(cropItemId, cropsPerMinute));
		addSimpleIconTranslatableText(cropStack, "skyblocker.config.farming.general.blocksPerSec", ChatFormatting.YELLOW, Double.toString(FarmingHud.blockBreaks()));
		//noinspection DataFlowIssue
		addComponent(Components.progressComponent(Ico.LANTERN, Component.translatable("skyblocker.config.farming.general.farmingLevel"), FarmingHud.farmingXpPercentProgress(), ChatFormatting.GOLD.getColor()));
		addSimpleIconTranslatableText(Ico.LIME_DYE, "skyblocker.config.farming.general.farmingXPPerHour", ChatFormatting.YELLOW, FarmingHud.NUMBER_FORMAT.format(FarmingHud.farmingXpPerHour()));

		Entity cameraEntity = client.getCameraEntity();
		Component yaw = cameraEntity == null ? Component.translatable("skyblocker.config.farming.general.noCameraEntity") : Component.literal(String.format("%.2f", Mth.wrapDegrees(cameraEntity.getYRot())));
		Component pitch = cameraEntity == null ? Component.translatable("skyblocker.config.farming.general.noCameraEntity") : Component.literal(String.format("%.2f", Mth.wrapDegrees(cameraEntity.getXRot())));
		addComponent(new PlainTextComponent(Component.translatable("skyblocker.config.farming.general.yaw", yaw).withStyle(ChatFormatting.GOLD)));
		addComponent(new PlainTextComponent(Component.translatable("skyblocker.config.farming.general.pitch", pitch).withStyle(ChatFormatting.GOLD)));
		if (LowerSensitivity.isSensitivityLowered()) {
			addComponent(new PlainTextComponent(Component.translatable("skyblocker.garden.hud.mouseLocked").withStyle(ChatFormatting.ITALIC)));
		}
	}

	/**
	 * Gets the price text of the given crop id, calculated with the given crops per minute and the npc price if it's higher than the bazaar sell price, or the bazaar sell price otherwise.
	 * The used price depends on the config:
	 * - BAZAAR: only bazaar price (if available)
	 * - NPC: only npc price (if available)
	 * - BOTH: higher of NPC or bazaar price
	 */
	private Component getPriceText(String cropItemId, float cropsPerMinute) {
		DoubleBooleanPair itemBazaarPrice = ItemUtils.getItemPrice(cropItemId); // Gets the bazaar sell price of the crop.
		double bazaarPrice = itemBazaarPrice.leftDouble();
		boolean hasBazaarData = itemBazaarPrice.rightBoolean();

		// Gets the npc sell price of the crop or set to the min double value if it doesn't exist.
		double itemNpcPrice = TooltipInfoType.NPC.hasOrNullWarning(cropItemId) ? TooltipInfoType.NPC.getData().getDouble(cropItemId) : Double.MIN_VALUE;

		double priceToUse = 0;
		Component sourceLabel = null;
		boolean hasValidPrice = false;

		switch (SkyblockerConfigManager.get().farming.garden.farmingHud.type) {
			case NPC -> {
				// Use NPC price if it's available.
				if (itemNpcPrice > 0 && itemNpcPrice != Double.MIN_VALUE) {
					priceToUse = itemNpcPrice;
					sourceLabel = Component.literal(" (").append(Component.translatable("skyblocker.config.farming.garden.farmingHud.type.NPC")).append(")");
					hasValidPrice = true;
				}
			}
			case BAZAAR -> {
				// Use Bazaar price if data is available.
				if (hasBazaarData) {
					priceToUse = bazaarPrice;
					sourceLabel = Component.literal(" (").append(Component.translatable("skyblocker.config.farming.garden.farmingHud.type.BAZAAR")).append(")");
					hasValidPrice = true;
				}
			}
			case BOTH -> {
				// Use the NPC price if it's higher than the Bazaar price and available.
				if (itemNpcPrice > bazaarPrice && itemNpcPrice != Double.MIN_VALUE) {
					priceToUse = itemNpcPrice;
					sourceLabel = Component.literal(" (").append(Component.translatable("skyblocker.config.farming.garden.farmingHud.type.NPC")).append(")");
					hasValidPrice = true;
				}
				// Otherwise, use Bazaar price if available.
				else if (hasBazaarData) {
					priceToUse = bazaarPrice;
					sourceLabel = Component.literal(" (").append(Component.translatable("skyblocker.config.farming.garden.farmingHud.type.BAZAAR")).append(")");
					hasValidPrice = true;
				}
			}
		}


		// Multiply by 60 to convert to hourly and divide by 100 for rounding is combined into multiplying by 0.6.
		return hasValidPrice ? Component.literal(FarmingHud.NUMBER_FORMAT.format((int) (priceToUse * cropsPerMinute * 0.6) * 100)).append(sourceLabel) : Component.translatable("skyblocker.config.farming.general.noData");
	}

	@Override
	public boolean isEnabledIn(Location location) {
		return location.equals(Location.GARDEN) && SkyblockerConfigManager.get().farming.garden.farmingHud.enableHud;
	}

	@Override
	public void setEnabledIn(Location location, boolean enabled) {
		if (!location.equals(Location.GARDEN)) return;
		SkyblockerConfigManager.get().farming.garden.farmingHud.enableHud = enabled;
	}

	@Override
	public Set<Location> availableLocations() {
		return AVAILABLE_LOCATIONS;
	}

	@Override
	public Component getDisplayName() {
		return Component.literal("Farming HUD");
	}
}
