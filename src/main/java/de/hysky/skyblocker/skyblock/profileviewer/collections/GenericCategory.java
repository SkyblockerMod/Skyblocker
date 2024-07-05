package de.hysky.skyblocker.skyblock.profileviewer.collections;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerPage;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.NEURepoManager;
import io.github.moulberry.repo.data.NEUItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.*;

import static de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerScreen.fetchCollectionsData;

public class GenericCategory implements ProfileViewerPage {
    private final String category;
    private final LinkedList<ItemStack> collections = new LinkedList<>();
    private static final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
    private static final NumberFormat FORMATTER = NumberFormat.getInstance(Locale.US);
    private static final Identifier BUTTON_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/profile_viewer/button_icon_toggled.png");
    private static final int COLUMN_GAP = 26;
    private static final int ROW_GAP = 34;
    private static final int COLUMNS = 7;

    private final Map<String, String[]> collectionsMap;
    private final Map<String, List<Integer>> tierRequirementsMap;
    private final Map<String, String> ICON_TRANSLATION = Map.ofEntries(
            Map.entry("MUSHROOM_COLLECTION", "RED_MUSHROOM"));
    private final String[] ROMAN_NUMERALS = {"-", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX", "XX"};

    public GenericCategory(JsonObject hProfile, JsonObject pProfile, String collection) {
        Map<String, Map<String, ?>> fetchedData = fetchCollectionsData();
        //noinspection unchecked
        collectionsMap = (Map<String, String[]>) fetchedData.get("COLLECTIONS");
        //noinspection unchecked
        tierRequirementsMap = (Map<String, List<Integer>>) fetchedData.get("TIER_REQS");
        this.category = collection;
        setupItemStacks(hProfile, pProfile);
    }

    private int calculateTier(int achieved, List<Integer> requirements) {
        return (int) requirements.stream().filter(req -> achieved >= req).count();
    }

    private void setupItemStacks(JsonObject hProfile, JsonObject pProfile) {
        JsonObject playerCollection = pProfile.getAsJsonObject("collection");

        for (String collection : collectionsMap.get(this.category)) {
            Map<String, NEUItem> items = NEURepoManager.NEU_REPO.getItems().getItems();
            ItemStack itemStack = items.values().stream()
                    .filter(i -> Formatting.strip(i.getSkyblockItemId()).equals(ICON_TRANSLATION.getOrDefault(collection, collection).replace(':', '-')))
                    .findFirst()
                    .map(NEUItem::getSkyblockItemId)
                    .map(ItemRepository::getItemStack)
                    .map(ItemStack::copy)
                    .orElse(Ico.BARRIER.copy());

            if (itemStack.getItem().getName().getString().equals("Barrier")) itemStack.set(DataComponentTypes.ITEM_NAME, Text.of(collection));

            int personalColl = playerCollection != null && playerCollection.has(collection) ? playerCollection.get(collection).getAsInt() : 0;

            int coopColl = 0;
            for (String member : hProfile.get("members").getAsJsonObject().keySet()) {
                if (!hProfile.getAsJsonObject("members").getAsJsonObject(member).has("collection")) continue;
                JsonObject memberColl = hProfile.getAsJsonObject("members").getAsJsonObject(member).getAsJsonObject("collection");
                coopColl += memberColl.has(collection) ? memberColl.get(collection).getAsInt() : 0;
            }

            int collectionTier = calculateTier(coopColl, tierRequirementsMap.get(collection));
            List<Integer> tierRequirements = tierRequirementsMap.get(collection);

            List<Text> lore = new ArrayList<>();
            Style style = Style.EMPTY.withItalic(false);
            lore.add(Text.literal("Collection: " + FORMATTER.format(personalColl)).setStyle(style).formatted(Formatting.YELLOW));
            if (hProfile.get("members").getAsJsonObject().keySet().size() > 1) {
                lore.add(Text.literal("Co-op Collection: " + FORMATTER.format(coopColl)).setStyle(style).formatted(Formatting.AQUA));
            }
            lore.add(Text.literal("Collection Tier: " + collectionTier).setStyle(style).formatted(Formatting.LIGHT_PURPLE));
            itemStack.set(DataComponentTypes.LORE, new LoreComponent(lore));

            if (collectionTier == tierRequirements.size()) itemStack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

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
                if (!text.getString().startsWith("Collection Tier: ")) continue;
                int cTier = Integer.parseInt(text.getString().substring("Collection Tier: ".length()));
                Color colour = Boolean.TRUE.equals(itemStack.get(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE)) ? Color.MAGENTA : Color.darkGray;
                context.drawText(textRenderer, Text.literal(toRomanNumerals(cTier)), x + 9 - (textRenderer.getWidth(toRomanNumerals(cTier)) / 2), y + 21, colour.getRGB(), false);
                break;
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
