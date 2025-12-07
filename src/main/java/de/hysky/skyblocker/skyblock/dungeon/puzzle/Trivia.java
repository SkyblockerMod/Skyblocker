package de.hysky.skyblocker.skyblock.dungeon.puzzle;

import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.Room;
import de.hysky.skyblocker.skyblock.waypoint.FairySouls;
import de.hysky.skyblocker.utils.SkyblockTime;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Trivia extends DungeonPuzzle {
	@SuppressWarnings("unused")
	private static final Trivia INSTANCE = new Trivia();

	//FIXME I think its worth replacing this with something less fragile and is capable of handing multiple lines
	//perhaps manual incremental reading based off the start of a question
	@VisibleForTesting
	public static final Pattern PATTERN = Pattern.compile("^ +(?:([A-Za-z,' ]*\\?)| ([ⓐⓑⓒ]) ([a-zA-Z0-9 ]+))|(\\[STATUE] Oruo the Omniscient: (\\w+ answered Question #\\d correctly!|I bestow upon you all the power of a hundred years!|Yikes))$");
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final BlockPos CHOICE_A = new BlockPos(20, 70, 6);
	private static final BlockPos CHOICE_B = new BlockPos(15, 70, 9);
	private static final BlockPos CHOICE_C = new BlockPos(10, 70, 6);
	private static final float[] ANSWER_COLOR = new float[]{0, 1f, 0};
	private static final Direction[] DIRECTIONS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
	private static final ArrayList<Box> BOXES_TO_HIGHLIGHT = new ArrayList<>();

	private static final Map<String, List<String>> answers = new Object2ObjectOpenHashMap<>();
	private List<String> solutions = Collections.emptyList();
	private static String currentSolution = "";

	public Trivia() {
		super("trivia", "trivia-room");
		ClientReceiveMessageEvents.ALLOW_GAME.register(this::onMessage);
	}


	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean shouldRun() {
		return Utils.isInDungeons() && SkyblockerConfigManager.get().dungeons.puzzleSolvers.solveTrivia;
	}

	public boolean onMessage(Text message, boolean overlay) {
		if (!shouldRun() || overlay) return true;

		Matcher matcher = PATTERN.matcher(Formatting.strip(message.getString()));
		if (!matcher.matches()) return true;

		// Reset state when a question is answered and when the puzzle is failed or completed.
		if (matcher.group(4) != null) {
			reset();
			return true;
		}

		String answerChoice = matcher.group(3);
		if (answerChoice == null) {
			// Message is a question
			updateSolutions(matcher.group(0));
			reset();
		} else {
			if (solutions.isEmpty()) return true;
			if (!solutions.contains(answerChoice)) {
				// Incorrect answer choice
				ClientPlayerEntity player = MinecraftClient.getInstance().player;
				if (player == null) return true;
				Utils.sendMessageToBypassEvents(Text.of("    " + Formatting.GOLD + " " + matcher.group(2) + " " + Formatting.RED + answerChoice));
				return false;
			}
			currentSolution = matcher.group(2);
		}

		return true;
	}

	private void updateSolutions(String question) {
		try {
			String trimmedQuestion = question.trim();
			if (trimmedQuestion.equals("What SkyBlock year is it?")) {
				int year = SkyblockTime.skyblockYear.get();
				solutions = Collections.singletonList("Year " + year);
			} else {
				solutions = answers.getOrDefault(trimmedQuestion, Collections.emptyList());
			}
		} catch (Exception e) { //Hopefully the solver doesn't go south
			LOGGER.error("[Skyblocker] Failed to update the Trivia puzzle answers!", e);
		}
	}

	@Override
	public void tick(MinecraftClient client) {
		if (!shouldRun() || currentSolution.isEmpty() || !BOXES_TO_HIGHLIGHT.isEmpty() || client.world == null) return;
		BlockPos correctBlockPos = updateCorrectBlockPos();
		if (correctBlockPos == null) return;

		Room room = DungeonManager.getCurrentRoom();
		for (Direction direction : DIRECTIONS) {
			//noinspection DataFlowIssue - the room must not be null and must be matched (would not tick otherwise)
			Box buttonBox = RenderHelper.getBlockBoundingBox(client.world, room.relativeToActual(correctBlockPos).offset(direction));
			if (buttonBox != null) BOXES_TO_HIGHLIGHT.add(buttonBox);
		}
	}

	@Nullable
	private static BlockPos updateCorrectBlockPos() {
		return switch (currentSolution) {
			case "ⓐ" -> CHOICE_A;
			case "ⓑ" -> CHOICE_B;
			case "ⓒ" -> CHOICE_C;
			default -> null;
		};
	}

	@Override
	public void extractRendering(PrimitiveCollector collector) {
		if (!shouldRun() || BOXES_TO_HIGHLIGHT.isEmpty()) return;
		for (Box box : BOXES_TO_HIGHLIGHT) {
			collector.submitFilledBox(box, ANSWER_COLOR, 0.5f, false);
			collector.submitOutlinedBox(box, ANSWER_COLOR, 5f, false);
		}
	}

	@Override
	public void reset() {
		currentSolution = "";
		BOXES_TO_HIGHLIGHT.clear();
	}

	@Init(priority = 100) // Load after FairySouls
	public static void init() {
		answers.put("What is the status of The Watcher?", List.of("Stalker"));
		answers.put("What is the status of Bonzo?", List.of("New Necromancer"));
		answers.put("What is the status of Scarf?", List.of("Apprentice Necromancer"));
		answers.put("What is the status of The Professor?", List.of("Professor"));
		answers.put("What is the status of Thorn?", List.of("Shaman Necromancer"));
		answers.put("What is the status of Livid?", List.of("Master Necromancer"));
		answers.put("What is the status of Sadan?", List.of("Necromancer Lord"));
		answers.put("What is the status of Maxor?", List.of("The Wither Lords"));
		answers.put("What is the status of Goldor?", List.of("The Wither Lords"));
		answers.put("What is the status of Storm?", List.of("The Wither Lords"));
		answers.put("What is the status of Necron?", List.of("The Wither Lords"));
		answers.put("What is the status of Maxor, Storm, Goldor, and Necron?", List.of("The Wither Lords"));
		answers.put("Which brother is on the Spider's Den?", List.of("Rick"));
		answers.put("What is the name of Rick's brother?", List.of("Pat"));
		//Full Question: "What is the name of the vendor in the Hub who sells stained glass?"
		//The solver cannot handle multiple lines right now and just sees "glass?" as the question
		answers.put("glass?", List.of("Wool Weaver"));
		answers.put("What is the name of the person that upgrades pets?", List.of("Kat"));
		answers.put("What is the name of the lady of the Nether?", List.of("Elle"));
		answers.put("Which villager in the Village gives you a Rogue Sword?", List.of("Jamie"));
		answers.put("How many unique minions are there?", List.of("59 Minions"));
		answers.put("Which of these enemies does not spawn in the Spider's Den?", List.of("Zombie Spider", "Cave Spider", "Wither Skeleton", "Dashing Spooder", "Broodfather", "Night Spider"));
		answers.put("Which of these monsters only spawns at night?", List.of("Zombie Villager", "Ghast"));
		answers.put("Which of these is not a dragon in The End?", List.of("Zoomer Dragon", "Weak Dragon", "Stonk Dragon", "Holy Dragon", "Boomer Dragon", "Booger Dragon", "Older Dragon", "Elder Dragon", "Stable Dragon", "Professor Dragon"));
		FairySouls.runAsyncAfterFairySoulsLoad(() -> {
			answers.put("How many total Fairy Souls are there?", getFairySoulsSizeString(null));
			answers.put("How many Fairy Souls are there in Spider's Den?", getFairySoulsSizeString("combat_1"));
			answers.put("How many Fairy Souls are there in The End?", getFairySoulsSizeString("combat_3"));
			answers.put("How many Fairy Souls are there in The Farming Islands?", getFairySoulsSizeString("farming_1"));
			answers.put("How many Fairy Souls are there in Crimson Isle?", getFairySoulsSizeString("crimson_isle"));
			answers.put("How many Fairy Souls are there in The Park?", getFairySoulsSizeString("foraging_1"));
			answers.put("How many Fairy Souls are there in Jerry's Workshop?", getFairySoulsSizeString("winter"));
			answers.put("How many Fairy Souls are there in Hub?", getFairySoulsSizeString("hub"));
			answers.put("How many Fairy Souls are there in The Hub?", getFairySoulsSizeString("hub"));
			answers.put("How many Fairy Souls are there in Deep Caverns?", getFairySoulsSizeString("mining_2"));
			answers.put("How many Fairy Souls are there in Gold Mine?", getFairySoulsSizeString("mining_1"));
			answers.put("How many Fairy Souls are there in Dungeon Hub?", getFairySoulsSizeString("dungeon_hub"));
		});
	}

	private static List<String> getFairySoulsSizeString(@Nullable String location) {
		return List.of("%d Fairy Souls".formatted(FairySouls.getFairySoulsSize(location)));
	}
}
