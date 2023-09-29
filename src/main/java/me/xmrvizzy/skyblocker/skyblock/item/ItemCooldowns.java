package me.xmrvizzy.skyblocker.skyblock.item;

import com.google.common.collect.ImmutableList;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.events.ClientPlayerBlockBreakEvent;
import me.xmrvizzy.skyblocker.utils.ItemUtils;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
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

    private static final Map<String, CooldownEntry> itemCooldowns = new HashMap<>();
    private static SkyblockerConfig.ItemCooldown config;

    public static void init() {
        ClientPlayerBlockBreakEvent.AFTER.register(ItemCooldowns::afterBlockBreak);
        UseItemCallback.EVENT.register(ItemCooldowns::onItemInteract);
        config = SkyblockerConfig.get().general.itemCooldown;
    }

    public static void afterBlockBreak(BlockPos pos, PlayerEntity player) {
        if (!config.enableItemCooldowns) return;

        String usedItemId = ItemUtils.getItemId(player.getMainHandStack());
        if (usedItemId == null) return;

        if (usedItemId.equals(JUNGLE_AXE_ID)) {
            if (!isItemOnCooldown(JUNGLE_AXE_ID)) {
                itemCooldowns.put(JUNGLE_AXE_ID, new CooldownEntry(2000));
            }
        }
        else if (usedItemId.equals(TREECAPITATOR_ID)) {
            if (!isItemOnCooldown(TREECAPITATOR_ID)) {
                itemCooldowns.put(TREECAPITATOR_ID, new CooldownEntry(2000));
            }
        }
    }

    private static TypedActionResult<ItemStack> onItemInteract(PlayerEntity player, World world, Hand hand) {
        if (!config.enableItemCooldowns) return TypedActionResult.pass(ItemStack.EMPTY);

        String usedItemId = ItemUtils.getItemId(player.getMainHandStack());
        if (usedItemId != null && usedItemId.equals(GRAPPLING_HOOK_ID) && player.fishHook != null) {
            if (!isItemOnCooldown(GRAPPLING_HOOK_ID) && !isPlayerWearingBatArmor(player)) {
                itemCooldowns.put(GRAPPLING_HOOK_ID, new CooldownEntry(2000));
            }
        }

        return TypedActionResult.pass(ItemStack.EMPTY);
    }

    public static boolean isItemOnCooldown(ItemStack itemStack) {
        return isItemOnCooldown(ItemUtils.getItemId(itemStack));
    }

    private static boolean isItemOnCooldown(String itemId) {
        if (itemCooldowns.containsKey(itemId)) {
            CooldownEntry cooldownEntry = itemCooldowns.get(itemId);
            if (cooldownEntry.isOnCooldown()) {
                return true;
            }
            else {
                itemCooldowns.remove(cooldownEntry);
                return false;
            }
        }

        return false;
    }

    public static CooldownEntry getItemCooldownEntry(ItemStack itemStack) {
        return itemCooldowns.get(ItemUtils.getItemId(itemStack));
    }

    private static boolean isPlayerWearingBatArmor(PlayerEntity player) {
        for (ItemStack stack : player.getArmorItems()) {
            String itemId = ItemUtils.getItemId(stack);
            if (!BAT_ARMOR_IDS.contains(itemId)) {
                return false;
            }
        }
        return true;
    }

    public static class CooldownEntry {
        private final int cooldown;
        private final long startTime;

        public CooldownEntry(int cooldown) {
            this.cooldown = cooldown;
            this.startTime = System.currentTimeMillis();
        }

        public boolean isOnCooldown() {
            return (this.startTime + this.cooldown) > System.currentTimeMillis();
        }

        public long getRemainingCooldown() {
            long time = (this.startTime + this.cooldown) - System.currentTimeMillis();
            return time <= 0 ? 0 : time;
        }

        public float getRemainingCooldownPercent() {
            return this.isOnCooldown() ? ((float) this.getRemainingCooldown()) / ((float) cooldown) : 0.0f;
        }
    }
}
