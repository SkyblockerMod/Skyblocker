package de.hysky.skyblocker.skyblock.itemlist;

import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.NEURepoManager;
import io.github.moulberry.repo.constants.PetNumbers;
import io.github.moulberry.repo.data.NEUItem;
import io.github.moulberry.repo.data.Rarity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemStackBuilder {
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

        NbtCompound root = new NbtCompound();
        root.put("Count", NbtByte.of((byte) 1));

        String id = item.getMinecraftItemId();
        int damage = item.getDamage();
        root.put("id", NbtString.of(ItemFixerUpper.convertItemId(id, damage)));

        NbtCompound tag = new NbtCompound();
        root.put("tag", tag);

        NbtCompound extra = new NbtCompound();
        tag.put(ItemUtils.EXTRA_ATTRIBUTES, extra);
        extra.put(ItemUtils.ID, NbtString.of(internalName));

        NbtCompound display = new NbtCompound();
        tag.put("display", display);

        String name = injectData(item.getDisplayName(), injectors);
        display.put("Name", NbtString.of(Text.Serializer.toJson(Text.of(name))));

        NbtList lore = new NbtList();
        display.put("Lore", lore);
        item.getLore().forEach(el -> lore.add(NbtString.of(Text.Serializer.toJson(Text.of(injectData(el, injectors))))));

        String nbttag = item.getNbttag();
        // add skull texture
        Matcher skullUuid = Pattern.compile("(?<=SkullOwner:\\{)Id:\"(.{36})\"").matcher(nbttag);
        Matcher skullTexture = Pattern.compile("(?<=Properties:\\{textures:\\[0:\\{Value:)\"(.+?)\"").matcher(nbttag);
        if (skullUuid.find() && skullTexture.find()) {
            NbtCompound skullOwner = new NbtCompound();
            tag.put("SkullOwner", skullOwner);
            UUID uuid = UUID.fromString(skullUuid.group(1));
            skullOwner.put("Id", NbtHelper.fromUuid(uuid));
            skullOwner.put("Name", NbtString.of(internalName));

            NbtCompound properties = new NbtCompound();
            skullOwner.put("Properties", properties);
            NbtList textures = new NbtList();
            properties.put("textures", textures);
            NbtCompound texture = new NbtCompound();
            textures.add(texture);
            texture.put("Value", NbtString.of(skullTexture.group(1)));
        }
        // add leather armor dye color
        Matcher colorMatcher = Pattern.compile("color:(\\d+)").matcher(nbttag);
        if (colorMatcher.find()) {
            NbtInt color = NbtInt.of(Integer.parseInt(colorMatcher.group(1)));
            display.put("color", color);
        }
        // add enchantment glint
        if (nbttag.contains("ench:")) {
            NbtList enchantments = new NbtList();
            enchantments.add(new NbtCompound());
            tag.put("Enchantments", enchantments);
        }

        // Add firework star color
        Matcher explosionColorMatcher = Pattern.compile("\\{Explosion:\\{(?:Type:[0-9a-z]+,)?Colors:\\[(?<color>[0-9]+)]\\}").matcher(nbttag);
        if (explosionColorMatcher.find()) {
            NbtCompound explosion = new NbtCompound();

            explosion.putInt("Type", FireworkRocketItem.Type.SMALL_BALL.getId()); //Forget about the actual ball type because it probably doesn't matter
            explosion.putIntArray("Colors", new int[]{Integer.parseInt(explosionColorMatcher.group("color"))});
            tag.put("Explosion", explosion);
        }

        return ItemStack.fromNbt(root);
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

    private static String injectData(String string, List<Pair<String, String>> injectors) {
        for (Pair<String, String> injector : injectors) {
            string = string.replaceAll(injector.getLeft(), injector.getRight());
        }
        return string;
    }
}
