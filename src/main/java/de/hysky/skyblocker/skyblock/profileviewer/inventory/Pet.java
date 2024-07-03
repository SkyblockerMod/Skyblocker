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
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.hysky.skyblocker.skyblock.itemlist.ItemStackBuilder.SKULL_TEXTURE_PATTERN;

public class Pet {
    private final String name;
    private final double xp;
    private final String tier;
    private final Optional<String> heldItem;
    private final Optional<String> skin;
    private final Optional<String> skinTexture;
    private final int level;
    private final ItemStack icon;

    private final Pattern statsMatcher = Pattern.compile("\\{[A-Za-z_]+}");
    private final Pattern numberMatcher = Pattern.compile("\\{\\d+}");



    private static final Map<String, Integer> TIER_MAP = Map.of(
            "COMMON", 0, "UNCOMMON", 1, "RARE", 2, "EPIC", 3, "LEGENDARY", 4, "MYTHIC", 5
    );

    private static final Map<Integer, Formatting> RARITY_COLOR_MAP = Map.of(
            0, Formatting.WHITE, // COMMON
            1, Formatting.GREEN, // UNCOMMON
            2, Formatting.BLUE, // RARE
            3, Formatting.DARK_PURPLE, // EPIC
            4, Formatting.GOLD, // LEGENDARY
            5, Formatting.LIGHT_PURPLE // MYTHIC
    );

    public Pet(PetCache.PetInfo petData) {
        this.name = petData.type();
        this.xp = petData.exp();
        this.heldItem = petData.item();
        this.skin = petData.skin();
        this.skinTexture = calculateSkinTexture();
        this.tier = petData.tier();
        this.level = LevelFinder.getLevelInfo(this.name.equals("GOLDEN_DRAGON") ? "PET_GREG" : "PET_" + this.tier, (long) xp).level;
        this.icon = createIcon();
    }

    private String getName() {
        return name;
    }

    public long getXP() {
        return (long) xp;
    }

    private int getTier() {
        return TIER_MAP.getOrDefault(tier, 0);
    }

    public String getTierAsString() {
        return tier;
    }

    private Optional<String> calculateSkinTexture() {
        // This should also save: SkullOwner && pet skin name via item.getDisplayName()
        if (this.skin.isPresent()) {
            NEUItem item = NEURepoManager.NEU_REPO.getItems().getItemBySkyblockId("PET_SKIN_" + this.skin.get());
            if (item == null) return Optional.empty();
            Matcher skullTexture = SKULL_TEXTURE_PATTERN.matcher(item.getNbttag());
            if (skullTexture.find()) return Optional.of(skullTexture.group(1));
        }
        return Optional.empty();
    }
    public int getLevel() { return level; }
    public ItemStack getIcon() { return icon; }


    private ItemStack createIcon() {
        if (NEURepoManager.isLoading() || !ItemRepository.filesImported()) return Ico.BARRIER;
        Map<String, NEUItem> items = NEURepoManager.NEU_REPO.getItems().getItems();
        if (items == null) return Ico.BARRIER;

        String targetItemId = this.getName() + ";" + (this.getTier() + (heldItem.isPresent() && heldItem.get().equals("PET_ITEM_TIER_BOOST") ? 1 : 0));
        NEUItem item = NEURepoManager.NEU_REPO.getItems().getItems().get(targetItemId);

        // For cases life RIFT_FERRET Where it can be tier boosted into a pet that otherwise can't exist
        if (item == null && heldItem.isPresent() && heldItem.get().equals("PET_ITEM_TIER_BOOST")) {
            item = NEURepoManager.NEU_REPO.getItems().getItems().get(getName() + ";" + getTier());
        }

        return fromNEUItem(item, this.heldItem.map(ItemRepository::getItemStack).orElse(null));
    }

