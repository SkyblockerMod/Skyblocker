package de.hysky.skyblocker.skyblock.crimson.kuudra;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.ChestValue;
import de.hysky.skyblocker.skyblock.crimson.CrimsonFaction;
import de.hysky.skyblocker.skyblock.item.PetInfo;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.data.ProfiledData;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class Kuudra {
	public static final int KUUDRA_MAGMA_CUBE_SIZE = 30;
	private static final Path FILE = SkyblockerMod.CONFIG_DIR.resolve("kuudra.json");
	private static final ProfiledData<KuudraProfileData> DATA = new ProfiledData<>(FILE, KuudraProfileData.CODEC, true, true);
	private static final Pattern FACTION_SHOP_PATTERN = Pattern.compile("^(?<faction>Mage|Barbarian) Shop$");

	protected static KuudraPhase phase = KuudraPhase.OTHER;

	@Init
	public static void init() {
		DATA.load();
		ScreenEvents.AFTER_INIT.register((_client, screen, _scaledWidth, _scaledHeight) -> {
			if (Utils.isOnSkyblock() && screen instanceof GenericContainerScreen genericContainerScreen) {
				String title = screen.getTitle().getString();
				Matcher factionShopMatcher = FACTION_SHOP_PATTERN.matcher(title);

				switch (title) {
					case String s when factionShopMatcher.matches() -> {
						ScreenEvents.afterTick(screen).register(_screen -> {
							checkKuudraKeyShop(genericContainerScreen, factionShopMatcher);
						});
					}
					case String s when s.startsWith("Pets") -> {
						ScreenEvents.afterTick(screen).register(_screen -> {
							checkForKuudraPet(genericContainerScreen);
						});
					}
					default -> {}
				}
			}
		});
		ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> reset());
		ClientReceiveMessageEvents.ALLOW_GAME.register(Kuudra::onMessage);
	}

	/**
	 * Returns the {@link KuudraProfileData} for the player's currently selected profile, returns empty if it does not exist.
	 */
	public static KuudraProfileData getKuudraProfileData() {
		return DATA.containsKey() ? DATA.get() : KuudraProfileData.EMPTY;
	}

	private static void checkKuudraKeyShop(GenericContainerScreen screen, Matcher factionShopMatcher) {
		// We determine the faction based off what shop the player is using since you can only shop for keys
		// at your faction's emissary, plus we hit two birds with one stone (getting faction and key prices).
		//
		// This should always match if the code is at this point
		CrimsonFaction faction = CrimsonFaction.valueOf(factionShopMatcher.group("faction").toUpperCase(Locale.ENGLISH));
		List<ItemStack> kuudraKeyItems = screen.getScreenHandler().slots.stream()
				.filter(Slot::hasStack)
				.map(Slot::getStack)
				.filter(stack -> KuudraProfileData.EMPTY.kuudraKeyPrices().containsKey(stack.getSkyblockId()))
				.toList();
		// Add all default entries to the map to ensure each key has a price in case the coins regex fails to match
		Object2IntMap<String> kuudraKeyPrices = new Object2IntOpenHashMap<>();
		kuudraKeyPrices.putAll(KuudraProfileData.EMPTY.kuudraKeyPrices());

		for (ItemStack kuudraKey : kuudraKeyItems) {
			Matcher matcher = ItemUtils.getLoreLineIfMatch(kuudraKey, ChestValue.DUNGEON_CHEST_COIN_COST_PATTERN);

			if (matcher != null) {
				// Same logic as getting coin value from dungeon chests
				String foundString = matcher.group(1).replaceAll("\\D", "");
				if (!NumberUtils.isCreatable(foundString)) continue;
				int amount = Integer.parseInt(foundString);

				kuudraKeyPrices.put(kuudraKey.getSkyblockId(), amount);
			}
		}

		KuudraProfileData storedData = getKuudraProfileData();

		if (faction != storedData.faction() || !kuudraKeyPrices.equals(storedData.kuudraKeyPrices())) {
			KuudraProfileData newData = new KuudraProfileData(faction, storedData.kuudraPet(), kuudraKeyPrices);
			DATA.put(newData);
			DATA.save();
		}
	}

	private static void checkForKuudraPet(GenericContainerScreen screen) {
		for (Slot slot : screen.getScreenHandler().slots) {
			if (slot.hasStack()) {
				PetInfo currentPet = slot.getStack().getPetInfo();
				KuudraProfileData storedData = getKuudraProfileData();

				// Ignore non-pet items
				if (currentPet.isEmpty()) continue;

				// If this is a Kuudra pet, the rarity of this pet is greater than or equal to the rarity of the stored one, and its exp is greater than
				// or equal to that of the stored one then we want to override the stored Kuudra pet data with this one.
				//
				// We also check if the stored pet is empty since in that case we want to update it.
				if (currentPet.type().equals("KUUDRA") && ((currentPet.tierIndex() >= storedData.kuudraPet().tierIndex() && currentPet.exp() >= storedData.kuudraPet().exp()) || storedData.kuudraPet().isEmpty())) {
					KuudraProfileData newData = new KuudraProfileData(storedData.faction(), currentPet, storedData.kuudraKeyPrices());
					DATA.put(newData);
					DATA.save();

					return;
				}
			}
		}
	}

	private static boolean onMessage(Text text, boolean overlay) {
		if (Utils.isInKuudra() && !overlay) {
			String message = Formatting.strip(text.getString());

			if (message.equals("[NPC] Elle: ARGH! All of the supplies fell into the lava! You need to retrieve them quickly!")) {
				phase = KuudraPhase.RETRIEVE_SUPPLIES;
			}

			if (message.equals("[NPC] Elle: Phew! The Ballista is finally ready! It should be strong enough to tank Kuudra's blows now!")) {
				phase = KuudraPhase.DPS;
			}

			if (message.equals("[NPC] Elle: POW! SURELY THAT'S IT! I don't think he has any more in him!")) {
				phase = KuudraPhase.OTHER;
			}

			if (message.equals("[NPC] Elle: What just happened!? Is this Kuudra's real lair?")) {
				phase = KuudraPhase.KUUDRA_LAIR;
			}
		}

		return true;
	}

	private static void reset() {
		phase = KuudraPhase.OTHER;
	}
}
