package de.hysky.skyblocker.skyblock.events;

import com.mojang.serialization.Codec;
import net.minecraft.world.item.ItemStackTemplate;

public record SkyblockEvent(String name, ItemStackTemplate icon, Codec<? extends ExtraEventData> extraDataDecoder) {
}
