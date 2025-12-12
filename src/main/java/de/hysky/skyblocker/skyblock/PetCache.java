package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.item.PetInfo;
import de.hysky.skyblocker.skyblock.item.SkyblockItemRarity;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.RegexUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.data.ProfiledData;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.azureaaron.networth.utils.PetConstants;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Doesn't work with auto pet right now because that's complicated.
 * <p>
 * Want support? Ask the Admins for a Mod API event or open your pets menu.
 */
public class PetCache {
	private static final Path FILE = SkyblockerMod.CONFIG_DIR.resolve("pet_cache.json");
	private static final ProfiledData<PetInfo> CACHED_PETS = new ProfiledData<>(FILE, PetInfo.CODEC, true, true);
	private static final Pattern AUTOPET_PATTERN = Pattern.compile("^Autopet equipped your \\[Lvl (?<level>\\d+)\\] (?<name>[A-Za-z ]+)(?: ✦)?! VIEW RULE$");

	/**
	 * Used in case the server lags to prevent the screen tick check from overwriting the clicked pet logic
	 */
	private static boolean shouldLook4Pets;

	@Init
	public static void init() {
		CACHED_PETS.load();

		ScreenEvents.BEFORE_INIT.register((_client, screen, _scaledWidth, _scaledHeight) -> {
			if (Utils.isOnSkyblock() && screen instanceof GenericContainerScreen genericContainerScreen) {
				if (genericContainerScreen.getTitle().getString().startsWith("Pets")) {
					shouldLook4Pets = true;

					ScreenEvents.afterTick(screen).register(screen1 -> {
						if (shouldLook4Pets) {
							for (Slot slot : genericContainerScreen.getScreenHandler().slots) {
								ItemStack stack = slot.getStack();

								if (!stack.isEmpty() && ItemUtils.getLoreLineIf(stack, line -> line.equals("Click to despawn!")) != null) {
									shouldLook4Pets = false;
									parsePet(stack, false);

									break;
								}
							}
						}
					});
				}
			}
		});
		ClientReceiveMessageEvents.ALLOW_GAME.register(PetCache::onMessage);
	}

	public static void handlePetEquip(Slot slot, int slotId) {
		//Ignore inventory clicks
		if (slotId >= 0 && slotId <= 53) {
			ItemStack stack = slot.getStack();

			if (!stack.isEmpty()) parsePet(stack, true);
		}
	}

	private static void parsePet(ItemStack stack, boolean clicked) {
		String profileId = Utils.getProfileId();

		if (stack.getSkyblockId().equals("PET") && !profileId.isEmpty()) {
			//I once hoped that all pets would have a petInfo field, but that turned out to be false ;(
			PetInfo petInfo = stack.getPetInfo();

			//This probably shouldn't happen since I would imagine pets inside of a pet menu would have a pet info but you never know...
			if (petInfo.isEmpty()) return;

			shouldLook4Pets = false;

			//Handle deselecting pets
			if (clicked && getCurrentPet() != null && getCurrentPet().uuid().orElse("").equals(petInfo.uuid().orElse(""))) {
				CACHED_PETS.remove();
			} else {
				CACHED_PETS.put(petInfo);
			}

			CACHED_PETS.save();
		}
	}

	/**
	 * Parses the Auto Pet messages to try and detect the active pet
	 */
	private static boolean onMessage(Text text, boolean overlay) {
		if (!Utils.isOnSkyblock() || overlay) return true;

		String stringified = Formatting.strip(text.getString());
		Matcher matcher = AUTOPET_PATTERN.matcher(stringified);

		if (matcher.matches()) {
			int level = RegexUtils.parseIntFromMatcher(matcher, "level");
			String name = matcher.group("name");

			OrderedText ordered = text.asOrderedText();
			int nameIndex = stringified.indexOf(name);
			MutableInt codePointIndex = new MutableInt(0);
			MutableInt color = new MutableInt(-1);

			//The index has nothing to do with the codepoint's position so we must track it ourselves
			//The visitor automatically folds section symbols into regular Style instances so we don't need to care about those either :)
			ordered.accept((index, style, codePoint) -> {
				if (codePointIndex.intValue() == nameIndex) {
					color.setValue(style.getColor().getRgb());

					return false;
				}

				codePointIndex.getAndIncrement();
				return true;
			});

			SkyblockItemRarity rarity = SkyblockItemRarity.fromColor(color.intValue());

			if (rarity != null && rarity != SkyblockItemRarity.UNKNOWN) {
				//This is technically an internal class but I don't feel like copying it out right now and I got no plans to change/remove it :shrug:
				int petOffset = PetConstants.RARITY_OFFSETS.getOrDefault(rarity.name(), 0);
				//The list is copied due to a FastUtil bug with sub list iterators
				IntList petLevels = new IntArrayList(PetConstants.PET_LEVELS.subList(petOffset, petOffset + level - 1));
				double exp = petLevels.intStream().sum();

				//Find pet in NEU repo
				ItemStack stack = ItemRepository.getItemsStream()
						.filter(s -> s.getName().getString().contains("] " + name))
						.findFirst()
						.orElse(ItemStack.EMPTY);

				if (!stack.isEmpty()) {
					//We need to change the item id of the stack in order for the pet info to parse properly cause the id in the custom data is the neu id
					ItemStack copied = stack.copy();
					NbtCompound customData = copied.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();

					customData.putString("id", "PET");
					copied.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(customData));

					//If the pet from the NEU repo is missing the data then try to guess the type
					String type = !copied.getPetInfo().isEmpty() ? copied.getPetInfo().type() : name.toUpperCase(Locale.ENGLISH).replace(" ", "_");
					PetInfo petInfo = new PetInfo(Optional.of(name), type, exp, rarity, Optional.empty(), Optional.empty(), Optional.empty());

					CACHED_PETS.put(petInfo);
					CACHED_PETS.save();
				}
			}
		}

		return true;
	}

	@Nullable
	public static PetInfo getCurrentPet() {
		return CACHED_PETS.get();
	}
}
