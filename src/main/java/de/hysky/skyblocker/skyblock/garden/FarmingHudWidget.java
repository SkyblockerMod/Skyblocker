package de.hysky.skyblocker.skyblock.garden;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.Widget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.ProgressComponent;
import de.hysky.skyblocker.utils.ItemUtils;
import it.unimi.dsi.fastutil.doubles.DoubleBooleanPair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.Map;

public class FarmingHudWidget extends Widget {
    private static final MutableText TITLE = Text.literal("Farming").formatted(Formatting.YELLOW, Formatting.BOLD);
    public static final Map<String, String> FARMING_TOOLS = Map.ofEntries(
            Map.entry("THEORETICAL_HOE_WHEAT_1", "WHEAT"),
            Map.entry("THEORETICAL_HOE_WHEAT_2", "WHEAT"),
            Map.entry("THEORETICAL_HOE_WHEAT_3", "WHEAT"),
            Map.entry("THEORETICAL_HOE_CARROT_1", "CARROT_ITEM"),
            Map.entry("THEORETICAL_HOE_CARROT_2", "CARROT_ITEM"),
            Map.entry("THEORETICAL_HOE_CARROT_3", "CARROT_ITEM"),
            Map.entry("THEORETICAL_HOE_POTATO_1", "POTATO_ITEM"),
            Map.entry("THEORETICAL_HOE_POTATO_2", "POTATO_ITEM"),
            Map.entry("THEORETICAL_HOE_POTATO_3", "POTATO_ITEM"),
            Map.entry("THEORETICAL_HOE_CANE_1", "SUGAR_CANE"),
            Map.entry("THEORETICAL_HOE_CANE_2", "SUGAR_CANE"),
            Map.entry("THEORETICAL_HOE_CANE_3", "SUGAR_CANE"),
            Map.entry("THEORETICAL_HOE_WARTS_1", "NETHER_STALK"),
            Map.entry("THEORETICAL_HOE_WARTS_2", "NETHER_STALK"),
            Map.entry("THEORETICAL_HOE_WARTS_3", "NETHER_STALK"),
            Map.entry("FUNGI_CUTTER", "RED_MUSHROOM"),
            Map.entry("CACTUS_KNIFE", "CACTUS"),
            Map.entry("MELON_DICER", "MELON"),
            Map.entry("MELON_DICER_2", "MELON"),
            Map.entry("MELON_DICER_3", "MELON"),
            Map.entry("PUMPKIN_DICER", "PUMPKIN"),
            Map.entry("PUMPKIN_DICER_2", "PUMPKIN"),
            Map.entry("PUMPKIN_DICER_3", "PUMPKIN"),
            Map.entry("COCO_CHOPPER", "INK_SACK.3")
    );
    public static final FarmingHudWidget INSTANCE = new FarmingHudWidget();
    private final MinecraftClient client = MinecraftClient.getInstance();

    public FarmingHudWidget() {
        super(TITLE, Formatting.YELLOW.getColorValue());
        setX(SkyblockerConfigManager.get().farming.garden.farmingHud.x);
        setY(SkyblockerConfigManager.get().farming.garden.farmingHud.y);
        update();
    }

    @Override
    public void updateContent() {
        if (client.player == null) return;

        ItemStack farmingTool = client.player.getMainHandStack();
        addSimpleIcoText(farmingTool, "Counter: ", Formatting.YELLOW, FarmingHud.NUMBER_FORMAT.format(FarmingHud.counter()));
        float cropsPerMinute = FarmingHud.cropsPerMinute();
        addSimpleIcoText(farmingTool, "Crops/min: ", Formatting.YELLOW, FarmingHud.NUMBER_FORMAT.format((int) cropsPerMinute / 10 * 10));
        DoubleBooleanPair itemPrice = ItemUtils.getItemPrice(FARMING_TOOLS.get(ItemUtils.getItemId(farmingTool)));
        addSimpleIcoText(Ico.GOLD, "Coins/h: ", Formatting.GOLD, itemPrice.rightBoolean() ? FarmingHud.NUMBER_FORMAT.format((int) (itemPrice.leftDouble() * cropsPerMinute * 0.6) * 100) : "No Data"); // Multiply by 60 to convert to hourly and divide by 100 for rounding is combined into multiplying by 0.6

        addSimpleIcoText(farmingTool, "Blocks/s: ", Formatting.YELLOW, Integer.toString(FarmingHud.blockBreaks()));
        //noinspection DataFlowIssue
        addComponent(new ProgressComponent(Ico.LANTERN, Text.literal("Farming Level: "), FarmingHud.farmingXpPercentProgress(), Formatting.GOLD.getColorValue()));
        addSimpleIcoText(Ico.LIME_DYE, "Farming XP/h: ", Formatting.YELLOW, FarmingHud.NUMBER_FORMAT.format((int) FarmingHud.farmingXpPerHour()));

        Entity cameraEntity = client.getCameraEntity();
        String yaw = cameraEntity == null ? "No Camera Entity" : String.format("%.3f", MathHelper.wrapDegrees(cameraEntity.getYaw()));
        String pitch = cameraEntity == null ? "No Camera Entity" : String.format("%.3f", MathHelper.wrapDegrees(cameraEntity.getPitch()));
        addComponent(new PlainTextComponent(Text.literal("Yaw: " + yaw).formatted(Formatting.YELLOW)));
        addComponent(new PlainTextComponent(Text.literal("Pitch: " + pitch).formatted(Formatting.YELLOW)));
        if (LowerSensitivity.isSensitivityLowered()) {
            addComponent(new PlainTextComponent(Text.translatable("skyblocker.garden.hud.mouseLocked").formatted(Formatting.ITALIC)));
        }
    }
}
