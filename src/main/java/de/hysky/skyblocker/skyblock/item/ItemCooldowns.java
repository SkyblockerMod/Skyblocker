package de.hysky.skyblocker.skyblock.item;

import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.common.collect.ImmutableList;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.ApiUtils;
import de.hysky.skyblocker.utils.Http;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
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

import com.mojang.util.UndashedUuid;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

import java.util.stream.StreamSupport;
import java.util.concurrent.CompletableFuture;

public class ItemCooldowns {
    private static final String JUNGLE_AXE_ID = "JUNGLE_AXE";
    private static final String TREECAPITATOR_ID = "TREECAPITATOR_AXE";
    private static final String GRAPPLING_HOOK_ID = "GRAPPLING_HOOK";
    private static final ImmutableList<String> BAT_ARMOR_IDS = ImmutableList.of("BAT_PERSON_HELMET", "BAT_PERSON_CHESTPLATE", "BAT_PERSON_LEGGINGS", "BAT_PERSON_BOOTS");
    public static final long HypixelApiCooldown = 180000; //3min 180000
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
    public static int MonkeyLevel = 1;
    public static double CalcMonkeyExp = 0;
    public static double currentCooldown = 0;
    public static long unixTimeStamp = 0;

    public static void init() {
        ClientPlayerBlockBreakEvents.AFTER.register(ItemCooldowns::afterBlockBreak);
        UseItemCallback.EVENT.register(ItemCooldowns::onItemInteract);
        unixTimeStamp = System.currentTimeMillis() - HypixelApiCooldown;
    }

    public static void currentCooldown() {
        long DeltaTime = System.currentTimeMillis() - unixTimeStamp;
        if (DeltaTime < HypixelApiCooldown) {
            return;
        }
        unixTimeStamp = System.currentTimeMillis();
        CompletableFuture.runAsync(() -> {
            String name = MinecraftClient.getInstance().getSession().getUsername();
            String playeruuid = ApiUtils.name2Uuid(name);
            try (Http.ApiResponse response = Http.sendHypixelRequest("skyblock/profiles", "?uuid=" + playeruuid)) {
                if (!response.ok())
                    throw new IllegalStateException("Failed to get profile uuid for player " + name + "! Response: " + response.content());
                JsonObject responseJson = JsonParser.parseString(response.content()).getAsJsonObject();
                JsonObject players = StreamSupport.stream(responseJson.getAsJsonArray("profiles").spliterator(), false)
                        .map(JsonElement::getAsJsonObject)
                        .filter(profile -> profile.getAsJsonPrimitive("selected").getAsBoolean())
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No selected profile found!?"))
                        .getAsJsonObject("members").entrySet().stream()
                        .filter(entry -> entry.getKey().equals(playeruuid))
                        .map(Map.Entry::getValue)
                        .map(JsonElement::getAsJsonObject)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Player somehow not found inside their own profile!"));
                for (JsonElement element : players.getAsJsonObject("pets_data").getAsJsonArray("pets")) {
                    if (!element.getAsJsonObject().get("type").getAsString().equals("MONKEY")) continue;
                    if (!element.getAsJsonObject().get("active").getAsString().equals("true")) continue;
                    if (element.getAsJsonObject().get("tier").getAsString().equals("LEGENDARY")) {
                        CalcMonkeyExp = Double.parseDouble(element.getAsJsonObject().get("exp").getAsString());
                        MonkeyLevel = 0;
                        for (int xpLevel : EXPERIENCE_LEVELS) {
                            if (CalcMonkeyExp < xpLevel) {
                                break;
                            } else {
                                CalcMonkeyExp -= xpLevel;
                                MonkeyLevel++;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("[Skyblocker] Failed to get Player Pet Data, is the API Down/Limited?");
            }
            double BaseCooldown = 2000;
            double EvolvedAxesCooldownReductionPercentage = MonkeyLevel * 0.5;
            double MonkeyPetCDRReduction = (BaseCooldown * EvolvedAxesCooldownReductionPercentage) / 100;
            currentCooldown = BaseCooldown - MonkeyPetCDRReduction;
        });
    }

    public static void afterBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state) {
        if (!SkyblockerConfigManager.get().general.itemCooldown.enableItemCooldowns) return;
        String usedItemId = ItemUtils.getItemId(player.getMainHandStack());
        if (usedItemId.isEmpty()) return;
        if (state.isIn(BlockTags.LOGS)) {
            currentCooldown();
            if (usedItemId.equals(JUNGLE_AXE_ID) || usedItemId.equals(TREECAPITATOR_ID)) {
                if (!isOnCooldown(JUNGLE_AXE_ID) || !isOnCooldown(TREECAPITATOR_ID)) {
                    ITEM_COOLDOWNS.put(usedItemId, new CooldownEntry((int) currentCooldown));
                }
            }
        }
    }

    private static TypedActionResult<ItemStack> onItemInteract(PlayerEntity player, World world, Hand hand) {
        if (!SkyblockerConfigManager.get().general.itemCooldown.enableItemCooldowns)
            return TypedActionResult.pass(ItemStack.EMPTY);

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