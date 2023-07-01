package me.xmrvizzy.skyblocker.skyblock.item;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPInputStream;

public class PriceInfoTooltip {
    private static final Logger LOGGER = LoggerFactory.getLogger(PriceInfoTooltip.class.getName());
    private static final SkyblockerMod skyblocker = SkyblockerMod.getInstance();
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static JsonObject npcPricesJson;
    private static JsonObject bazaarPricesJson;
    private static JsonObject oneDayAvgPricesJson;
    private static JsonObject threeDayAvgPricesJson;
    private static JsonObject lowestPricesJson;
    private static JsonObject isMuseumJson;
    private static JsonObject motesPricesJson;
    private static boolean nullMsgSend = false;
    private final static Gson gson = new Gson();
    private static final Map<String, String> apiAddresses;

    public static void onInjectTooltip(ItemStack stack, TooltipContext context, List<Text> lines) {
        if (!Utils.isOnSkyblock() || client.player == null) return;

        String name = getInternalNameFromNBT(stack);
        if (name == null) return;

        int count = stack.getCount();
        boolean bazaarOpened = lines.stream().anyMatch(each -> each.getString().contains("Buy price:") || each.getString().contains("Sell price:"));

        if (SkyblockerConfig.get().general.itemTooltip.enableNPCPrice) {
            if (npcPricesJson == null) {
                nullWarning();
            }
            else if (npcPricesJson.has(name)) {
                lines.add(Text.literal(String.format("%-21s", "NPC Price:"))
                        .formatted(Formatting.YELLOW)
                        .append(getCoinsMessage(npcPricesJson.get(name).getAsDouble(), count)));
            }
        }
        
        if (SkyblockerConfig.get().general.itemTooltip.enableMotesPrice && Utils.isInTheRift()) {
            if(motesPricesJson == null) {
        		nullWarning();
        	}
        	else if (motesPricesJson.has(name)) {
        		lines.add(Text.literal(String.format("%-20s", "Motes Price:"))
        				.formatted(Formatting.LIGHT_PURPLE)
        				.append(getMotesMessage(motesPricesJson.get(name).getAsInt(), count)));
        	}
        }

        boolean bazaarExist = false;
        if (SkyblockerConfig.get().general.itemTooltip.enableBazaarPrice && !bazaarOpened) {
            if (bazaarPricesJson == null) {
                nullWarning();
            }
            else if (bazaarPricesJson.has(name)) {
                JsonObject getItem = bazaarPricesJson.getAsJsonObject(name);
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
        }

        // bazaarOpened & bazaarExist check for lbin, because Skytils keeps some bazaar item data in lbin api
        boolean lbinExist = false;
        if (SkyblockerConfig.get().general.itemTooltip.enableLowestBIN && !bazaarOpened && !bazaarExist) {
            if (lowestPricesJson == null) {
                nullWarning();
            }
            else if (lowestPricesJson.has(name)) {
                lines.add(Text.literal(String.format("%-19s", "Lowest BIN Price:"))
                        .formatted(Formatting.GOLD)
                        .append(getCoinsMessage(lowestPricesJson.get(name).getAsDouble(), count)));
                lbinExist = true;
            }
        }

        if (SkyblockerConfig.get().general.itemTooltip.enableAvgBIN) {
            if (threeDayAvgPricesJson == null || oneDayAvgPricesJson == null) {
                nullWarning();
            }
            else {
                /*
                  We are skipping check average prices for potions, runes
                  and enchanted books because there is no data for their in API.
                 */
                if (name.contains("PET-")) {
                    name = name.replace("PET-", "")
                            .replace("UNCOMMON", "1")
                            .replace("COMMON", "0")
                            .replace("RARE", "2")
                            .replace("EPIC", "3")
                            .replace("LEGENDARY", "4")
                            .replace("MYTHIC", "5")
                            .replace("-", ";");
                } else if (name.contains("RUNE-")) {
                    name = name.replace("RUNE-", "");
                    name = name.substring(0, name.indexOf("-")) + "_RUNE;" + name.substring(name.lastIndexOf("-") + 1);
                } else if (name.contains("POTION-") || name.contains("ENCHANTED_BOOK-")) {
                    name = "";
                } else {
                    name = name.replace(":", "-");
                }

                if (!name.isEmpty() && lbinExist) {
                    SkyblockerConfig.Average type = SkyblockerConfig.get().general.itemTooltip.avg;

                    // "No data" line because of API not keeping old data, it causes NullPointerException
                    if (type == SkyblockerConfig.Average.ONE_DAY || type == SkyblockerConfig.Average.BOTH) {
                        lines.add(
                                Text.literal(String.format("%-19s", "1 Day Avg. Price:"))
                                        .formatted(Formatting.GOLD)
                                        .append(oneDayAvgPricesJson.get(name) == null
                                                ? Text.literal("No data").formatted(Formatting.RED)
                                                : getCoinsMessage(oneDayAvgPricesJson.get(name).getAsDouble(), count)
                                        )
                        );
                    }
                    if (type == SkyblockerConfig.Average.THREE_DAY || type == SkyblockerConfig.Average.BOTH) {
                        lines.add(
                                Text.literal(String.format("%-19s", "3 Day Avg. Price:"))
                                        .formatted(Formatting.GOLD)
                                        .append(threeDayAvgPricesJson.get(name) == null
                                                ? Text.literal("No data").formatted(Formatting.RED)
                                                : getCoinsMessage(threeDayAvgPricesJson.get(name).getAsDouble(), count)
                                        )
                        );
                    }
                }
            }
        }

        if (SkyblockerConfig.get().general.itemTooltip.enableMuseumDate && !bazaarOpened) {
            if (isMuseumJson == null) {
                nullWarning();
            }
            else {
                String timestamp = getTimestamp(stack);

                if (isMuseumJson.has(name)) {
                    String itemCategory = isMuseumJson.get(name).toString().replaceAll("\"", "");
                    String format = switch (itemCategory) {
                        case "Weapons" -> "%-18s";
                        case "Armor" -> "%-19s";
                        default -> "%-20s";
                    };
                    lines.add(Text.literal(String.format(format, "Museum: (" + itemCategory + ")"))
                            .formatted(Formatting.LIGHT_PURPLE)
                            .append(Text.literal(timestamp).formatted(Formatting.RED)));
                } else if (!timestamp.isEmpty()) {
                    lines.add(Text.literal(String.format("%-21s", "Obtained: "))
                            .formatted(Formatting.LIGHT_PURPLE)
                            .append(Text.literal(timestamp).formatted(Formatting.RED)));
                }
            }
        }
    }
    
    private static void nullWarning() {
        if (!nullMsgSend && client.player != null) {
            client.player.sendMessage(Text.translatable("skyblocker.itemTooltip.nullMessage"), false);
            nullMsgSend = true;
        }
    }

    public static NbtCompound getItemNBT(ItemStack stack) {
        if (stack == null) return null;
        return stack.getNbt();
    }

    /**
     * this method converts the "timestamp" variable into the same date format as Hypixel represents it in the museum.
     * Currently, there are two types of timestamps the legacy which is built like this
     * "dd/MM/yy hh:mm" ("25/04/20 16:38") and the current which is built like this
     * "MM/dd/yy hh:mm aa" ("12/24/20 11:08 PM"). Since Hypixel transforms the two formats into one format without
     * taking into account of their formats, we do the same. The final result looks like this
     * "MMMM dd, yyyy" (December 24, 2020).
     * Since the legacy format has a 25 as "month" SimpleDateFormat converts the 25 into 2 years and 1 month and makes
     * "25/04/20 16:38" -> "January 04, 2022" instead of "April 25, 2020".
     * This causes the museum rank to be much worse than it should be.
     *
     * @param stack the item under the pointer
     * @return if the item have a "Timestamp" it will be shown formated on the tooltip
     */
    public static String getTimestamp(ItemStack stack) {
        NbtCompound tag = getItemNBT(stack);

        if (tag != null && tag.contains("ExtraAttributes", 10)) {
            NbtCompound ea = tag.getCompound("ExtraAttributes");

            if (ea.contains("timestamp", 8)) {
                SimpleDateFormat nbtFormat = new SimpleDateFormat("MM/dd/yy");

                try {
                    Date date = nbtFormat.parse(ea.getString("timestamp"));
                    SimpleDateFormat skyblockerFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH);
                    return skyblockerFormat.format(date);
                } catch (ParseException e) {
                    LOGGER.warn("[Skyblocker-tooltip] getTimestamp", e);
                }
            }
        }

        return "";
    }

