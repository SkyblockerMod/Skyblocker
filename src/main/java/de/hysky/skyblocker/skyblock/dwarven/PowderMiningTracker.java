package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.ChatEvents;
import de.hysky.skyblocker.events.HudRenderEvents;
import de.hysky.skyblocker.skyblock.item.ItemPrice;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.doubles.DoubleBooleanPair;
import it.unimi.dsi.fastutil.objects.*;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.text.NumberFormat;
import java.util.List;
import java.util.regex.Pattern;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class PowderMiningTracker {
	private static final Pattern GEMSTONE_SYMBOLS = Pattern.compile("[α☘☠✎✧❁❂❈❤⸕] ");
	// This constructor takes in a comparator that is triggered to decide where to add the element in the tree map
	// This causes it to be sorted at all times. This is for rendering them in a sort of easy-to-read manner.
	private static final Object2IntAVLTreeMap<Text> REWARDS = new Object2IntAVLTreeMap<>((o1, o2) -> {
				String o1String = GEMSTONE_SYMBOLS.matcher(o1.getString()).replaceAll("");
				String o2String = GEMSTONE_SYMBOLS.matcher(o2.getString()).replaceAll("");
				int priority1 = comparePriority(o1String);
				int priority2 = comparePriority(o2String);
				if (priority1 != priority2) return Integer.compare(priority1, priority2);
				return o1String.compareTo(o2String);
			}
	);
	private static final Object2ObjectMap<String, String> NAME2ID_MAP = new Object2ObjectArrayMap<>(50);
	private static boolean insideChestMessage = false;
	private static double profit = 0;

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private static boolean isEnabled() {
		return SkyblockerConfigManager.get().mining.crystalHollows.enablePowderTracker;
	}

	@Init
	public static void init() {
		ChatEvents.RECEIVE_TEXT.register(text -> {
			if (Utils.getLocation() != Location.CRYSTAL_HOLLOWS || !isEnabled()) return;
			List<Text> siblings = text.getSiblings();
			switch (siblings.size()) {
				// The separator message has 1 sibling: "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬" for which we can just use .getString on the main text
				case 1 -> {
					if (insideChestMessage && text.getString().equals("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")) insideChestMessage = false;
				}
				// CHEST LOCKPICKED message has 2 siblings: "  ", "CHEST LOCKPICKED "
				// LOOT CHEST COLLECTED message has 2 siblings: "  ", "LOOT CHEST COLLECTED "
				// Reward message with 1 item count has 2 siblings: "    ", "item name"
				case 2 -> {
					String space = siblings.get(0).getString();
					Text second = siblings.get(1);
					String secondString = second.getString();
					if (!insideChestMessage && space.equals("  ") && (secondString.equals("CHEST LOCKPICKED ") || (SkyblockerConfigManager.get().mining.crystalHollows.countNaturalChestsInTracker && secondString.equals("LOOT CHEST COLLECTED ")))) {
						insideChestMessage = true;
						return;
					}

					if (insideChestMessage && space.equals("    ")) {
						REWARDS.mergeInt(second, 1, Integer::sum);
						calculateProfitForItem(second, 1);
					}
				}
				// Reward message with more than 1 item count has 3 siblings: "    ", "item name ", "x<count>" (the space at the end of the item name is not a typo, it's there)
				// For some reason the green goblin egg and 1 more that I can't figure out have an extra sibling that is always empty (at the 2nd position)
				// To account for that, this case includes 4 size and there's a check for the 2nd sibling to be empty and the amount parsing has getLast() instead of hardcoded position
				case 3, 4 -> {
					if (!insideChestMessage) return;
					String space = siblings.get(0).getString();
					if (!space.equals("    ")) return;

					Text itemName = siblings.get(1);
					int amount;

					if (itemName.getString().isEmpty() && siblings.size() == 3) {
						itemName = siblings.get(2);
						amount = 1;
					} else {
						String nameTrimmed = itemName.getString().stripTrailing();
						itemName = Text.literal(nameTrimmed).setStyle(itemName.getStyle());
						amount = Integer.parseInt(siblings.getLast().getString().substring(1).replace(",", ""));
					}

					REWARDS.mergeInt(itemName, amount, Integer::sum);
					calculateProfitForItem(itemName, amount);
				}
				default -> {}
			}
		});

		HudRenderEvents.AFTER_MAIN_HUD.register((context, tickCounter) -> {
			if (Utils.getLocation() != Location.CRYSTAL_HOLLOWS || !isEnabled()) return;
			int y = MinecraftClient.getInstance().getWindow().getScaledHeight() / 2 - 100;
			var set = REWARDS.object2IntEntrySet();
			for (Object2IntMap.Entry<Text> entry : set) {
				context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, entry.getKey(), 5, y, 0xFFFFFF);
				context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.of(String.valueOf(entry.getIntValue())), 10 + MinecraftClient.getInstance().textRenderer.getWidth(entry.getKey()), y, 0xFFFFFF);
				y += 10;
			}
			context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.literal("Gain: " + NumberFormat.getInstance().format(profit) + " coins").formatted(Formatting.GOLD), 5, y + 10, 0xFFFFFF);
		});

		ItemPrice.ON_PRICE_UPDATE.register(() -> {
			profit = 0;
			ObjectSortedSet<Object2IntMap.Entry<Text>> set = REWARDS.object2IntEntrySet();
			for (Object2IntMap.Entry<Text> entry : set) {
				calculateProfitForItem(entry.getKey(), entry.getIntValue());
			}
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
				literal(SkyblockerMod.NAMESPACE)
						.then(
								literal("clearrewards")
										.executes(context -> {
											REWARDS.clear();
											return 1;
										})
						)
						.then(
								literal("listrewards")
										.executes(context -> {
											var set = REWARDS.object2IntEntrySet();
											for (Object2IntMap.Entry<Text> entry : set) {
												MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(entry.getKey().copy().append(" ").append(Text.of(String.valueOf(entry.getIntValue()))));
											}
											return 1;
										})
						)
		));
	}

	private static int comparePriority(String s) {
		// Puts gemstone powder at the top of the list, then gold and diamond essence, then gemstones by ascending rarity and then whatever else.
		switch (s) {
			case "Gemstone Powder" -> {
				return 1;
			}
			case "Gold Essence" -> {
				return 2;
			}
			case "Diamond Essence" -> {
				return 3;
			}
			default -> {
				if (s.startsWith("Rough")) return 4;
				if (s.startsWith("Flawed")) return 5;
				if (s.startsWith("Fine")) return 6;
				if (s.startsWith("Flawless")) return 7;
			}
		}
		return 8;
	}

	private static void calculateProfitForItem(Text text, int amount) {
		String id = getItemId(text);
		DoubleBooleanPair price = ItemUtils.getItemPrice(id);
		if (price.rightBoolean()) profit += price.leftDouble() * amount;
	}

	static {
		NAME2ID_MAP.put("Rough Ruby Gemstone", "ROUGH_RUBY_GEM");
		NAME2ID_MAP.put("Flawed Ruby Gemstone", "FLAWED_RUBY_GEM");
		NAME2ID_MAP.put("Fine Ruby Gemstone", "FINE_RUBY_GEM");
		NAME2ID_MAP.put("Flawless Ruby Gemstone", "FLAWLESS_RUBY_GEM");

		NAME2ID_MAP.put("Rough Amethyst Gemstone", "ROUGH_AMETHYST_GEM");
		NAME2ID_MAP.put("Flawed Amethyst Gemstone", "FLAWED_AMETHYST_GEM");
		NAME2ID_MAP.put("Fine Amethyst Gemstone", "FINE_AMETHYST_GEM");
		NAME2ID_MAP.put("Flawless Amethyst Gemstone", "FLAWLESS_AMETHYST_GEM");

		NAME2ID_MAP.put("Rough Jade Gemstone", "ROUGH_JADE_GEM");
		NAME2ID_MAP.put("Flawed Jade Gemstone", "FLAWED_JADE_GEM");
		NAME2ID_MAP.put("Fine Jade Gemstone", "FINE_JADE_GEM");
		NAME2ID_MAP.put("Flawless Jade Gemstone", "FLAWLESS_JADE_GEM");

		NAME2ID_MAP.put("Rough Amber Gemstone", "ROUGH_AMBER_GEM");
		NAME2ID_MAP.put("Flawed Amber Gemstone", "FLAWED_AMBER_GEM");
		NAME2ID_MAP.put("Fine Amber Gemstone", "FINE_AMBER_GEM");
		NAME2ID_MAP.put("Flawless Amber Gemstone", "FLAWLESS_AMBER_GEM");

		NAME2ID_MAP.put("Rough Sapphire Gemstone", "ROUGH_SAPPHIRE_GEM");
		NAME2ID_MAP.put("Flawed Sapphire Gemstone", "FLAWED_SAPPHIRE_GEM");
		NAME2ID_MAP.put("Fine Sapphire Gemstone", "FINE_SAPPHIRE_GEM");
		NAME2ID_MAP.put("Flawless Sapphire Gemstone", "FLAWLESS_SAPPHIRE_GEM");

		NAME2ID_MAP.put("Rough Topaz Gemstone", "ROUGH_TOPAZ_GEM");
		NAME2ID_MAP.put("Flawed Topaz Gemstone", "FLAWED_TOPAZ_GEM");
		NAME2ID_MAP.put("Fine Topaz Gemstone", "FINE_TOPAZ_GEM");
		NAME2ID_MAP.put("Flawless Topaz Gemstone", "FLAWLESS_TOPAZ_GEM");

		NAME2ID_MAP.put("Rough Jasper Gemstone", "ROUGH_JASPER_GEM");
		NAME2ID_MAP.put("Flawed Jasper Gemstone", "FLAWED_JASPER_GEM");
		NAME2ID_MAP.put("Fine Jasper Gemstone", "FINE_JASPER_GEM");
		NAME2ID_MAP.put("Flawless Jasper Gemstone", "FLAWLESS_JASPER_GEM");

		NAME2ID_MAP.put("Pickonimbus 2000", "PICKONIMBUS");
		NAME2ID_MAP.put("Ascension Rope", "ASCENSION_ROPE");
		NAME2ID_MAP.put("Wishing Compass", "WISHING_COMPASS");
		NAME2ID_MAP.put("Gold Essence", "ESSENCE_GOLD");
		NAME2ID_MAP.put("Diamond Essence", "ESSENCE_DIAMOND");
		NAME2ID_MAP.put("Prehistoric Egg", "PREHISTORIC_EGG");
		NAME2ID_MAP.put("Sludge Juice", "SLUDGE_JUICE");
		NAME2ID_MAP.put("Oil Barrel", "OIL_BARREL");
		NAME2ID_MAP.put("Jungle Heart", "JUNGLE_HEART");
		NAME2ID_MAP.put("Treasurite", "TREASURITE");
		NAME2ID_MAP.put("Yoggie", "YOGGIE");

		NAME2ID_MAP.put("Goblin Egg", "GOBLIN_EGG");
		NAME2ID_MAP.put("Green Goblin Egg", "GOBLIN_EGG_GREEN");
		NAME2ID_MAP.put("Blue Goblin Egg", "GOBLIN_EGG_BLUE");
		NAME2ID_MAP.put("Red Goblin Egg", "GOBLIN_EGG_RED");
		NAME2ID_MAP.put("Yellow Goblin Egg", "GOBLIN_EGG_YELLOW");

		NAME2ID_MAP.put("Control Switch", "CONTROL_SWITCH");
		NAME2ID_MAP.put("Electron Transmitter", "ELECTRON_TRANSMITTER");
		NAME2ID_MAP.put("FTX 3070", "FTX_3070");
		NAME2ID_MAP.put("Synthetic Heart", "SYNTHETIC_HEART");
		NAME2ID_MAP.put("Robotron Reflector", "ROBOTRON_REFLECTOR");
		NAME2ID_MAP.put("Superlite Motor", "SUPERLITE_MOTOR");
	}

	private static String getItemId(Text text) {
		return NAME2ID_MAP.getOrDefault(GEMSTONE_SYMBOLS.matcher(text.getString()).replaceAll(""), "");
	}
}
