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
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.profileviewer2.LoadingInformation;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.CollectionTiers;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.EliteLeaderboards;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.RomanNumerals;
import de.hysky.skyblocker.utils.SkyBlockColors;
import de.hysky.skyblocker.utils.SkyBlockTooltipStyles;

public sealed class CollectionItemWidget extends AbstractWidget permits MinionWidget {
	private static final Identifier BACKGROUND = SkyblockerMod.id("profile_viewer2/basic_background");
	private static final int WIDTH = 20;
	private static final int TEXT_Y_OFFSET = 2;
	private final ItemStack icon;
	private final Component tierText;
	private final List<Component> tooltip;
	private final @Nullable Identifier tooltipStyle;

	public CollectionItemWidget(ItemStack icon, Component tierText, List<Component> tooltip, @Nullable Identifier tooltipStyle) {
		super(0, 0, WIDTH, WIDTH + TEXT_Y_OFFSET + Minecraft.getInstance().font.lineHeight, Component.empty());
		this.icon = icon;
		this.tierText = tierText;
		this.tooltip = tooltip;
		this.tooltipStyle = tooltipStyle;

		// Make widget unclickable
		this.active = false;
	}

	public static CollectionItemWidget create(String id, LoadingInformation info, CollectionTiers.Report report) {
		int tier = report.tier();
		boolean isMaxTier = tier == CollectionTiers.getMaxTier(id);

		String neuId = id.replace(':', '-');
		ItemStack stack = ItemRepository.getItemStack(neuId, Ico.BARRIER).getStackOrThrow().copy();

		// Make max collections have glint
		if (isMaxTier) {
			stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
		}

		Component tierText = buildTierText(tier, isMaxTier);
		List<Component> tooltip = buildTooltip(id, stack.getHoverName().getString(), info, report);
		Identifier tooltipStyle = getTooltipType(isMaxTier);

		return new CollectionItemWidget(stack, tierText, tooltip, tooltipStyle);
	}

	protected static Component buildTierText(int tier, boolean isMaxTier) {
		String tierText = tier == 0 ? "0" : RomanNumerals.decimalToRoman(tier);
		return Component.literal(tierText).withColor(isMaxTier ? SkyBlockColors.GOLD.getValue() : CommonColors.DARK_GRAY);
	}

	private static List<Component> buildTooltip(String id, String name, LoadingInformation info, CollectionTiers.Report report) {
		List<Component> tooltip = new ArrayList<>();

		tooltip.add(Component.literal(name));
		tooltip.add(Component.literal("Collection Item").withStyle(ChatFormatting.DARK_GRAY));
		tooltip.add(Component.empty());

		if (info.profile().hasBeenCoop()) {
			tooltip.add(Component.literal("Personal: " + Formatters.INTEGER_NUMBERS.format(report.personal())).withStyle(ChatFormatting.GOLD));
			tooltip.add(Component.literal("Co-op: " + Formatters.INTEGER_NUMBERS.format(report.coop())).withStyle(ChatFormatting.AQUA));
		}

		tooltip.add(Component.literal("Collection: " + Formatters.INTEGER_NUMBERS.format(report.total())).withStyle(ChatFormatting.YELLOW));
		tooltip.add(Component.empty());
		tooltip.add(Component.literal(String.format(Locale.ENGLISH, "Collection Tier: %d/%d", report.tier(), CollectionTiers.getMaxTier(id))).withStyle(ChatFormatting.LIGHT_PURPLE));

		String leaderboardId = EliteLeaderboards.getCollectionLeaderboardMappings().getOrDefault(id, "");
		int leaderboardPosition = info.getLeaderboardPosition(leaderboardId);

		if (leaderboardPosition != EliteLeaderboards.NO_POSITION) {
			tooltip.add(Component.literal("Leaderboard: #" + Formatters.INTEGER_NUMBERS.format(leaderboardPosition)).withStyle(ChatFormatting.LIGHT_PURPLE));
		}

		return List.copyOf(tooltip);
	}

	protected static @Nullable Identifier getTooltipType(boolean isMaxTier) {
		return isMaxTier ? SkyBlockTooltipStyles.LEGENDARY : null;
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
