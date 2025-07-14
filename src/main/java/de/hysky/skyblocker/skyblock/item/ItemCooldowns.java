package de.hysky.skyblocker.skyblock.item;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemCooldowns {
    private static final String JUNGLE_AXE_ID = "JUNGLE_AXE";
    private static final String TREECAPITATOR_ID = "TREECAPITATOR_AXE";
	private static final String FIG_AXE_ID = "FIG_AXE";
	private static final String FIGSTONE_ID = "FIGSTONE_AXE";
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

    @Init
    public static void init() {
        UseItemCallback.EVENT.register(ItemCooldowns::onItemInteract);
    }

    private static ActionResult onItemInteract(PlayerEntity player, World world, Hand hand) {
		if (!SkyblockerConfigManager.get().uiAndVisuals.itemCooldown.enableItemCooldowns)
			return ActionResult.PASS;
		String usedItemId = ItemUtils.getItemId(player.getMainHandStack());
		switch (usedItemId) {
			case FIG_AXE_ID, FIGSTONE_ID, JUNGLE_AXE_ID, TREECAPITATOR_ID -> handleItemCooldown(usedItemId, 1000);
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
        for (ItemStack stack : ItemUtils.getArmor(player)) {
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
