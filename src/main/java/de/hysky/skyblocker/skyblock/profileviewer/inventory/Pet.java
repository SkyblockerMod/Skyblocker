package de.hysky.skyblocker.skyblock.profileviewer.inventory;

import de.hysky.skyblocker.skyblock.PetCache;
import de.hysky.skyblocker.skyblock.itemlist.ItemFixerUpper;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.profileviewer.utils.LevelFinder;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.NEURepoManager;
import io.github.moulberry.repo.constants.PetNumbers;
import io.github.moulberry.repo.data.NEUItem;
import io.github.moulberry.repo.data.Rarity;
import io.github.moulberry.repo.util.PetId;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static de.hysky.skyblocker.skyblock.itemlist.ItemStackBuilder.SKULL_TEXTURE_PATTERN;
import static de.hysky.skyblocker.skyblock.itemlist.ItemStackBuilder.SKULL_UUID_PATTERN;

public class Pet {
    private final String name;
    private final double xp;
    private final String tier;
    private final Optional<String> heldItem;
    private final int level;
    private final ItemStack icon;

    private static final Map<String, Integer> TIER_MAP = Map.of(
            "COMMON", 0, "UNCOMMON", 1, "RARE", 2, "EPIC", 3, "LEGENDARY", 4, "MYTHIC", 5
    );

    public Pet(PetCache.PetInfo petData) {
        this.name = petData.type();
        this.xp = petData.exp();
        this.heldItem = petData.item();
        if ((heldItem.isPresent() && heldItem.get().equals("PET_ITEM_TIER_BOOST"))) {
            this.tier = switch (petData.tier()) {
                case "COMMON" -> "UNCOMMON";
                case "UNCOMMON" -> "RARE";
                case "RARE" -> "EPIC";
                case "EPIC" -> "LEGENDARY";
                case "LEGENDARY" -> "MYTHIC";
                default -> petData.tier();
            };
        } else {
            this.tier = petData.tier();
        }
        this.level = LevelFinder.getLevelInfo(this.name.equals("GOLDEN_DRAGON") ? "PET_GREG" : "PET_" + this.tier, (long) xp).level;
        this.icon = createIcon();
    }

    public String getName() { return name; }
    public long getXP() { return (long) xp; }
    public int getTier() { return TIER_MAP.getOrDefault(tier, 0); }
    public String getTierAsString() { return tier; }
    public String getSkin() { return null; }
    public int getLevel() { return level; }
    public ItemStack getIcon() { return icon; }


    private ItemStack createIcon() {
        if (NEURepoManager.isLoading() || !ItemRepository.filesImported()) return Ico.BARRIER;
        Map<String, NEUItem> items = NEURepoManager.NEU_REPO.getItems().getItems();
        if (items == null) return Ico.BARRIER;

        String targetItemId = this.getName() + ";" + this.getTier();
        NEUItem item = items.values().stream()
                .filter(i -> Formatting.strip(i.getSkyblockItemId()).equals(targetItemId))
                .findFirst().orElse(null);

        NEUItem petItem = null;
        if (this.heldItem.isPresent()) {
            petItem = items.values().stream()
                    .filter(i -> Formatting.strip(i.getSkyblockItemId()).equals(this.heldItem.get()))
                    .findFirst().orElse(null);
        }

        return fromNEUItem(item, petItem);
    }

    /**
     * Converts NEU item data into an ItemStack.
     * <p> This method converts NEU item data into a Pet by using the placeholder
     * information from NEU-REPO and injecting the player's calculated pet stats into the lore and transforming
     * the NBT Data into modern DataComponentTypes before returning the final ItemStack </p
     *
     * @param item The NEUItem representing the pet.
     * @param helditem The NEUItem representing the held item, if any.
     * @return The ItemStack representing the pet with all its properties set.
     */
    private ItemStack fromNEUItem(NEUItem item, NEUItem helditem) {
        if (item == null) return Ico.BARRIER;
        List<Pair<String, String>> injectors = new ArrayList<>(createLoreReplacers(item.getSkyblockItemId(), helditem));
        Identifier itemId = Identifier.of(ItemFixerUpper.convertItemId(item.getMinecraftItemId(), item.getDamage()));
        ItemStack stack = new ItemStack(Registries.ITEM.get(itemId));

        NbtCompound customData = new NbtCompound();
        customData.put(ItemUtils.ID, NbtString.of(item.getSkyblockItemId()));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.of(injectData(item.getDisplayName(), injectors)));

