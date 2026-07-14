package de.hysky.skyblocker.skyblock.events;

import com.mojang.serialization.Codec;
import net.minecraft.world.item.ItemStackTemplate;

// should this have an id field? for config per chance, but name could be directly used
public record SkyblockEvent(String name, ItemStackTemplate icon, Codec<? extends ExtraEventData> extraDataDecoder) {
}
