package me.xmrvizzy.skyblocker.skyblock.rei;

import dev.architectury.event.CompoundEventResult;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.registry.screen.FocusedStackProvider;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

public class SkyblockFocusedStackProvider implements FocusedStackProvider {
    @Override
    public CompoundEventResult<EntryStack<?>> provide(Screen screen, Point mouse) {

        return CompoundEventResult.pass();
//        if (screen instanceof HandledScreen<?> handledScreen) {
//            System.out.println(handledScreen.getTitle().getString() + ", item: " + handledScreen.getScreenHandler().getCursorStack().getItem().getName().getString());
//            return CompoundEventResult.interruptFalse(EntryStacks.of(handledScreen.getScreenHandler().getCursorStack()));
//        } else {
//            System.out.println(screen.getTitle().getString());
//            screen.
//        }
//        return CompoundEventResult.pass();
    }

    @Override
    public double getPriority() {
        return 100d;
    }
}
