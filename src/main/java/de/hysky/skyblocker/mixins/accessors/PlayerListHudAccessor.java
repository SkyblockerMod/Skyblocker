package de.hysky.skyblocker.mixins.accessors;

import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Comparator;

@Mixin(PlayerListHud.class)
public interface PlayerListHudAccessor {
    @Accessor("ENTRY_ORDERING")
    static Comparator<PlayerListEntry> getOrdering() {
        throw new UnsupportedOperationException();
    }
}
