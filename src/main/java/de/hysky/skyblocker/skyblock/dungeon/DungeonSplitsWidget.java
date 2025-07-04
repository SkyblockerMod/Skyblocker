package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.events.ChatEvents;
import de.hysky.skyblocker.events.DungeonEvents;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.skyblock.tabhud.widget.TableWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Component;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.profile.ProfiledData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.client.font.TextRenderer;
import com.mojang.serialization.Codec;

import java.nio.file.Path;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RegisterWidget
public class DungeonSplitsWidget extends TableWidget {
	private static final Pattern FLOOR_PATTERN = Pattern.compile(".*?(?=T)The Catacombs \\((?<floor>[EFM]\\D*\\d*)\\)");

	private static final Pattern DUNGEON_START = Pattern.compile("\\[NPC] Mort: Here, I found this map when I first entered the dungeon\\.|\\[NPC] Mort: Right-click the Orb for spells, and Left-click \\(or Drop\\) to use your Ultimate!");
	private static final Pattern BLOOD_OPEN = Pattern.compile("^\\[BOSS] The Watcher: (Congratulations, you made it through the Entrance\\.|Ah, you've finally arrived\\.|Ah, we meet again\\.\\.\\.|So you made it this far\\.\\.\\. interesting\\.|You've managed to scratch and claw your way here, eh\\?|I'm starting to get tired of seeing you around here\\.\\.|Oh\\.\\. hello\\?|Things feel a little more roomy now, eh\\?)$|^The BLOOD DOOR has been opened!$");
	private static final Pattern BLOOD_CLEAR = Pattern.compile("\\[BOSS] The Watcher: You have proven yourself\\. You may pass\\.");
	private static final Pattern DUNGEON_END = Pattern.compile("^\\s*☠ Defeated (.+) in 0?([\\dhms ]+?)\\s*(\\(NEW RECORD!\\))?$");

	private static final Pattern F1_ENTRY = Pattern.compile("^\\[BOSS] Bonzo: Gratz for making it this far, but I'm basically unbeatable\\.$");
	private static final Pattern F2_ENTRY = Pattern.compile("^\\[BOSS] Scarf: This is where the journey ends for you, Adventurers\\.$");
	private static final Pattern F3_ENTRY = Pattern.compile("^\\[BOSS] The Professor: I was burdened with terrible news recently\\.\\.\\.$");
	private static final Pattern F4_ENTRY = Pattern.compile("^\\[BOSS] Thorn: Welcome Adventurers! I am Thorn, the Spirit! And host of the Vegan Trials!$");
	private static final Pattern F5_ENTRY = Pattern.compile("^\\[BOSS] Livid: Welcome, you've arrived right on time\\. I am Livid, the Master of Shadows\\.$");
	private static final Pattern F6_ENTRY = Pattern.compile("^\\[BOSS] Sadan: So you made it all the way here\\.\\.\\. Now you wish to defy me\\? Sadan\\?!$");

	private static final Pattern F7_ENTRY = Pattern.compile("^\\[BOSS] Maxor: WELL! WELL! WELL! LOOK WHO'S HERE!$");
	private static final Pattern F7_MAXOR = Pattern.compile("^\\[BOSS] Storm: Pathetic Maxor, just like expected\\.$");
	private static final Pattern F7_STORM = Pattern.compile("^\\[BOSS] Goldor: Who dares trespass into my domain\\?$");
	private static final Pattern F7_TERMINALS = Pattern.compile("^The Core entrance is opening!$");
	private static final Pattern F7_GOLDOR = Pattern.compile("^\\[BOSS] Necron: You went further than any human before, congratulations\\.$");
	private static final Pattern F7_NECRON = Pattern.compile("^\\[BOSS] Necron: All this, for nothing\\.\\.\\.$");

	private static final Pattern BONZO_SIKE = Pattern.compile("\\[BOSS] Bonzo: Oh I'm dead!");
	private static final Pattern SCARF_MINIONS = Pattern.compile("^\\[BOSS] Scarf: Did you forget\\? I was taught by the best! Let's dance\\.$");
	private static final Pattern PROFESSOR_TRANSFORM = Pattern.compile("^\\[BOSS] The Professor: Oh\\? You found my Guardians' one weakness\\?$");
	private static final Pattern TERRACOTTAS = Pattern.compile("^\\[BOSS] Sadan: ENOUGH!$");
	private static final Pattern SADAN_GIANTS = Pattern.compile("^\\[BOSS] Sadan: ENOUGH!$");

