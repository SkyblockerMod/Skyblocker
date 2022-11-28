package me.xmrvizzy.skyblocker.skyblock.dwarven;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.DraggableContainer;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class DwarvenHudConfigScreen extends BaseOwoScreen<FlowLayout> {
    private final int hudX;
    private final int hudY;
    protected DwarvenHudConfigScreen(Text title) {
        super(title);
        this.hudX = SkyblockerMod.getInstance().CONFIG.dwarvenMines.dwarvenHud.x();
        this.hudY = SkyblockerMod.getInstance().CONFIG.dwarvenMines.dwarvenHud.y();
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.surface(Surface.VANILLA_TRANSLUCENT);
        rootComponent.child(
                Containers.draggable(
                        Sizing.content(),
                        Sizing.content(),
                        Components.label(Text.of("Commission 1: 1%\nCommission 2: 2%"))
                ).surface(Surface.VANILLA_TRANSLUCENT)
                        .id("dwarven_config_hud")
                        .tooltip(Text.of("Drag the top of the hud to drag!"))
                        .positioning(Positioning.absolute(hudX, hudY))
        );
        rootComponent.childById(DraggableContainer.class, "dwarven_config_hud").padding(Insets.of(3));
    }

    @Override
    public void close() {
        SkyblockerMod.getInstance().CONFIG.dwarvenMines.dwarvenHud.x(this.uiAdapter.rootComponent.childById(DraggableContainer.class, "dwarven_config_hud").x());
        SkyblockerMod.getInstance().CONFIG.dwarvenMines.dwarvenHud.y(this.uiAdapter.rootComponent.childById(DraggableContainer.class, "dwarven_config_hud").y());
        SkyblockerMod.getInstance().CONFIG.save();
        super.close();
    }
}
