package de.hysky.skyblocker.skyblock.galatea;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.Set;

@RegisterWidget
public class SweepDetailsHudWidget extends ComponentBasedWidget {
    MinecraftClient CLIENT = MinecraftClient.getInstance();

    public SweepDetailsHudWidget() {
        super(Text.literal("Sweep Details"), 0xFF6E37CC, "sweepDetails");
        update();
    }

    // todo: localization, combine toughness with tree name maybe? indent / add emoji for penalty text + test
    @Override
    public void updateContent() {
        if (CLIENT.player == null || CLIENT.currentScreen instanceof WidgetsConfigurationScreen) {
            addComponent(new IcoTextComponent(new ItemStack(Items.STRIPPED_SPRUCE_LOG), Text.literal("Fig Tree")));
            addComponent(new PlainTextComponent(Text.literal("Toughness: 3.5")));
            addComponent(new PlainTextComponent(Text.literal("Sweep: 314.15")));
            return;
        }

        if (!SweepDetailsListener.active || System.currentTimeMillis() > SweepDetailsListener.lastMatch + 1_000) {
            SweepDetailsListener.active = false;
            addComponent(new IcoTextComponent(new ItemStack(Items.STONE_AXE), Text.literal("Punch a tree!")));
            return;
        }

        ItemStack logItemStack;
        if (SweepDetailsListener.lastTreeType == null) {
            logItemStack = new ItemStack(Items.BARRIER);
        } else if (SweepDetailsListener.lastTreeType.equals("Fig")) {
            logItemStack = new ItemStack(Items.STRIPPED_SPRUCE_LOG);
        } else {
            logItemStack = new ItemStack(Items.MANGROVE_LOG);
        }

        addComponent(new IcoTextComponent(logItemStack, Text.literal(SweepDetailsListener.lastTreeType + " Tree")));
        addComponent(new PlainTextComponent(Text.literal("Toughness: " + SweepDetailsListener.toughness)));

        Text sweepAmount;
        if (SweepDetailsListener.maxSweep > SweepDetailsListener.lastSweep) {
            sweepAmount = Text.literal(SweepDetailsListener.lastSweep + " (" + SweepDetailsListener.maxSweep + ")");
        } else {
            sweepAmount = Text.literal(Float.toString(SweepDetailsListener.maxSweep));
        }
        addComponent(new PlainTextComponent(Text.literal("Sweep: ").append(sweepAmount)));

        if (SweepDetailsListener.axePenalty) {
            addComponent(new IcoTextComponent(new ItemStack(Items.BARRIER), Text.literal("Axe Throw Penalty (" + SweepDetailsListener.axePenaltyAmount + "%)")));
        }

        if (SweepDetailsListener.stylePenalty) {
            addComponent(new IcoTextComponent(new ItemStack(Items.BARRIER), Text.literal("Style Penalty (" + SweepDetailsListener.stylePenaltyAmount + "%)")));
            addComponent(new PlainTextComponent(Text.literal("Correct Style: " + SweepDetailsListener.correctStyle)));
        }
    }

    @Override
    public Set<Location> availableLocations() {
        return Set.of(Location.GALATEA);
    }

    @Override
    public void setEnabledIn(Location location, boolean enabled) {
        if (location != Location.GALATEA) return;
        // todo: add config option here
    }

    @Override
    public boolean isEnabledIn(Location location) {
        if (location != Location.GALATEA) return false;
        // todo: replace with config option here
        return true;
    }

    @Override
    public boolean shouldUpdateBeforeRendering() {
        return true;
    }
}