	/**
	 * Colors used for split names in rainbow order.
	 */
	private static final int[] SPLIT_COLORS = {
			0xfd5858, // red
			0xfdab58, // orange
			0xfdfd58, // yellow
			0xabfd58, // chartreuse
			0x58fd58, // green
			0x58fdab, // aqua
			0x58abfd, // blue
			0x5858fd, // indigo
			0xab58fd, // violet
			0xfd58fd  // pink
	};

	private static final Map<String, List<Split>> FLOOR_SPLITS = new HashMap<>();

	static {
		FLOOR_SPLITS.put("F1", List.of(
				new Split("Blood Open", BLOOD_OPEN),
				new Split("Blood Clear", BLOOD_CLEAR),
				new Split("Bonzo Entry", F1_ENTRY),
				new Split("Bonzo's Sike", BONZO_SIKE),
				new Split("Finish", DUNGEON_END)
		));
		FLOOR_SPLITS.put("F2", List.of(
				new Split("Blood Open", BLOOD_OPEN),
				new Split("Blood Clear", BLOOD_CLEAR),
				new Split("Scarf Entry", F2_ENTRY),
				new Split("Scarf Minions", SCARF_MINIONS),
				new Split("Finish", DUNGEON_END)
		));
		FLOOR_SPLITS.put("F3", List.of(
				new Split("Blood Open", BLOOD_OPEN),
				new Split("Blood Clear", BLOOD_CLEAR),
				new Split("Professor Entry", F3_ENTRY),
				new Split("Professor Transform", PROFESSOR_TRANSFORM),
				new Split("Finish", DUNGEON_END)
		));
		FLOOR_SPLITS.put("F4", List.of(
				new Split("Blood Open", BLOOD_OPEN),
				new Split("Blood Clear", BLOOD_CLEAR),
				new Split("Thorn Entry", F4_ENTRY),
				new Split("Finish", DUNGEON_END)
		));
		FLOOR_SPLITS.put("F5", List.of(
				new Split("Blood Open", BLOOD_OPEN),
				new Split("Blood Clear", BLOOD_CLEAR),
				new Split("Livid Entry", F5_ENTRY),
				new Split("Finish", DUNGEON_END)
		));
		FLOOR_SPLITS.put("F6", List.of(
				new Split("Blood Open", BLOOD_OPEN),
				new Split("Blood Clear", BLOOD_CLEAR),
				new Split("Sadan Entry", F6_ENTRY),
				new Split("Terracottas", TERRACOTTAS),
				new Split("Giants", SADAN_GIANTS),
				new Split("Finish", DUNGEON_END)
		));
		FLOOR_SPLITS.put("F7", List.of(
				new Split("Blood Open", BLOOD_OPEN),
				new Split("Blood Clear", BLOOD_CLEAR),
				new Split("Portal Entry", F7_ENTRY),
				new Split("Maxor", F7_MAXOR),
				new Split("Storm", F7_STORM),
				new Split("Terminals", F7_TERMINALS),
				new Split("Goldor", F7_GOLDOR),
				new Split("Necron", F7_NECRON),
				new Split("Finish", DUNGEON_END)
		));
	}

	private static final Path BEST_FILE = SkyblockerMod.CONFIG_DIR.resolve("dungeon_split_bests.json");
	private static final Codec<Map<String, Map<String, Long>>> BEST_CODEC = Codec.unboundedMap(Codec.STRING, Codec.unboundedMap(Codec.STRING, Codec.LONG));
	private static final ProfiledData<Map<String, Map<String, Long>>> BEST_SPLITS = new ProfiledData<>(BEST_FILE, BEST_CODEC, true, true);

	private static DungeonSplitsWidget instance;

	private final List<Split> splits = new ArrayList<>();
	private long startTime = 0L;
	private long elapsedTime = 0L;
	private boolean running = false;
	private Formatting timerColor = Formatting.YELLOW;
	private String floor = "?";
	/**
	 * The floor that the splits list was last loaded for.
	 */
	private String loadedFloor = null;

	private int nameWidth = 0;
	private int midWidth = 0;
	private int rightWidth = 0;

