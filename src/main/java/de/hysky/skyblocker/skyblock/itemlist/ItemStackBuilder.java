package de.hysky.skyblocker.skyblock.itemlist;

import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.TextTransformer;
import de.hysky.skyblocker.utils.datafixer.LegacyItemStackFixer;
import de.hysky.skyblocker.utils.datafixer.LegacyStringNbtReader;
import io.github.moulberry.repo.constants.PetNumbers;
import io.github.moulberry.repo.data.NEUItem;
import io.github.moulberry.repo.data.Rarity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

public class ItemStackBuilder {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static Map<String, Map<Rarity, PetNumbers>> petNums;

	protected static void loadPetNums() {
		try {
			petNums = NEURepoManager.getConstants().getPetNumbers();
		} catch (Exception e) {
			ItemRepository.LOGGER.error("Failed to load petnums.json");
		}
	}

	protected static ItemStack fromNEUItem(NEUItem item) {
		try {
			CompoundTag nbt = new CompoundTag();
			CompoundTag tag = LegacyStringNbtReader.parse(item.getNbttag());

			//Construct the nbt
			nbt.put("tag", tag);
			nbt.putString("id", item.getMinecraftItemId());
			nbt.putShort("Damage", (short) item.getDamage());
			nbt.putInt("Count", 1);

			ItemStack stack = LegacyItemStackFixer.fixLegacyStack(nbt);

			//The item couldn't be fixed up
			if (stack.isEmpty()) {
				LOGGER.error("[Skyblocker ItemStackBuilder] Failed to build item with skyblock id: {}!", item.getSkyblockItemId());

				return createErrorStack(item.getSkyblockItemId());
			}

			List<Tuple<String, String>> injectors = new ArrayList<>(petData(item.getSkyblockItemId()));

			//Inject data into the item name
			String name = injectData(item.getDisplayName(), injectors);
			stack.set(DataComponents.CUSTOM_NAME, TextTransformer.fromLegacy(name));

			//Inject Data into the lore
			stack.set(DataComponents.LORE, new ItemLore(item.getLore().stream()
					.map(line -> TextTransformer.fromLegacy(injectData(line, injectors)))
					.map(Component.class::cast)
					.toList()));

			return stack;
		} catch (Exception e) {
			LOGGER.error("[Skyblocker ItemStackBuilder] Failed to build item with skyblock id: {}!", item.getSkyblockItemId(), e);
		}

		return createErrorStack(item.getSkyblockItemId());
	}

	private static ItemStack createErrorStack(String skyblockItemId) {
		ItemStack errorStack = new ItemStack(Items.BARRIER);
		errorStack.set(DataComponents.CUSTOM_NAME, Component.nullToEmpty(skyblockItemId));

		return errorStack;
	}

	private static List<Tuple<String, String>> petData(String internalName) {
		List<Tuple<String, String>> list = new ArrayList<>();

		String petName = internalName.split(";")[0];
		if (!internalName.contains(";") || !petNums.containsKey(petName)) return list;

		final Rarity[] rarities = {
				Rarity.COMMON,
				Rarity.UNCOMMON,
				Rarity.RARE,
				Rarity.EPIC,
				Rarity.LEGENDARY,
				Rarity.MYTHIC,
		};
		Rarity rarity = rarities[Integer.parseInt(internalName.split(";")[1])];
		PetNumbers data = petNums.get(petName).get(rarity);

		int minLevel = data.getLowLevel();
		int maxLevel = data.getHighLevel();
		list.add(new Tuple<>("\\{LVL\\}", minLevel + " ➡ " + maxLevel));

		Map<String, Double> statNumsMin = data.getStatsAtLowLevel().getStatNumbers();
		Map<String, Double> statNumsMax = data.getStatsAtHighLevel().getStatNumbers();
		Set<Map.Entry<String, Double>> entrySet = statNumsMin.entrySet();
		for (Map.Entry<String, Double> entry : entrySet) {
			String key = entry.getKey();
			String left = "\\{" + key + "\\}";
			String right = statNumsMin.get(key) + " ➡ " + statNumsMax.get(key);
			list.add(new Tuple<>(left, right));
		}

		List<Double> otherNumsMin = data.getStatsAtLowLevel().getOtherNumbers();
		List<Double> otherNumsMax = data.getStatsAtHighLevel().getOtherNumbers();
		for (int i = 0; i < otherNumsMin.size(); ++i) {
			String left = "\\{" + i + "\\}";
			String right = otherNumsMin.get(i) + " ➡ " + otherNumsMax.get(i);
			list.add(new Tuple<>(left, right));
		}

		return list;
	}

	private static String injectData(String string, List<Tuple<String, String>> injectors) {
		for (Tuple<String, String> injector : injectors) {
			string = string.replaceAll(injector.getA(), injector.getB());
		}
		return string;
	}
}
