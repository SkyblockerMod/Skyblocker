package de.hysky.skyblocker.skyblock.museum;

import de.hysky.skyblocker.skyblock.item.WikiLookup;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.recipebook.AnimatedResultButton;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DonationButton extends ClickableWidget {
	private static final int SIZE = 33;
	private static final int ITEM_OFFSET = 8;

	private Donation donation = null;
	private ItemStack itemStack = null;
	private static final String WIKI_LOCKUP_KEY = WikiLookup.wikiLookup.getBoundKeyTranslationKey();
	private static final TextRenderer TEXT_RENDERER = MinecraftClient.getInstance().textRenderer;
	private String textToRender;
	private List<Text> tooltip;

	protected DonationButton(int x, int y) {
		super(x, y, SIZE, SIZE + 2, ScreenTexts.EMPTY);
	}

	protected ItemStack getDisplayStack() {
		return this.itemStack;
	}

	/**
	 * Initializes the button with a donation object.
	 *
	 * @param donation The donation to associate with this button.
	 */
	public void init(Donation donation) {
		this.donation = donation;
		this.textToRender = MuseumUtils.formatPrice(donation.getPriceData().getEffectivePrice());

		// Determine the item stack to display
		this.itemStack = !donation.isSet()
				? ItemRepository.getItemStack(donation.getId())
				: ItemRepository.getItemStack(
				donation.getSet().stream()
						.filter(piece -> piece.getLeft().toLowerCase(Locale.ENGLISH).contains("helmet") || piece.getLeft().toLowerCase(Locale.ENGLISH).contains("hat"))
						.findFirst()
						.orElse(donation.getSet().get(1)) // gets chestplate
						.getLeft()
		);

		if (itemStack != null) {
			this.visible = true;
			createTooltip();
		}
	}


	/**
	 * Clears the display stack and resets the button state.
	 */
	protected void clearDisplayStack() {
		this.visible = false;
		this.itemStack = null;
		this.tooltip = null;
		this.textToRender = null;
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		context.drawGuiTexture(RenderLayer::getGuiTextured, AnimatedResultButton.SLOT_CRAFTABLE_TEXTURE, this.getX(), this.getY(), this.width, this.height);

		int yOffset = 8;

		if (donation.hasPrice()) {
			int textWidth = TEXT_RENDERER.getWidth(textToRender);
			int centeredX = this.getX() + (this.width / 2) - (textWidth / 2);
			int textY = this.getY() + ITEM_OFFSET + 13;
			context.drawText(TEXT_RENDERER, textToRender, centeredX, textY, 0xFF00FF00, true);

			yOffset -= 4;
		}

		context.drawItemWithoutEntity(itemStack, this.getX() + ITEM_OFFSET, this.getY() + yOffset);

	}

	/**
	 * Creates the tooltip for the button based on its associated donation data
	 */
	private void createTooltip() {
		List<Text> tooltip = new ArrayList<>();

		boolean soulbound = ItemUtils.isSoulbound(itemStack);
		Pair<String, Double> discount = donation.getDiscount();
		List<Pair<String, Integer>> countsTowards = donation.getCountsTowards();

		// Display name
		tooltip.add(MuseumUtils.getDisplayName(donation.getId(), donation.isSet()));

		// Set pieces display names
		if (donation.isSet()) {
			for (Pair<String, PriceData> piece : donation.getSet()) {
				ItemStack stack = ItemRepository.getItemStack(piece.getLeft());
				if (stack != null) {
					Text itemName = stack.getName().copy();
					if (soulbound) {
						tooltip.add(Text.literal("  ").append(itemName));
					} else if (piece.getRight().getEffectivePrice() > 0) {
						tooltip.add(Text.literal("  ").append(itemName).append(Text.literal(" (").formatted(Formatting.DARK_GRAY)).append(Text.literal(MuseumUtils.formatPrice(piece.getRight().getEffectivePrice())).formatted(Formatting.GOLD)).append(Text.literal(")").formatted(Formatting.DARK_GRAY)));
					} else {
						tooltip.add(Text.literal("  ").append(itemName).append(Text.literal(" (").formatted(Formatting.DARK_GRAY)).append(Text.literal("Unknown").formatted(Formatting.RED)).append(Text.literal(")").formatted(Formatting.DARK_GRAY)));
					}
				}
			}
		}
		tooltip.add(Text.empty());

		Text xpText = Text.literal(String.valueOf(donation.getTotalXp())).append(" SkyBlock XP").formatted(Formatting.AQUA);
		tooltip.add(Text.literal("Reward: ").formatted(Formatting.GRAY).append(xpText));

		if (soulbound) {
			tooltip.add(Text.literal("Untradable").formatted(Formatting.GRAY).append(Text.literal(" (Soulbound)").formatted(Formatting.DARK_GRAY)));
		} else {
			PriceData priceData = donation.getPriceData();
			Text lBinText = donation.hasLBinPrice() ? Text.literal(MuseumUtils.formatPrice(priceData.getLBinPrice())).append(" Coins").formatted(Formatting.GOLD) : Text.literal("Unknown").formatted(Formatting.RED);
			MutableText craftCostText = donation.isCraftable() ? Text.literal(MuseumUtils.formatPrice(donation.hasDiscount() ? priceData.getCraftCost() - discount.getRight() : priceData.getCraftCost())).append(" Coins").formatted(Formatting.GOLD) : Text.literal("Unknown").formatted(Formatting.RED);
			Text discountText = donation.hasDiscount() && donation.isCraftable() ? Text.literal(" (").formatted(Formatting.DARK_GRAY).append(Text.literal(MuseumUtils.formatPrice(priceData.getCraftCost())).formatted(Formatting.GOLD)).append(" - ").append(Text.literal(MuseumUtils.formatPrice(discount.getRight())).formatted(Formatting.GOLD)).append(Text.literal(")").formatted(Formatting.DARK_GRAY)) : Text.empty();
			Text xpCoinsRatio = Text.literal(MuseumUtils.formatPrice(donation.getXpCoinsRatio())).append(" Coins per XP").formatted(Formatting.AQUA);

			tooltip.add(Text.literal("Lowest BIN: ").formatted(Formatting.GRAY).append(lBinText));
			tooltip.add(Text.literal("Craft Cost: ").formatted(Formatting.GRAY).append(craftCostText).append(discountText));
			tooltip.add(Text.literal("Coins/XP ratio: ").formatted(Formatting.GRAY).append(xpCoinsRatio));
		}

		if (countsTowards.size() > 1) {
			tooltip.add(Text.empty());
			tooltip.add(Text.literal("Will count for:").formatted(Formatting.GRAY));
			for (Pair<String, Integer> credit : countsTowards) {
				tooltip.add(Text.literal(" ● ").formatted(Formatting.GRAY).append(MuseumUtils.getDisplayName(credit.getLeft(), donation.isSet())).append(Text.literal(" (" + credit.getRight() + " XP)").formatted(Formatting.AQUA)));
			}
		}

		if (donation.isCraftable() && donation.hasDiscount()) {
			tooltip.add(Text.empty());
			tooltip.add(Text.literal("Crafted with: ").formatted(Formatting.GRAY).append(Text.literal("(Donated Item)").formatted(Formatting.DARK_GRAY)));
			tooltip.add(Text.literal(" - ").formatted(Formatting.GRAY).append(MuseumUtils.getDisplayName(discount.getLeft(), donation.isSet())).append(Text.literal(" ✔").formatted(Formatting.GREEN)).append(Text.literal(" (").formatted(Formatting.DARK_GRAY).append(Text.literal(MuseumUtils.formatPrice(discount.getRight())).formatted(Formatting.GOLD)).append(")").formatted(Formatting.DARK_GRAY)));
		}

		tooltip.add(Text.empty());
		tooltip.add(Text.literal("Click on " + WIKI_LOCKUP_KEY.substring(WIKI_LOCKUP_KEY.lastIndexOf('.') + 1).toUpperCase(Locale.ENGLISH) + " to open the wiki page!").formatted(Formatting.YELLOW));

		this.tooltip = tooltip;
	}

	protected List<Text> getItemTooltip() {
		return this.tooltip;
	}


	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}
