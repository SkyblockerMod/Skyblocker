package de.hysky.skyblocker.skyblock.item;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.PetCache;
import de.hysky.skyblocker.utils.ItemUtils;
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemCooldowns {
    private static final String JUNGLE_AXE_ID = "JUNGLE_AXE";
    private static final String TREECAPITATOR_ID = "TREECAPITATOR_AXE";
    private static final String GRAPPLING_HOOK_ID = "GRAPPLING_HOOK";
	private static final String ROGUE_SWORD_ID = "ROGUE_SWORD";
	private static final String LEAPING_SWORD_ID = "LEAPING_SWORD";
	private static final String SILK_EDGE_SWORD_ID = "SILK_EDGE_SWORD";
	private static final String GREAT_SPOOK_STAFF_ID = "GREAT_SPOOK_STAFF";
	private static final String SPIRIT_LEAP_ID = "SPIRIT_LEAP";
	private static final String GIANTS_SWORD_ID = "GIANTS_SWORD";
	private static final String SHADOW_FURY_ID = "SHADOW_FURY";
	private static final String LIVID_DAGGER_ID = "LIVID_DAGGER";
	private static final String INK_WAND_ID = "INK_WAND";

    private static final List<String> BAT_ARMOR_IDS = List.of("BAT_PERSON_HELMET", "BAT_PERSON_CHESTPLATE", "BAT_PERSON_LEGGINGS", "BAT_PERSON_BOOTS");
    private static final Map<String, CooldownEntry> ITEM_COOLDOWNS = new HashMap<>();
    private static final int[] EXPERIENCE_LEVELS = {
            0, 660, 730, 800, 880, 960, 1050, 1150, 1260, 1380, 1510, 1650, 1800, 1960, 2130,
            2310, 2500, 2700, 2920, 3160, 3420, 3700, 4000, 4350, 4750, 5200, 5700, 6300, 7000,
            7800, 8700, 9700, 10800, 12000, 13300, 14700, 16200, 17800, 19500, 21300, 23200,
            25200, 27400, 29800, 32400, 35200, 38200, 41400, 44800, 48400, 52200, 56200, 60400,
            64800, 69400, 74200, 79200, 84700, 90700, 97200, 104200, 111700, 119700, 128200,
            137200, 147700, 156700, 167700, 179700, 192700, 206700, 221700, 237700, 254700,
            272700, 291700, 311700, 333700, 357700, 383700, 411700, 441700, 476700, 516700,
            561700, 611700, 666700, 726700, 791700, 861700, 936700, 1016700, 1101700, 1191700,
            1286700, 1386700, 1496700, 1616700, 1746700, 1886700
    };
    private static int monkeyLevel = 1;
    private static double monkeyExp = 0;

    @Init
    public static void init() {
        ClientPlayerBlockBreakEvents.AFTER.register(ItemCooldowns::afterBlockBreak);
        UseItemCallback.EVENT.register(ItemCooldowns::onItemInteract);
    }

    public static void updateCooldown() {
        PetInfo pet = PetCache.getCurrentPet();

        if (pet != null && pet.tier().equals(SkyblockItemRarity.LEGENDARY)) {
            monkeyExp = pet.exp();

            monkeyLevel = 0;
            for (int xpLevel : EXPERIENCE_LEVELS) {
                if (monkeyExp < xpLevel) {
                    break;
                } else {
                    monkeyExp -= xpLevel;
                    monkeyLevel++;
                }
            }
        }
    }

    private static int getCooldown4Foraging() {
        int baseCooldown = 2000;
        int monkeyPetCooldownReduction = baseCooldown * monkeyLevel / 200;
        return baseCooldown - monkeyPetCooldownReduction;
    }

    public static void afterBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state) {
        if (!SkyblockerConfigManager.get().uiAndVisuals.itemCooldown.enableItemCooldowns) return;
        String usedItemId = ItemUtils.getItemId(player.getMainHandStack());
        if (usedItemId.isEmpty()) return;
        if (state.isIn(BlockTags.LOGS)) {
            if (usedItemId.equals(JUNGLE_AXE_ID) || usedItemId.equals(TREECAPITATOR_ID)) {
                updateCooldown();
                if (!isOnCooldown(JUNGLE_AXE_ID) || !isOnCooldown(TREECAPITATOR_ID)) {
                    ITEM_COOLDOWNS.put(usedItemId, new CooldownEntry(getCooldown4Foraging()));
                }
            }
        }
    }

    private static ActionResult onItemInteract(PlayerEntity player, World world, Hand hand) {
		if (!SkyblockerConfigManager.get().uiAndVisuals.itemCooldown.enableItemCooldowns)
			return ActionResult.PASS;
		String usedItemId = ItemUtils.getItemId(player.getMainHandStack());
		switch (usedItemId) {
			case SILK_EDGE_SWORD_ID, LEAPING_SWORD_ID -> handleItemCooldown(usedItemId, 1000);
			case GRAPPLING_HOOK_ID -> handleItemCooldown(GRAPPLING_HOOK_ID, 2000, player.fishHook != null && !isWearingBatArmor(player));
			case ROGUE_SWORD_ID, SPIRIT_LEAP_ID, LIVID_DAGGER_ID -> handleItemCooldown(usedItemId, 5000);
			case SHADOW_FURY_ID -> handleItemCooldown(SHADOW_FURY_ID, 15000);
			case INK_WAND_ID, GIANTS_SWORD_ID -> handleItemCooldown(usedItemId, 30000);
			case GREAT_SPOOK_STAFF_ID -> handleItemCooldown(GREAT_SPOOK_STAFF_ID, 60000);
			// Handle any unlisted items if necessary
			default -> {}
		}
        return ActionResult.PASS;
    }

	// Method to handle item cooldowns with optional condition
	private static void handleItemCooldown(String itemId, int cooldownTime, boolean additionalCondition) {
		if (!isOnCooldown(itemId) && additionalCondition) {
			ITEM_COOLDOWNS.put(itemId, new CooldownEntry(cooldownTime));
		}
	}

	// Overloaded method for cases without additional conditions
	private static void handleItemCooldown(String itemId, int cooldownTime) {
		handleItemCooldown(itemId, cooldownTime, true);
	}

    public static boolean isOnCooldown(ItemStack itemStack) {
        return isOnCooldown(ItemUtils.getItemId(itemStack));
    }

    private static boolean isOnCooldown(String itemId) {
        if (ITEM_COOLDOWNS.containsKey(itemId)) {
            CooldownEntry cooldownEntry = ITEM_COOLDOWNS.get(itemId);
            if (cooldownEntry.isOnCooldown()) {
                return true;
            } else {
                ITEM_COOLDOWNS.remove(itemId);
                return false;
            }
        }

        return false;
    }

    public static CooldownEntry getItemCooldownEntry(ItemStack itemStack) {
        return ITEM_COOLDOWNS.get(ItemUtils.getItemId(itemStack));
    }

    private static boolean isWearingBatArmor(PlayerEntity player) {
        for (ItemStack stack : player.getArmorItems()) {
            String itemId = ItemUtils.getItemId(stack);
            if (!BAT_ARMOR_IDS.contains(itemId)) {
                return false;
            }
        }
        return true;
    }

    public record CooldownEntry(int cooldown, long startTime) {
        public CooldownEntry(int cooldown) {
            this(cooldown, System.currentTimeMillis());
        }

        public boolean isOnCooldown() {
            return (this.startTime + this.cooldown) > System.currentTimeMillis();
        }

        public long getRemainingCooldown() {
            long time = (this.startTime + this.cooldown) - System.currentTimeMillis();
            return Math.max(time, 0);
        }

        public float getRemainingCooldownPercent() {
            return this.isOnCooldown() ? (float) this.getRemainingCooldown() / cooldown : 0.0f;
        }
    }
}