    public static String getInternalNameFromNBT(ItemStack stack) {
        NbtCompound tag = getItemNBT(stack);
        if (tag != null && tag.contains("ExtraAttributes", 10)) {
            NbtCompound ea = tag.getCompound("ExtraAttributes");

            if (ea.contains("id", 8)) {
                String internalName = ea.getString("id");

                // Transformation to API format.
                if ("ENCHANTED_BOOK".equals(internalName)) {
                    if (ea.contains("enchantments")) {
                        NbtCompound enchants = ea.getCompound("enchantments");
                        String enchant = enchants.getKeys().stream().findFirst().get();
                        return internalName + "-" + enchant.toUpperCase(Locale.ENGLISH) + "-" + enchants.getInt(enchant);
                    }
                } else if ("PET".equals(internalName)) {
                    if (ea.contains("petInfo")) {
                        JsonObject petInfo = gson.fromJson(ea.getString("petInfo"), JsonObject.class);
                        return internalName + "-" + petInfo.get("type").getAsString() + "-" + petInfo.get("tier").getAsString();
                    }
                } else if ("POTION".equals(internalName)) {
                    // New API just contains 'enhanced' tag.
                    String enhanced = ea.contains("enhanced") ? "-ENHANCED" : "";
                    //String extended = ea.contains("extended") ? "-EXTENDED" : "";
                    //String splash = ea.contains("splash") ? "-SPLASH" : "";
                    if (ea.contains("potion") && ea.contains("potion_level")) {
                        return internalName + "-" + ea.getString("potion").toUpperCase(Locale.ENGLISH) + "-" + ea.getInt("potion_level")
                                + enhanced; //+ extended + splash;
                    }
                } else if ("RUNE".equals(internalName)) {
                    if (ea.contains("runes")) {
                        NbtCompound runes = ea.getCompound("runes");
                        String rune = runes.getKeys().stream().findFirst().get();
                        return internalName + "-" + rune.toUpperCase(Locale.ENGLISH) + "-" + runes.getInt(rune);
                    }
                }

                return internalName;
            }
            else
                return null;
        }
        else
            return null;
    }

