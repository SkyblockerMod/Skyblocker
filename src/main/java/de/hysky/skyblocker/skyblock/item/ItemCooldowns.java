package de.hysky.skyblocker.skyblock.item;

import com.google.common.collect.ImmutableList;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class ItemCooldowns {
    private static final String JUNGLE_AXE_ID = "JUNGLE_AXE";
    private static final String TREECAPITATOR_ID = "TREECAPITATOR_AXE";
    private static final String GRAPPLING_HOOK_ID = "GRAPPLING_HOOK";
    private static final ImmutableList<String> BAT_ARMOR_IDS = ImmutableList.of("BAT_PERSON_HELMET", "BAT_PERSON_CHESTPLATE", "BAT_PERSON_LEGGINGS", "BAT_PERSON_BOOTS");

    private static final Map<String, CooldownEntry> ITEM_COOLDOWNS = new HashMap<>();

    public static void init() {
        ClientPlayerBlockBreakEvents.AFTER.register(ItemCooldowns::afterBlockBreak);
        UseItemCallback.EVENT.register(ItemCooldowns::onItemInteract);
    }

    public static void afterBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state) {
        if (!SkyblockerConfigManager.get().general.itemCooldown.enableItemCooldowns) return;

        String usedItemId = ItemUtils.getItemId(player.getMainHandStack());
        if (usedItemId.isEmpty()) return;

        if (state.isIn(BlockTags.LOGS)) {
            if (usedItemId.equals(JUNGLE_AXE_ID)) {
                if (!isOnCooldown(JUNGLE_AXE_ID)) {
                    ITEM_COOLDOWNS.put(JUNGLE_AXE_ID, new CooldownEntry(2000));
                }
            } else if (usedItemId.equals(TREECAPITATOR_ID)) {
                if (!isOnCooldown(TREECAPITATOR_ID)) {
                    ITEM_COOLDOWNS.put(TREECAPITATOR_ID, new CooldownEntry(2000));
                }
            }
        }
    }

    private static TypedActionResult<ItemStack> onItemInteract(PlayerEntity player, World world, Hand hand) {
        if (!SkyblockerConfigManager.get().general.itemCooldown.enableItemCooldowns) return TypedActionResult.pass(ItemStack.EMPTY);

        String usedItemId = ItemUtils.getItemId(player.getMainHandStack());
        if (usedItemId.equals(GRAPPLING_HOOK_ID) && player.fishHook != null) {
            if (!isOnCooldown(GRAPPLING_HOOK_ID) && !isWearingBatArmor(player)) {
                ITEM_COOLDOWNS.put(GRAPPLING_HOOK_ID, new CooldownEntry(2000));
            }
        }

        return TypedActionResult.pass(ItemStack.EMPTY);
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
