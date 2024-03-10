package de.hysky.skyblocker.skyblock.tabhud.widget.hud;

import de.hysky.skyblocker.skyblock.garden.FarmingHud;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.Widget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.ProgressComponent;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.Map;

public class HudFarmingWidget extends Widget {
    private static final MutableText TITLE = Text.literal("Farming").formatted(Formatting.YELLOW, Formatting.BOLD);
    private static final Map<String, ItemStack> FARMING_TOOLS = Map.ofEntries(
            Map.entry("THEORETICAL_HOE_WHEAT_1", Ico.WHEAT),
            Map.entry("THEORETICAL_HOE_WHEAT_2", Ico.WHEAT),
            Map.entry("THEORETICAL_HOE_WHEAT_3", Ico.WHEAT),
            Map.entry("THEORETICAL_HOE_CARROT_1", Ico.CARROT),
            Map.entry("THEORETICAL_HOE_CARROT_2", Ico.CARROT),
            Map.entry("THEORETICAL_HOE_CARROT_3", Ico.CARROT),
            Map.entry("THEORETICAL_HOE_POTATO_1", Ico.POTATO),
            Map.entry("THEORETICAL_HOE_POTATO_2", Ico.POTATO),
            Map.entry("THEORETICAL_HOE_POTATO_3", Ico.POTATO),
            Map.entry("THEORETICAL_HOE_CANE_1", Ico.SUGAR_CANE),
            Map.entry("THEORETICAL_HOE_CANE_2", Ico.SUGAR_CANE),
            Map.entry("THEORETICAL_HOE_CANE_3", Ico.SUGAR_CANE),
            Map.entry("THEORETICAL_HOE_WARTs_1", Ico.NETHER_WART),
            Map.entry("THEORETICAL_HOE_WARTs_2", Ico.NETHER_WART),
            Map.entry("THEORETICAL_HOE_WARTs_3", Ico.NETHER_WART),
            Map.entry("FUNGI_CUTTER", Ico.MUSHROOM),
            Map.entry("CACTUS_KNIFE", Ico.CACTUS),
            Map.entry("MELON_DICER", Ico.MELON),
            Map.entry("PUMPKIN_DICER", Ico.PUMPKIN),
            Map.entry("COCO_CHOPPER", Ico.COCOA_BEANS)
    );
    public static final HudFarmingWidget INSTANCE = new HudFarmingWidget();
    private final MinecraftClient client = MinecraftClient.getInstance();

    public HudFarmingWidget() {
        super(TITLE, Formatting.YELLOW.getColorValue());
        update();
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void updateContent() {
        ItemStack icon = FARMING_TOOLS.getOrDefault(ItemUtils.getItemId(client.player.getMainHandStack()), Ico.HOE);
        addSimpleIcoText(icon, "Counter: ", Formatting.YELLOW, FarmingHud.NUMBER_FORMAT.format(FarmingHud.counter()));
        addSimpleIcoText(icon, "Crops/min: ", Formatting.YELLOW, FarmingHud.NUMBER_FORMAT.format((int) FarmingHud.cropsPerMinute() / 100 * 100));
        addSimpleIcoText(icon, "Blocks/s: ", Formatting.YELLOW, Integer.toString(FarmingHud.blockBreaks()));
        addComponent(new ProgressComponent(Ico.LANTERN, Text.literal("Farming Level: "), FarmingHud.farmingXpPercentProgress(), Formatting.GOLD.getColorValue()));
        addSimpleIcoText(Ico.LIME_DYE, "Farming XP/h: ", Formatting.YELLOW, FarmingHud.NUMBER_FORMAT.format((int) FarmingHud.farmingXpPerHour()));

        double yaw = client.getCameraEntity().getYaw();
        double pitch = client.getCameraEntity().getPitch();
        addComponent(new PlainTextComponent(Text.literal("Yaw: " + String.format("%.3f", MathHelper.wrapDegrees(yaw))).formatted(Formatting.YELLOW)));
        addComponent(new PlainTextComponent(Text.literal("Pitch: " + String.format("%.3f", MathHelper.wrapDegrees(pitch))).formatted(Formatting.YELLOW)));
    }
}
