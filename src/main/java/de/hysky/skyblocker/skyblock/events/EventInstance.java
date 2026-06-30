package de.hysky.skyblocker.skyblock.events;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.utils.Formatters;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public record EventInstance(SkyblockEvent event, Instant start, Duration duration, Optional<? extends ExtraEventData> eventData, AdditionalInfo additionalInfo) {
	public static final MapCodec<EventInstance> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			SkyblockEvents.CODEC.fieldOf("event").forGetter(EventInstance::event),
			Codec.mapEither(Codec.LONG.fieldOf("start"), Codec.LONG.fieldOf("timestamp"))
					.xmap(Either::unwrap, Either::left).xmap(Instant::ofEpochSecond, Instant::getEpochSecond).forGetter(EventInstance::start),
			Codec.LONG.fieldOf("duration").xmap(Duration::ofSeconds, Duration::getSeconds).forGetter(EventInstance::duration),
			ExtraEventData.CODEC.forGetter(EventInstance::eventData),
			AdditionalInfo.MAP_CODEC.forGetter(EventInstance::additionalInfo)
	).apply(instance, EventInstance::new));
	public static final Codec<EventInstance> CODEC = MAP_CODEC.codec();

	public Instant end() {
		return start.plus(duration);
	}

	public record AdditionalInfo(Optional<String> warpCommand) {
		public static final AdditionalInfo EMPTY = new AdditionalInfo(Optional.empty());
		public static final MapCodec<AdditionalInfo> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.mapEither(Codec.STRING.optionalFieldOf("warp"), Codec.STRING.optionalFieldOf("location"))
					.xmap(Either::unwrap, Either::left)
					.xmap(op -> op.filter(s -> !s.isBlank()), Function.identity())
					.forGetter(AdditionalInfo::warpCommand)
		).apply(instance, AdditionalInfo::new));
	}

	public List<ClientTooltipComponent> createTooltip() {
		List<ClientTooltipComponent> components = new ArrayList<>();
		eventData.ifPresent(data -> data.addInformation(components));
		additionalInfo.warpCommand().ifPresent(_ -> components.add(ClientTooltipComponent.create(Component.translatable("skyblocker.events.tab.clickToWarp").withStyle(ChatFormatting.ITALIC).getVisualOrderText())));
		components.add(ClientTooltipComponent.create(Component.literal(Formatters.DATE_FORMATTER.format(start)).withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY).getVisualOrderText()));
		return components;
	}

	public Toast createToast() {
		return eventData.map(data -> data.createToast(this)).orElseGet(() -> new EventToast(this));
	}
}
