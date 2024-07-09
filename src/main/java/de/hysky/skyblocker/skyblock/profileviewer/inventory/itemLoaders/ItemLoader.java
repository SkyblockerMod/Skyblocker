package de.hysky.skyblocker.skyblock.profileviewer.inventory.itemLoaders;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.skyblock.PetCache;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerScreen;
import de.hysky.skyblocker.skyblock.profileviewer.inventory.Pet;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.NEURepoManager;
import io.github.moulberry.repo.data.NEUItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.datafixer.fix.ItemIdFix;
import net.minecraft.datafixer.fix.ItemInstanceTheFlatteningFix;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;

import static de.hysky.skyblocker.skyblock.itemlist.ItemRepository.getItemStack;

public class ItemLoader {

    public List<ItemStack> loadItems(JsonObject data) {
        NbtList containerContent = decompress(data);
        List<ItemStack> itemList = new ArrayList<>();

        for (int i = 0; i < containerContent.size(); i++) {
            if (containerContent.getCompound(i).getInt("id") == 0) {
                itemList.add(ItemStack.EMPTY);
                continue;
            }

            NbtCompound nbttag = containerContent.getCompound(i).getCompound("tag");
            NbtCompound extraAttributes = nbttag.getCompound("ExtraAttributes");
            String internalName = extraAttributes.getString("id");
            if (internalName.equals("PET")) {
                PetCache.PetInfo petInfo = PetCache.PetInfo.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(extraAttributes.getString("petInfo"))).getOrThrow();
                Pet pet = new Pet(petInfo);
                itemList.add(pet.getIcon());
                continue;
            }

            Identifier itemId = identifierFromOldId(containerContent.getCompound(i).getInt("id"), containerContent.getCompound(i).getInt("Damage"));
            ItemStack stack = itemId.toString().equals("minecraft:air") ? getItemStack(internalName) : new ItemStack(Registries.ITEM.get(itemId));

            if (stack == null || stack.isEmpty() || stack.getItem().equals(Ico.BARRIER.getItem())) {
                // Last ditch effort to find item in NEU REPO
                Map<String, NEUItem> items = NEURepoManager.NEU_REPO.getItems().getItems();
                stack = items.values().stream()
                        .filter(j -> Formatting.strip(j.getSkyblockItemId()).equals(Formatting.strip(internalName).replace(":", "-")))
                        .findFirst()
                        .map(NEUItem::getSkyblockItemId)
                        .map(ItemRepository::getItemStack)
                        .orElse(Ico.BARRIER.copy());


                if (stack.getName().getString().contains("barrier")) {
                    stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Err: " + internalName));
                    itemList.add(stack);
                    continue;
                }
            }

            // Custom Data
            NbtCompound customData = new NbtCompound();

            // Add Skyblock Item Id
            customData.put(ItemUtils.ID, NbtString.of(internalName));


            // Item Name
            stack.set(DataComponentTypes.CUSTOM_NAME, Text.of(nbttag.getCompound("display").getString("Name")));

            // Lore
            NbtList loreList = nbttag.getCompound("display").getList("Lore", 8);
            stack.set(DataComponentTypes.LORE, new LoreComponent(loreList.stream()
                    .map(NbtElement::asString)
                    .map(Text::literal)
                    .collect(Collectors.toList())));

            // add skull texture
            NbtList texture = nbttag.getCompound("SkullOwner").getCompound("Properties").getList("textures", 10);
            if (!texture.isEmpty()) {
                stack.set(DataComponentTypes.PROFILE, new ProfileComponent(Optional.of(internalName), Optional.of(UUID.fromString(nbttag.getCompound("SkullOwner").get("Id").asString())), ItemUtils.propertyMapWithTexture(texture.getCompound(0).getString("Value"))));
            }

            // Colour
            if (nbttag.getCompound("display").contains("color")) {
                int color = nbttag.getCompound("display").getInt("color");
                stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(color, false));
            }

            // add enchantment glint
            if (nbttag.getKeys().contains("ench")) {
                stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
            }

            // Hide weapon damage and other useless info
            stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, new AttributeModifiersComponent(List.of(), false));

            // Set Count
            stack.setCount(containerContent.getCompound(i).getInt("Count"));

            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(extraAttributes));

            itemList.add(stack);
        }

        return itemList;
    }

    private static Identifier identifierFromOldId(int id, int damage) {
        try {
            return damage != 0 ? Identifier.of(ItemInstanceTheFlatteningFix.getItem(ItemIdFix.fromId(id), damage)) : Identifier.of(ItemIdFix.fromId(id));
        } catch (Exception e) {
            return Identifier.of("air");
        }
    }

    private static NbtList decompress(JsonObject data) {
        try {
            return NbtIo.readCompressed(new ByteArrayInputStream(Base64.getDecoder().decode(data.get("data").getAsString())), NbtSizeTracker.ofUnlimitedBytes()).getList("i", NbtElement.COMPOUND_TYPE);
        } catch (Exception e) {
            ProfileViewerScreen.LOGGER.error("[Skyblocker Profile Viewer] Failed to decompress item data", e);
        }
        return null;
    }
}
