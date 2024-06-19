package de.hysky.skyblocker.skyblock.chocolatefactory;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.LineSmoothener;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.ProfileUtils;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RabbitCollection extends TooltipAdder {
	private static final Object2ReferenceMap<String, RabbitRarity> RABBITS = new Object2ReferenceOpenHashMap<>(500); //There's roughly 460 rabbits so far
	private static final Logger LOGGER = LoggerFactory.getLogger(RabbitCollection.class);
	//The slot in chocolate factory screen
	private static final byte CF_HOPPITY_COLLECTION_SLOT = 50;
	//The slot in chocolate collection screen
	private static final byte HC_HOPPITY_COLLECTION_SLOT = 4;
	private static final Pattern RABBIT_FOUND_PATTERN = Pattern.compile("§D§LHOPPITY'S HUNT §7You found (?:§\\S)+(.*?) (?:§\\S)+\\((?:§\\S)+([^§]+).*");
	private static RabbitRarity lastRarity = null;

	public RabbitCollection() {
		super("^Chocolate Factory|\\(\\d+/\\d+\\) Hoppity's Collection$", Integer.MIN_VALUE);
	}

	private static void reset() {
		RabbitRarity.resetCounts();
		lastRarity = null;
	}

	public static void init() {
		readJson();
		SkyblockEvents.PROFILE_CHANGE.register(() -> {
			reset();
			apiRequest();
		});
		SkyblockEvents.JOIN.register(RabbitCollection::apiRequest);
		ClientReceiveMessageEvents.GAME.register(RabbitCollection::onMessage);
//		ApiAuthentication.TOKEN_REQUEST_CALLBACK.register(RabbitCollection::apiRequest);
	}

	//These 2 messages are sent one after the other when a rabbit is found
	//The found rabbit can also be a duplicate rabbit, so we have to check if the next message says "NEW RABBIT!"
	private static void onMessage(Text message, boolean overlay) {
		if (overlay) return;
		String str = message.getString();
		Matcher matcher = RABBIT_FOUND_PATTERN.matcher(str);
		if (matcher.matches()) {
			try {
				lastRarity = RabbitRarity.fromString(matcher.group(2).toLowerCase(Locale.ROOT));
			} catch (IllegalArgumentException e) {
				LOGGER.warn("[Skyblocker Rabbit Collection] Unknown rabbit rarity: {} - report this to Skyblocker!", matcher.group(1));
				lastRarity = null;
			}
			return;
		}
		if (str.startsWith("§d§lNEW RABBIT!") && lastRarity != null) {
			lastRarity.incrementCollected();
			lastRarity = null;
		}
	}

	private static void apiRequest() {
		ProfileUtils.updateProfile().thenAccept(profile -> {
			if (profile == null) return;
			if (!profile.has("events")) return;
			JsonObject events = profile.getAsJsonObject("events");
			if (!events.has("easter")) return;
			JsonObject easter = events.getAsJsonObject("easter");
			if (!easter.has("rabbits")) return;
			JsonObject rabbits = easter.getAsJsonObject("rabbits");
			for (String rabbit : rabbits.keySet()) {
				if (rabbit.equals("collected_eggs") || rabbit.equals("collected_locations")) continue;
				RabbitRarity rarity = RABBITS.get(rabbit);
				if (rarity == null) {
					LOGGER.warn("[Skyblocker Rabbit Collection] Unknown rabbit: {}", rabbit);
					continue;
				}
				rarity.incrementCollected();
			}
		});
	}

	private static void readJson() {
		try (JsonReader reader = new JsonReader(new InputStreamReader(NEURepoManager.NEU_REPO.file("constants/hoppity.json").stream()))) {
			reader.beginObject(); // Begin reading the json object
			reader.nextName(); // Skip the `hoppity` key
			reader.beginObject(); // Open up the `hoppity` object
			reader.nextName(); // Skip the `rarities` key
			reader.beginObject(); // Open up the `rarities` object
			while (reader.hasNext()) {
				final String key = reader.nextName(); //Read the current rarity key
				final RabbitRarity rarity = RabbitRarity.fromString(key);
				reader.beginObject(); // Open up the current rarity object
				reader.nextName(); // Skip the `rabbits` key
				reader.beginArray(); // Open up the `rabbits` array
				while (reader.hasNext()) {
					var name = reader.nextString();
					RABBITS.put(name, rarity);
					rarity.incrementMax();
				}
				reader.endArray(); // Close the `rabbits` array
				reader.skipValue(); // Skip the `chocolate` key
				reader.skipValue(); // Skip the `chocolate` value
				reader.skipValue(); // Skip the `multiplier` key
				reader.skipValue(); // Skip the `multiplier` value
				reader.endObject(); // Close the current rarity object
			}
			//No need to read the rest, we only care about the rabbits
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Rabbit Collection] Failed to read rabbits from json!", e);
		}
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
		if (focusedSlot == null) return;
		if ((focusedSlot.id == CF_HOPPITY_COLLECTION_SLOT || focusedSlot.id == HC_HOPPITY_COLLECTION_SLOT) && stack.isOf(Items.PLAYER_HEAD)) {
			lines.add(LineSmoothener.createSmoothLine());
			for (RabbitRarity entry : RabbitRarity.entries) {
				lines.add(Text.literal(entry.toString()).formatted(entry.color)
				              .align(Text.empty()
				                         .append(Text.literal(String.valueOf(entry.getCollectedAmount())).formatted(Formatting.GREEN))
				                         .append(Text.literal("/").formatted(Formatting.DARK_GRAY))
				                         .append(Text.literal(String.valueOf(entry.getMaxAmount())).formatted(Formatting.GREEN)), 110));
			}
		}
	}

	public enum RabbitRarity {
		COMMON(Formatting.WHITE),
		UNCOMMON(Formatting.GREEN),
		RARE(Formatting.BLUE),
		EPIC(Formatting.DARK_PURPLE),
		LEGENDARY(Formatting.GOLD),
		MYTHIC(Formatting.LIGHT_PURPLE),
		DIVINE(Formatting.AQUA);

		public final Formatting color;
		private short collectedAmount = 0;
		private short maxAmount = 0;

		RabbitRarity(Formatting color) {
			this.color = color;
		}

		public void incrementCollected() {
			collectedAmount++;
		}

		public void incrementMax() {
			maxAmount++;
		}

		public short getCollectedAmount() {
			return collectedAmount;
		}

		public short getMaxAmount() {
			return maxAmount;
		}

		@Override
		public String toString() {
			return switch (this) {
				case COMMON    -> "Common Rabbits";
				case UNCOMMON  -> "Uncommon Rabbits";
				case RARE      -> "Rare Rabbits";
				case EPIC      -> "Epic Rabbits";
				case LEGENDARY -> "Legendary Rabbits";
				case MYTHIC    -> "Mythic Rabbits";
				case DIVINE    -> "Divine Rabbits";
			};
		}

		// This is to avoid creating a new array every time we iterate over the values
		public static final @Unmodifiable List<RabbitRarity> entries = Arrays.asList(values());

		public static RabbitRarity fromString(String key) {
			return switch (key) { // @formatter:off
				case "common"    -> COMMON;
				case "uncommon"  -> UNCOMMON;
				case "rare"      -> RARE;
				case "epic"      -> EPIC;
				case "legendary" -> LEGENDARY;
				case "mythic"    -> MYTHIC;
				case "divine"    -> DIVINE;
				default          -> throw new IllegalArgumentException("Invalid rarity key: " + key);
			};
		}

		public static void resetCounts() {
			for (RabbitRarity rarity : entries) {
				rarity.collectedAmount = 0;
			}
		}
	}
}
