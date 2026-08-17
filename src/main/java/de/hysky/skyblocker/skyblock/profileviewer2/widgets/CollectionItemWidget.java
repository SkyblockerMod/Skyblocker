package de.hysky.skyblocker.skyblock.profileviewer2.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jspecify.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.profileviewer2.model.ApiProfile;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.CollectionTiers;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.RomanNumerals;
import de.hysky.skyblocker.utils.SkyBlockTooltipStyles;

public final class CollectionItemWidget extends AbstractWidget {
	private static final Identifier BACKGROUND = SkyblockerMod.id("profile_viewer2/basic_background");
	private static final int WIDTH = 20;
	private static final int TEXT_Y_OFFSET = 2;
	private final ItemStack icon;
	private final Component tierText;
	private final List<Component> tooltip;
	private final @Nullable Identifier tooltipStyle;

	public CollectionItemWidget(String id, FlexibleItemStack icon, ApiProfile profile, CollectionTiers.Report report) {
		super(0, 0, WIDTH, WIDTH + TEXT_Y_OFFSET + Minecraft.getInstance().font.lineHeight, Component.empty());

		boolean isMaxTier = report.tier() == CollectionTiers.getMaxTier(id);
		ItemStack stack = icon.getStackOrThrow().copy();

		// Make max collections have glint
		if (isMaxTier) {
			stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
		}

		this.icon = stack;

		this.tierText = Component.literal(RomanNumerals.decimalToRoman(report.tier())).withStyle(isMaxTier ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY);

		String name = icon.getOrDefault(DataComponents.CUSTOM_NAME, Component.empty()).getString();
		this.tooltip = buildTooltip(id, name, profile, report);
		this.tooltipStyle = isMaxTier ? SkyBlockTooltipStyles.LEGENDARY : null;

		this.active = false;
	}

	private static List<Component> buildTooltip(String id, String name, ApiProfile profile, CollectionTiers.Report report) {
		List<Component> tooltip = new ArrayList<>();

		tooltip.add(Component.literal(name));
		tooltip.add(Component.literal("Collection Item").withStyle(ChatFormatting.DARK_GRAY));
		tooltip.add(Component.empty());

		if (profile.hasBeenCoop()) {
			tooltip.add(Component.literal("Personal: " + Formatters.INTEGER_NUMBERS.format(report.personal())).withStyle(ChatFormatting.GOLD));
			tooltip.add(Component.literal("Co-op: " + Formatters.INTEGER_NUMBERS.format(report.coop())).withStyle(ChatFormatting.AQUA));
		}

		tooltip.add(Component.literal("Collection: " + Formatters.INTEGER_NUMBERS.format(report.total())).withStyle(ChatFormatting.YELLOW));
		tooltip.add(Component.empty());
		tooltip.add(Component.literal(String.format(Locale.ENGLISH, "Collection Tier: %d/%d", report.tier(), CollectionTiers.getMaxTier(id))).withStyle(ChatFormatting.LIGHT_PURPLE));

		return List.copyOf(tooltip);
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		Font font = Minecraft.getInstance().font;

		int itemBoxHeight = this.getHeight() - TEXT_Y_OFFSET - font.lineHeight;
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.getX(), this.getY(), this.getWidth(), itemBoxHeight);
		graphics.item(this.icon, this.getX() + (this.getWidth() - GuiRenderer.DEFAULT_ITEM_SIZE) / 2, this.getY() + (itemBoxHeight - GuiRenderer.DEFAULT_ITEM_SIZE) / 2);

		graphics.text(font, this.tierText, this.getX() + (this.getWidth() - font.width(this.tierText)) / 2, this.getY() + (this.getHeight() - font.lineHeight), CommonColors.WHITE, false);

		if (this.isHovered()) {
			graphics.setComponentTooltipForNextFrame(font, this.tooltip, mouseX, mouseY, this.tooltipStyle);
		}
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {}

	@Override
	public boolean shouldTakeFocusAfterInteraction() {
		return false;
	}
}
