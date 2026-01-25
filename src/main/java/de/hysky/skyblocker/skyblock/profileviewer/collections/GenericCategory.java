package de.hysky.skyblocker.skyblock.profileviewer.collections;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerPage;
import de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerScreen;
import de.hysky.skyblocker.skyblock.profileviewer.utils.Collection;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.RomanNumerals;
import it.unimi.dsi.fastutil.ints.IntList;
import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

public class GenericCategory implements ProfileViewerPage {
	private final String category;
	private final LinkedList<ItemStack> collections = new LinkedList<>();
	private static final Font textRenderer = Minecraft.getInstance().font;
	private static final Identifier BUTTON_TEXTURE = SkyblockerMod.id("textures/gui/profile_viewer/button_icon_toggled.png");
	private static final int COLUMN_GAP = 26;
	private static final int ROW_GAP = 34;
	private static final int COLUMNS = 7;

	private final Map<String, List<Collection>> collectionsData;
	private final Map<String, String> ICON_TRANSLATION = Map.ofEntries(
			Map.entry("MUSHROOM_COLLECTION", "RED_MUSHROOM"));

	public GenericCategory(JsonObject hProfile, JsonObject pProfile, String collection) {
		collectionsData = ProfileViewerScreen.getCollections();
		this.category = collection;
		setupItemStacks(hProfile, pProfile);
	}

	private int calculateTier(long achieved, IntList requirements) {
		return (int) requirements.intStream().filter(req -> achieved >= req).count();
	}

	private void setupItemStacks(JsonObject hProfile, JsonObject pProfile) {
		JsonObject playerCollection = pProfile.getAsJsonObject("collection");

		for (Collection collection : collectionsData.get(this.category)) {
			ItemStack itemStack = ItemRepository.getItemStack(ICON_TRANSLATION.getOrDefault(collection.id(), collection.id()).replace(':', '-'));
			itemStack = itemStack == null ? Ico.BARRIER.copy() : itemStack.copy();

			if (itemStack.getItem().getName().getString().equals("Barrier")) {
				itemStack.set(DataComponents.CUSTOM_NAME, Component.nullToEmpty(collection.id()));
				System.out.println(collection);
				System.out.println(this.category);
			}

			Style style = Style.EMPTY.withColor(ChatFormatting.WHITE).withItalic(false);
			itemStack.set(DataComponents.CUSTOM_NAME, Component.literal(ChatFormatting.stripFormatting(itemStack.getComponents().get(DataComponents.CUSTOM_NAME).getString())).setStyle(style));


			long personalColl = playerCollection != null && playerCollection.has(collection.id()) ? playerCollection.get(collection.id()).getAsLong() : 0;

			long totalCollection = 0;
			for (String member : hProfile.get("members").getAsJsonObject().keySet()) {
				if (!hProfile.getAsJsonObject("members").getAsJsonObject(member).has("collection")) continue;
				JsonObject memberColl = hProfile.getAsJsonObject("members").getAsJsonObject(member).getAsJsonObject("collection");
				totalCollection += memberColl.has(collection.id()) ? memberColl.get(collection.id()).getAsLong() : 0;
			}

			int collectionTier = calculateTier(totalCollection, collection.tiers());
			IntList tierRequirements = collection.tiers();

			List<Component> lore = new ArrayList<>();
			lore.add(Component.literal("Collection Item").setStyle(style).withStyle(ChatFormatting.DARK_GRAY));
			lore.add(Component.empty());

			if (hProfile.get("members").getAsJsonObject().keySet().size() > 1) {
				lore.add(Component.literal("Personal: " + Formatters.INTEGER_NUMBERS.format(personalColl)).setStyle(style).withStyle(ChatFormatting.GOLD));
				lore.add(Component.literal("Co-op: " + Formatters.INTEGER_NUMBERS.format(totalCollection - personalColl)).setStyle(style).withStyle(ChatFormatting.AQUA));
			}
			lore.add(Component.literal("Collection: " + Formatters.INTEGER_NUMBERS.format(totalCollection)).setStyle(style).withStyle(ChatFormatting.YELLOW));

			lore.add(Component.empty());
			lore.add(Component.literal("Collection Tier: " + collectionTier + "/" + tierRequirements.size()).setStyle(style).withStyle(ChatFormatting.LIGHT_PURPLE));

			if (collectionTier == tierRequirements.size()) itemStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

			itemStack.set(DataComponents.LORE, new ItemLore(lore));

			itemStack.set(DataComponents.CUSTOM_DATA, CustomData.EMPTY);

			collections.add(itemStack);
		}
	}


	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta, int rootX, int rootY) {
		Component categoryTitle = Component.literal(category.charAt(0) + category.substring(1).toLowerCase(Locale.ENGLISH) + " Collections").withStyle(ChatFormatting.BOLD);
		context.drawString(textRenderer, categoryTitle, rootX + 88 - (textRenderer.width(categoryTitle) / 2), rootY, Color.DARK_GRAY.getRGB(), false);

		for (int i = 0; i < collections.size(); i++) {
			int x = rootX + 2 + (i % COLUMNS) * COLUMN_GAP;
			int y = rootY + 19 + (i / COLUMNS) * ROW_GAP;

			context.fill(x - 3, y - 3, x + 19, y + 19, Color.BLACK.getRGB());
			context.blit(RenderPipelines.GUI_TEXTURED, BUTTON_TEXTURE, x - 2, y - 2, 0, 0, 20, 20, 20, 20);
			context.renderItem(collections.get(i), x, y);

			ItemStack itemStack = collections.get(i);
			List<Component> lore = itemStack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).lines();
			for (Component text : lore) {
				if (text.getString().startsWith("Collection Tier: ")) {
					String tierText = text.getString().substring("Collection Tier: ".length());
					if (tierText.contains("/")) {
						String[] parts = tierText.split("/");
						int cTier = Integer.parseInt(parts[0].trim());
						Color colour = itemStack.hasFoil() ? Color.MAGENTA : Color.darkGray;
						//DO NOT CHANGE THIS METHOD CALL! Aaron's Mod mixes in here to provide chroma text for max collections
						//and changing the method called here will break that! Consult Aaron before making any changes :)
						context.drawString(textRenderer, Component.literal(RomanNumerals.decimalToRoman(cTier)), x + 9 - (textRenderer.width(RomanNumerals.decimalToRoman(cTier)) / 2), y + 21, colour.getRGB(), false);
					}
					break;
				}
			}

			if (mouseX > x && mouseX < x + 16 && mouseY > y && mouseY < y + 16) {
				List<Component> tooltip = collections.get(i).getTooltipLines(Item.TooltipContext.EMPTY, Minecraft.getInstance().player, TooltipFlag.NORMAL);
				context.setComponentTooltipForNextFrame(textRenderer, tooltip, mouseX, mouseY);
			}
		}
	}
}