    private static Text getCoinsMessage(double price, int count) {
        if (count == 1) {
            String priceString = String.format(Locale.ENGLISH, "%1$,.1f", price);
            return Text.literal(priceString + " Coins").formatted(Formatting.DARK_AQUA);
        }
        else {
            String priceStringTotal = String.format(Locale.ENGLISH, "%1$,.1f", price * count);
            MutableText priceTextTotal = Text.literal(priceStringTotal + " Coins ").formatted(Formatting.DARK_AQUA);

            String priceStringEach = String.format(Locale.ENGLISH, "%1$,.1f", price);
            MutableText priceTextEach =  Text.literal( "(" + priceStringEach + " each)").formatted(Formatting.GRAY);

            return priceTextTotal.append(priceTextEach);
        }
    }
    
    private static Text getMotesMessage(int price, int count) {
        float motesMultiplier = SkyblockerConfig.get().locations.rift.mcGrubberStacks * 0.05f + 1;
        if (count == 1) {
            String priceString = String.format(Locale.ENGLISH, "%1$,.1f", price * motesMultiplier).replace(".0", "");
            return Text.literal(priceString + " Motes").formatted(Formatting.DARK_AQUA);
        }
        else {
            String priceStringTotal = String.format(Locale.ENGLISH, "%1$,.1f", price * count * motesMultiplier).replace(".0", "");
            MutableText priceTextTotal = Text.literal(priceStringTotal + " Motes ").formatted(Formatting.DARK_AQUA);

            String priceStringEach = String.format(Locale.ENGLISH, "%1$,.1f", price * motesMultiplier).replace(".0", "");
            MutableText priceTextEach =  Text.literal( "(" + priceStringEach + " each)").formatted(Formatting.GRAY);

            return priceTextTotal.append(priceTextEach);
        }
    }

