package de.hysky.skyblocker.skyblock.item.tooltip;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.GeneralConfig;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ItemTooltip {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ItemTooltip.class.getName());
    private static final MinecraftClient client = MinecraftClient.getInstance();
    protected static final GeneralConfig.ItemTooltip config = SkyblockerConfigManager.get().general.itemTooltip;
    private static volatile boolean sentNullWarning = false;

    public static void getTooltip(ItemStack stack, Item.TooltipContext tooltipContext, TooltipType tooltipType, List<Text> lines) {
        if (!Utils.isOnSkyblock() || client.player == null) return;

        String name = getInternalNameFromNBT(stack, false);
        String internalID = getInternalNameFromNBT(stack, true);
        String neuName = name;
        if (name == null || internalID == null) return;

        if (name.startsWith("ISSHINY_")) {
            name = "SHINY_" + internalID;
            neuName = internalID;
        }

        if (lines.isEmpty()) {
            return;
        }

        int count = stack.getCount();

        boolean bazaarExist = false;

        if (TooltipInfoType.BAZAAR.isTooltipEnabledAndHasOrNullWarning(name)) {
            JsonObject getItem = TooltipInfoType.BAZAAR.getData().getAsJsonObject(name);
            lines.add(Text.literal(String.format("%-18s", "Bazaar buy Price:"))
                    .formatted(Formatting.GOLD)
                    .append(getItem.get("buyPrice").isJsonNull()
                            ? Text.literal("No data").formatted(Formatting.RED)
                            : getCoinsMessage(getItem.get("buyPrice").getAsDouble(), count)));
            lines.add(Text.literal(String.format("%-19s", "Bazaar sell Price:"))
                    .formatted(Formatting.GOLD)
                    .append(getItem.get("sellPrice").isJsonNull()
                            ? Text.literal("No data").formatted(Formatting.RED)
                            : getCoinsMessage(getItem.get("sellPrice").getAsDouble(), count)));
            bazaarExist = true;
        }

        // bazaarOpened & bazaarExist check for lbin, because Skytils keeps some bazaar item data in lbin api
        boolean lbinExist = false;
        if (TooltipInfoType.LOWEST_BINS.isTooltipEnabledAndHasOrNullWarning(name) && !bazaarExist) {
            lines.add(Text.literal(String.format("%-19s", "Lowest BIN Price:"))
                    .formatted(Formatting.GOLD)
                    .append(getCoinsMessage(TooltipInfoType.LOWEST_BINS.getData().get(name).getAsDouble(), count)));
            lbinExist = true;
        }

        if (SkyblockerConfigManager.get().general.itemTooltip.enableAvgBIN) {
            if (TooltipInfoType.ONE_DAY_AVERAGE.getData() == null || TooltipInfoType.THREE_DAY_AVERAGE.getData() == null) {
                nullWarning();
            } else {
                /*
                  We are skipping check average prices for potions, runes
                  and enchanted books because there is no data for their in API.
                 */
                neuName = getNeuName(internalID, neuName);

                if (!neuName.isEmpty() && lbinExist) {
                    GeneralConfig.Average type = config.avg;

                    // "No data" line because of API not keeping old data, it causes NullPointerException
                    if (type == GeneralConfig.Average.ONE_DAY || type == GeneralConfig.Average.BOTH) {
                        lines.add(
                                Text.literal(String.format("%-19s", "1 Day Avg. Price:"))
                                        .formatted(Formatting.GOLD)
                                        .append(TooltipInfoType.ONE_DAY_AVERAGE.getData().get(neuName) == null
                                                ? Text.literal("No data").formatted(Formatting.RED)
                                                : getCoinsMessage(TooltipInfoType.ONE_DAY_AVERAGE.getData().get(neuName).getAsDouble(), count)
                                        )
                        );
                    }
                    if (type == GeneralConfig.Average.THREE_DAY || type == GeneralConfig.Average.BOTH) {
                        lines.add(
                                Text.literal(String.format("%-19s", "3 Day Avg. Price:"))
                                        .formatted(Formatting.GOLD)
                                        .append(TooltipInfoType.THREE_DAY_AVERAGE.getData().get(neuName) == null
                                                ? Text.literal("No data").formatted(Formatting.RED)
                                                : getCoinsMessage(TooltipInfoType.THREE_DAY_AVERAGE.getData().get(neuName).getAsDouble(), count)
                                        )
                        );
                    }
                }
            }
        }
    }

    @NotNull
    public static String getNeuName(String internalID, String neuName) {
        switch (internalID) {
            case "PET" -> {
                neuName = neuName.replaceAll("LVL_\\d*_", "");
                String[] parts = neuName.split("_");
                String type = parts[0];
                neuName = neuName.replaceAll(type + "_", "");
                neuName = neuName + "-" + type;
                neuName = neuName.replace("UNCOMMON", "1")
                        .replace("COMMON", "0")
                        .replace("RARE", "2")
                        .replace("EPIC", "3")
                        .replace("LEGENDARY", "4")
                        .replace("MYTHIC", "5")
                        .replace("-", ";");
            }
            case "RUNE" -> neuName = neuName.replaceAll("_(?!.*_)", ";");
            case "POTION" -> neuName = "";
            case "ATTRIBUTE_SHARD" ->
                    neuName = internalID + "+" + neuName.replace("SHARD-", "").replaceAll("_(?!.*_)", ";");
            default -> neuName = neuName.replace(":", "-");
        }
        return neuName;
    }


    public static void nullWarning() {
        if (!sentNullWarning && client.player != null) {
            LOGGER.warn(Constants.PREFIX.get().append(Text.translatable("skyblocker.itemTooltip.nullMessage")).getString());
            sentNullWarning = true;
        }
    }

    // TODO What in the world is this?
    public static String getInternalNameFromNBT(ItemStack stack, boolean internalIDOnly) {
        NbtCompound customData = ItemUtils.getCustomData(stack);

        if (customData == null || !customData.contains(ItemUtils.ID, NbtElement.STRING_TYPE)) {
            return null;
        }
        String internalName = customData.getString(ItemUtils.ID);

        if (internalIDOnly) {
            return internalName;
        }

        // Transformation to API format.
        if (customData.contains("is_shiny")) {
            return "ISSHINY_" + internalName;
        }

        switch (internalName) {
            case "ENCHANTED_BOOK" -> {
                if (customData.contains("enchantments")) {
                    NbtCompound enchants = customData.getCompound("enchantments");
                    Optional<String> firstEnchant = enchants.getKeys().stream().findFirst();
                    String enchant = firstEnchant.orElse("");
                    return "ENCHANTMENT_" + enchant.toUpperCase(Locale.ENGLISH) + "_" + enchants.getInt(enchant);
                }
            }
            case "PET" -> {
                if (customData.contains("petInfo")) {
                    JsonObject petInfo = SkyblockerMod.GSON.fromJson(customData.getString("petInfo"), JsonObject.class);
                    return "LVL_1_" + petInfo.get("tier").getAsString() + "_" + petInfo.get("type").getAsString();
                }
            }
            case "POTION" -> {
                String enhanced = customData.contains("enhanced") ? "_ENHANCED" : "";
                String extended = customData.contains("extended") ? "_EXTENDED" : "";
                String splash = customData.contains("splash") ? "_SPLASH" : "";
                if (customData.contains("potion") && customData.contains("potion_level")) {
                    return (customData.getString("potion") + "_" + internalName + "_" + customData.getInt("potion_level")
                            + enhanced + extended + splash).toUpperCase(Locale.ENGLISH);
                }
            }
            case "RUNE" -> {
                if (customData.contains("runes")) {
                    NbtCompound runes = customData.getCompound("runes");
                    Optional<String> firstRunes = runes.getKeys().stream().findFirst();
                    String rune = firstRunes.orElse("");
                    return rune.toUpperCase(Locale.ENGLISH) + "_RUNE_" + runes.getInt(rune);
                }
            }
            case "ATTRIBUTE_SHARD" -> {
                if (customData.contains("attributes")) {
                    NbtCompound shards = customData.getCompound("attributes");
                    Optional<String> firstShards = shards.getKeys().stream().findFirst();
                    String shard = firstShards.orElse("");
                    return internalName + "-" + shard.toUpperCase(Locale.ENGLISH) + "_" + shards.getInt(shard);
                }
            }
        }
        return internalName;
    }

    public static Text getCoinsMessage(double price, int count) {
        // Format the price string once
        String priceString = String.format(Locale.ENGLISH, "%1$,.1f", price);

        // If count is 1, return a simple message
        if (count == 1) {
            return Text.literal(priceString + " Coins").formatted(Formatting.DARK_AQUA);
        }

        // If count is greater than 1, include the "each" information
        String priceStringTotal = String.format(Locale.ENGLISH, "%1$,.1f", price * count);

        return Text.literal(priceStringTotal + " Coins ").formatted(Formatting.DARK_AQUA)
                   .append(Text.literal("(" + priceString + " each)").formatted(Formatting.GRAY));
    }

    // If these options is true beforehand, the client will get first data of these options while loading.
    // After then, it will only fetch the data if it is on Skyblock.
    public static int minute = 0;

    public static void init() {
        Scheduler.INSTANCE.scheduleCyclic(() -> {
            if (!Utils.isOnSkyblock() && 0 < minute) {
                sentNullWarning = false;
                return;
            }

            if (++minute % 60 == 0) {
                sentNullWarning = false;
            }

            List<CompletableFuture<Void>> futureList = new ArrayList<>();

            TooltipInfoType.NPC.downloadIfEnabled(futureList);
            TooltipInfoType.BAZAAR.downloadIfEnabled(futureList);
            TooltipInfoType.LOWEST_BINS.downloadIfEnabled(futureList);

            if (config.enableAvgBIN) {
                GeneralConfig.Average type = config.avg;

                if (type == GeneralConfig.Average.BOTH || TooltipInfoType.ONE_DAY_AVERAGE.getData() == null || TooltipInfoType.THREE_DAY_AVERAGE.getData() == null || minute % 5 == 0) {
                    TooltipInfoType.ONE_DAY_AVERAGE.download(futureList);
                    TooltipInfoType.THREE_DAY_AVERAGE.download(futureList);
                } else if (type == GeneralConfig.Average.ONE_DAY) {
                    TooltipInfoType.ONE_DAY_AVERAGE.download(futureList);
                } else if (type == GeneralConfig.Average.THREE_DAY) {
                    TooltipInfoType.THREE_DAY_AVERAGE.download(futureList);
                }
            }

            TooltipInfoType.MOTES.downloadIfEnabled(futureList);
            TooltipInfoType.MUSEUM.downloadIfEnabled(futureList);
            TooltipInfoType.COLOR.downloadIfEnabled(futureList);
            TooltipInfoType.ACCESSORIES.downloadIfEnabled(futureList);

            CompletableFuture.allOf(futureList.toArray(CompletableFuture[]::new)).exceptionally(e -> {
                LOGGER.error("Encountered unknown error while downloading tooltip data", e);
                return null;
            });
        }, 1200, true);
    }
}