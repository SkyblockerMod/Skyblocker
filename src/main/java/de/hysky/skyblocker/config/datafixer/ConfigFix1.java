package de.hysky.skyblocker.config.datafixer;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import de.hysky.skyblocker.utils.datafixer.ItemStackComponentizationFixer;
import java.util.Locale;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;

public class ConfigFix1 extends ConfigDataFix {
	public ConfigFix1(Schema outputSchema, boolean changesType) {
		super(outputSchema, changesType);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return fixTypeEverywhereTyped(
				"ConfigFix1",
				getInputSchema().getType(ConfigDataFixer.CONFIG_TYPE),
				typed -> typed.update(DSL.remainderFinder(), this::fix)
		);
	}

	private <T> Dynamic<T> fix(Dynamic<T> dynamic) {
		return fixMisc(fixQuickNav(fixChat(fixSlayers(fixOtherLocations(fixFarming(fixMining(fixCrimsonIsle(fixDungeons(fixHelpers(fixUIAndVisuals(fixGeneral(fixVersion(dynamic)))))))))))));
	}

	private static <T> Dynamic<T> fixGeneral(Dynamic<T> dynamic) {
		return dynamic.update("general", general -> general.update("itemTooltip", itemTooltip -> itemTooltip.setFieldIfPresent("dungeonQuality", general.get("dungeonQuality").result())).remove("dungeonQuality"));
	}

	private static <T> Dynamic<T> fixUIAndVisuals(Dynamic<T> dynamic) {
		OptionalDynamic<T> general = dynamic.get("general");
		return dynamic.set("uiAndVisuals", dynamic.emptyMap()
				.setFieldIfPresent("compactorDeletorPreview", general.get("compactorDeletorPreview").result())
				.setFieldIfPresent("dontStripSkinAlphaValues", general.get("dontStripSkinAlphaValues").result())
				.setFieldIfPresent("backpackPreviewWithoutShift", general.get("backpackPreviewWithoutShift").result())
				.setFieldIfPresent("hideEmptyTooltips", general.get("hideEmptyTooltips").result())
				.setFieldIfPresent("fancyCraftingTable", general.get("fancyCraftingTable").result())
				.setFieldIfPresent("hideStatusEffectOverlay", general.get("hideStatusEffectOverlay").result())
				.setFieldIfPresent("chestValue", general.get("chestValue").result())
				.setFieldIfPresent("itemCooldown", general.get("itemCooldown").result())
				.setFieldIfPresent("titleContainer", general.get("titleContainer").result())
				.setFieldIfPresent("tabHud", general.get("tabHud").result())
				.setFieldIfPresent("fancyAuctionHouse", general.get("fancyAuctionHouse").result())
				.setFieldIfPresent("bars", general.get("bars").result())
				.setFieldIfPresent("waypoints", general.get("waypoints").result())
				.setFieldIfPresent("teleportOverlay", general.get("teleportOverlay").result())
				.setFieldIfPresent("searchOverlay", general.get("searchOverlay").result())
				.setFieldIfPresent("flameOverlay", general.get("flameOverlay").result())
		).update("general", newGeneral -> newGeneral
				.remove("compactorDeletorPreview")
				.remove("dontStripSkinAlphaValues")
				.remove("backpackPreviewWithoutShift")
				.remove("hideEmptyTooltips")
				.remove("fancyCraftingTable")
				.remove("hideStatusEffectOverlay")
				.remove("chestValue")
				.remove("itemCooldown")
				.remove("titleContainer")
				.remove("tabHud")
				.remove("fancyAuctionHouse")
				.remove("bars")
				.remove("waypoints")
				.remove("teleportOverlay")
				.remove("searchOverlay")
				.remove("flameOverlay")
		);
	}