    /**
     * Converts NEU item data into an ItemStack.
     * <p> This method converts NEU item data into a Pet by using the placeholder
     * information from NEU-REPO and injecting the player's calculated pet stats into the lore and transforming
     * the NBT Data into modern DataComponentTypes before returning the final ItemStack </p
     *
     * @param item The NEUItem representing the pet.
     * @param heldItem The NEUItem representing the held item, if any.
     * @return The ItemStack representing the pet with all its properties set.
     */
    private ItemStack fromNEUItem(NEUItem item, ItemStack heldItem) {
        if (item == null) {
            ItemStack errIcon = Ico.BARRIER.copy();
            errIcon.set(DataComponentTypes.CUSTOM_NAME, Text.of(this.getName()));
            return errIcon;
        }

        Identifier itemId = Identifier.of(ItemFixerUpper.convertItemId(item.getMinecraftItemId(), item.getDamage()));
        ItemStack petStack = new ItemStack(Registries.ITEM.get(itemId)).copy();

        List<String> lore = item.getLore();
        List<Text> formattedLore = new ArrayList<>();

        Map<String, Map<Rarity, PetNumbers>> petNums = NEURepoManager.NEU_REPO.getConstants().getPetNumbers();
        Rarity rarity = Rarity.values()[getTier()];
        PetNumbers data = petNums.get(getName()).get(rarity);

        for (String line : lore) {
            if (line.contains("Right-click to add this") || line.contains("pet menu!")) continue;

            String formattedLine = line;

            Matcher stats = statsMatcher.matcher(formattedLine);
            Matcher other = numberMatcher.matcher(formattedLine);
            while (stats.find()) {
                String placeholder = stats.group();
                String statKey = placeholder.substring(1, placeholder.length() - 1);
                String statValue = String.valueOf(fixDecimals(data.interpolatedStatsAtLevel(this.level).getStatNumbers().get(statKey), true));
                formattedLine = formattedLine.replace(placeholder, statValue);
            }

            while (other.find()) {
                String placeholder = other.group();
                int numberKey = Integer.parseInt(placeholder.substring(1, placeholder.length() - 1));
                String statValue = String.valueOf(fixDecimals(data.interpolatedStatsAtLevel(this.level).getOtherNumbers().get(numberKey), false));
                formattedLine = formattedLine.replace(placeholder, statValue);
            }

            formattedLore.add(Text.of(formattedLine));
        }

        if (heldItem != null) {
            formattedLore.set(formattedLore.size() - 2, Text.of("§r§6Held Item: " + heldItem.getName().getString()));
            formattedLore.add(formattedLore.size() - 1, Text.empty());
        }

        // Skin Head Texture
        if (skinTexture.isPresent()) {
            formattedLore.set(0, Text.of(formattedLore.getFirst().getString() + ", " + Formatting.strip(NEURepoManager.NEU_REPO.getItems().getItems().get("PET_SKIN_" + skin.get()).getDisplayName())));
            petStack.set(DataComponentTypes.PROFILE, new ProfileComponent(
                    Optional.of(item.getSkyblockItemId()), Optional.of(UUID.randomUUID()),
                    ItemUtils.propertyMapWithTexture(this.skinTexture.get())));
        } else {
            Matcher skullTexture = SKULL_TEXTURE_PATTERN.matcher(item.getNbttag());
            if (skullTexture.find()) {
                petStack.set(DataComponentTypes.PROFILE, new ProfileComponent(
                        Optional.of(item.getSkyblockItemId()), Optional.of(UUID.randomUUID()),
                        ItemUtils.propertyMapWithTexture(skullTexture.group(1))));
            }
        }

        Style style = Style.EMPTY.withItalic(false);
        formattedLore.set(formattedLore.size()-1, Text.literal(Rarity.values()[getTier() + (boosted() ? 1 : 0)].toString()).setStyle(style).formatted(Formatting.BOLD, RARITY_COLOR_MAP.get(getTier() + (boosted() ? 1 : 0))));

        // Update the lore and name
        petStack.set(DataComponentTypes.LORE, new LoreComponent(formattedLore));
        petStack.set(DataComponentTypes.CUSTOM_NAME, Text.of(item.getDisplayName().formatted(RARITY_COLOR_MAP.get(this.getTier() + (boosted() ? 1 : 0))).replace("{LVL}", String.valueOf(this.level))));

        return petStack;
    }

    private String fixDecimals(double num, boolean truncate) {
        if (num % 1 == 0) return String.valueOf((int) (num));
        BigDecimal roundedNum = new BigDecimal(num).setScale(truncate ? 1 : 3, RoundingMode.HALF_UP);
        return roundedNum.stripTrailingZeros().toPlainString();
    }

    private Boolean boosted() {
        return this.heldItem.isPresent() && this.heldItem.get().equals("PET_ITEM_TIER_BOOST");
    }
}
