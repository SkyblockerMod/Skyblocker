package de.hysky.skyblocker.skyblock.itemlist;

import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.TextTransformer;
import io.github.moulberry.repo.constants.PetNumbers;
import io.github.moulberry.repo.data.NEUItem;
import io.github.moulberry.repo.data.Rarity;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemStackBuilder {
    public static final Pattern SKULL_UUID_PATTERN = Pattern.compile("(?<=SkullOwner:\\{)Id:\"(.{36})\"");
    public static final Pattern SKULL_TEXTURE_PATTERN = Pattern.compile("(?<=Properties:\\{textures:\\[0:\\{Value:)\"(.+?)\"");
    private static final Pattern COLOR_PATTERN = Pattern.compile("color:(\\d+)");
    private static final Pattern EXPLOSION_COLOR_PATTERN = Pattern.compile("\\{Explosion:\\{(?:Type:[0-9a-z]+,)?Colors:\\[(?<color>[0-9]+)]\\}");
    private static Map<String, Map<Rarity, PetNumbers>> petNums;

    public static void loadPetNums() {
        try {
            petNums = NEURepoManager.NEU_REPO.getConstants().getPetNumbers();
        } catch (Exception e) {
            ItemRepository.LOGGER.error("Failed to load petnums.json");
        }
    }

    public static ItemStack fromNEUItem(NEUItem item) {
        String internalName = item.getSkyblockItemId();

        List<Pair<String, String>> injectors = new ArrayList<>(petData(internalName));

        String legacyId = item.getMinecraftItemId();
        Identifier itemId = Identifier.of(ItemFixerUpper.convertItemId(legacyId, item.getDamage()));

        ItemStack stack = new ItemStack(Registries.ITEM.get(itemId));

        // Custom Data
        NbtCompound customData = new NbtCompound();

        // Add Skyblock Item Id
        customData.put(ItemUtils.ID, NbtString.of(internalName));

        // Item Name
        String name = injectData(item.getDisplayName(), injectors);
        stack.set(DataComponentTypes.CUSTOM_NAME, TextTransformer.fromLegacy(name));

        // Lore
        stack.set(DataComponentTypes.LORE, new LoreComponent(item.getLore().stream().map(line -> TextTransformer.fromLegacy(injectData(line, injectors))).map(Text.class::cast).toList()));

        String nbttag = item.getNbttag();
        // add skull texture
        Matcher skullUuid = SKULL_UUID_PATTERN.matcher(nbttag);
        Matcher skullTexture = SKULL_TEXTURE_PATTERN.matcher(nbttag);
        if (skullUuid.find() && skullTexture.find()) {
            UUID uuid = UUID.fromString(skullUuid.group(1));
            String textureValue = skullTexture.group(1);

            stack.set(DataComponentTypes.PROFILE, new ProfileComponent(Optional.of(internalName.substring(0, Math.min(internalName.length(), 15))), Optional.of(uuid), ItemUtils.propertyMapWithTexture(textureValue)));
        }

        // add leather armor dye color
        Matcher colorMatcher = COLOR_PATTERN.matcher(nbttag);
        if (colorMatcher.find()) {
            int color = Integer.parseInt(colorMatcher.group(1));
            stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(color, false));
        }
        // add enchantment glint
        if (nbttag.contains("ench:")) {
            stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        }

        //Hide weapon damage and other useless info
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, new AttributeModifiersComponent(List.of(), false));

        // Add firework star color
        Matcher explosionColorMatcher = EXPLOSION_COLOR_PATTERN.matcher(nbttag);
        if (explosionColorMatcher.find()) {
            //We used create an IntArrayList and took the color as the list's capacity and not add anything to the list which y'know casually leaked a lot of memory...
            IntList color = IntList.of(Integer.parseInt(explosionColorMatcher.group("color")));

            //Forget about the actual ball type because it probably doesn't matter
            stack.set(DataComponentTypes.FIREWORK_EXPLOSION, new FireworkExplosionComponent(FireworkExplosionComponent.Type.SMALL_BALL, color, IntList.of(), false, false));
        }

        // Attach custom nbt data
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(customData));

        return stack;
    }

    private static List<Pair<String, String>> petData(String internalName) {
        List<Pair<String, String>> list = new ArrayList<>();

        String petName = internalName.split(";")[0];
        if (!internalName.contains(";") || !petNums.containsKey(petName)) return list;

        final Rarity[] rarities = {
                Rarity.COMMON,
                Rarity.UNCOMMON,
                Rarity.RARE,
                Rarity.EPIC,
                Rarity.LEGENDARY,
                Rarity.MYTHIC,
        };
        Rarity rarity = rarities[Integer.parseInt(internalName.split(";")[1])];
        PetNumbers data = petNums.get(petName).get(rarity);

        int minLevel = data.getLowLevel();
        int maxLevel = data.getHighLevel();
        list.add(new Pair<>("\\{LVL\\}", minLevel + " ➡ " + maxLevel));

        Map<String, Double> statNumsMin = data.getStatsAtLowLevel().getStatNumbers();
        Map<String, Double> statNumsMax = data.getStatsAtHighLevel().getStatNumbers();
        Set<Map.Entry<String, Double>> entrySet = statNumsMin.entrySet();
        for (Map.Entry<String, Double> entry : entrySet) {
            String key = entry.getKey();
            String left = "\\{" + key + "\\}";
            String right = statNumsMin.get(key) + " ➡ " + statNumsMax.get(key);
            list.add(new Pair<>(left, right));
        }

        List<Double> otherNumsMin = data.getStatsAtLowLevel().getOtherNumbers();
        List<Double> otherNumsMax = data.getStatsAtHighLevel().getOtherNumbers();
        for (int i = 0; i < otherNumsMin.size(); ++i) {
            String left = "\\{" + i + "\\}";
            String right = otherNumsMin.get(i) + " ➡ " + otherNumsMax.get(i);
            list.add(new Pair<>(left, right));
        }

        return list;
    }

    public static String injectData(String string, List<Pair<String, String>> injectors) {
        for (Pair<String, String> injector : injectors) {
            string = string.replaceAll(injector.getLeft(), injector.getRight());
        }
        return string;
    }
}
