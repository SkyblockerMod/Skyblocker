package de.hysky.skyblocker.skyblock.experiment;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.HelperConfig;
import de.hysky.skyblocker.utils.render.gui.ContainerSolver;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public sealed abstract class ExperimentSolver extends ContainerSolver permits ChronomatronSolver, SuperpairsSolver, UltrasequencerSolver {
    public enum State {
        REMEMBER, WAIT, SHOW, END
    }

    private State state = State.REMEMBER;
    private final Map<Integer, ItemStack> slots = new HashMap<>();

    protected ExperimentSolver(String containerName) {
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
    protected final boolean isEnabled() {
        return isEnabled(SkyblockerConfigManager.get().helpers.experiments);
    }

    protected abstract boolean isEnabled(HelperConfig.Experiments experimentsConfig);

    @Override
    protected void start(GenericContainerScreen screen) {
        super.start(screen);
        state = State.REMEMBER;
        ScreenEvents.afterTick(screen).register(this::tick);
    }

    @Override
    protected void reset() {
        super.reset();
        state = State.REMEMBER;
        slots.clear();
    }

    protected abstract void tick(Screen screen);
}
