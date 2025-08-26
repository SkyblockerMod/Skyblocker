package de.hysky.skyblocker.config.backup;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class ConfigBackupScreen extends Screen {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Screen parent;
	private BackupListWidget listWidget;
	private SettingsListWidget detailsWidget;

	public ConfigBackupScreen(@Nullable Screen parent) {
		super(Text.translatable("skyblocker.config.general.backup.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int listWidth = width / 2 - 4;
		int detailsWidth = width - listWidth - 8;
		int listHeight = height - 64; // reserve space for title and buttons

		if (listWidget == null) {
			listWidget = new BackupListWidget(client, listWidth, listHeight, 32, 24);
		} else {
			listWidget.setDimensions(listWidth, listHeight);
			listWidget.updateEntries();
		}
		listWidget.setX(4);

		if (detailsWidget == null) {
			detailsWidget = new SettingsListWidget(client, detailsWidth, listHeight, 32, 10);
		} else {
			detailsWidget.setDimensions(detailsWidth, listHeight);
		}
		detailsWidget.setX(listWidth + 8);
		detailsWidget.updateEntries(listWidget.getSelectedPath());

		addDrawableChild(listWidget);
		addDrawableChild(detailsWidget);
		ButtonWidget restoreBtn = ButtonWidget.builder(Text.translatable("skyblocker.config.general.backup.restore"), b -> {
			Path selected = listWidget.getSelectedPath();
			if (selected != null) {
				assert client != null;
				client.setScreen(new ConfirmScreen(confirm -> {
					if (confirm) {
						try {
							ConfigBackupManager.restoreBackup(selected);
						} catch (IOException e) {
							LOGGER.error("[Skyblocker] Failed to restore backup {}", selected.getFileName().toString(), e);
							client.getToastManager().add(new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION,
									Text.translatable("skyblocker.config.backup.restore.error"),
									Text.literal("Failed to restore backup")));
							return;
						}
						if (parent != null) {
							client.setScreen(SkyblockerConfigManager.createGUI(parent));
						} else {
							client.setScreen(null);
						}
					} else {
						client.setScreen(this);
					}
				}, Text.translatable("skyblocker.config.general.backup.confirm.title"),
						Text.stringifiedTranslatable("skyblocker.config.general.backup.confirm.text", selected.getFileName().toString()),
						ScreenTexts.YES, ScreenTexts.NO));
			}
		}).size(90, 20).position(width / 2 - 95, height - 28).build();
		addDrawableChild(restoreBtn);
		ButtonWidget done = ButtonWidget.builder(ScreenTexts.DONE, b -> close()).size(90, 20).position(width / 2 + 5, height - 28).build();
		addDrawableChild(done);
	}

	@Override
	public void close() {
		assert client != null;
		client.setScreen(parent);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		listWidget.render(context, mouseX, mouseY, delta);
		detailsWidget.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xFFFFFFFF);
	}

	private class BackupListWidget extends ElementListWidget<BackupEntry> {
		BackupListWidget(MinecraftClient client, int width, int height, int y, int itemHeight) {
			super(client, width, height, y, itemHeight);
			updateEntries();
		}

		Path getSelectedPath() {
			BackupEntry entry = getSelectedOrNull();
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
		public int getRowWidth() {
			return super.getRowWidth();
		}

		@Override
		protected int getScrollbarX() {
			return getX() + getWidth() - 6;
		}
	}

	private class BackupEntry extends ElementListWidget.Entry<BackupEntry> {
		private final Path path;

		BackupEntry(Path path) {
			this.path = path;
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			listWidget.setSelected(this);
			return true;
		}

		@Override
		public List<Element> children() {
			return List.of();
		}

		@Override
		public List<Selectable> selectableChildren() {
			return List.of();
		}

		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			if (this.equals(listWidget.getSelectedOrNull())) {
				int textWidth = textRenderer.getWidth(path.getFileName().toString()) + 8;
				int highlightRight = x + Math.min(textWidth, entryWidth - 5);
				context.fill(x, y, highlightRight, y + entryHeight, 0x80FFFFFF);
			}

			context.drawText(textRenderer, path.getFileName().toString(), x + 4, y + 7, 0xFFFFFFFF, false);
		}
	}

	private Set<String> changedPaths = Collections.emptySet();

	private class SettingsListWidget extends ElementListWidget<StringEntry> {
		SettingsListWidget(MinecraftClient client, int width, int height, int y, int itemHeight) {
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
				// ignore
			}
		}

		@Override
		public int getRowWidth() {
			return getWidth() - 10;
		}

		@Override
		protected int getScrollbarX() {
			return getX() + getWidth() - 6;
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

		private record JsonLine(String text, @Nullable String path) {
		}

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
	}

	private class StringEntry extends ElementListWidget.Entry<StringEntry> {
		private final String text;
		private final @Nullable String path;

		StringEntry(String text, @Nullable String path) {
			this.text = text;
			this.path = path;
		}

		@Override
		public List<Element> children() {
			return List.of();
		}

		@Override
		public List<Selectable> selectableChildren() {
			return List.of();
		}

		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			int color = 0xFFFFFFFF;
			if (path != null && changedPaths.contains(path)) {
				color = 0xFFFFFF55;
			}
			context.drawText(textRenderer, text, x + 2, y + 2, color, false);
		}
	}
}