	public DungeonSplitsWidget() {
		super(Text.literal("Splits").formatted(Formatting.GOLD, Formatting.BOLD),
				Formatting.GOLD.getColorValue(), "dungeon_splits", 3, 0, false);
		instance = this;

		BEST_SPLITS.init();

		DungeonEvents.DUNGEON_LOADED.register(this::onDungeonLoaded);
		ChatEvents.RECEIVE_STRING.register(this::onChatMessage);
		SkyblockEvents.LOCATION_CHANGE.register(this::onLocationChange);
	}

	public static DungeonSplitsWidget getInstance() {
		return instance;
	}

	private void onDungeonLoaded() {
		running = false;
		elapsedTime = 0L;
		startTime = 0L;
		timerColor = Formatting.YELLOW;
		loadedFloor = null; // force reloading splits once the scoreboard is ready
		updateFloor();
		loadFloorSplits();
	}

	private void onLocationChange(Location location) {
		if (location != Location.DUNGEON) {
			if (running) {
				stopTimer(false);
			}
			running = false;
			elapsedTime = 0L;
			startTime = 0L;
			floor = "?";
			loadedFloor = null;
			splits.clear();
		}
	}

	private void onChatMessage(String message) {
		if (!Utils.isInDungeons()) return;
		String stripped = Formatting.strip(message);

		if (!running && DUNGEON_START.matcher(stripped).matches()) {
			startTime = System.currentTimeMillis();
			elapsedTime = 0L;
			running = true;
			timerColor = Formatting.YELLOW;
			for (Split split : splits) {
				split.reset();
			}
			return;
		}

		if (!running) return;

		for (int i = 0; i < splits.size(); i++) {
			Split split = splits.get(i);
			if (!split.completed && split.trigger.matcher(stripped).matches()) {
				long time = System.currentTimeMillis() - startTime;
				split.complete(time);

				long prev = i == 0 ? 0L : splits.get(i - 1).completedTime;
				long segment = time - prev;
				updateBest(split, segment);

				if (i == splits.size() - 1) {
					stopTimer(true);
				}
				return;
			}
		}

		if (stripped.contains("EXTRA STATS")) {
			boolean allCompleted = true;
			for (Split split : splits) {
				if (!split.completed) {
					allCompleted = false;
					break;
				}
			}
			if (!allCompleted) {
				stopTimer(false);
			}
		}
	}

	private void updateFloor() {
		String old = floor;
		for (String line : Utils.STRING_SCOREBOARD) {
			Matcher m = FLOOR_PATTERN.matcher(line);
			if (m.matches()) {
				floor = m.group("floor");
				break;
			}
		}
		if (!floor.equals(old)) {
			loadFloorSplits();
		}
	}

	/**
	 * Load the split list and best times for the currently detected floor if it has changed.
	 */
	private void loadFloorSplits() {
		if (floor.equals(loadedFloor) || floor.equals("?")) {
			return;
		}

		loadedFloor = floor;
		splits.clear();

		List<Split> group = FLOOR_SPLITS.get(floor);
		if (group != null) {
			Map<String, Map<String, Long>> data = BEST_SPLITS.computeIfAbsent(HashMap::new);
			Map<String, Long> floorData = data.get(floor);
			for (Split s : group) {
				long best = floorData != null ? floorData.getOrDefault(s.name, 0L) : 0L;
				splits.add(new Split(s.name, s.trigger, best));
			}
		}
	}


	@Override
	public boolean shouldUpdateBeforeRendering() {
		return true;
	}

	@Override
	public Set<Location> availableLocations() {
		return Set.of(Location.DUNGEON);
	}

	@Override
	public void setEnabledIn(Location location, boolean enabled) {
// always enabled when in dungeons
	}

	@Override
	public boolean isEnabledIn(Location location) {
		return location == Location.DUNGEON;
	}

	@Override
	public void updateContent() {
		if (!(MinecraftClient.getInstance().currentScreen instanceof WidgetsConfigurationScreen)) {
			updateFloor();
			loadFloorSplits();
		}

		addComponent(new PlainTextComponent(Text.literal("Floor: " + floor)));

		super.updateContent();

		long now = running ? System.currentTimeMillis() - startTime : (startTime == 0L ? 0L : elapsedTime);
		addComponent(new PlainTextComponent(Text.literal(formatTime(now)).formatted(timerColor)));
	}

