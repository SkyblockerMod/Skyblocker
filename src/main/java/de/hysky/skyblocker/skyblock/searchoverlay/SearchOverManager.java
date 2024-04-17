package de.hysky.skyblocker.skyblock.searchoverlay;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType;
import de.hysky.skyblocker.utils.Http;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import io.github.moulberry.repo.data.NEUItem;
import it.unimi.dsi.fastutil.Pair;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class SearchOverManager {

    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static final Logger LOGGER = LoggerFactory.getLogger("Skyblocker Search Overlay");

    private static final Pattern BAZAAR_ENCHANTMENT_PATTERN = Pattern.compile("ENCHANTMENT_(\\D*)_(\\d+)");
    private static final Pattern AUCTION_PET_AND_RUNE_PATTERN = Pattern.compile("([A-Z0-9_]+);(\\d+)");

    /**
     * converts index (in array) +1 to a roman numeral
     */
    private static final String[] ROMAN_NUMERALS = new String[]{
            "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI",
            "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX", "XX"
    };

    private static @Nullable SignBlockEntity sign = null;
    private static boolean signFront = true;
    private static boolean isAuction;
    private static boolean isCommand;

    protected static String search = "";

    // Use non-final variables and swap them to prevent concurrent modification
    private static HashSet<String> bazaarItems;
    private static HashSet<String> auctionItems;
    private static HashMap<String, String> namesToId;

    public static String[] suggestionsArray = {};

    /**
     * uses the skyblock api and Moulberry auction to load a list of items in bazaar and auction house
     */
    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register(SearchOverManager::registerSearchCommands);
        NEURepoManager.runAsyncAfterLoad(SearchOverManager::loadItems);
    }

    private static void registerSearchCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        if (SkyblockerConfigManager.get().general.searchOverlay.enableCommands) {
            dispatcher.register(literal("ahs").executes(context -> startCommand(true)));
            dispatcher.register(literal("bzs").executes(context -> startCommand(false)));
        }
    }

    private static int startCommand(boolean isAuction) {
        isCommand = true;
        SearchOverManager.isAuction = isAuction;
        search = "";
        suggestionsArray = new String[]{};
        CLIENT.send(() -> CLIENT.setScreen(new OverlayScreen(Text.of(""))));
        return Command.SINGLE_SUCCESS;
    }

    private static void loadItems() {
        HashSet<String> bazaarItems = new HashSet<>();
        HashSet<String> auctionItems = new HashSet<>();
        HashMap<String, String> namesToId = new HashMap<>();

        //get bazaar items
        try {
            if (TooltipInfoType.BAZAAR.getData() == null) TooltipInfoType.BAZAAR.run();

            JsonObject products = TooltipInfoType.BAZAAR.getData();
            for (Map.Entry<String, JsonElement> entry : products.entrySet()) {
                if (entry.getValue().isJsonObject()) {
                    JsonObject product = entry.getValue().getAsJsonObject();
                    String id = product.get("id").getAsString();
                    int sellVolume = product.get("sellVolume").getAsInt();
                    if (sellVolume == 0)
                        continue; //do not add items that do not sell e.g. they are not actual in the bazaar
                    Matcher matcher = BAZAAR_ENCHANTMENT_PATTERN.matcher(id);
                    if (matcher.matches()) {//format enchantments
                        //remove ultimate if in name
                        String name = matcher.group(1);
                        if (!name.contains("WISE")) { //only way found to remove ultimate from everything but ultimate wise
                            name = name.replace("ULTIMATE_", "");
                        }
                        name = name.replace("_", " ");
                        name = capitalizeFully(name);
                        int enchantLevel = Integer.parseInt(matcher.group(2));
                        String level = "";
                        if (enchantLevel > 0) {
                            level = ROMAN_NUMERALS[enchantLevel - 1];
                        }
                        bazaarItems.add(name + " " + level);
                        namesToId.put(name + " " + level, matcher.group(1) + ";" + matcher.group(2));
                        continue;
                    }
                    //look up id for name
                    NEUItem neuItem = NEURepoManager.NEU_REPO.getItems().getItemBySkyblockId(id);
                    if (neuItem != null) {
                        String name = Formatting.strip(neuItem.getDisplayName());
                        bazaarItems.add(name);
                        namesToId.put(name, id);
                        continue;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("[Skyblocker] Failed to load bazaar item list! ", e);
        }

        //get auction items
        try {
            if (TooltipInfoType.THREE_DAY_AVERAGE.getData() == null) {
                TooltipInfoType.THREE_DAY_AVERAGE.run();
            }
            for (Map.Entry<String, JsonElement> entry : TooltipInfoType.THREE_DAY_AVERAGE.getData().entrySet()) {
                String id = entry.getKey();

                Matcher matcher = AUCTION_PET_AND_RUNE_PATTERN.matcher(id);
                if (matcher.find()) {//is a pet or rune convert id to name
                    String name = matcher.group(1).replace("_", " ");
                    name = capitalizeFully(name);
                    auctionItems.add(name);
                    namesToId.put(name, id);
                    continue;
                }
                //something else look up in NEU repo.
                id = id.split("[+-]")[0];
                NEUItem neuItem = NEURepoManager.NEU_REPO.getItems().getItemBySkyblockId(id);
                if (neuItem != null) {
                    String name = Formatting.strip(neuItem.getDisplayName());
                    auctionItems.add(name);
                    namesToId.put(name, id);
                    continue;
                }
            }
        } catch (Exception e) {
            LOGGER.error("[Skyblocker] Failed to load auction house item list! ", e);
        }

        SearchOverManager.bazaarItems = bazaarItems;
        SearchOverManager.auctionItems = auctionItems;
        SearchOverManager.namesToId = namesToId;
    }

    /**
     * Capitalizes the first letter off every word in a string
     * @param str string to capitalize
     */
    private static String capitalizeFully(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        return Arrays.stream(str.split("\\s+"))
                .map(t -> t.substring(0, 1).toUpperCase() + t.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    /**
     * Receives data when a search is started and resets values
     * @param sign the sign that is being edited
     * @param front if it's the front of the sign
     * @param isAuction if the sign is loaded from the auction house menu or bazaar
     */
    public static void updateSign(@NotNull SignBlockEntity sign, boolean front, boolean isAuction) {
        signFront = front;
        SearchOverManager.sign = sign;
        isCommand = false;
        SearchOverManager.isAuction = isAuction;
        if (SkyblockerConfigManager.get().general.searchOverlay.keepPreviousSearches) {
            Text[] messages = SearchOverManager.sign.getText(signFront).getMessages(CLIENT.shouldFilterText());
            search = messages[0].getString();
            if (!messages[1].getString().isEmpty()) {
                if (!search.endsWith(" ")) {
                    search += " ";
                }
                search += messages[1].getString();
            }
        } else {
            search = "";
        }
        suggestionsArray = new String[]{};
    }

    /**
     * Updates the search value and the suggestions based on that value.
     * @param newValue new search value
     */
    protected static void updateSearch(String newValue) {
        search = newValue;
        //update the suggestion values
        int totalSuggestions = SkyblockerConfigManager.get().general.searchOverlay.maxSuggestions;
        if (newValue.isBlank() || totalSuggestions == 0) return; //do not search for empty value
        suggestionsArray = (isAuction ? auctionItems : bazaarItems).stream().filter(item -> item.toLowerCase().contains(search.toLowerCase())).limit(totalSuggestions).toArray(String[]::new);
    }

    /**
     * Gets the suggestion in the suggestion array at the index
     * @param index index of suggestion
     */
    protected static String getSuggestion(int index) {
        if (suggestionsArray.length > index && suggestionsArray[index] != null) {
            return suggestionsArray[index];
        } else {//there are no suggestions yet
            return "";
        }
    }

    protected static String getSuggestionId(int index) {
        return namesToId.get(getSuggestion(index));
    }

    /**
     * Gets the item name in the history array at the index
     * @param index index of suggestion
     */
    protected static String getHistory(int index) {
        if (isAuction) {
            if (SkyblockerConfigManager.get().general.searchOverlay.auctionHistory.size() > index) {
                return SkyblockerConfigManager.get().general.searchOverlay.auctionHistory.get(index);
            }
        } else {
            if (SkyblockerConfigManager.get().general.searchOverlay.bazaarHistory.size() > index) {
                return SkyblockerConfigManager.get().general.searchOverlay.bazaarHistory.get(index);
            }
        }
        return null;
    }

    protected static String getHistoryId(int index) {
        return namesToId.get(getHistory(index));
    }

    /**
     * Add the current search value to the start of the history list and truncate to the max history value and save this to the config
     */
    private static void saveHistory() {
        //save to history
        SkyblockerConfig.SearchOverlay config = SkyblockerConfigManager.get().general.searchOverlay;
        if (isAuction) {
            if (config.auctionHistory.isEmpty() || !config.auctionHistory.get(0).equals(search)) {
                config.auctionHistory.add(0, search);
                if (config.auctionHistory.size() > config.historyLength) {
                    config.auctionHistory = config.auctionHistory.subList(0, config.historyLength);
                }
            }
        } else {
            if (config.bazaarHistory.isEmpty() || !config.bazaarHistory.get(0).equals(search)) {
                config.bazaarHistory.add(0, search);
                if (config.bazaarHistory.size() > config.historyLength) {
                    config.bazaarHistory = config.bazaarHistory.subList(0, config.historyLength);
                }
            }
        }
        SkyblockerConfigManager.save();
    }

    /**
     *Saves the current value of ({@link SearchOverManager#search}) then pushes it to a command or sign depending on how the gui was opened
     */
    protected static void pushSearch() {
        //save to history
        if (!search.isEmpty()) {
            saveHistory();
        }
        if (isCommand) {
            pushCommand();
        } else {
            pushSign();
        }
    }

    /**
     * runs the command to search for the value in ({@link SearchOverManager#search})
     */
    private static void pushCommand() {
        if (search.isEmpty()) return;

        String command;
        if (isAuction) {
            command = "/ahSearch " + search;
        } else {
            command = "/bz " + search;
        }
        MessageScheduler.INSTANCE.sendMessageAfterCooldown(command);
    }

    /**
     * pushes the ({@link SearchOverManager#search}) to the sign. It needs to targetSize it over two lines without splitting a word
     */
    private static void pushSign() {
        //splits text into 2 lines max = 30 chars
        Pair<String, String> split = splitString(search);

        // send packet to update sign
        if (CLIENT.player != null && sign != null) {
            Text[] messages = sign.getText(signFront).getMessages(CLIENT.shouldFilterText());
            CLIENT.player.networkHandler.sendPacket(new UpdateSignC2SPacket(sign.getPos(), signFront,
                    split.left(),
                    split.right(),
                    messages[2].getString(),
                    messages[3].getString()
            ));
        }
    }

    public static Pair<String, String> splitString(String s) {
        if (s.length() <= 15) {
            return Pair.of(s, "");
        }
        int index = s.lastIndexOf(' ', 15);
        if (index == -1) {
            return Pair.of(s.substring(0, 15), "");
        }
        return Pair.of(s.substring(0, index), s.substring(index + 1, Math.min(index + 16, s.length())).trim());
    }
}