        stack.set(DataComponentTypes.LORE, new LoreComponent(
                item.getLore().stream().map(line -> injectData(line, injectors))
                        .filter(line -> !line.contains("SKIP")).map(Text::of)
                        .collect(Collectors.toList())));

        Matcher skullUuid = SKULL_UUID_PATTERN.matcher(item.getNbttag());
        Matcher skullTexture = SKULL_TEXTURE_PATTERN.matcher(item.getNbttag());
        if (skullUuid.find() && skullTexture.find()) {
            UUID uuid = UUID.fromString(skullUuid.group(1));
            String textureValue = this.getSkin() == null ? skullTexture.group(1) : this.getSkin();
            stack.set(DataComponentTypes.PROFILE, new ProfileComponent(
                    Optional.of(item.getSkyblockItemId()), Optional.of(uuid),
                    ItemUtils.propertyMapWithTexture(textureValue)));
        }
        return stack;
    }

    /**
     * Generates a list of placeholder-replacement pairs for the itemName of a pet item.
     * <p> This method uses the pet's data from the NEU repository and uses PetInfo to generate replacers, and optionally
     * includes data about a held item. </p>
     *
     * @param itemSkyblockID The initial itemName string containing the pet's name and tier separated by a semicolon.
     * @param helditem The NEUItem representing the held item, if any.
     * @return A list of placeholder-replacement pairs to be used for injecting data into the pet item's itemName.
     */
    private List<Pair<String, String>> createLoreReplacers(String itemSkyblockID, NEUItem helditem) {
        List<Pair<String, String>> list = new ArrayList<>();
        Map<@PetId String, Map<Rarity, PetNumbers>> petNums = NEURepoManager.NEU_REPO.getConstants().getPetNumbers();
        String petName = itemSkyblockID.split(";")[0];
        if (!itemSkyblockID.contains(";") || !petNums.containsKey(petName)) return list;

        Rarity rarity = Rarity.values()[Integer.parseInt(itemSkyblockID.split(";")[1])];
        try {
            PetNumbers data = petNums.get(petName).get(rarity);
            list.add(new Pair<>("\\{LVL\\}", String.valueOf(this.level)));
            data.interpolatedStatsAtLevel(this.level).getStatNumbers().forEach((key, value) ->
                    list.add(new Pair<>("\\{" + key + "\\}", fixDecimals(value, true))));

            List<Double> otherNumsMin = data.interpolatedStatsAtLevel(this.level).getOtherNumbers();
            for (int i = 0; i < otherNumsMin.size(); ++i) {
                list.add(new Pair<>("\\{" + i + "\\}", fixDecimals(otherNumsMin.get(i), false)));
            }

            list.add(new Pair<>("Right-click to add this pet to",
                    helditem != null ? "§r§6Held Item: " + helditem.getDisplayName() : "SKIP"));
            list.add(new Pair<>("pet menu!", "SKIP"));
        } catch (Exception e) {
            if (petName.equals("GOLDEN_DRAGON")) {
                list.add(new Pair<>("Golden Dragon",
                        "§r§7[Lvl " + this.level + "] " + "§6Golden Dragon Egg §c[Not Supported by NEU-Repo]"));
            }
        }
        return list;
    }

    private String injectData(String string, List<Pair<String, String>> injectors) {
        for (Pair<String, String> injector : injectors) {
            if (string.contains(injector.getLeft())) return injector.getRight();
            string = string.replaceAll(injector.getLeft(), injector.getRight());
        }
        return string;
    }

    private String fixDecimals(double num, boolean truncate) {
        if (num % 1 == 0) return String.valueOf((int) num);
        BigDecimal roundedNum = new BigDecimal(num).setScale(3, RoundingMode.HALF_UP);
        return truncate && num > 1 ? String.valueOf(roundedNum.intValue())
                : roundedNum.stripTrailingZeros().toPlainString();
    }
}
