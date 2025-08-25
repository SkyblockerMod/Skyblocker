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
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Trivia extends DungeonPuzzle {
	@SuppressWarnings("unused")
	private static final Trivia INSTANCE = new Trivia();

	//FIXME I think its worth replacing this with something less fragile and is capable of handing multiple lines
	//perhaps manual incremental reading based off the start of a question
	@VisibleForTesting
	public static final Pattern PATTERN = Pattern.compile("^ +(?:([A-Za-z,' ]*\\?)| ([ⓐⓑⓒ]) ([a-zA-Z0-9 ]+))|(\\[STATUE] Oruo the Omniscient: I bestow upon you all the power of a hundred years!)$");
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final BlockPos CHOICE_A = new BlockPos(20, 70, 6);
	private static final BlockPos CHOICE_B = new BlockPos(15, 70, 9);
	private static final BlockPos CHOICE_C = new BlockPos(10, 70, 6);
	private static final float[] ANSWER_COLOR = new float[]{0, 1f, 0};

	private static final Map<String, String[]> answers = Collections.synchronizedMap(new HashMap<>());
	private List<String> solutions = Collections.emptyList();
	private static String currentSolution = "";
	private static BlockPos correctBlockPos = null;

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

		// Reset state when the puzzle ends
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
			if (!solutions.contains(answerChoice)) {
				// Incorrect answer choice
				ClientPlayerEntity player = MinecraftClient.getInstance().player;
				if (player == null) return true;
				Utils.sendMessageToBypassEvents(Text.of("    " + Formatting.GOLD + " " + matcher.group(2) + " " + Formatting.RED + answerChoice));
				return false;
			}
			currentSolution = matcher.group(2);
			updateCorrectBlockPos();
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
				String[] questionAnswers = answers.get(trimmedQuestion);
				if (questionAnswers != null) solutions = Arrays.asList(questionAnswers);
			}
		} catch (Exception e) { //Hopefully the solver doesn't go south
			LOGGER.error("[Skyblocker] Failed to update the Trivia puzzle answers!", e);
		}
	}

	@Override
	public void tick(MinecraftClient client) {
	}

	private static void updateCorrectBlockPos() {
		switch (currentSolution) {
			case "ⓐ":
				correctBlockPos = CHOICE_A;
				break;
			case "ⓑ":
				correctBlockPos = CHOICE_B;
				break;
			case "ⓒ":
				correctBlockPos = CHOICE_C;
				break;
			case null, default:
				correctBlockPos = null;
				break;
		}
	}

	@Override
	public void render(WorldRenderContext context) {
		if (!shouldRun() || correctBlockPos == null) return;

		Room room = DungeonManager.getCurrentRoom();
		RenderHelper.renderFilled(context, room.relativeToActual(correctBlockPos), ANSWER_COLOR, 0.5f, false);
	}

	@Override
	public void reset() {
		currentSolution = "";
		correctBlockPos = null;
	}

	@Init
	public static void init() {
		answers.put("What is the status of The Watcher?", new String[]{"Stalker"});
		answers.put("What is the status of Bonzo?", new String[]{"New Necromancer"});
		answers.put("What is the status of Scarf?", new String[]{"Apprentice Necromancer"});
		answers.put("What is the status of The Professor?", new String[]{"Professor"});
		answers.put("What is the status of Thorn?", new String[]{"Shaman Necromancer"});
		answers.put("What is the status of Livid?", new String[]{"Master Necromancer"});
		answers.put("What is the status of Sadan?", new String[]{"Necromancer Lord"});
		answers.put("What is the status of Maxor?", new String[]{"The Wither Lords"});
		answers.put("What is the status of Goldor?", new String[]{"The Wither Lords"});
		answers.put("What is the status of Storm?", new String[]{"The Wither Lords"});
		answers.put("What is the status of Necron?", new String[]{"The Wither Lords"});
		answers.put("What is the status of Maxor, Storm, Goldor, and Necron?", new String[]{"The Wither Lords"});
		answers.put("Which brother is on the Spider's Den?", new String[]{"Rick"});
		answers.put("What is the name of Rick's brother?", new String[]{"Pat"});
		//Full Question: "What is the name of the vendor in the Hub who sells stained glass?"
		//The solver cannot handle multiple lines right now and just sees "glass?" as the question
		answers.put("glass?", new String[]{"Wool Weaver"});
		answers.put("What is the name of the person that upgrades pets?", new String[]{"Kat"});
		answers.put("What is the name of the lady of the Nether?", new String[]{"Elle"});
		answers.put("Which villager in the Village gives you a Rogue Sword?", new String[]{"Jamie"});
		answers.put("How many unique minions are there?", new String[]{"59 Minions"});
		answers.put("Which of these enemies does not spawn in the Spider's Den?", new String[]{"Zombie Spider", "Cave Spider", "Wither Skeleton", "Dashing Spooder", "Broodfather", "Night Spider"});
		answers.put("Which of these monsters only spawns at night?", new String[]{"Zombie Villager", "Ghast"});
		answers.put("Which of these is not a dragon in The End?", new String[]{"Zoomer Dragon", "Weak Dragon", "Stonk Dragon", "Holy Dragon", "Boomer Dragon", "Booger Dragon", "Older Dragon", "Elder Dragon", "Stable Dragon", "Professor Dragon"});
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

	@NotNull
	private static String[] getFairySoulsSizeString(@Nullable String location) {
		return new String[]{"%d Fairy Souls".formatted(FairySouls.getFairySoulsSize(location))};
	}
}
