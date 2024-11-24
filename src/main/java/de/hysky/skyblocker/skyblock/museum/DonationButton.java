package de.hysky.skyblocker.skyblock.museum;

import de.hysky.skyblocker.skyblock.item.MuseumItemCache;
import de.hysky.skyblocker.skyblock.item.WikiLookup;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.ItemUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.recipebook.AnimatedResultButton;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DonationButton extends ClickableWidget {

	private static final int SIZE = 33;
	private static final int ITEM_OFFSET = 8;
	private final Map<String, String> setEffectivePricesText = new Object2ObjectArrayMap<>();
	List<Text> tooltip;
	private Donation donation = null;
	private ItemStack itemStack = null;
	private String effectivePriceText = null;

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
		this.effectivePriceText = FormatingUtils.formatPrice(donation.getEffectivePrice());

		// Populate effective prices for armor pieces in the set
		if (donation.isArmorSet()) {
			donation.getSet().forEach(piece -> setEffectivePricesText.put(piece.getId(), FormatingUtils.formatPrice(piece.getEffectivePrice())));
		}

		// Determine the item stack to display
		this.itemStack = !donation.isArmorSet()
				? ItemRepository.getItemStack(donation.getId())
				: ItemRepository.getItemStack(
				donation.getSet().stream()
						.filter(piece -> piece.getId().toLowerCase().contains("helmet") || piece.getId().toLowerCase().contains("hat"))
						.findFirst()
						.orElse(donation.getSet().get(1)) // Get chestplate if helmet not found
						.getId()
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
		this.visible = false; // Hides the button
		this.itemStack = null;
		this.tooltip = null;
		this.effectivePriceText = null;
		this.setEffectivePricesText.clear();
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		if (visible) {
			MinecraftClient client = MinecraftClient.getInstance();

			context.drawGuiTexture(RenderLayer::getGuiTextured, AnimatedResultButton.SLOT_CRAFTABLE_TEXTURE, this.getX(), this.getY(), this.width, this.height);

			int yOffset = 8;

			// Draw effective price if available
			if (donation.hasPrice()) {
				int textWidth = client.textRenderer.getWidth(effectivePriceText);
				int centeredX = this.getX() + (this.width / 2) - (textWidth / 2);
				int textY = this.getY() + ITEM_OFFSET + 13;
				context.drawText(client.textRenderer, effectivePriceText, centeredX, textY, 0xFF00FF00, true);
				yOffset -= 4;
			}

			context.drawItemWithoutEntity(itemStack, this.getX() + ITEM_OFFSET, this.getY() + yOffset);
		}
	}

	/**
	 * Creates the tooltip for the button based on its associated donation data
	 */
	private void createTooltip() {
		List<Text> tooltip = new ArrayList<>();
		Style textStyle = Style.EMPTY;

		if (donation.isArmorSet()) {
			for (ArmorPiece piece : donation.getSet()) {
				ItemStack stack = ItemRepository.getItemStack(piece.getId());
				if (stack != null) {
					textStyle = stack.getName().getSiblings().getFirst().getStyle();
					Text itemName = stack.getName().copy();
					if (ItemUtils.getLore(stack).stream().anyMatch(lore -> lore.getString().toLowerCase().contains("soulbound"))) {
						tooltip.add(Text.literal("  ").append(itemName));
					} else if (setEffectivePricesText.get(piece.getId()) != null) {
						tooltip.add(Text.literal("  ").append(itemName).append(Text.literal(" (").formatted(Formatting.DARK_GRAY)).append(Text.literal(setEffectivePricesText.get(piece.getId())).formatted(Formatting.GOLD)).append(Text.literal(")").formatted(Formatting.DARK_GRAY)));
					} else {
						tooltip.add(Text.literal("  ").append(itemName).append(Text.literal(" (").formatted(Formatting.DARK_GRAY)).append(Text.literal("Unknown").formatted(Formatting.RED)).append(Text.literal(")").formatted(Formatting.DARK_GRAY)));
					}
				}
			}
			String armorName = MuseumItemCache.ARMOR_NAMES.get(donation.getId());
			tooltip.addFirst(Text.literal(Objects.requireNonNullElseGet(armorName, () -> donation.getId().toLowerCase())).setStyle(textStyle));
		} else {
			ItemStack stack = ItemRepository.getItemStack(donation.getId());
			if (stack != null) tooltip.add(stack.getName());
		}

		Text lbinText = donation.hasLBinPrice() ? Text.literal(FormatingUtils.formatPrice(donation.getPrice())).append(" Coins").formatted(Formatting.GOLD) : Text.literal("Unknown").formatted(Formatting.RED);
		Text craftCostText = donation.isCraftable() ? Text.literal(FormatingUtils.formatPrice(donation.getCraftCost())).append(" Coins").formatted(Formatting.GOLD) : Text.literal("Unknown").formatted(Formatting.RED);

		String wikiLookupKey = WikiLookup.wikiLookup.getBoundKeyTranslationKey();

		tooltip.add(Text.literal(""));
		tooltip.add(Text.literal("Reward: ").formatted(Formatting.GRAY).append(Text.literal(String.valueOf(donation.getXp())).append(" SkyBlock XP").formatted(Formatting.AQUA)));
		tooltip.add(Text.literal("Lowest BIN: ").formatted(Formatting.GRAY).append(lbinText));
		tooltip.add(Text.literal("Craft Cost: ").formatted(Formatting.GRAY).append(craftCostText));
		tooltip.add(Text.literal(""));
		tooltip.add(Text.literal("Click on " + wikiLookupKey.substring(wikiLookupKey.lastIndexOf('.') + 1).toUpperCase() + " to open the wiki page!").formatted(Formatting.YELLOW));

		this.tooltip = tooltip;
	}

	protected List<Text> getItemTooltip() {
		return this.tooltip;
	}


	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}