	private static <T> Dynamic<T> fixHelpers(Dynamic<T> dynamic) {
		OptionalDynamic<T> general = dynamic.get("general");
		return dynamic.set("helpers", dynamic.emptyMap()
				.setFieldIfPresent("enableNewYearCakesHelper", general.get("enableNewYearCakesHelper").result())
				.setFieldIfPresent("mythologicalRitual", general.get("mythologicalRitual").result())
				.setFieldIfPresent("experiments", general.get("experiments").result())
				.setFieldIfPresent("fishing", general.get("fishing").result())
				.setFieldIfPresent("fairySouls", general.get("fairySouls").result())
		).update("general", newGeneral -> newGeneral
				.remove("enableNewYearCakesHelper")
				.remove("mythologicalRitual")
				.remove("experiments")
				.remove("fishing")
				.remove("fairySouls")
		);
	}

	private static <T> Dynamic<T> fixDungeons(Dynamic<T> dynamic) {
		OptionalDynamic<T> general = dynamic.get("general");
		OptionalDynamic<T> dungeons = dynamic.get("locations").get("dungeons");
		return dynamic.set("dungeons", dynamic.emptyMap()
				.setFieldIfPresent("fancyPartyFinder", general.get("betterPartyFinder").result())
				.setFieldIfPresent("croesusHelper", dungeons.get("croesusHelper").result())
				.setFieldIfPresent("playerSecretsTracker", dungeons.get("playerSecretsTracker").result())
				.setFieldIfPresent("starredMobGlow", dungeons.get("starredMobGlow").result())
				.setFieldIfPresent("starredMobBoundingBoxes", dungeons.get("starredMobBoundingBoxes").result())
				.setFieldIfPresent("allowDroppingProtectedItems", dungeons.get("allowDroppingProtectedItems").result())
				.set("dungeonMap", dynamic.emptyMap()
						.setFieldIfPresent("enableMap", dungeons.get("enableMap").result())
						.setFieldIfPresent("mapScaling", dungeons.get("mapScaling").result())
						.setFieldIfPresent("mapX", dungeons.get("mapX").result())
						.setFieldIfPresent("mapY", dungeons.get("mapY").result())
				)
				.set("puzzleSolvers", dynamic.emptyMap()
						.setFieldIfPresent("solveThreeWeirdos", dungeons.get("solveThreeWeirdos").result())
						.setFieldIfPresent("blazeSolver", dungeons.get("blazeSolver").result())
						.setFieldIfPresent("creeperSolver", dungeons.get("creeperSolver").result())
						.setFieldIfPresent("solveTrivia", dungeons.get("solveTrivia").result())
						.setFieldIfPresent("solveTicTacToe", dungeons.get("solveTicTacToe").result())
						.setFieldIfPresent("solveWaterboard", dungeons.get("solveWaterboard").result())
						.setFieldIfPresent("solveBoulder", dungeons.get("solveBoulder").result())
						.setFieldIfPresent("solveIceFill", dungeons.get("solveIceFill").result())
						.setFieldIfPresent("solveSilverfish", dungeons.get("solveSilverfish").result())
				)
				.set("theProfessor", dynamic.emptyMap()
						.setFieldIfPresent("fireFreezeStaffTimer", dungeons.get("fireFreezeStaffTimer").result())
						.setFieldIfPresent("floor3GuardianHealthDisplay", dungeons.get("floor3GuardianHealthDisplay").result())
				)
				.setFieldIfPresent("livid", dungeons.get("lividColor").result())
				.setFieldIfPresent("terminals", dungeons.get("terminals").result())
				.setFieldIfPresent("secretWaypoints", dungeons.get("secretWaypoints").result())
				.setFieldIfPresent("mimicMessage", dungeons.get("mimicMessage").result())
				.setFieldIfPresent("doorHighlight", dungeons.get("doorHighlight").result())
				.setFieldIfPresent("dungeonScore", dungeons.get("dungeonScore").result())
				.setFieldIfPresent("dungeonChestProfit", dungeons.get("dungeonChestProfit").result())
		).update("locations", locations -> locations.remove("dungeons")).update("general", newGeneral -> newGeneral.remove("betterPartyFinder"));
	}