	@Override
	protected List<Row> buildRows() {
		long now = running ? System.currentTimeMillis() - startTime : (startTime == 0L ? 0L : elapsedTime);
		long previous = 0L;
		boolean showingCurrent = false;
		List<Row> rows = new ArrayList<>();

		for (int idx = 0; idx < splits.size(); idx++) {
			Split split = splits.get(idx);
			Component name = new PlainTextComponent(Text.literal(split.name)
					.withColor(SPLIT_COLORS[idx % SPLIT_COLORS.length]));

			String bestStr = formatTime(split.bestTime);
			Component mid;
			Component right;
			int border = 0;

			if (split.completed) {
				long segmentTime = split.completedTime - previous;
				long diff = segmentTime - split.bestTime;
				Formatting fmt = diff <= 0 ? Formatting.GREEN : Formatting.RED;
				String diffStr = String.format("%+.2fs", diff / 1000.0);
				mid = new PlainTextComponent(Text.literal(diffStr).formatted(fmt));
				right = new PlainTextComponent(Text.literal(formatTime(split.completedTime))
						.formatted(Formatting.YELLOW));
				previous = split.completedTime;
			} else if (!showingCurrent && (running || startTime != 0L)) {
				long segmentTime = now - previous;
				mid = new PlainTextComponent(Text.literal(formatTime(segmentTime)));
				right = new PlainTextComponent(Text.literal(bestStr));
				showingCurrent = true;
				border = SPLIT_COLORS[idx % SPLIT_COLORS.length];
			} else {
				mid = new PlainTextComponent(Text.literal("--"));
				right = new PlainTextComponent(Text.literal(bestStr));
			}

			rows.add(new Row(List.of(name, mid, right), border));
		}
		return rows;
	}


	private static String padSpaces(int pixelWidth, TextRenderer tr) {
		int spaceWidth = tr.getWidth(" ");
		int count = (pixelWidth + spaceWidth - 1) / spaceWidth;
		return " ".repeat(count);
	}

	private void stopTimer(boolean success) {
		if (running) {
			elapsedTime = System.currentTimeMillis() - startTime;
			running = false;

			if (success) {
				Map<String, Map<String, Long>> data = new HashMap<>(BEST_SPLITS.computeIfAbsent(HashMap::new));
				Map<String, Long> floorData = new HashMap<>(data.getOrDefault(floor, new HashMap<>()));
				boolean updated = false;
				for (Split split : splits) {
					if (split.completed) {
						long currentBest = floorData.getOrDefault(split.name, 0L);
						if (currentBest == 0L || split.completedTime < currentBest) {
							floorData.put(split.name, split.completedTime);
							updated = true;
						}
					}
				}
				if (updated) {
					data.put(floor, floorData);
					BEST_SPLITS.put(data);
					BEST_SPLITS.save();
				}
			}
		}
		timerColor = success ? Formatting.GREEN : Formatting.RED;
	}

	private void updateBest(Split split, long completionTime) {
		Map<String, Map<String, Long>> data = new HashMap<>(BEST_SPLITS.computeIfAbsent(HashMap::new));
		Map<String, Long> floorData = new HashMap<>(data.getOrDefault(floor, new HashMap<>()));
		long currentBest = floorData.getOrDefault(split.name, 0L);
		if (currentBest == 0L || completionTime < currentBest) {
			floorData.put(split.name, completionTime);
			data.put(floor, floorData);
			BEST_SPLITS.put(data);
			BEST_SPLITS.save();
		}
	}

	private static String formatTime(long millis) {
		long totalSeconds = millis / 1000;
		long minutes = totalSeconds / 60;
		long seconds = totalSeconds % 60;
		long hundredths = (millis % 1000) / 10;
		return String.format("%02d:%02d.%02d", minutes, seconds, hundredths);
	}

	private static class Split {
		final String name;
		final Pattern trigger;
		/**
		 * Best total time when this split was reached.
		 */
		long bestTime;
		long completedTime;
		boolean completed;

		Split(String name, Pattern trigger) {
			this(name, trigger, 0);
		}

		Split(String name, Pattern trigger, long bestTime) {
			this.name = name;
			this.trigger = trigger;
			this.bestTime = bestTime;
		}

		void complete(long time) {
			this.completed = true;
			this.completedTime = time;
		}

		void reset() {
			this.completed = false;
			this.completedTime = 0L;
		}
	}
}
