package de.hysky.skyblocker.skyblock.museum;

import de.hysky.skyblocker.skyblock.item.WikiLookup;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.ItemUtils;
import it.unimi.dsi.fastutil.objects.ObjectDoublePair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import it.unimi.dsi.fastutil.objects.ObjectObjectMutablePair;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DonationButton extends ClickableWidget {
    private static final int SIZE = 33;
    private static final int ITEM_OFFSET = 8;
    private static final TextRenderer TEXT_RENDERER = MinecraftClient.getInstance().textRenderer;
    private Donation donation = null;
    private ItemStack itemStack = null;
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
		this.visible = false;
        this.donation = donation;
        this.textToRender = MuseumUtils.formatPrice(donation.getPriceData().getEffectivePrice());

        // Determine the item stack to display
        this.itemStack = !donation.isSet()
                ? ItemRepository.getItemStack(donation.getId())
                : ItemRepository.getItemStack(
                donation.getSet().stream()
                        .filter(piece -> piece.left().toLowerCase(Locale.ENGLISH).contains("helmet") || piece.left().toLowerCase(Locale.ENGLISH).contains("hat"))
                        .findFirst()
                        .orElse(donation.getSet().get(1)) // gets chestplate
                        .left()
        );

        if (itemStack != null && !itemStack.isEmpty()) {
            this.visible = true;
            createTooltip();
        }
    }


    /**
     * Clears the display stack and resets the button state.
     */
    protected void clearDisplayStack() {
        this.visible = false;
        this.donation = null;
        this.itemStack = null;
        this.tooltip = null;
        this.textToRender = null;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawGuiTexture(RenderLayer::getGuiTextured, AnimatedResultButton.SLOT_CRAFTABLE_TEXTURE, this.getX(), this.getY(), this.width, this.height);

        int yOffset = 8;

        if (donation.hasPrice()) {
            context.drawCenteredTextWithShadow(TEXT_RENDERER, textToRender, this.getX() + (this.width / 2), this.getY() + ITEM_OFFSET + 13, 0xFF00FF00);

            yOffset -= 4;
        }

        context.drawItemWithoutEntity(itemStack, this.getX() + ITEM_OFFSET, this.getY() + yOffset);

    }

    /**
     * Creates the tooltip for the button based on its associated donation data
     */
    private void createTooltip() {
        final String WIKI_LOCKUP_KEY = WikiLookup.wikiLookup.getBoundKeyTranslationKey();
        List<Text> tooltip = new ArrayList<>();

        boolean soulbound = ItemUtils.isSoulbound(itemStack);
        ObjectDoublePair<String> discount = donation.getDiscount();
        List<ObjectIntPair<String>> countsTowards = donation.getCountsTowards();

        // Display name
        tooltip.add(MuseumUtils.getDisplayName(donation.getId(), donation.isSet()));

        // Set pieces display names
        if (donation.isSet()) {
            for (ObjectObjectMutablePair<String, PriceData> piece : donation.getSet()) {
                ItemStack stack = ItemRepository.getItemStack(piece.left());
                if (stack != null) {
                    Text itemName = stack.getName().copy();
                    if (soulbound) {
                        tooltip.add(Text.literal("  ").append(itemName));
                    } else if (piece.right().getEffectivePrice() > 0) {
                        tooltip.add(Text.literal("  ").append(itemName).append(Text.literal(" (").formatted(Formatting.DARK_GRAY)).append(Text.literal(MuseumUtils.formatPrice(piece.right().getEffectivePrice())).formatted(Formatting.GOLD)).append(Text.literal(")").formatted(Formatting.DARK_GRAY)));
                    } else {
                        tooltip.add(Text.literal("  ").append(itemName).append(Text.literal(" (").formatted(Formatting.DARK_GRAY)).append(Text.translatable("skyblocker.museum.hud.unknownPrice").formatted(Formatting.RED)).append(Text.literal(")").formatted(Formatting.DARK_GRAY)));
                    }
                }
            }
        }
        tooltip.add(Text.empty());

        Text xpText = Text.literal(String.valueOf(donation.getTotalXp())).append(" ").append(Text.translatable("skyblocker.museum.hud.skyblockXp")).formatted(Formatting.AQUA);
        tooltip.add(Text.translatable("skyblocker.museum.hud.xpReward").append(": ").formatted(Formatting.GRAY).append(xpText));

        if (soulbound) {
            tooltip.add(Text.translatable("skyblocker.museum.hud.untradeableItem").formatted(Formatting.GRAY).append(Text.literal(" (").append(Text.translatable("skyblocker.museum.hud.soulboundItem")).append(")").formatted(Formatting.DARK_GRAY)));
        } else {
            PriceData priceData = donation.getPriceData();
            Text lBinText = donation.hasLBinPrice() ? Text.literal(MuseumUtils.formatPrice(priceData.getLBinPrice())).append(" Coins").formatted(Formatting.GOLD) : Text.translatable("skyblocker.museum.hud.unknownPrice").formatted(Formatting.RED);
            MutableText craftCostText = donation.isCraftable() ? Text.literal(MuseumUtils.formatPrice(donation.hasDiscount() ? priceData.getCraftCost() - discount.rightDouble() : priceData.getCraftCost())).append(" ").append(Text.translatable("skyblocker.museum.hud.coin")).formatted(Formatting.GOLD) : Text.translatable("skyblocker.museum.hud.unknownPrice").formatted(Formatting.RED);
            Text discountText = donation.hasDiscount() && donation.isCraftable() ? Text.literal(" (").formatted(Formatting.DARK_GRAY).append(Text.literal(MuseumUtils.formatPrice(priceData.getCraftCost())).formatted(Formatting.GOLD)).append(" - ").append(Text.literal(MuseumUtils.formatPrice(discount.rightDouble())).formatted(Formatting.GOLD)).append(Text.literal(")").formatted(Formatting.DARK_GRAY)) : Text.empty();
            Text xpCoinsRatio = Text.literal(MuseumUtils.formatPrice(donation.getXpCoinsRatio())).append(" ").append(Text.translatable("skyblocker.museum.hud.ratioText")).formatted(Formatting.AQUA);

            tooltip.add(Text.translatable("skyblocker.museum.hud.sorter.lBin").append(": ").formatted(Formatting.GRAY).append(lBinText));
            tooltip.add(Text.translatable("skyblocker.museum.hud.sorter.craftCost").append(": ").formatted(Formatting.GRAY).append(craftCostText).append(discountText));
            tooltip.add(Text.translatable("skyblocker.museum.hud.sorter.ratio").append(": ").formatted(Formatting.GRAY).append(xpCoinsRatio));
        }

        if (countsTowards.size() > 1) {
            tooltip.add(Text.empty());
            tooltip.add(Text.translatable("skyblocker.museum.hud.countsFor").append(":").formatted(Formatting.GRAY));
            for (ObjectIntPair<String> credit : countsTowards) {
                tooltip.add(Text.literal(" ● ").formatted(Formatting.GRAY).append(MuseumUtils.getDisplayName(credit.left(), donation.isSet())).append(Text.literal(" (" + credit.rightInt() + " XP)").formatted(Formatting.AQUA)));
            }
        }

        if (donation.isCraftable() && donation.hasDiscount()) {
            tooltip.add(Text.empty());
            tooltip.add(Text.translatable("skyblocker.museum.hud.craftIngredient").append(": ").formatted(Formatting.GRAY).append(Text.literal("(").append(Text.translatable("skyblocker.museum.hud.alreadyDonatedItem").append(")")).formatted(Formatting.DARK_GRAY)));
            tooltip.add(Text.literal(" - ").formatted(Formatting.GRAY).append(MuseumUtils.getDisplayName(discount.left(), donation.isSet())).append(Text.literal(" ✔").formatted(Formatting.GREEN)).append(Text.literal(" (").formatted(Formatting.DARK_GRAY).append(Text.literal(MuseumUtils.formatPrice(discount.rightDouble())).formatted(Formatting.GOLD)).append(")").formatted(Formatting.DARK_GRAY)));
        }

        tooltip.add(Text.empty());
        tooltip.add(Text.translatable("skyblocker.museum.hud.wikiLookup", WIKI_LOCKUP_KEY.substring(WIKI_LOCKUP_KEY.lastIndexOf('.') + 1).toUpperCase(Locale.ENGLISH)).formatted(Formatting.YELLOW));

        this.tooltip = tooltip;
    }

    protected List<Text> getItemTooltip() {
        return this.tooltip;
    }


    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}