	private static <T> Dynamic<T> fixCrimsonIsle(Dynamic<T> dynamic) {
		return dynamic.setFieldIfPresent("crimsonIsle", dynamic.get("locations").get("crimsonIsle").result()).update("locations", locations -> locations.remove("crimsonIsle"));
	}

	private static <T> Dynamic<T> fixMining(Dynamic<T> dynamic) {
		OptionalDynamic<T> dwarvenMines = dynamic.get("locations").get("dwarvenMines");
		return dynamic.set("mining", dynamic.emptyMap()
				.setFieldIfPresent("enableDrillFuel", dwarvenMines.get("enableDrillFuel").result())
				.set("dwarvenMines", dynamic.emptyMap()
						.setFieldIfPresent("solveFetchur", dwarvenMines.get("solveFetchur").result())
						.setFieldIfPresent("solvePuzzler", dwarvenMines.get("solvePuzzler").result())
				)
				.set("dwarvenHud", dwarvenMines.get("dwarvenHud").result().orElseThrow()
						.renameField("x", "commissionsX")
						.renameField("y", "commissionsY")
				)
				.setFieldIfPresent("crystalsHud", dwarvenMines.get("crystalsHud").result())
				.setFieldIfPresent("crystalsWaypoints", dwarvenMines.get("crystalsWaypoints").result())
				.set("crystalHollows", dynamic.emptyMap()
						.setFieldIfPresent("metalDetectorHelper", dwarvenMines.get("metalDetectorHelper").result())
				)
		).update("locations", locations -> locations.remove("dwarvenMines"));
	}

	private static <T> Dynamic<T> fixFarming(Dynamic<T> dynamic) {
		return dynamic.set("farming", dynamic.emptyMap()
				.setFieldIfPresent("garden", dynamic.get("locations").get("garden").result())
		).update("locations", locations -> locations.remove("garden"));
	}

	private static <T> Dynamic<T> fixOtherLocations(Dynamic<T> dynamic) {
		return dynamic.renameField("locations", "otherLocations");
	}

	private static <T> Dynamic<T> fixSlayers(Dynamic<T> dynamic) {
		return dynamic.renameField("slayer", "slayers");
	}

	private static <T> Dynamic<T> fixChat(Dynamic<T> dynamic) {
		return dynamic.renameField("messages", "chat");
	}

	private static <T> Dynamic<T> fixQuickNav(Dynamic<T> dynamic) {
		return dynamic.update("quickNav", quickNav -> quickNav.updateMapValues(button ->
				button.getFirst().asString().getOrThrow().startsWith("button") ? button.mapSecond(ConfigFix1::fixQuickNavButton) : button
		));
	}

	private static <T> Dynamic<T> fixQuickNavButton(Dynamic<T> button) {
		return button.update("item", item -> item
				.renameField("itemName", "id")
				.renameAndFixField("nbt", "components", nbt -> fixNbt(item.get("itemName"), nbt))
		);
	}

	private static Dynamic<?> fixNbt(OptionalDynamic<?> id, Dynamic<?> nbt) {
		try {
			String itemNbt = "{id:\"minecraft:" + id.asString().getOrThrow().toLowerCase(Locale.ROOT) + "\",Count:1";
			String extraNbt = nbt.asString().getOrThrow();
			if (extraNbt.length() > 2) itemNbt += "," + extraNbt;
			itemNbt += "}";

			ItemStack fixed = ItemStackComponentizationFixer.fixUpItem(TagParser.parseCompoundFully(itemNbt));

			return nbt.createString(ItemStackComponentizationFixer.componentsAsString(fixed));
		} catch (Exception e) {
			ConfigDataFixer.LOGGER.error(LogUtils.FATAL_MARKER, "[Skyblocker Config Data Fixer] Failed to convert nbt to components!", e);
		}

		return nbt.createString("[]");
	}

	private static <T> Dynamic<T> fixMisc(Dynamic<T> dynamic) {
		return dynamic.set("misc", dynamic.emptyMap()
				.setFieldIfPresent("richPresence", dynamic.get("richPresence").result())
		).remove("richPresence");
	}
}
