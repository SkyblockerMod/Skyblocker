package de.hysky.skyblocker.skyblock.entity.glow.adder;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display.ItemDisplay;
import net.minecraft.world.entity.Display.TextDisplay;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.HuntingConfig;
import de.hysky.skyblocker.skyblock.entity.MobGlow;
import de.hysky.skyblocker.skyblock.entity.MobGlowAdder;
import de.hysky.skyblocker.skyblock.hunting.safari.SafariUtils;
import de.hysky.skyblocker.skyblock.item.HeadTextures;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;

public class SafariGlowAdder extends MobGlowAdder {
	@SuppressWarnings("unused")
	private static final SafariGlowAdder INSTANCE = new SafariGlowAdder();

	@Init
	public static void init() {}

	@Override
	public int computeColour(Entity entity) {
		HuntingConfig huntingConfig = SkyblockerConfigManager.get().hunting;

		return switch (entity) {
			// Cavern Biome
			case ItemDisplay display when huntingConfig.cavernBiome.highlightRockmiteMounds && SafariUtils.isInCavernBiome() && ItemUtils.getHeadTexture(display.getItemStack()).equals(HeadTextures.ROCKMITE_MOUND) && display.getPosRotInterpolationDuration() == 0 -> huntingConfig.cavernBiome.rockmiteMoundHighlightColor.getRGB();

			// Forest Biome
			case Shulker shulker when huntingConfig.forestBiome.highlightHideonfloor && SafariUtils.isInForestBiome() && shulker.getColor() == DyeColor.GREEN -> huntingConfig.forestBiome.hideonfloorHighlightColor.getRGB();
			case ItemDisplay display when huntingConfig.forestBiome.highlightHideonfloor && SafariUtils.isInForestBiome() && display.getItemStack().is(Items.DYED_SHULKER_BOX.green()) -> huntingConfig.forestBiome.hideonfloorHighlightColor.getRGB();

			// Haunted Biome
			case ItemDisplay display when huntingConfig.hauntedBiome.highlightDuplico && SafariUtils.isInHauntedBiome() && display.getPosRotInterpolationDuration() == 3 && isNotDuplico(display) -> huntingConfig.hauntedBiome.duplicoHighlightColor.getRGB();
			case Bat _ when huntingConfig.hauntedBiome.highlightBloodbat && SafariUtils.isInHauntedBiome() -> huntingConfig.hauntedBiome.bloodbatHighlightColor.getRGB();

			// Sparkling Critters
			case Entity _ when huntingConfig.safari.highlightSparklingCritters && isSparkling(entity) -> huntingConfig.safari.sparklingCritterHighlightColor.getRGB();

			default -> NO_GLOW;
		};
	}

	private static boolean isNotDuplico(ItemDisplay display) {
		ItemStack stack = display.getItemStack();

		// Exclude Gimmiegolds and moving Hideonwalls
		return !stack.is(Items.PLAYER_HEAD) && !stack.is(Items.DYED_SHULKER_BOX.purple());
	}

	private static boolean isSparkling(Entity entity) {
		// Ignore useless entities
		if (entity instanceof ArmorStand || entity instanceof TextDisplay) {
			return false;
		}

		// Ignore hideonwall due to issues
		if (entity instanceof Shulker shulker && shulker.getColor() == DyeColor.PURPLE) {
			return false;
		}

		if (entity instanceof ItemDisplay display) {
			ItemStack stack = display.getItemStack();

			// Exclude things that aren't player heads or Hideonfloors
			// Duplico already has a highlight of sorts (and does not work with name tag detection) and glow is weird with painting occlusion
			if (!stack.is(Items.PLAYER_HEAD) && !stack.is(Items.DYED_SHULKER_BOX.green())) {
				return false;
			}
		}

		List<ArmorStand> armorStands = MobGlow.getArmorStands(entity);
		Component customName = !armorStands.isEmpty() ? armorStands.getFirst().getCustomName() : null;

		return customName != null && customName.getString().contains("SPARKLING");
	}

	@Override
	public boolean isEnabled() {
		return Utils.isInSafari();
	}
}
