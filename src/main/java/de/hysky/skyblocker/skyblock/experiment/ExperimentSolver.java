package de.hysky.skyblocker.skyblock.experiment;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.HelperConfig;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

public abstract sealed class ExperimentSolver extends SimpleContainerSolver permits ChronomatronSolver, SuperpairsSolver, UltrasequencerSolver {
    public enum State {
        REMEMBER, WAIT, SHOW, END
    }

    private State state = State.REMEMBER;
    private final Int2ObjectMap<ItemStack> slots = new Int2ObjectOpenHashMap<>();

    protected ExperimentSolver(@NotNull @Language("RegExp") String containerName) {
        super(containerName);
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Int2ObjectMap<ItemStack> getSlots() {
        return slots;
    }

    @Override
    public final boolean isEnabled() {
        return isEnabled(SkyblockerConfigManager.get().helpers.experiments);
    }

    protected abstract boolean isEnabled(HelperConfig.Experiments experimentsConfig);

    @Override
    public void start(GenericContainerScreen screen) {
        state = State.REMEMBER;
        ScreenEvents.afterTick(screen).register(this::tick);
    }

    @Override
    public void reset() {
        state = State.REMEMBER;
        slots.clear();
    }

    protected abstract void tick(Screen screen);
}
