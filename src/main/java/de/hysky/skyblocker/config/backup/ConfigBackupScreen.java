package de.hysky.skyblocker.config.backup;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ConfigBackupScreen extends Screen {
	private static final Logger LOGGER = LogUtils.getLogger();

	private final Screen parent;
	private BackupListWidget listWidget;
	private SettingsListWidget detailsWidget;

	public ConfigBackupScreen(@Nullable Screen parent) {
		super(Component.translatable("skyblocker.config.general.backup.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int listWidth = width / 2 - 4;
		int detailsWidth = width - listWidth - 8;
		int listHeight = height - 64; // reserve space for title and buttons

		if (listWidget == null) {
			listWidget = new BackupListWidget(minecraft, listWidth, listHeight, 32, 25);
			listWidget.updateEntries();
		} else {
			listWidget.setSize(listWidth, listHeight);
		}
		listWidget.setX(4);
		listWidget.refreshScrollAmount();
		addRenderableWidget(listWidget);

		if (detailsWidget == null) {
			detailsWidget = new SettingsListWidget(minecraft, detailsWidth, listHeight, 32, 10);
			detailsWidget.updateEntries(listWidget.getSelectedPath());
		} else {
			detailsWidget.setSize(detailsWidth, listHeight);
		}
		detailsWidget.setX(listWidth + 8);
		detailsWidget.refreshScrollAmount();
		addRenderableWidget(detailsWidget);

		Button restoreBtn = Button.builder(Component.translatable("skyblocker.config.general.backup.restore"), b -> {
			Path selected = listWidget.getSelectedPath();
			if (selected != null) {
				assert minecraft != null;
				minecraft.setScreen(new ConfirmScreen(confirm -> {
					if (confirm) {
						try {
							ConfigBackupManager.restoreBackup(selected);
						} catch (IOException e) {
							LOGGER.error("[Skyblocker] Failed to restore backup {}", selected.getFileName().toString(), e);
							minecraft.getToastManager().addToast(new SystemToast(SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
									Component.translatable("skyblocker.config.general.backup.restore.error"),
									null
							));
							return;
						}
						if (parent != null) {
							minecraft.setScreen(SkyblockerConfigManager.createGUI(parent));
						} else {
							minecraft.setScreen(null);
						}
					} else {
						minecraft.setScreen(this);
					}
				}, Component.translatable("skyblocker.config.general.backup.confirm.title"),
						Component.translatableEscape("skyblocker.config.general.backup.confirm.text", selected.getFileName().toString()),
						CommonComponents.GUI_YES, CommonComponents.GUI_NO));
			}
		}).size(90, 20).pos(width / 2 - 95, height - 28).build();
		addRenderableWidget(restoreBtn);

		Button done = Button.builder(CommonComponents.GUI_DONE, b -> onClose()).size(90, 20).pos(width / 2 + 5, height - 28).build();
		addRenderableWidget(done);

		StringWidget titleWidget = new StringWidget(title, font);
		titleWidget.setPosition((width - font.width(title)) / 2, 12);
		addRenderableWidget(titleWidget);
	}

	@Override
	public void onClose() {
		assert minecraft != null;
		minecraft.setScreen(parent);
	}

	private class BackupListWidget extends ObjectSelectionList<BackupEntry> {
		BackupListWidget(Minecraft client, int width, int height, int y, int itemHeight) {
			super(client, width, height, y, itemHeight);
			updateEntries();
		}

		Path getSelectedPath() {
			BackupEntry entry = getSelected();
			return entry != null ? entry.path : null;
		}

		@Override
		public void setSelected(@Nullable BackupEntry entry) {
			super.setSelected(entry);
			detailsWidget.updateEntries(entry != null ? entry.path : null);
		}

		void updateEntries() {
			clearEntries();
			try {
				List<Path> backups = ConfigBackupManager.listBackups();
				for (Path backup : backups) {
					addEntry(new BackupEntry(backup));
				}
			} catch (IOException e) {
				// ignored
			}
		}

		@Override
		protected int scrollBarX() {
			return getX() + getWidth() - 6;
		}
	}

	private class BackupEntry extends ObjectSelectionList.Entry<BackupEntry> {
		private final Path path;

		BackupEntry(Path path) {
			this.path = path;
		}

		@Override
		public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			context.drawCenteredString(font, path.getFileName().toString(), this.getContentXMiddle(), this.getY() + 7, 0xFFFFFFFF);
			if (isMouseOver(mouseX, mouseY)) context.requestCursor(CursorTypes.POINTING_HAND);
		}

		@Override
		public Component getNarration() {
			return Component.empty();
		}
	}

	private Set<String> changedPaths = Collections.emptySet();

	private class SettingsListWidget extends ContainerObjectSelectionList<StringEntry> {
		SettingsListWidget(Minecraft client, int width, int height, int y, int itemHeight) {
			super(client, width, height, y, itemHeight);
		}

		void updateEntries(@Nullable Path path) {
			clearEntries();
			changedPaths = Collections.emptySet();
			if (path == null) return;
			try {
				JsonElement backupJson = JsonParser.parseReader(Files.newBufferedReader(path));
				JsonElement currentJson = JsonParser.parseReader(Files.newBufferedReader(SkyblockerConfigManager.getConfigPath()));
				Set<String> diffs = new HashSet<>();
				findDiffs("", backupJson, currentJson, diffs);
				changedPaths = diffs;
				List<JsonLine> lines = new ArrayList<>();
				formatJson(null, backupJson, "", 0, true, lines);
				for (JsonLine l : lines) {
					addEntry(new StringEntry(l.text, l.path));
				}
			} catch (IOException e) {
				LOGGER.error("[Skyblocker] Error reading backup file for diff display", e);
			}
		}

		@Override
		public int getRowWidth() {
			return getWidth() - 8 - 20;
		}

		private void findDiffs(String pathPrefix, JsonElement backup, JsonElement current, Set<String> diffs) {
			if (backup == null) return;
			if (!backup.equals(current)) {
				if (backup.isJsonObject() && current != null && current.isJsonObject()) {
					for (var entry : backup.getAsJsonObject().entrySet()) {
						String next = pathPrefix.isEmpty() ? entry.getKey() : pathPrefix + "." + entry.getKey();
						findDiffs(next, entry.getValue(), current.getAsJsonObject().get(entry.getKey()), diffs);
					}
				} else if (backup.isJsonArray() && current != null && current.isJsonArray()) {
					for (int i = 0; i < backup.getAsJsonArray().size(); i++) {
						String next = pathPrefix + "[" + i + "]";
						JsonElement curr = i < current.getAsJsonArray().size() ? current.getAsJsonArray().get(i) : null;
						findDiffs(next, backup.getAsJsonArray().get(i), curr, diffs);
					}
				} else {
					diffs.add(pathPrefix);
				}
			}
		}

		private record JsonLine(String text, @Nullable String path) {}

		private void formatJson(String key, JsonElement element, String path, int indent, boolean last, List<JsonLine> out) {
			String ind = "  ".repeat(indent);
			String newPath = key == null ? path : (path.isEmpty() ? key : path + "." + key);

			if (element.isJsonObject()) {
				String line = key == null ? ind + "{" : ind + "\"" + key + "\": {";
				out.add(new JsonLine(line, null));
				var it = element.getAsJsonObject().entrySet().iterator();
				while (it.hasNext()) {
					var e = it.next();
					formatJson(e.getKey(), e.getValue(), newPath, indent + 1, !it.hasNext(), out);
				}
				out.add(new JsonLine(ind + "}" + (last ? "" : ","), path.isEmpty() ? null : path));
			} else if (element.isJsonArray()) {
				String line = key == null ? ind + "[" : ind + "\"" + key + "\": [";
				out.add(new JsonLine(line, null));
				var arr = element.getAsJsonArray();
				for (int i = 0; i < arr.size(); i++) {
					formatJson(null, arr.get(i), newPath + "[" + i + "]", indent + 1, i == arr.size() - 1, out);
				}
				out.add(new JsonLine(ind + "]" + (last ? "" : ","), path));
			} else {
				String value = element.toString();
				String line = key == null ? ind + value : ind + "\"" + key + "\": " + value;
				out.add(new JsonLine(line + (last ? "" : ","), newPath));
			}
		}

		@Override
		protected void renderScrollbar(GuiGraphics context, int mouseX, int mouseY) {
			super.renderScrollbar(context, mouseX, mouseY);
			if (scrollbarVisible()) {
				int scrollBarX = scrollBarX();
				int listWidgetY = getY();
				int totalHeight = height + maxScrollAmount();
				int scrollbarThumbHeight = scrollerHeight();
				for (int i = 0; i < children().size(); i++) {
					StringEntry entry = children().get(i);
					if (entry.path != null && changedPaths.contains(entry.path)) {
						// similar calculation to getRowTop
						int entryY = 4 + i * defaultEntryHeight + getY();
						// height - scrollbarThumbHeight - 2 because we draw a two pixel high indicator.
						// scrollbarThumbHeight thumb height calculations so the changed line is in view when the indicator is in the middle of the scrollbar thumb.
						int barY = entryY * (height - scrollbarThumbHeight - 2) / (totalHeight - defaultEntryHeight) + listWidgetY + scrollbarThumbHeight / 2;
						context.fill(scrollBarX, barY, scrollBarX + 6, barY + 2, 0xFFFFFF55);
					}
				}
			}
		}
	}

	private class StringEntry extends ContainerObjectSelectionList.Entry<StringEntry> {
		private final String text;
		private final @Nullable String path;

		StringEntry(String text, @Nullable String path) {
			this.text = text;
			this.path = path;
		}

		@Override
		public List<GuiEventListener> children() {
			return List.of();
		}

		@Override
		public List<NarratableEntry> narratables() {
			return List.of();
		}

		@Override
		public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			int color = 0xFFFFFFFF;
			if (path != null && changedPaths.contains(path)) {
				color = 0xFFFFFF55;
			}
			context.drawString(font, text, this.getX() + 2, this.getY() + 2, color, false);
		}
	}
}
