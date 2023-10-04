/*
 * Copyright (C) 2022 NotEnoughUpdates contributors
 * Copyright (C) 2023 Skyblocker contributors
 *
 * This file is part of Skyblocker.
 *
 * The majority of this code is taken from NotEnoughUpdates,
 * slightly adjusted to port to Fabric and 1.20.x.
 *
 * Skyblocker is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * Skyblocker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Skyblocker. If not, see <https://www.gnu.org/licenses/>.
 */

package me.xmrvizzy.skyblocker.skyblock;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.PlayerLocator;
import me.xmrvizzy.skyblocker.utils.Line;
import me.xmrvizzy.skyblocker.utils.Utils;
import me.xmrvizzy.skyblocker.utils.Vec3Comparable;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;

public class CrystalWishingCompassSolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(CrystalWishingCompassSolver.class);

    enum SolverState {
        NOT_STARTED,
        PROCESSING_FIRST_USE,
        NEED_SECOND_COMPASS,
        PROCESSING_SECOND_USE,
        SOLVED,
        FAILED_EXCEPTION,
        FAILED_TIMEOUT_NO_REPEATING,
        FAILED_TIMEOUT_NO_PARTICLES,
        FAILED_INTERSECTION_CALCULATION,
        FAILED_INVALID_SOLUTION,
    }

    enum CompassTarget {
        GOBLIN_QUEEN("Goblin Queen", Formatting.YELLOW, true),
        GOBLIN_KING("King Yolkar", Formatting.GOLD),
        BAL("Bal", Formatting.RED),
        JUNGLE_TEMPLE("Jungle Temple", Formatting.GREEN, true),
        ODAWA("Odawa", Formatting.GREEN),
        PRECURSOR_CITY("Precursor City", Formatting.WHITE, true),
        MINES_OF_DIVAN("Mines of Divan", Formatting.BLUE, true),
        CRYSTAL_NUCLEUS("Crystal Nucleus", Formatting.DARK_PURPLE, true);

        private String name;
        private Formatting color;
        private boolean needsThe;

        CompassTarget(String name, Formatting color, boolean needsThe) {
            this.name = name;
            this.color = color;
            this.needsThe = needsThe;
        }

        CompassTarget(String name, Formatting color) {
            this(name, color, false);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (needsThe) {
                sb.append(Formatting.AQUA + "the ");
            }
            sb.append(color);
            sb.append(name);
            return sb.toString();
        }
    }

    enum Crystal {
        AMBER,
        AMETHYST,
        JADE,
        SAPPHIRE,
        TOPAZ,
    }

    enum HollowsZone {
        CRYSTAL_NUCLEUS,
        JUNGLE,
        MITHRIL_DEPOSITS,
        GOBLIN_HOLDOUT,
        PRECURSOR_REMNANTS,
        MAGMA_FIELDS,
    }

    private static final CrystalWishingCompassSolver solver = new CrystalWishingCompassSolver();

    public static CrystalWishingCompassSolver getInstance() {
        return solver;
    }

    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final ArrayDeque<ParticleData> seenParticles = new ArrayDeque<>();

    // There is a small set of breakable blocks above the nucleus at Y > 181. While this zone is reported
    // as the Crystal Nucleus by Hypixel, for wishing compass purposes it is in the appropriate quadrant.
    private static final Box NUCLEUS_BB = new Box(462, 63, 461, 564, 181, 565);
    // Bounding box around all breakable blocks in the crystal hollows, appears as bedrock in-game
    private static final Box HOLLOWS_BB = new Box(201, 30, 201, 824, 189, 824);

    // Zone bounding boxes
    private static final Box PRECURSOR_REMNANTS_BB = new Box(512, 63, 512, 824, 189, 824);
    private static final Box MITHRIL_DEPOSITS_BB = new Box(512, 63, 201, 824, 189, 513);
    private static final Box GOBLIN_HOLDOUT_BB = new Box(201, 63, 512, 513, 189, 824);
    private static final Box JUNGLE_BB = new Box(201, 63, 201, 513, 189, 513);
    private static final Box MAGMA_FIELDS_BB = new Box(201, 30, 201, 824, 64, 824);

    // Structure bounding boxes (size + 2 in each dimension to make it an actual bounding box)
    private static final Box PRECURSOR_CITY_BB = new Box(0, 0, 0, 107, 122, 107);
    private static final Box GOBLIN_KING_BB = new Box(0, 0, 0, 59, 53, 56);
    private static final Box GOBLIN_QUEEN_BB = new Box(0, 0, 0, 108, 114, 108);
    private static final Box JUNGLE_TEMPLE_BB = new Box(0, 0, 0, 108, 120, 108);
    private static final Box ODAWA_BB = new Box(0, 0, 0, 53, 46, 54);
    private static final Box MINES_OF_DIVAN_BB = new Box(0, 0, 0, 108, 125, 108);
    private static final Box KHAZAD_DUM_BB = new Box(0, 0, 0, 110, 46, 108);

    private static final Vec3Comparable JUNGLE_DOOR_OFFSET_FROM_CRYSTAL = new Vec3Comparable(-57, 36, -21);

    private static final double MAX_DISTANCE_BETWEEN_PARTICLES = 0.6;
    private static final double MAX_DISTANCE_FROM_USE_TO_FIRST_PARTICLE = 9.0;

    // 64.0 is an arbitrary value but seems to work well
    private static final double MINIMUM_DISTANCE_SQ_BETWEEN_COMPASSES = 64.0;

    // All particles typically arrive in < 3500, so 5000 should be enough buffer
    public static final long ALL_PARTICLES_MAX_MILLIS = 5000L;

    private SolverState solverState;
    private Compass firstCompass;
    private Compass secondCompass;
    private Line solutionIntersectionLine;
    private EnumSet<CompassTarget> possibleTargets;
    private Vec3Comparable solution;
    private Vec3Comparable originalSolution;
    private EnumSet<CompassTarget> solutionPossibleTargets;

    public SolverState getSolverState() {
        return solverState;
    }

    public Vec3i getSolutionCoords() {
        return new Vec3i((int)solution.getX(), (int)solution.getY(), (int)solution.getZ());
    }

    public EnumSet<CompassTarget> getPossibleTargets() {
        return possibleTargets;
    }

    public static HollowsZone getZoneForCoords(BlockPos blockPos) {
        return getZoneForCoords(new Vec3Comparable(blockPos));
    }

    public static HollowsZone getZoneForCoords(Vec3Comparable coords) {
        if (NUCLEUS_BB.contains(coords)) return HollowsZone.CRYSTAL_NUCLEUS;
        if (JUNGLE_BB.contains(coords)) return HollowsZone.JUNGLE;
        if (MITHRIL_DEPOSITS_BB.contains(coords)) return HollowsZone.MITHRIL_DEPOSITS;
        if (GOBLIN_HOLDOUT_BB.contains(coords)) return HollowsZone.GOBLIN_HOLDOUT;
        if (PRECURSOR_REMNANTS_BB.contains(coords)) return HollowsZone.PRECURSOR_REMNANTS;
        if (MAGMA_FIELDS_BB.contains(coords)) return HollowsZone.MAGMA_FIELDS;
        throw new IllegalArgumentException("Coordinates do not fall in known zone: " + coords.toString());
    }

    private static void resetForNewTarget() {
        LOGGER.debug("[Skyblocker] Resetting for new target");
        solver.solverState = SolverState.NOT_STARTED;
        solver.firstCompass = null;
        solver.secondCompass = null;
        solver.solutionIntersectionLine = null;
        solver.possibleTargets = null;
        solver.solution = null;
        solver.originalSolution = null;
        solver.solutionPossibleTargets = null;
    }

    public static void init() {
        UseItemCallback.EVENT.register((player, world, hand) -> onUseItem(player));
        UseBlockCallback.EVENT.register((player, world, hand, blockState) -> onUseBlock(player));

        resetForNewTarget();
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> resetForNewTarget());
    }

    public static TypedActionResult<ItemStack> onUseItem(PlayerEntity player) {
        ItemStack heldItem = player.getMainHandStack();

        if (PlayerLocator.getPlayerLocation() != PlayerLocator.Location.CRYSTAL_HOLLOWS
                || player != client.player) {
            return new TypedActionResult<ItemStack>(ActionResult.PASS, heldItem);
        }

        if (heldItem == null || heldItem.getItem() != Items.PLAYER_HEAD) {
            return new TypedActionResult<ItemStack>(ActionResult.PASS, heldItem);
        }

        NbtCompound nbt = heldItem.getNbt();
        if (nbt == null || !nbt.contains("ExtraAttributes")) {
            return new TypedActionResult<ItemStack>(ActionResult.PASS, heldItem);
        }

        String heldInternalName = nbt.getCompound("ExtraAttributes").getString("id");
        if (heldInternalName == null || !heldInternalName.equals("WISHING_COMPASS")) {
            return new TypedActionResult<ItemStack>(ActionResult.PASS, heldItem);
        }

        BlockPos playerPos = client.player.getBlockPos().toImmutable();

        try {
            HandleCompassResult result = solver.handleCompassUse(playerPos);
            switch (result) {
                case SUCCESS:
                    return new TypedActionResult<ItemStack>(ActionResult.PASS, heldItem);
                case STILL_PROCESSING_PRIOR_USE:
                    client.player.sendMessage(Text.literal(Formatting.YELLOW +
                        "[Skyblocker] Wait a little longer before using the wishing compass again."), false);
                    return new TypedActionResult<ItemStack>(ActionResult.FAIL, heldItem);
                case LOCATION_TOO_CLOSE:
                    client.player.sendMessage(Text.literal(Formatting.YELLOW +
                        "[Skyblocker] Move a little further before using the wishing compass again."), false);
                    return new TypedActionResult<ItemStack>(ActionResult.FAIL, heldItem);
                case POSSIBLE_TARGETS_CHANGED:
                    client.player.sendMessage(Text.literal(Formatting.YELLOW +
                        "[Skyblocker] Possible wishing compass targets have changed. Solver has been reset."), false);
                    return new TypedActionResult<ItemStack>(ActionResult.FAIL, heldItem);
                case NO_PARTICLES_FOR_PREVIOUS_COMPASS:
                    client.player.sendMessage(Text.literal(Formatting.YELLOW +
                        "[Skyblocker] No particles detected for prior compass use. Need another position to solve."), false);
                    return new TypedActionResult<ItemStack>(ActionResult.PASS, heldItem);
                case PLAYER_IN_NUCLEUS:
                    client.player.sendMessage(Text.literal(Formatting.YELLOW +
                        "[Skyblocker] Wishing compass must be used outside the nucleus for accurate results."), false);
                    return new TypedActionResult<ItemStack>(ActionResult.FAIL, heldItem);
                default:
                    throw new IllegalStateException("Unexpected wishing compass solver state: \n" + solver.getDiagnosticMessage());
            }
        } catch (Exception e) {
            client.player.sendMessage(Text.literal(Formatting.RED +
                "[Skyblocker] Error processing wishing compass action - see log for details"), false);
            e.printStackTrace();
            solver.solverState = SolverState.FAILED_EXCEPTION;
            return new TypedActionResult<ItemStack>(ActionResult.FAIL, heldItem);
        }
    }

    public static ActionResult onUseBlock(PlayerEntity player) {
        return CrystalWishingCompassSolver.onUseItem(player).getResult();
    }

    public HandleCompassResult handleCompassUse(BlockPos playerPos) {
        long lastCompassUsedMillis = 0;
        switch (solverState) {
            case PROCESSING_SECOND_USE:
                if (secondCompass != null) {
                    lastCompassUsedMillis = secondCompass.whenUsedMillis;
                }
            case PROCESSING_FIRST_USE:
                if (lastCompassUsedMillis == 0 && firstCompass != null) {
                    lastCompassUsedMillis = firstCompass.whenUsedMillis;
                }
                if (lastCompassUsedMillis != 0
                        && (System.currentTimeMillis() > lastCompassUsedMillis + ALL_PARTICLES_MAX_MILLIS)) {
                    return HandleCompassResult.NO_PARTICLES_FOR_PREVIOUS_COMPASS;
                }

                return HandleCompassResult.STILL_PROCESSING_PRIOR_USE;
            case SOLVED:
            case FAILED_EXCEPTION:
            case FAILED_TIMEOUT_NO_REPEATING:
            case FAILED_TIMEOUT_NO_PARTICLES:
            case FAILED_INTERSECTION_CALCULATION:
            case FAILED_INVALID_SOLUTION:
                resetForNewTarget();
                // falls through, NOT_STARTED is the state when resetForNewTarget returns
            case NOT_STARTED:
                if (NUCLEUS_BB.contains(new Vec3Comparable(playerPos.getX(), playerPos.getY(), playerPos.getZ()))) {
                    return HandleCompassResult.PLAYER_IN_NUCLEUS;
                }

                firstCompass = new Compass(playerPos, System.currentTimeMillis());
                seenParticles.clear();
                solverState = SolverState.PROCESSING_FIRST_USE;
                possibleTargets = calculatePossibleTargets(playerPos);
                return HandleCompassResult.SUCCESS;
            case NEED_SECOND_COMPASS:
                if (firstCompass.whereUsed.getSquaredDistance(playerPos) < MINIMUM_DISTANCE_SQ_BETWEEN_COMPASSES) {
                    return HandleCompassResult.LOCATION_TOO_CLOSE;
                }

                HollowsZone firstCompassZone = getZoneForCoords(firstCompass.whereUsed);
                HollowsZone playerZone = getZoneForCoords(playerPos);
                if (!possibleTargets.equals(calculatePossibleTargets(playerPos)) || firstCompassZone != playerZone) {
                    resetForNewTarget();
                    return HandleCompassResult.POSSIBLE_TARGETS_CHANGED;
                }

                secondCompass = new Compass(playerPos, System.currentTimeMillis());
                solverState = SolverState.PROCESSING_SECOND_USE;
                return HandleCompassResult.SUCCESS;
        }

        throw new IllegalStateException("Unexpected compass state");
    }

    /*
     * Processes particles if the wishing compass was used within the last 5 seconds.
     *
     * The first and the last particles are used to create a line for each wishing compass
     * use that is then used to calculate the target.
     *
     * Once two lines have been calculated, the shortest line between the two is calculated
     * with the midpoint on that line being the wishing compass target. The accuracy of this
     * seems to be very high.
     *
     * The target location varies based on various criteria, including, but not limited to:
     *  Topaz Crystal (Khazad-dûm)                Magma Fields
     *  Odawa (Jungle Village)                    Jungle w/no Jungle Key in inventory
     *  Amethyst Crystal (Jungle Temple)          Jungle w/Jungle Key in inventory
     *  Sapphire Crystal (Lost Precursor City)    Precursor Remnants
     *  Jade Crystal (Mines of Divan)             Mithril Deposits
     *  King Yolkar                               Goblin Holdout without "King's Scent I" effect
     *  Goblin Queen                              Goblin Holdout with "King's Scent I" effect
     *  Crystal Nucleus                           All Crystals found and none placed
     *                                            per-area structure missing, or because Hypixel.
     *                                            Always within 1 block of X=513 Y=106 Z=551.
     */
    public void onSpawnParticle(ParticleType particleType, double x, double y, double z) {
        if (particleType != ParticleTypes.HAPPY_VILLAGER
                || PlayerLocator.getPlayerLocation() != PlayerLocator.Location.CRYSTAL_HOLLOWS) {
            return;
        }

        // Capture particle troubleshooting info for two minutes starting when the first compass is used.
        // This list is reset each time the first compass is used from a NOT_STARTED state.
        if (firstCompass != null && !solverState.equals(SolverState.SOLVED)
                && System.currentTimeMillis() < firstCompass.whenUsedMillis + 2 * 60 * 1000) {
            seenParticles.add(new ParticleData(new Vec3Comparable(x, y, z), System.currentTimeMillis()));
        }

        try {
            SolverState originalSolverState = solverState;
            solveUsingParticle(x, y, z, System.currentTimeMillis());
            if (solverState != originalSolverState) {
                switch (solverState) {
                    case SOLVED:
                        showSolution();
                        break;
                    case FAILED_EXCEPTION:
                        client.player.sendMessage(Text.literal(Formatting.RED +
                            "[Skyblocker] Unable to determine wishing compass target."), false);
                        logDiagnosticData(false);
                        break;
                    case FAILED_TIMEOUT_NO_REPEATING:
                        client.player.sendMessage(Text.literal(Formatting.RED +
                            "[Skyblocker] Timed out waiting for repeat set of compass particles."), false);
                        logDiagnosticData(false);
                        break;
                    case FAILED_TIMEOUT_NO_PARTICLES:
                        client.player.sendMessage(Text.literal(Formatting.RED +
                            "[Skyblocker] Timed out waiting for compass particles."), false);
                        logDiagnosticData(false);
                        break;
                    case FAILED_INTERSECTION_CALCULATION:
                        client.player.sendMessage(Text.literal(Formatting.RED +
                            "[Skyblocker] Unable to determine intersection of wishing compasses."), false);
                        logDiagnosticData(false);
                        break;
                    case FAILED_INVALID_SOLUTION:
                        client.player.sendMessage(Text.literal(Formatting.RED +
                            "[Skyblocker] Failed to find solution."), false);
                        logDiagnosticData(false);
                        break;
                    case NEED_SECOND_COMPASS:
                        client.player.sendMessage(Text.literal(Formatting.YELLOW +
                            "[Skyblocker] Need another position to determine wishing compass target."), false);
                        break;
                }
            }
        } catch (Exception e) {
            client.player.sendMessage(Text.literal(Formatting.RED +
                "[Skyblocker] Exception while calculating wishing compass solution - see log for details"), false);
            e.printStackTrace();
        }
    }

    /**
     * @param x Particle x coordinate
     * @param y Particle y coordinate
     * @param z Particle z coordinate
     */
    public void solveUsingParticle(double x, double y, double z, long currentTimeMillis) {
        Compass currentCompass;
        switch (solverState) {
            case PROCESSING_FIRST_USE:
                currentCompass = firstCompass;
                break;
            case PROCESSING_SECOND_USE:
                currentCompass = secondCompass;
                break;
            default:
                return;
        }

        currentCompass.processParticle(x, y, z, currentTimeMillis);
        switch (currentCompass.compassState) {
            case FAILED_TIMEOUT_NO_PARTICLES:
                solverState = SolverState.FAILED_TIMEOUT_NO_PARTICLES;
                return;
            case FAILED_TIMEOUT_NO_REPEATING:
                solverState = SolverState.FAILED_TIMEOUT_NO_REPEATING;
                return;
            case WAITING_FOR_FIRST_PARTICLE:
            case COMPUTING_LAST_PARTICLE:
                return;
            case COMPLETED:
                if (solverState == SolverState.NEED_SECOND_COMPASS) {
                    return;
                }
                if (solverState == SolverState.PROCESSING_FIRST_USE) {
                    solverState = SolverState.NEED_SECOND_COMPASS;
                    return;
                }
                break;
        }

        // First and Second compasses have completed
        solutionIntersectionLine = firstCompass.line.getIntersectionLineSegment(secondCompass.line);

        if (solutionIntersectionLine == null) {
            solverState = SolverState.FAILED_INTERSECTION_CALCULATION;
            return;
        }

        solution = new Vec3Comparable(solutionIntersectionLine.getMidpoint());

        Vec3Comparable firstDirection = firstCompass.getDirection();
        Vec3Comparable firstSolutionDirection = firstCompass.getDirectionTo(solution);
        Vec3Comparable secondDirection = secondCompass.getDirection();
        Vec3Comparable secondSolutionDirection = secondCompass.getDirectionTo(solution);
        if (!firstDirection.signumEquals(firstSolutionDirection)
                || !secondDirection.signumEquals(secondSolutionDirection)
                || !HOLLOWS_BB.contains(solution)) {
            solverState = SolverState.FAILED_INVALID_SOLUTION;
            return;
        }

        solutionPossibleTargets = getSolutionTargets(
            getZoneForCoords(firstCompass.whereUsed),
            getFoundCrystals(),
            possibleTargets,
            solution
        );

        // Adjust the Jungle Temple solution coordinates
        if (solutionPossibleTargets.size() == 1 && solutionPossibleTargets.contains(CompassTarget.JUNGLE_TEMPLE)) {
            originalSolution = solution;
            solution = solution.add(JUNGLE_DOOR_OFFSET_FROM_CRYSTAL);
        }

        solverState = SolverState.SOLVED;
    }

    private static boolean isKeyInInventory() {
        for (ItemStack item : client.player.getInventory().main) {
            if (item != null && item.getName().getString().contains("Jungle Key")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isKingsScentPresent() {
        String footertext = PlayerListMgr.getFooter();
        return (footertext != null && footertext.contains("King's Scent I"));
    }

    private static EnumSet<Crystal> getFoundCrystals() {
        return EnumSet.noneOf(Crystal.class);
    }

    // Returns candidates based on:
    //  - Structure Y levels observed in various lobbies. It is assumed
    //    that structures other than Khazad Dum cannot have any portion
    //    in the Magma Fields.
    //
    //  - Structure sizes & offsets into other zones that assume at least
    //    one block must be in the correct zone.
    //
    //  - An assumption that any structure could be missing with a
    //    special exception for the Jungle Temple since it often conflicts
    //    with Bal and a lobby with a missing Jungle Temple has not been
    //    observed. This exception will remove Bal as a target if:
    //      - Target candidates include both Bal & the Jungle Temple.
    //      - The Amethyst crystal has not been acquired.
    //      - The zone that the compass was used in is the Jungle.
    //
    //     - If the solution is the Crystal Nucleus then a copy of the
    //       passed in possible targets is returned.
    //
    // |----------|------------|
    // |  Jungle  |  Mithril   |
    // |          |  Deposits  |
    // |----------|----------- |
    // |  Goblin  |  Precursor |
    // |  Holdout |  Deposits  |
    // |----------|------------|
    static public EnumSet<CompassTarget> getSolutionTargets(
        HollowsZone compassUsedZone,
        EnumSet<Crystal> foundCrystals,
        EnumSet<CompassTarget> possibleTargets,
        Vec3Comparable solution
    ) {
        EnumSet<CompassTarget> solutionPossibleTargets;
        solutionPossibleTargets = possibleTargets.clone();

        HollowsZone solutionZone = getZoneForCoords(solution);
        if (solutionZone == HollowsZone.CRYSTAL_NUCLEUS) {
            return solutionPossibleTargets;
        }

        solutionPossibleTargets.remove(CompassTarget.CRYSTAL_NUCLEUS);

        // Y coordinates are 43-71 from 13 samples
        // Y=41/74 is the absolute min/max based on structure size if
        // the center of the topaz crystal has to be in magma fields.
        if (solutionPossibleTargets.contains(CompassTarget.BAL) && solution.getY() > 75) {
            solutionPossibleTargets.remove(CompassTarget.BAL);
        }

        // Y coordinates are 93-157 from 15 samples.
        // Y=83/167 is the absolute min/max based on structure size
        if (solutionPossibleTargets.contains(CompassTarget.GOBLIN_KING)
                && (solution.getY() < 82 || solution.getY() > 168)) {
            solutionPossibleTargets.remove(CompassTarget.GOBLIN_KING);
        }

        // Y coordinates are 129-139 from 10 samples
        // Y=126/139 is the absolute min/max based on structure size
        if (solutionPossibleTargets.contains(CompassTarget.GOBLIN_QUEEN)
                && (solution.getY() < 125 || solution.getY() > 140)) {
            solutionPossibleTargets.remove(CompassTarget.GOBLIN_QUEEN);
        }

        // Y coordinates are 72-80 from 10 samples
        // Y=73/80 is the absolute min/max based on structure size
        if (solutionPossibleTargets.contains(CompassTarget.JUNGLE_TEMPLE)
                && (solution.getY() < 72 || solution.getY() > 81)) {
            solutionPossibleTargets.remove(CompassTarget.JUNGLE_TEMPLE);
        }

        // Y coordinates are 87-155 from 7 samples
        // Y=74/155 is the absolute min/max solution based on structure size
        if (solutionPossibleTargets.contains(CompassTarget.ODAWA)
                && (solution.getY() < 73 || solution.getY() > 155)) {
            solutionPossibleTargets.remove(CompassTarget.ODAWA);
        }

        // Y coordinates are 122-129 from 8 samples
        // Y=122/129 is the absolute min/max based on structure size
        if (solutionPossibleTargets.contains(CompassTarget.PRECURSOR_CITY)
                && (solution.getY() < 121 || solution.getY() > 130)) {
            solutionPossibleTargets.remove(CompassTarget.PRECURSOR_CITY);
        }

        // Y coordinates are 98-102 from 15 samples
        // Y=98/100 is the absolute min/max based on structure size,
        // but 102 has been seen - possibly with earlier code that rounded up
        if (solutionPossibleTargets.contains(CompassTarget.MINES_OF_DIVAN)
                && (solution.getY() < 97 || solution.getY() > 102)) {
            solutionPossibleTargets.remove(CompassTarget.MINES_OF_DIVAN);
        }

        // Now filter by structure offset
        if (solutionPossibleTargets.contains(CompassTarget.GOBLIN_KING)
                && (solution.getX() > GOBLIN_HOLDOUT_BB.maxX + GOBLIN_KING_BB.maxX
                    || solution.getZ() < GOBLIN_HOLDOUT_BB.minZ - GOBLIN_KING_BB.maxZ)) {
            solutionPossibleTargets.remove(CompassTarget.GOBLIN_KING);
        }

        if (solutionPossibleTargets.contains(CompassTarget.GOBLIN_QUEEN)
                && (solution.getX() > GOBLIN_HOLDOUT_BB.maxX + GOBLIN_QUEEN_BB.maxX
                    || solution.getZ() < GOBLIN_HOLDOUT_BB.minZ - GOBLIN_QUEEN_BB.maxZ)) {
            solutionPossibleTargets.remove(CompassTarget.GOBLIN_QUEEN);
        }

        if (solutionPossibleTargets.contains(CompassTarget.JUNGLE_TEMPLE)
                && (solution.getX() > JUNGLE_BB.maxX + JUNGLE_TEMPLE_BB.maxX
                    || solution.getZ() > JUNGLE_BB.maxZ + JUNGLE_TEMPLE_BB.maxZ)) {
            solutionPossibleTargets.remove(CompassTarget.JUNGLE_TEMPLE);
        }

        if (solutionPossibleTargets.contains(CompassTarget.ODAWA)
                && (solution.getX() > JUNGLE_BB.maxX + ODAWA_BB.maxX
                    || solution.getZ() > JUNGLE_BB.maxZ + ODAWA_BB.maxZ)) {
            solutionPossibleTargets.remove(CompassTarget.ODAWA);
        }

        if (solutionPossibleTargets.contains(CompassTarget.PRECURSOR_CITY)
                && (solution.getX() < PRECURSOR_REMNANTS_BB.minX - PRECURSOR_CITY_BB.maxX
                    || solution.getZ() < PRECURSOR_REMNANTS_BB.minZ - PRECURSOR_CITY_BB.maxZ)) {
            solutionPossibleTargets.remove(CompassTarget.PRECURSOR_CITY);
        }

        if (solutionPossibleTargets.contains(CompassTarget.MINES_OF_DIVAN)
                && (solution.getX() < MITHRIL_DEPOSITS_BB.minX - MINES_OF_DIVAN_BB.maxX
                    || solution.getZ() > MITHRIL_DEPOSITS_BB.maxZ + MINES_OF_DIVAN_BB.maxZ)) {
            solutionPossibleTargets.remove(CompassTarget.MINES_OF_DIVAN);
        }

        // Special case the Jungle Temple
        if (solutionPossibleTargets.contains(CompassTarget.JUNGLE_TEMPLE)
                && solutionPossibleTargets.contains(CompassTarget.BAL)
                && !getFoundCrystals().contains(Crystal.AMETHYST)
                && compassUsedZone == HollowsZone.JUNGLE) {
            solutionPossibleTargets.remove(CompassTarget.BAL);
        }

        return solutionPossibleTargets;
    }

    private EnumSet<CompassTarget> calculatePossibleTargets(BlockPos playerPos) {
        EnumSet<CompassTarget> candidateTargets = EnumSet.of(CompassTarget.CRYSTAL_NUCLEUS);
        EnumSet<Crystal> foundCrystals = this.getFoundCrystals();

        // Add targets based on missing crystals.
        // NOTE:
        //   We used to assume that only the adjacent zone's targets could be returned. That turned
        //   out to be incorrect (e.g. a compass in the jungle pointed to the Precursor City when
        //   the king would have been a valid target). Now we assume that any structure could be
        //   missing (because Hypixel) and  depend on the solution coordinates to filter the list.
        for (Crystal crystal : Crystal.values()) {
            if (getFoundCrystals().contains(crystal)) {
                continue;
            }

            switch (crystal) {
                case JADE:
                    candidateTargets.add(CompassTarget.MINES_OF_DIVAN);
                    break;
                case AMBER:
                    candidateTargets.add(
                        isKingsScentPresent() ? CompassTarget.GOBLIN_QUEEN : CompassTarget.GOBLIN_KING);
                    break;
                case TOPAZ:
                    candidateTargets.add(CompassTarget.BAL);
                    break;
                case AMETHYST:
                    candidateTargets.add(isKeyInInventory() ? CompassTarget.JUNGLE_TEMPLE : CompassTarget.ODAWA);
                    break;
                case SAPPHIRE:
                    candidateTargets.add(CompassTarget.PRECURSOR_CITY);
                    break;
            }
        }

        return candidateTargets;
    }

    private String getSolutionCoordsText() {
        return solution == null ? "" :
            String.format("%.0f %.0f %.0f", solution.getX(), solution.getY(), solution.getZ());
    }

    private String getWishingCompassDestinationsMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(Formatting.YELLOW);
        sb.append("[Skyblocker] ");
        sb.append(Formatting.AQUA);
        sb.append("Wishing compass points to ");
        int index = 1;
        for (CompassTarget target : solutionPossibleTargets) {
            if (index > 1) {
                sb.append(Formatting.AQUA + ((index == solutionPossibleTargets.size()) ? " or " : ", "));
            }
            sb.append(target.toString());
            index++;
        }

        sb.append(Formatting.AQUA);
        sb.append(" (");
        sb.append(getSolutionCoordsText());
        sb.append(")");
        return sb.toString();
    }

    private void showSolution() {
        if (solution == null) return;

        if (NUCLEUS_BB.contains(solution)) {
            client.player.sendMessage(Text.literal(Formatting.YELLOW + "[Skyblocker] " +
                Formatting.AQUA + "Wishing compass target is the Crystal Nucleus"), false);
            return;
        }

        String destinationMessage = getWishingCompassDestinationsMessage();

        client.player.sendMessage(Text.literal(destinationMessage), false);
    }

    private String getDiagnosticMessage() {
        StringBuilder diagsMessage = new StringBuilder();

        diagsMessage.append(Formatting.AQUA);
        diagsMessage.append("Solver State: ");
        diagsMessage.append(Formatting.WHITE);
        diagsMessage.append(solverState.name());
        diagsMessage.append("\n");

        if (firstCompass == null) {
            diagsMessage.append(Formatting.AQUA);
            diagsMessage.append("First Compass: ");
            diagsMessage.append(Formatting.WHITE);
            diagsMessage.append("<NONE>");
            diagsMessage.append("\n");
        } else {
            firstCompass.appendCompassDiagnostics(diagsMessage, "First Compass");
        }

        if (secondCompass == null) {
            diagsMessage.append(Formatting.AQUA);
            diagsMessage.append("Second Compass: ");
            diagsMessage.append(Formatting.WHITE);
            diagsMessage.append("<NONE>");
            diagsMessage.append("\n");
        } else {
            secondCompass.appendCompassDiagnostics(diagsMessage, "Second Compass");
        }

        diagsMessage.append(Formatting.AQUA);
        diagsMessage.append("Intersection Line: ");
        diagsMessage.append(Formatting.WHITE);
        diagsMessage.append((solutionIntersectionLine == null) ? "<NONE>" : solutionIntersectionLine);
        diagsMessage.append("\n");

        diagsMessage.append(Formatting.AQUA);
        diagsMessage.append("Jungle Key in Inventory: ");
        diagsMessage.append(Formatting.WHITE);
        diagsMessage.append(isKeyInInventory());
        diagsMessage.append("\n");

        diagsMessage.append(Formatting.AQUA);
        diagsMessage.append("King's Scent Present: ");
        diagsMessage.append(Formatting.WHITE);
        diagsMessage.append(isKingsScentPresent());
        diagsMessage.append("\n");

        diagsMessage.append(Formatting.AQUA);
        diagsMessage.append("First Compass Targets: ");
        diagsMessage.append(Formatting.WHITE);
        diagsMessage.append(possibleTargets == null ? "<NONE>" : possibleTargets.toString());
        diagsMessage.append("\n");

        diagsMessage.append(Formatting.AQUA);
        diagsMessage.append("Current Calculated Targets: ");
        diagsMessage.append(Formatting.WHITE);
        diagsMessage.append(calculatePossibleTargets(client.player.getBlockPos()));
        diagsMessage.append("\n");

        diagsMessage.append(Formatting.AQUA);
        diagsMessage.append("Found Crystals: ");
        diagsMessage.append(Formatting.WHITE);
        diagsMessage.append(getFoundCrystals());
        diagsMessage.append("\n");

        if (originalSolution != null) {
            diagsMessage.append(Formatting.AQUA);
            diagsMessage.append("Original Solution: ");
            diagsMessage.append(Formatting.WHITE);
            diagsMessage.append(originalSolution);
            diagsMessage.append("\n");
        }

        diagsMessage.append(Formatting.AQUA);
        diagsMessage.append("Solution: ");
        diagsMessage.append(Formatting.WHITE);
        diagsMessage.append((solution == null) ? "<NONE>" : solution.toString());
        diagsMessage.append("\n");

        diagsMessage.append(Formatting.AQUA);
        diagsMessage.append("Solution Targets: ");
        diagsMessage.append(Formatting.WHITE);
        diagsMessage.append((solutionPossibleTargets == null) ? "<NONE>" : solutionPossibleTargets.toString());
        diagsMessage.append("\n");

        diagsMessage.append(Formatting.AQUA);
        diagsMessage.append("Seen particles:\n");
        for (ParticleData particleData : seenParticles) {
            diagsMessage.append(Formatting.WHITE);
            diagsMessage.append(particleData);
            diagsMessage.append("\n");
        }

        return diagsMessage.toString();
    }

    public void logDiagnosticData(boolean outputAlways) {
        if (!Utils.isOnSkyblock()) {
            return;
        }

        LOGGER.debug(String.format("[Skyblocker] %s", getDiagnosticMessage()));
    }

    enum CompassState {
        WAITING_FOR_FIRST_PARTICLE,
        COMPUTING_LAST_PARTICLE,
        COMPLETED,
        FAILED_TIMEOUT_NO_REPEATING,
        FAILED_TIMEOUT_NO_PARTICLES,
    }

    enum HandleCompassResult {
        SUCCESS,
        LOCATION_TOO_CLOSE,
        STILL_PROCESSING_PRIOR_USE,
        POSSIBLE_TARGETS_CHANGED,
        NO_PARTICLES_FOR_PREVIOUS_COMPASS,
        PLAYER_IN_NUCLEUS
    }

    static class Compass {
        public CompassState compassState;
        public Line line = null;

        private final BlockPos whereUsed;
        private final long whenUsedMillis;
        private Vec3Comparable firstParticle = null;
        private Vec3Comparable previousParticle = null;
        private Vec3Comparable lastParticle = null;
        private final ArrayList<ProcessedParticle> processedParticles;

        Compass(BlockPos whereUsed, long whenUsedMillis) {
            this.whereUsed = whereUsed;
            this.whenUsedMillis = whenUsedMillis;
            compassState = CompassState.WAITING_FOR_FIRST_PARTICLE;
            processedParticles = new ArrayList<>();
        }

        public Vec3Comparable getDirection() {
            if (firstParticle == null || lastParticle == null) {
                return null;
            }

            return new Vec3Comparable(lastParticle.subtract(firstParticle).normalize());
        }

        public Vec3Comparable getDirectionTo(Vec3Comparable target) {
            if (firstParticle == null || target == null) {
                return null;
            }

            return new Vec3Comparable(target.subtract(firstParticle).normalize());
        }

        public double particleSpread() {
            if (firstParticle == null || lastParticle == null) {
                return 0.0;
            }
            return firstParticle.distanceTo(lastParticle);
        }

        public void processParticle(double x, double y, double z, long particleTimeMillis) {
            if (compassState == CompassState.FAILED_TIMEOUT_NO_REPEATING
                    || compassState == CompassState.FAILED_TIMEOUT_NO_PARTICLES
                    || compassState == CompassState.COMPLETED) {
                throw new UnsupportedOperationException("processParticle should not be called in a failed or completed state");
            }

            if (particleTimeMillis - this.whenUsedMillis > ALL_PARTICLES_MAX_MILLIS) {
                // Assume we have failed if we're still trying to process particles
                compassState = CompassState.FAILED_TIMEOUT_NO_REPEATING;
                return;
            }

            Vec3Comparable currentParticle = new Vec3Comparable(x, y, z);
            if (compassState == CompassState.WAITING_FOR_FIRST_PARTICLE) {
                if (currentParticle.distanceTo(new Vec3Comparable(whereUsed)) < MAX_DISTANCE_FROM_USE_TO_FIRST_PARTICLE) {
                    processedParticles.add(new ProcessedParticle(currentParticle, particleTimeMillis));
                    firstParticle = currentParticle;
                    previousParticle = currentParticle;
                    compassState = CompassState.COMPUTING_LAST_PARTICLE;
                }
                return;
            }

            // State is COMPUTING_LAST_PARTICLE, keep updating the previousParticle until
            // the first particle in the second sequence is seen.
            if (currentParticle.distanceTo(previousParticle) <= MAX_DISTANCE_BETWEEN_PARTICLES) {
                processedParticles.add(new ProcessedParticle(currentParticle, particleTimeMillis));
                previousParticle = currentParticle;
                return;
            }

            if (currentParticle.distanceTo(firstParticle) > MAX_DISTANCE_BETWEEN_PARTICLES) {
                return;
            }

            // It's a repeating particle
            processedParticles.add(new ProcessedParticle(currentParticle, particleTimeMillis));
            lastParticle = previousParticle;
            line = new Line(firstParticle, lastParticle);
            compassState = CompassState.COMPLETED;
        }

        public void appendCompassDiagnostics(StringBuilder diagsMessage, String compassName) {
            diagsMessage.append(Formatting.AQUA);
            diagsMessage.append("Compass State: ");
            diagsMessage.append(Formatting.WHITE);
            diagsMessage.append(compassState.name());
            diagsMessage.append("\n");

            diagsMessage.append(Formatting.AQUA);
            diagsMessage.append(compassName);
            diagsMessage.append(" Used Millis: ");
            diagsMessage.append(Formatting.WHITE);
            diagsMessage.append(whenUsedMillis);
            diagsMessage.append("\n");

            diagsMessage.append(Formatting.AQUA);
            diagsMessage.append(compassName);
            diagsMessage.append(" Used Position: ");
            diagsMessage.append(Formatting.WHITE);
            diagsMessage.append((whereUsed == null) ? "<NONE>" : whereUsed.toString());
            diagsMessage.append("\n");

            diagsMessage.append(Formatting.AQUA);
            diagsMessage.append(compassName);
            diagsMessage.append(" All Seen Particles: \n");
            diagsMessage.append(Formatting.WHITE);
            for (ProcessedParticle particle : processedParticles) {
                diagsMessage.append(particle.toString());
                diagsMessage.append("\n");
            }

            diagsMessage.append(Formatting.AQUA);
            diagsMessage.append(compassName);
            diagsMessage.append(" Particle Spread: ");
            diagsMessage.append(Formatting.WHITE);
            diagsMessage.append(particleSpread());
            diagsMessage.append("\n");

            diagsMessage.append(Formatting.AQUA);
            diagsMessage.append(compassName);
            diagsMessage.append(" Compass Line: ");
            diagsMessage.append(Formatting.WHITE);
            diagsMessage.append((line == null) ? "<NONE>" : line.toString());
            diagsMessage.append("\n");
        }

        static class ProcessedParticle {
            Vec3Comparable coords;
            long particleTimeMillis;

            ProcessedParticle(Vec3Comparable coords, long particleTimeMillis) {
                this.coords = coords;
                this.particleTimeMillis = particleTimeMillis;
            }

            @Override
            public String toString() {
                return coords.toString() + " " + particleTimeMillis;
            }
        }
    }

    private static class ParticleData {
        Vec3Comparable particleLocation;
        long systemTime;

        public ParticleData(Vec3Comparable particleLocation, long systemTime) {
            this.particleLocation = particleLocation;
            this.systemTime = systemTime;
        }

        public String toString() {
            return "Location: " + particleLocation.toString() + ", systemTime: " + systemTime;
        }
    }
}
