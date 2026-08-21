package de.hysky.skyblocker.skyblock.end;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Util;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.config.OptionWidgetCollector;
import de.hysky.skyblocker.skyblock.tabhud.widget.ElementBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Elements;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlainTextElement;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.JsonValueInput;
import de.hysky.skyblocker.utils.JsonValueOutput;
import de.hysky.skyblocker.utils.Location;

@RegisterWidget
public class EndHudWidget extends ElementBasedWidget {
	private static final MutableComponent TITLE = Component.literal("The End").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD);

	private static @Nullable EndHudWidget instance = null;

	private static final FlexibleItemStack ENDERMAN_HEAD = Util.make(new FlexibleItemStack(Items.PLAYER_HEAD), stack -> stack.set(DataComponents.PROFILE, ResolvableProfile.createUnresolved("MHF_Enderman")));
	private static final FlexibleItemStack POPPY = Util.make(new FlexibleItemStack(Items.POPPY), stack -> stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));

	private boolean showZealotKills = true;
	private boolean showProtectorLocation = true;

	public EndHudWidget() {
		super(TITLE, TextColor.DARK_PURPLE.getValue(), new Information("hud_end", Component.literal("End Hud"), Location.THE_END));
		instance = this;
		this.update();
	}

	public static EndHudWidget getInstance() {
		return Objects.requireNonNull(instance, "EndHudWidget not initialized");
	}

	@Override
	public void updateContent() {
		// Zealots
		if (showZealotKills) {
			TheEnd.EndStats endStats = TheEnd.PROFILES_STATS.computeIfAbsent(TheEnd.EndStats.EMPTY);
			addElement(Elements.iconTextComponent(ENDERMAN_HEAD, Component.literal("Zealots").withStyle(ChatFormatting.BOLD)));
			addElement(new PlainTextElement(Component.translatable("skyblocker.end.hud.zealotsSinceLastEye", endStats.zealotsSinceLastEye())));
			addElement(new PlainTextElement(Component.translatable("skyblocker.end.hud.zealotsTotalKills", Formatters.INTEGER_NUMBERS.format(endStats.totalZealotKills()))));
			String avg = endStats.eyes() == 0 ? "???" : Formatters.DOUBLE_NUMBERS.format((float) endStats.totalZealotKills() / endStats.eyes());
			addElement(new PlainTextElement(Component.translatable("skyblocker.end.hud.avgKillsPerEye", avg)));
		}

		// Endstone protector
		if (showProtectorLocation) {
			addElement(Elements.iconTextComponent(POPPY, Component.literal("End Stone Protector").withStyle(ChatFormatting.BOLD)));
			if (TheEnd.stage == 5) {
				addElement(new PlainTextElement(Component.translatable("skyblocker.end.hud.stage", "IMMINENT")));
			} else {
				addElement(new PlainTextElement(Component.translatable("skyblocker.end.hud.stage", String.valueOf(TheEnd.stage))));
			}
			if (TheEnd.currentProtectorLocation == null) {
				addElement(new PlainTextElement(Component.translatable("skyblocker.end.hud.location", "?")));
			} else {
				addElement(new PlainTextElement(Component.translatable("skyblocker.end.hud.location", TheEnd.currentProtectorLocation.name())));
			}
		}
	}

	@Override
	public void getOptionWidgets(OptionWidgetCollector collector) {
		super.getOptionWidgets(collector);
		collector.yesNoButton(Component.translatable("skyblocker.config.otherLocations.end.zealotKillsEnabled"), b -> showZealotKills = b, showZealotKills, Component.translatable("skyblocker.config.otherLocations.end.zealotKillsEnabled.@Tooltip"));
		collector.yesNoButton(Component.translatable("skyblocker.config.otherLocations.end.protectorLocationEnable"), b -> showProtectorLocation = b, showProtectorLocation);
	}

	@Override
	public void load(JsonValueInput input) {
		super.load(input);
		showZealotKills = input.readBooleanOr("zealot_kills", true);
		showProtectorLocation = input.readBooleanOr("protector_location", true);
	}

	@Override
	public void save(JsonValueOutput output) {
		super.save(output);
		output.writeBool("zealot_kills", showZealotKills);
		output.writeBool("protector_location", showProtectorLocation);
	}
}
