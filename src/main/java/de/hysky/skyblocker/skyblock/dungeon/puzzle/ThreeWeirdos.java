package de.hysky.skyblocker.skyblock.dungeon.puzzle;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.Room;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThreeWeirdos extends DungeonPuzzle {
    @SuppressWarnings("unused")
	private static final ThreeWeirdos INSTANCE = new ThreeWeirdos();
    protected static final Pattern PATTERN = Pattern.compile("^\\[NPC] ([A-Z][a-z]+): (?:The reward is(?: not in my chest!|n't in any of our chests\\.)|My chest (?:doesn't have the reward\\. We are all telling the truth\\.|has the reward and I'm telling the truth!)|At least one of them is lying, and the reward is not in [A-Z][a-z]+'s chest!|Both of them are telling the truth\\. Also, [A-Z][a-z]+ has the reward in their chest!)$");
    private static final float[] GREEN_COLOR_COMPONENTS = new float[]{0, 1, 0};
    private static BlockPos pos;
    static Box boundingBox;

    private ThreeWeirdos() {
        super("three-weirdos", "three-chests");
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            ClientWorld world = MinecraftClient.getInstance().world;
            if (overlay || !shouldSolve() || !SkyblockerConfigManager.get().dungeons.puzzleSolvers.solveThreeWeirdos || world == null || !DungeonManager.isCurrentRoomMatched()) return;

            @SuppressWarnings("DataFlowIssue")
            Matcher matcher = PATTERN.matcher(Formatting.strip(message.getString()));
            if (!matcher.matches()) return;
            String name = matcher.group(1);
            Room room = DungeonManager.getCurrentRoom();

            checkForNPC(world, room, new BlockPos(13, 69, 24), name);
            checkForNPC(world, room, new BlockPos(15, 69, 25), name);
            checkForNPC(world, room, new BlockPos(17, 69, 24), name);
        });
        UseBlockCallback.EVENT.register((player, world, hand, blockHitResult) -> {
            if (blockHitResult.getType() == HitResult.Type.BLOCK && blockHitResult.getBlockPos().equals(pos)) {
                pos = null;
            }
            return ActionResult.PASS;
        });
    }

    @Init
    public static void init() {
    }

    private void checkForNPC(ClientWorld world, Room room, BlockPos relative, String name) {
        BlockPos npcPos = room.relativeToActual(relative);
        List<ArmorStandEntity> npcs = world.getEntitiesByClass(
                ArmorStandEntity.class,
                Box.enclosing(npcPos, npcPos),
                entity -> entity.getName().getString().equals(name)
        );
        if (!npcs.isEmpty()) {
            pos = room.relativeToActual(relative.add(1, 0, 0));
            boundingBox = RenderHelper.getBlockBoundingBox(world, pos);
            npcs.forEach(entity -> entity.setCustomName(Text.literal(name).formatted(Formatting.GREEN)));
        }
    }

    @Override
    public void tick(MinecraftClient client) {}

    @Override
    public void render(WorldRenderContext context) {
        if (shouldSolve() && boundingBox != null) {
            RenderHelper.renderFilled(context, boundingBox, GREEN_COLOR_COMPONENTS, 0.5f, false);
        }
    }

    @Override
    public void reset() {
        super.reset();
        pos = null;
    }
}
