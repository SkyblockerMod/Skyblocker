package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.DungeonEvents;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.skyblock.tabhud.widget.TableWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Component;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.CodecUtils;
import de.hysky.skyblocker.utils.data.ProfiledData;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
	private static final Pattern GUARDIANS = Pattern.compile("^\\[BOSS] The Professor: Oh\\? You found my Guardians' one weakness\\?$");
	private static final Pattern PROFESSOR = Pattern.compile("^\\[BOSS] The Professor: I see. You have forced me to use my ultimate technique\\.$");
	private static final Pattern TERRACOTTAS = Pattern.compile("^\\[BOSS] Sadan: ENOUGH!$");
	private static final Pattern SADAN_GIANTS = Pattern.compile("^\\[BOSS] Sadan: You did it. I understand now, you have earned my respect\\.$");

	/**
	 * Colors used for split names in rainbow order.
	 */
	private static final int[] SPLIT_COLORS = {
			0xFD5858,
			0xFDAB58,
			0xFDFD58,
			0xABFD58,
			0x58FD58,
			0x58FDAB,
			0x58ABFD,
			0x5858FD,
			0xAB58FD,
			0xFD58FD
	};

	private static final Map<String, List<Split>> FLOOR_SPLITS = new HashMap<>();

	static {
		List<Split> floor1Splits = List.of(
				new Split("skyblocker.dungeons.splits.bloodOpen", BLOOD_OPEN),
				new Split("skyblocker.dungeons.splits.bloodClear", BLOOD_CLEAR),
				new Split("skyblocker.dungeons.splits.portalEntry", F1_ENTRY),
				new Split("skyblocker.dungeons.splits.bonzoSike", BONZO_SIKE),
				new Split("skyblocker.dungeons.splits.bonzo", DUNGEON_END)
		);
		FLOOR_SPLITS.put("F1", floor1Splits);
		FLOOR_SPLITS.put("M1", floor1Splits);


		List<Split> floor2Splits = List.of(
				new Split("skyblocker.dungeons.splits.bloodOpen", BLOOD_OPEN),
				new Split("skyblocker.dungeons.splits.bloodClear", BLOOD_CLEAR),
				new Split("skyblocker.dungeons.splits.portalEntry", F2_ENTRY),
				new Split("skyblocker.dungeons.splits.scarfMinions", SCARF_MINIONS),
				new Split("skyblocker.dungeons.splits.scarf", DUNGEON_END)
		);
		FLOOR_SPLITS.put("F2", floor2Splits);
		FLOOR_SPLITS.put("M2", floor2Splits);


		List<Split> floor3Splits = List.of(
				new Split("skyblocker.dungeons.splits.bloodOpen", BLOOD_OPEN),
				new Split("skyblocker.dungeons.splits.bloodClear", BLOOD_CLEAR),
				new Split("skyblocker.dungeons.splits.portalEntry", F3_ENTRY),
				new Split("skyblocker.dungeons.splits.guardians", GUARDIANS),
				new Split("skyblocker.dungeons.splits.professor", PROFESSOR),
				new Split("skyblocker.dungeons.splits.transformedProfessor", DUNGEON_END)
		);
		FLOOR_SPLITS.put("F3", floor3Splits);
		FLOOR_SPLITS.put("M3", floor3Splits);


		List<Split> floor4Splits = List.of(
				new Split("skyblocker.dungeons.splits.bloodOpen", BLOOD_OPEN),
				new Split("skyblocker.dungeons.splits.bloodClear", BLOOD_CLEAR),
				new Split("skyblocker.dungeons.splits.portalEntry", F4_ENTRY),
				new Split("skyblocker.dungeons.splits.thorn", DUNGEON_END)
		);
		FLOOR_SPLITS.put("F4", floor4Splits);
		FLOOR_SPLITS.put("M4", floor4Splits);


		List<Split> floor5Splits = List.of(
				new Split("skyblocker.dungeons.splits.bloodOpen", BLOOD_OPEN),
				new Split("skyblocker.dungeons.splits.bloodClear", BLOOD_CLEAR),
				new Split("skyblocker.dungeons.splits.portalEntry", F5_ENTRY),
				new Split("skyblocker.dungeons.splits.livid", DUNGEON_END)
		);
		FLOOR_SPLITS.put("F5", floor5Splits);
		FLOOR_SPLITS.put("M5", floor5Splits);

		List<Split> floor6Splits = List.of(
				new Split("skyblocker.dungeons.splits.bloodOpen", BLOOD_OPEN),
				new Split("skyblocker.dungeons.splits.bloodClear", BLOOD_CLEAR),
				new Split("skyblocker.dungeons.splits.portalEntry", F6_ENTRY),
				new Split("skyblocker.dungeons.splits.terracottas", TERRACOTTAS),
				new Split("skyblocker.dungeons.splits.sadanGiants", SADAN_GIANTS),
				new Split("skyblocker.dungeons.splits.sadan", DUNGEON_END)
		);
		FLOOR_SPLITS.put("F6", floor6Splits);
		FLOOR_SPLITS.put("M6", floor6Splits);

		FLOOR_SPLITS.put("F7", List.of(
				new Split("skyblocker.dungeons.splits.bloodOpen", BLOOD_OPEN),
				new Split("skyblocker.dungeons.splits.bloodClear", BLOOD_CLEAR),
				new Split("skyblocker.dungeons.splits.portalEntry", F7_ENTRY),
				new Split("skyblocker.dungeons.splits.maxor", F7_MAXOR),
				new Split("skyblocker.dungeons.splits.storm", F7_STORM),
				new Split("skyblocker.dungeons.splits.terminals", F7_TERMINALS),
				new Split("skyblocker.dungeons.splits.goldor", F7_GOLDOR),
				new Split("skyblocker.dungeons.splits.necron", DUNGEON_END)
		));
		FLOOR_SPLITS.put("M7", List.of(
				new Split("skyblocker.dungeons.splits.bloodOpen", BLOOD_OPEN),
				new Split("skyblocker.dungeons.splits.bloodClear", BLOOD_CLEAR),
				new Split("skyblocker.dungeons.splits.portalEntry", F7_ENTRY),
				new Split("skyblocker.dungeons.splits.maxor", F7_MAXOR),
				new Split("skyblocker.dungeons.splits.storm", F7_STORM),
				new Split("skyblocker.dungeons.splits.terminals", F7_TERMINALS),
				new Split("skyblocker.dungeons.splits.goldor", F7_GOLDOR),
				new Split("skyblocker.dungeons.splits.necron", F7_NECRON),
				new Split("skyblocker.dungeons.splits.witherKing", DUNGEON_END)
		));
	}

	private static final Path BEST_FILE = SkyblockerMod.CONFIG_DIR.resolve("dungeon_split_bests.json");
	private static final Codec<Object2ObjectMap<String, Object2LongMap<String>>> BEST_CODEC =
			CodecUtils.object2ObjectMapCodec(Codec.STRING, CodecUtils.object2LongMapCodec(Codec.STRING));
	private static final ProfiledData<Object2ObjectMap<String, Object2LongMap<String>>> BEST_SPLITS = new ProfiledData<>(BEST_FILE, BEST_CODEC);

	private static final Set<Location> AVAILABLE_LOCATIONS = Set.of(Location.DUNGEON);

	private static DungeonSplitsWidget instance;

	private final List<Split> splits = new ArrayList<>();
	private long startTime = 0L;
	private long elapsedTime = 0L;
	private boolean running = false;
	private ChatFormatting timerColor = ChatFormatting.YELLOW;
	private String floor = "?";
	/**
	 * The floor that the splits list was last loaded for.
	 */
	private String loadedFloor = null;

	public DungeonSplitsWidget() {
		super(net.minecraft.network.chat.Component.literal("Splits").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
				ChatFormatting.GOLD.getColor(), "Dungeon Splits", 3, 0, false);
		instance = this;

		BEST_SPLITS.init();

		DungeonEvents.DUNGEON_LOADED.register(this::onDungeonLoaded);
		ClientReceiveMessageEvents.ALLOW_GAME.register(this::onChatMessage);
		SkyblockEvents.LOCATION_CHANGE.register(this::onLocationChange);
	}

	public static DungeonSplitsWidget getInstance() {
		return instance;
	}

	private void onDungeonLoaded() {
		running = false;
		elapsedTime = 0L;
		startTime = 0L;
		timerColor = ChatFormatting.YELLOW;
		loadedFloor = null; // force reloading splits once Mort has been located
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

	@SuppressWarnings("SameReturnValue")
	private boolean onChatMessage(net.minecraft.network.chat.Component text, boolean overlay) {
		if (!Utils.isInDungeons() || overlay) return true;
		String stripped = ChatFormatting.stripFormatting(text.getString());

		if (!running && DUNGEON_START.matcher(stripped).matches()) {
			startTime = System.currentTimeMillis();
			elapsedTime = 0L;
			running = true;
			timerColor = ChatFormatting.YELLOW;
			for (Split split : splits) {
				split.reset();
			}
			return true;
		}

		if (!running) return true;

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
				return true;
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
		return true;
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
			Object2ObjectMap<String, Object2LongMap<String>> data = BEST_SPLITS.computeIfAbsent(Object2ObjectOpenHashMap::new);
			Object2LongMap<String> floorData = data.get(floor);
			for (Split s : group) {
				long best = floorData != null ? floorData.getOrDefault(s.key, 0L) : 0L;
				splits.add(new Split(s.key, s.trigger, best));
			}
		}
	}


	@Override
	public boolean shouldUpdateBeforeRendering() {
		return true;
	}

	@Override
	public Set<Location> availableLocations() {
		return AVAILABLE_LOCATIONS;
	}

	@Override
	public void setEnabledIn(Location location, boolean enabled) {
		if (location != Location.DUNGEON) return;
		SkyblockerConfigManager.get().dungeons.dungeonSplits = enabled;
	}

	@Override
	public boolean isEnabledIn(Location location) {
		return location == Location.DUNGEON && SkyblockerConfigManager.get().dungeons.dungeonSplits;
	}

	@Override
	public void updateContent() {
		if (!(Minecraft.getInstance().screen instanceof WidgetsConfigurationScreen)) {
			updateFloor();
			loadFloorSplits();
		}

		addComponent(new PlainTextComponent(net.minecraft.network.chat.Component.literal("Floor: " + floor)));

		super.updateContent();

		long now = running ? System.currentTimeMillis() - startTime : (startTime == 0L ? 0L : elapsedTime);
		addComponent(new PlainTextComponent(net.minecraft.network.chat.Component.literal(formatTime(now)).withStyle(timerColor)));
	}

	@Override
	protected List<Row> buildRows() {
		long now = running ? System.currentTimeMillis() - startTime : (startTime == 0L ? 0L : elapsedTime);
		long previous = 0L;
		boolean showingCurrent = false;
		List<Row> rows = new ArrayList<>();

		for (int idx = 0; idx < splits.size(); idx++) {
			Split split = splits.get(idx);
			Component name = new PlainTextComponent(net.minecraft.network.chat.Component.translatable(split.key)
					.withColor(SPLIT_COLORS[idx % SPLIT_COLORS.length]));

			String bestStr = formatTime(split.bestTime);
			Component mid;
			Component right;
			int border = 0;

			if (split.completed) {
				long segmentTime = split.completedTime - previous;
				long diff = segmentTime - split.bestTime;
				ChatFormatting fmt = diff <= 0 ? ChatFormatting.GREEN : ChatFormatting.RED;
				String diffStr = String.format("%+.2fs", diff / 1000.0);
				mid = new PlainTextComponent(net.minecraft.network.chat.Component.literal(diffStr).withStyle(fmt));
				right = new PlainTextComponent(net.minecraft.network.chat.Component.literal(formatTime(split.completedTime))
						.withStyle(ChatFormatting.YELLOW));
				previous = split.completedTime;
			} else if (!showingCurrent && (running || startTime != 0L)) {
				long segmentTime = now - previous;
				mid = new PlainTextComponent(net.minecraft.network.chat.Component.literal(formatTime(segmentTime)));
				right = new PlainTextComponent(net.minecraft.network.chat.Component.literal(bestStr));
				showingCurrent = true;
				border = SPLIT_COLORS[idx % SPLIT_COLORS.length];
			} else {
				mid = new PlainTextComponent(net.minecraft.network.chat.Component.literal("--"));
				right = new PlainTextComponent(net.minecraft.network.chat.Component.literal(bestStr));
			}

			rows.add(new Row(List.of(name, mid, right), border));
		}
		return rows;
	}

	private void stopTimer(boolean success) {
		if (running) {
			elapsedTime = System.currentTimeMillis() - startTime;
			running = false;

			if (success) {
				Object2ObjectMap<String, Object2LongMap<String>> data = new Object2ObjectOpenHashMap<>(BEST_SPLITS.computeIfAbsent(Object2ObjectOpenHashMap::new));
				Object2LongMap<String> floorData = new Object2LongOpenHashMap<>(data.getOrDefault(floor, new Object2LongOpenHashMap<>()));
				boolean updated = false;
				for (Split split : splits) {
					if (split.completed) {
						long currentBest = floorData.getOrDefault(split.key, 0L);
						if (currentBest == 0L || split.completedTime < currentBest) {
							floorData.put(split.key, split.completedTime);
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
		timerColor = success ? ChatFormatting.GREEN : ChatFormatting.RED;
	}

	private void updateBest(Split split, long completionTime) {
		Object2ObjectMap<String, Object2LongMap<String>> data = new Object2ObjectOpenHashMap<>(BEST_SPLITS.computeIfAbsent(Object2ObjectOpenHashMap::new));
		Object2LongMap<String> floorData = new Object2LongOpenHashMap<>(data.getOrDefault(floor, new Object2LongOpenHashMap<>()));
		long currentBest = floorData.getOrDefault(split.key, 0L);
		if (currentBest == 0L || completionTime < currentBest) {
			floorData.put(split.key, completionTime);
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
		final String key;
		final Pattern trigger;
		long bestTime;
		long completedTime;
		boolean completed;

		Split(String key, Pattern trigger) {
			this(key, trigger, 0);
		}

		Split(String key, Pattern trigger, long bestTime) {
			this.key = key;
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
