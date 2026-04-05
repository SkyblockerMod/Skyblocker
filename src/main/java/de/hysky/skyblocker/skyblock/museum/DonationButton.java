package de.hysky.skyblocker.skyblock.museum;

import de.hysky.skyblocker.skyblock.item.wikilookup.WikiLookupManager;
import de.hysky.skyblocker.skyblock.item.ItemPrice;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.ItemUtils;
import it.unimi.dsi.fastutil.objects.ObjectDoublePair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import it.unimi.dsi.fastutil.objects.ObjectObjectMutablePair;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class DonationButton extends AbstractWidget {
	private static final int SIZE = 33;
	private static final int ITEM_OFFSET = 8;
	private static final Font TEXT_RENDERER = Minecraft.getInstance().font;
	private @Nullable Donation donation = null;
	private @Nullable ItemStack itemStack = null;
	private @Nullable String textToRender;
	private @Nullable List<Component> tooltip;

	protected DonationButton(int x, int y) {
		super(x, y, SIZE, SIZE + 2, Component.empty());
	}

	protected @Nullable ItemStack getDisplayStack() {
		return this.itemStack;
	}

	/**
	 * Initializes the button with a donation object.
	 *
	 * @param donation The donation to associate with this button.
	 */
	public void init(Donation donation) {
		this.visible = true;
		this.donation = donation;
		this.textToRender = MuseumUtils.formatPrice(donation.getPriceData().getEffectivePrice());

		// Determine the item stack to display
		FlexibleItemStack flexible = !donation.isSet()
				? ItemRepository.getItemStack(donation.getId())
				: ItemRepository.getItemStack(MuseumItemCache.ARMOR_TO_ID.get(donation.getId()));
		this.itemStack = flexible == null ? null : flexible.getStackOrThrow();
		buildTooltip();
	}

	/**
	 * Clears the display stack and resets the button state.
	 */
	protected void resetButton() {
		this.visible = false;
		this.donation = null;
		this.itemStack = null;
		this.tooltip = null;
		this.textToRender = null;
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		assert donation != null && textToRender != null; // shouldn't be able to happen in-game, here to quiet down warnings
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, RecipeButton.SLOT_CRAFTABLE_SPRITE, this.getX(), this.getY(), this.width, this.height);

		boolean hasPrice = donation.hasPrice();
		if (itemStack != null && !itemStack.isEmpty()) {
			graphics.fakeItem(itemStack, this.getX() + ITEM_OFFSET, this.getY() + (hasPrice ? 4 : 8));
		}

		if (hasPrice) {
			graphics.centeredText(TEXT_RENDERER, textToRender, this.getX() + (this.width / 2), this.getY() + ITEM_OFFSET + 13, 0xFF00FF00);
		}
	}

	/**
	 * Builds the tooltip for the button based on its associated donation data
	 */
	private void buildTooltip() {
		assert donation != null; // shouldn't be able to happen in-game, here to quiet down warnings
		List<Component> tooltip = new ArrayList<>();

		boolean soulbound = itemStack != null && !itemStack.isEmpty() && ItemUtils.isSoulbound(itemStack);
		ObjectDoublePair<String> discount = donation.getDiscount();
		List<ObjectIntPair<String>> countsTowards = donation.getCountsTowards();

		// Display name
		tooltip.add(MuseumUtils.getDisplayName(donation.getId(), donation.isSet()));

		// Set pieces display names
		if (donation.isSet()) {
			for (ObjectObjectMutablePair<String, PriceData> piece : donation.getSet()) {
				FlexibleItemStack flexible = ItemRepository.getItemStack(piece.left());
				if (flexible != null) {
					ItemStack stack = flexible.getStackOrThrow();
					Component itemName = stack.getHoverName().copy();
					if (soulbound) {
						tooltip.add(Component.literal("  ").append(itemName));
					} else if (piece.right().getEffectivePrice() > 0) {
						tooltip.add(Component.literal("  ").append(itemName).append(Component.literal(" (").withStyle(ChatFormatting.DARK_GRAY)).append(Component.literal(MuseumUtils.formatPrice(piece.right().getEffectivePrice())).withStyle(ChatFormatting.GOLD)).append(Component.literal(")").withStyle(ChatFormatting.DARK_GRAY)));
					} else {
						tooltip.add(Component.literal("  ").append(itemName).append(Component.literal(" (").withStyle(ChatFormatting.DARK_GRAY)).append(Component.translatable("skyblocker.museum.hud.unknownPrice").withStyle(ChatFormatting.RED)).append(Component.literal(")").withStyle(ChatFormatting.DARK_GRAY)));
					}
				}
			}
		}
		tooltip.add(Component.empty());

		Component xpText = Component.literal(String.valueOf(donation.getTotalXp())).append(" ").append(Component.translatable("skyblocker.museum.hud.skyblockXp")).withStyle(ChatFormatting.AQUA);
		tooltip.add(Component.translatable("skyblocker.museum.hud.xpReward").append(": ").withStyle(ChatFormatting.GRAY).append(xpText));

		if (!soulbound) {
			PriceData priceData = donation.getPriceData();
			Component lBinText = donation.hasLBinPrice() ? Component.literal(MuseumUtils.formatPrice(priceData.getLBinPrice())).append(" Coins").withStyle(ChatFormatting.GOLD) : Component.translatable("skyblocker.museum.hud.unknownPrice").withStyle(ChatFormatting.RED);
			MutableComponent craftCostText = donation.isCraftable() ? Component.literal(MuseumUtils.formatPrice(donation.hasDiscount() ? priceData.getCraftCost() - discount.rightDouble() : priceData.getCraftCost())).append(" ").append(Component.translatable("skyblocker.museum.hud.coin")).withStyle(ChatFormatting.GOLD) : Component.translatable("skyblocker.museum.hud.unknownPrice").withStyle(ChatFormatting.RED);
			Component discountText = donation.hasDiscount() && donation.isCraftable() ? Component.literal(" (").withStyle(ChatFormatting.DARK_GRAY).append(Component.literal(MuseumUtils.formatPrice(priceData.getCraftCost())).withStyle(ChatFormatting.GOLD)).append(" - ").append(Component.literal(MuseumUtils.formatPrice(discount.rightDouble())).withStyle(ChatFormatting.GOLD)).append(Component.literal(")").withStyle(ChatFormatting.DARK_GRAY)) : Component.empty();
			Component xpCoinsRatio = Component.literal(MuseumUtils.formatPrice(donation.getXpCoinsRatio())).append(" ").append(Component.translatable("skyblocker.museum.hud.ratioText")).withStyle(ChatFormatting.AQUA);

			tooltip.add(Component.translatable("skyblocker.museum.hud.sorter.lBin").append(": ").withStyle(ChatFormatting.GRAY).append(lBinText));
			tooltip.add(Component.translatable("skyblocker.museum.hud.sorter.craftCost").append(": ").withStyle(ChatFormatting.GRAY).append(craftCostText).append(discountText));
			tooltip.add(Component.translatable("skyblocker.museum.hud.sorter.ratio").append(": ").withStyle(ChatFormatting.GRAY).append(xpCoinsRatio));
		}

		if (countsTowards.size() > 1) {
			tooltip.add(Component.empty());
			tooltip.add(Component.translatable("skyblocker.museum.hud.countsFor").append(":").withStyle(ChatFormatting.GRAY));
			for (ObjectIntPair<String> credit : countsTowards) {
				tooltip.add(Component.literal(" ● ").withStyle(ChatFormatting.GRAY).append(MuseumUtils.getDisplayName(credit.left(), donation.isSet())).append(Component.literal(" (" + credit.rightInt() + " XP)").withStyle(ChatFormatting.AQUA)));
			}
		}

		if (donation.isCraftable() && donation.hasDiscount()) {
			tooltip.add(Component.empty());
			tooltip.add(Component.translatable("skyblocker.museum.hud.craftIngredient").append(": ").withStyle(ChatFormatting.GRAY).append(Component.literal("(").append(Component.translatable("skyblocker.museum.hud.alreadyDonatedItem").append(")")).withStyle(ChatFormatting.DARK_GRAY)));
			tooltip.add(Component.literal(" - ").withStyle(ChatFormatting.GRAY).append(MuseumUtils.getDisplayName(discount.left(), donation.isSet())).append(Component.literal(" ✔").withStyle(ChatFormatting.GREEN)).append(Component.literal(" (").withStyle(ChatFormatting.DARK_GRAY).append(Component.literal(MuseumUtils.formatPrice(discount.rightDouble())).withStyle(ChatFormatting.GOLD)).append(")").withStyle(ChatFormatting.DARK_GRAY)));
		}

		String wikiLookupKeyString = WikiLookupManager.getKeysText();
		String itemPriceLookupKeyString = ItemPrice.ITEM_PRICE_LOOKUP.getTranslatedKeyMessage().getString();
		if (soulbound || !wikiLookupKeyString.isEmpty() || !itemPriceLookupKeyString.isEmpty()) tooltip.add(Component.empty());
		if (soulbound) tooltip.add(Component.literal("* Soulbound *").withStyle(ChatFormatting.DARK_GRAY));
		if (!wikiLookupKeyString.isEmpty()) {
			tooltip.add(Component.translatable("skyblocker.museum.hud.wikiLookup", wikiLookupKeyString).withStyle(ChatFormatting.YELLOW));
		}
		if (!soulbound && !itemPriceLookupKeyString.isEmpty()) {
			tooltip.add(Component.translatable("skyblocker.museum.hud.itemPriceLookup", itemPriceLookupKeyString).withStyle(ChatFormatting.YELLOW));
		}

		this.tooltip = tooltip;
	}

	protected List<Component> getItemTooltip() {
		return Objects.requireNonNull(this.tooltip, "Tried to get tooltip before button was initialized.");
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {}
}
