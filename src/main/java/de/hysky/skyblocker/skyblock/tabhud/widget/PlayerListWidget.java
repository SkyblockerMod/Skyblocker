package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.config.option.EnumOption;
import de.hysky.skyblocker.skyblock.tabhud.config.option.WidgetOption;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlayerComponent;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.Nullable;

@RegisterWidget
public class PlayerListWidget extends TabHudWidget {
	private static final MutableComponent TITLE = Component.literal("Players").withStyle(ChatFormatting.BOLD);

	private NameSorting sorting = NameSorting.DEFAULT;

	public PlayerListWidget() {
		super("Players", TITLE, ChatFormatting.AQUA.getColor());
	}

	@Override
	protected void updateContent(List<Component> lines, @Nullable List<PlayerInfo> playerListEntries) {
		if (playerListEntries == null) {
			lines.forEach(text -> addComponent(new PlainTextComponent(text)));
		} else switch (sorting) {
			case DEFAULT -> playerListEntries.forEach(playerListEntry -> addComponent(new PlayerComponent(playerListEntry)));
			case null, default -> playerListEntries.stream().sorted(sorting.comparator).forEach(playerListEntry -> addComponent(new PlayerComponent(playerListEntry)));
		}
	}

	@Override
	protected void updateContent(List<Component> lines) {}

	@Override
	public void getOptions(List<WidgetOption<?>> options) {
		super.getOptions(options);
		// TODO Translatable
		options.add(new EnumOption<>(NameSorting.class, "name_sorting", Component.literal("Name Sorting"), () -> sorting, s -> sorting = s, NameSorting.DEFAULT));
	}

	public enum NameSorting implements StringRepresentable {
		DEFAULT,
		ALPHABETICAL(Comparator.comparing(ple -> matchPlayerName(ple.getTabListDisplayName().getString(), "name").orElse(""), String.CASE_INSENSITIVE_ORDER)),
		SKYBLOCK_LEVEL(Comparator.<PlayerInfo>comparingInt(ple -> matchPlayerName(ple.getTabListDisplayName().getString(), "level").map(Integer::parseInt).orElse(0)).reversed());

		public final Comparator<PlayerInfo> comparator;

		NameSorting() {
			this(null);
		}

		NameSorting(Comparator<PlayerInfo> comparator) {
			this.comparator = comparator;
		}

		private static Optional<String> matchPlayerName(String name, String group) {
			Matcher matcher = PlayerListManager.PLAYER_NAME_PATTERN.matcher(name);
			return matcher.matches() ? Optional.of(matcher.group(group)) : Optional.empty();
		}

		@Override
		public String toString() {
			return switch (this) {
				case DEFAULT -> "Default";
				case ALPHABETICAL -> "Alphabetical";
				case SKYBLOCK_LEVEL -> "Skyblock Level";
			};
		}

		@Override
		public String getSerializedName() {
			return name().toLowerCase(Locale.ENGLISH);
		}
	}
}
