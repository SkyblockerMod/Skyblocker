package de.hysky.skyblocker.skyblock.tabhud.widget;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.config.OptionWidgetCollector;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.ElementCollector;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Elements;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlainTextElement;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.JsonValueInput;
import de.hysky.skyblocker.utils.JsonValueOutput;
import de.hysky.skyblocker.utils.Location;

@RegisterWidget
public class CropMilestonesWidget extends TabHudWidget {

	private boolean showIcon;
	private boolean showProgressBar;

	private static final Pattern PATTERN = Pattern.compile("(?<crop>[a-zA-Z ]+) (?<level>\\d+): (?<percentage>[0-9.]+%|MAX)");
	public CropMilestonesWidget() {
		super("Crop Milestones", Component.literal("Crop Milestones"), TextColor.AQUA.getValue(), Location.GARDEN);
		hideIfMissingByDefault = true;
	}

	@Override
	protected void updateContent(PlayerListManager.Widget widget) {
		Component line = widget.lines().getFirst();
		Matcher matcher = PATTERN.matcher(line.getString());
		if (!matcher.find()) {
			addElement(new PlainTextElement(line));
			return;
		}

		String itemName = matcher.group("crop").trim();
		int level = Integer.parseInt(matcher.group("level"));
		String progress = matcher.group("percentage").trim();

		FlexibleItemStack icon = showIcon ? JacobsContestWidget.FARM_DATA.get(itemName) : null;
		if (progress.equals("MAX") || !showProgressBar) {
			if (icon != null) addElement(Elements.iconTextComponent(icon, line));
			else addElement(new PlainTextElement(line));
		} else {
			float percentage = Float.parseFloat(progress.substring(0, progress.length() - 1));
			addElement(Elements.progressComponent(icon, Component.literal(itemName + " " + level), percentage));
		}
	}

	@Override
	public void getOptionWidgets(OptionWidgetCollector collector) {
		super.getOptionWidgets(collector);
		collector.yesNoButton(Component.translatable("skyblocker.config.hud.cropMilestones.showIcon"), b -> showIcon = b, showIcon);
		collector.yesNoButton(Component.translatable("skyblocker.config.hud.cropMilestones.showProgressBar"), b -> showProgressBar = b, showProgressBar);
	}

	@Override
	public void load(JsonValueInput input) {
		super.load(input);
		showIcon = input.readBooleanOr("icon", true);
		showProgressBar = input.readBooleanOr("progress_bar", true);
	}

	@Override
	public void save(JsonValueOutput output) {
		super.save(output);
		output.writeBool("icon", showIcon);
		output.writeBool("progress_bar", showProgressBar);
	}

	@Override
	protected void updateConfigContentTab(ElementCollector collector) {
		if (!showProgressBar) {
			Component text = Component.literal("Wheat 32: ").append(Component.literal("33.3%").withColor(TextColor.GREEN));
			if (showIcon) collector.addElement(Elements.iconTextComponent(Ico.WHEAT, text));
			else collector.addElement(new PlainTextElement(text));
		} else {
			collector.addElement(Elements.progressComponent(showIcon ? Ico.WHEAT : null, Component.literal("Wheat 32"), 33.3f, TextColor.DARK_AQUA.getValue()));
		}
	}
}
