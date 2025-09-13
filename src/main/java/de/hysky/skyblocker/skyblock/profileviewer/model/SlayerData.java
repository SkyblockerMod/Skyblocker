package de.hysky.skyblocker.skyblock.profileviewer.model;

import com.google.gson.annotations.SerializedName;
import de.hysky.skyblocker.skyblock.profileviewer.utils.LevelFinder;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.item.ItemStack;

import java.util.Map;

public class SlayerData {
	@SerializedName("slayer_bosses")
	public Map<String, SlayerBoss> slayerBosses = Map.of();

	public SlayerBoss getSlayerData(Slayer slayer) {
		return slayerBosses.getOrDefault(slayer.getName().toLowerCase(), new SlayerBoss());
	}

	public double getSlayerExperience(Slayer slayer) {
		return getSlayerData(slayer).xp;
	}

	public LevelFinder.LevelInfo getSkillLevel(Slayer slayer) {
//		return LevelFinder.getLevelInfo(slayer.levelFinderOverride, (long) getSlayerExperience(slayer));
		return LevelFinder.getLevelInfo(slayer.name, (long) getSlayerExperience(slayer));
	}

	public enum Slayer {
		REVENANT_HORROR("Zombie", Ico.REVENANT_HORROR_SKULL, Ico.FLESH),
		TARANTULA_BROODFATHER("Spider", Ico.TARANTULA_BROODFATHER_SKULL, Ico.STRING),
		SVEN_PACKMASTER("Wolf", Ico.SVEN_PACKMASTER_SKULL, Ico.MUTTON),
		VOIDGLOOM_SERAPH("Enderman", Ico.VOIDGLOOM_SERAPH_SKULL, Ico.E_PEARL),
		RIFTSTALKER_BLOODFIEND("Vampire", Ico.RIFTSTALKER_BLOODFIEND_SKULL, Ico.REDSTONE, "Vampire"),
		INFERNO_DEMONLORD("Blaze", Ico.INFERNO_DEMONLORD_SKULL, Ico.B_POWDER);

		private final String name;
		private final ItemStack itemStack;
		private final ItemStack dropItemStack;
		private final String levelFinderOverride;


		Slayer(String name, ItemStack itemStack, ItemStack dropItemStack) {
			this(name, itemStack, dropItemStack, "GenericSlayer");
		}

		Slayer(String name, ItemStack itemStack, ItemStack dropItemStack, String override) {
			this.name = name;
			this.itemStack = itemStack;
			this.dropItemStack = dropItemStack;
			this.levelFinderOverride = override;
		}

		public String getName() {
			return name;
		}

		public ItemStack getIcon() { return itemStack; }

		public ItemStack getDropIcon() { return dropItemStack; }
	}
}
