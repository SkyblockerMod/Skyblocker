package de.hysky.skyblocker.skyblock.experiment;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.HelperConfig;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public abstract sealed class ExperimentSolver extends SimpleContainerSolver permits ChronomatronSolver, SuperpairsSolver, UltrasequencerSolver {
    public enum State {
        REMEMBER, WAIT, SHOW, END
    }

    private State state = State.REMEMBER;
    private final Map<Integer, ItemStack> slots = new HashMap<>();

    protected ExperimentSolver(@NotNull @Language("RegExp") String containerName) {
        super(containerName);
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Map<Integer, ItemStack> getSlots() {
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
