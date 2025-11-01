package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.config.option.EnumOption;
import de.hysky.skyblocker.skyblock.tabhud.config.option.WidgetOption;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlayerComponent;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;

@RegisterWidget
public class PlayerListWidget extends TabHudWidget {
	private static final MutableText TITLE = Text.literal("Players").formatted(Formatting.BOLD);

	private NameSorting sorting = NameSorting.DEFAULT;

	public PlayerListWidget() {
		super("Players", TITLE, Formatting.AQUA.getColorValue());
	}

	@Override
	protected void updateContent(List<Text> lines, @Nullable List<PlayerListEntry> playerListEntries) {
		if (playerListEntries == null) {
			lines.forEach(text -> addComponent(new PlainTextComponent(text)));
		} else switch (sorting) {
			case DEFAULT -> playerListEntries.forEach(playerListEntry -> addComponent(new PlayerComponent(playerListEntry)));
			case null, default -> playerListEntries.stream().sorted(sorting.comparator).forEach(playerListEntry -> addComponent(new PlayerComponent(playerListEntry)));
		}
	}

	@Override
	protected void updateContent(List<Text> lines) {}

	@Override
	public void getOptions(List<WidgetOption<?>> options) {
		super.getOptions(options);
		// TODO Translatable
		options.add(new EnumOption<>(NameSorting.class, "name_sorting", Text.literal("Name Sorting"), () -> sorting, s -> sorting = s, NameSorting.DEFAULT));
	}

	public enum NameSorting implements StringIdentifiable {
		DEFAULT,
		ALPHABETICAL(Comparator.comparing(ple -> matchPlayerName(ple.getDisplayName().getString(), "name").orElse(""), String.CASE_INSENSITIVE_ORDER)),
		SKYBLOCK_LEVEL(Comparator.<PlayerListEntry>comparingInt(ple -> matchPlayerName(ple.getDisplayName().getString(), "level").map(Integer::parseInt).orElse(0)).reversed());

		public final Comparator<PlayerListEntry> comparator;

		NameSorting() {
			this(null);
		}

		NameSorting(Comparator<PlayerListEntry> comparator) {
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
		public String asString() {
			return name().toLowerCase(Locale.ENGLISH);
		}
	}
}