    // If these options is true beforehand, the client will get first data of these options while loading.
    // After then, it will only fetch the data if it is on Skyblock.
    public static int minute = -1;
    public static void init() {
        skyblocker.scheduler.scheduleCyclic(() -> {
            if (!Utils.isOnSkyblock() && 0 < minute++) {
                nullMsgSend = false;
                return;
            }

            List<CompletableFuture<Void>> futureList = new ArrayList<>();
            if ((SkyblockerConfig.get().general.itemTooltip.enableAvgBIN) && (oneDayAvgPricesJson == null || threeDayAvgPricesJson == null || minute % 5 == 0)) {
                SkyblockerConfig.Average type = SkyblockerConfig.get().general.itemTooltip.avg;

                if (type == SkyblockerConfig.Average.BOTH || oneDayAvgPricesJson == null || threeDayAvgPricesJson == null) {
                    futureList.add(CompletableFuture.runAsync(() -> oneDayAvgPricesJson = downloadPrices("1 day avg")));
                    futureList.add(CompletableFuture.runAsync(() -> threeDayAvgPricesJson = downloadPrices("3 day avg")));
                }
                else if (type == SkyblockerConfig.Average.ONE_DAY) {
                    futureList.add(CompletableFuture.runAsync(() -> oneDayAvgPricesJson = downloadPrices("1 day avg")));
                }
                else if (type == SkyblockerConfig.Average.THREE_DAY) {
                    futureList.add(CompletableFuture.runAsync(() -> threeDayAvgPricesJson = downloadPrices("3 day avg")));
                }
            }
            if (SkyblockerConfig.get().general.itemTooltip.enableLowestBIN)
                futureList.add(CompletableFuture.runAsync(() -> lowestPricesJson = downloadPrices("lowest bins")));

            if (SkyblockerConfig.get().general.itemTooltip.enableBazaarPrice)
                futureList.add(CompletableFuture.runAsync(() -> bazaarPricesJson = downloadPrices("bazaar")));

            if (SkyblockerConfig.get().general.itemTooltip.enableNPCPrice && npcPricesJson == null)
                futureList.add(CompletableFuture.runAsync(() -> npcPricesJson = downloadPrices("npc")));

            if (SkyblockerConfig.get().general.itemTooltip.enableMuseumDate && isMuseumJson == null)
                futureList.add(CompletableFuture.runAsync(() -> isMuseumJson = downloadPrices("museum")));
            
            if (SkyblockerConfig.get().general.itemTooltip.enableMotesPrice && motesPricesJson == null)
            	futureList.add(CompletableFuture.runAsync(() -> motesPricesJson = downloadPrices("motes")));

            minute++;
            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]))
                    .whenComplete((unused, throwable) -> nullMsgSend = false);
        }, 1200);
    }

    private static JsonObject downloadPrices(String type) {
        try {
            String url = apiAddresses.get(type);
            URL apiAddress = new URL(url);
            InputStream src = apiAddress.openStream();
            InputStreamReader reader = new InputStreamReader(url.contains(".gz") ? new GZIPInputStream(src) : src);
            return new Gson().fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            LOGGER.warn("[Skyblocker] Failed to download " + type + " prices!", e);
            return null;
        }
    }

    static {
        apiAddresses = new HashMap<>();
        apiAddresses.put("1 day avg", "https://moulberry.codes/auction_averages_lbin/1day.json.gz");
        apiAddresses.put("3 day avg", "https://moulberry.codes/auction_averages_lbin/3day.json.gz");
        apiAddresses.put("bazaar", "https://hysky.de/api/bazaar");
        apiAddresses.put("lowest bins", "https://lb.tricked.pro/lowestbins");
        apiAddresses.put("npc", "https://hysky.de/api/npcprice");
        apiAddresses.put("museum", "https://hysky.de/api/museum");
        apiAddresses.put("motes", "https://hysky.de/api/motesprice");
    }
}
