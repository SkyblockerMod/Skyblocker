package de.hysky.skyblocker.skyblock.profileviewer.collections;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerPage;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerScreen.fetchCollectionsData;
import static de.hysky.skyblocker.skyblock.profileviewer.utils.ProfileViewerUtils.COMMA_FORMATTER;

public class GenericCategory implements ProfileViewerPage {
    private final String category;
    private final LinkedList<ItemStack> collections = new LinkedList<>();
    private static final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
    private static final Identifier BUTTON_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/profile_viewer/button_icon_toggled.png");
    private static final int COLUMN_GAP = 26;
    private static final int ROW_GAP = 34;
    private static final int COLUMNS = 7;

    private final Map<String, String[]> collectionsMap;
    private final Map<String, IntList> tierRequirementsMap;
    private final Map<String, String> ICON_TRANSLATION = Map.ofEntries(
            Map.entry("MUSHROOM_COLLECTION", "RED_MUSHROOM"));
    private final String[] ROMAN_NUMERALS = {"-", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX", "XX"};

    public GenericCategory(JsonObject hProfile, JsonObject pProfile, String collection) {
        Map<String, Map<String, ?>> fetchedData = fetchCollectionsData();
        //noinspection unchecked
        collectionsMap = (Map<String, String[]>) fetchedData.get("COLLECTIONS");
        //noinspection unchecked
        tierRequirementsMap = (Map<String, IntList>) fetchedData.get("TIER_REQS");
        this.category = collection;
        setupItemStacks(hProfile, pProfile);
    }

    private int calculateTier(long achieved, IntList requirements) {
        return (int) requirements.intStream().filter(req -> achieved >= req).count();
    }

    private void setupItemStacks(JsonObject hProfile, JsonObject pProfile) {
        JsonObject playerCollection = pProfile.getAsJsonObject("collection");

        for (String collection : collectionsMap.get(this.category)) {
            ItemStack itemStack = ItemRepository.getItemStack(ICON_TRANSLATION.getOrDefault(collection, collection).replace(':', '-'));
            itemStack = itemStack == null ? Ico.BARRIER.copy() : itemStack.copy();

            if (itemStack.getItem().getName().getString().equals("Barrier"))  {
                itemStack.set(DataComponentTypes.CUSTOM_NAME, Text.of(collection));
                System.out.println(collection);
                System.out.println(this.category);
            }

            Style style = Style.EMPTY.withColor(Formatting.WHITE).withItalic(false);
            itemStack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(Formatting.strip(itemStack.getComponents().get(DataComponentTypes.CUSTOM_NAME).getString())).setStyle(style));


            long personalColl = playerCollection != null && playerCollection.has(collection) ? playerCollection.get(collection).getAsLong() : 0;

            long totalCollection = 0;
            for (String member : hProfile.get("members").getAsJsonObject().keySet()) {
                if (!hProfile.getAsJsonObject("members").getAsJsonObject(member).has("collection")) continue;
                JsonObject memberColl = hProfile.getAsJsonObject("members").getAsJsonObject(member).getAsJsonObject("collection");
                totalCollection += memberColl.has(collection) ? memberColl.get(collection).getAsLong() : 0;
            }

            int collectionTier = calculateTier(totalCollection, tierRequirementsMap.get(collection));
            IntList tierRequirements = tierRequirementsMap.get(collection);

            List<Text> lore = new ArrayList<>();
            lore.add(Text.literal("Collection Item").setStyle(style).formatted(Formatting.DARK_GRAY));
            lore.add(Text.empty());

            if (hProfile.get("members").getAsJsonObject().keySet().size() > 1) {
                lore.add(Text.literal("Personal: " + COMMA_FORMATTER.format(personalColl)).setStyle(style).formatted(Formatting.GOLD));
                lore.add(Text.literal("Co-op: " + COMMA_FORMATTER.format(totalCollection-personalColl)).setStyle(style).formatted(Formatting.AQUA));
            }
            lore.add(Text.literal("Collection: " + COMMA_FORMATTER.format(totalCollection)).setStyle(style).formatted(Formatting.YELLOW));

            lore.add(Text.empty());
            lore.add(Text.literal("Collection Tier: " + collectionTier + "/" + tierRequirements.size()).setStyle(style).formatted(Formatting.LIGHT_PURPLE));

            if (collectionTier == tierRequirements.size()) itemStack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

            itemStack.set(DataComponentTypes.LORE, new LoreComponent(lore));

            itemStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);

            collections.add(itemStack);
        }
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta, int rootX, int rootY) {
        Text categoryTitle = Text.literal(category.charAt(0) + category.substring(1).toLowerCase() + " Collections").formatted(Formatting.BOLD);
        context.drawText(textRenderer, categoryTitle, rootX + 88 - (textRenderer.getWidth(categoryTitle) / 2), rootY, Color.DARK_GRAY.getRGB(), false);

        for (int i = 0; i < collections.size(); i++) {
            int x = rootX + 2 + (i % COLUMNS) * COLUMN_GAP;
            int y = rootY + 19 + (i / COLUMNS) * ROW_GAP;

            context.fill(x - 3, y - 3, x + 19, y + 19, Color.BLACK.getRGB());
            context.drawTexture(BUTTON_TEXTURE, x - 2, y - 2, 0, 0, 20, 20, 20, 20);
            context.drawItem(collections.get(i), x, y);

            ItemStack itemStack = collections.get(i);
            List<Text> lore = itemStack.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT).lines();
            for (Text text : lore) {
                if (text.getString().startsWith("Collection Tier: ")) {
                    String tierText = text.getString().substring("Collection Tier: ".length());
                    if (tierText.contains("/")) {
                        String[] parts = tierText.split("/");
                        int cTier = Integer.parseInt(parts[0].trim());
                        Color colour = itemStack.hasGlint() ? Color.MAGENTA : Color.darkGray;
                        //DO NOT CHANGE THIS METHOD CALL! Aaron's Mod mixes in here to provide chroma text for max collections
                        //and changing the method called here will break that! Consult Aaron before making any changes :)
                        context.drawText(textRenderer, Text.literal(toRomanNumerals(cTier)), x + 9 - (textRenderer.getWidth(toRomanNumerals(cTier)) / 2), y + 21, colour.getRGB(), false);
                    }
                    break;
                }
            }

            if (mouseX > x && mouseX < x + 16 && mouseY > y && mouseY < y + 16) {
                List<Text> tooltip = collections.get(i).getTooltip(Item.TooltipContext.DEFAULT, MinecraftClient.getInstance().player, TooltipType.BASIC);
                context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
            }
        }
    }

    private String toRomanNumerals(int number) {
        return number <= ROMAN_NUMERALS.length ? ROMAN_NUMERALS[number] : "Err";
    }
}
