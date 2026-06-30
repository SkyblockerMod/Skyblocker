package de.hysky.skyblocker.skyblock.events;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.JacobsContestWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;

public interface ExtraEventData {
	MapCodec<Optional<? extends ExtraEventData>> CODEC = SkyblockEvents.CODEC.dispatchMap(
			"event",
			_ -> SkyblockEvents.DUMMY,
			event -> event.extraDataDecoder().lenientOptionalFieldOf("extras"));

	void addInformation(List<ClientTooltipComponent> components);

	default Toast createToast(EventInstance instance) {
		return new EventToast(instance);
	}

	record Nothing() implements ExtraEventData {
		public static final Nothing INSTANCE = new Nothing();
		public static final Codec<Nothing> CODEC = new Codec<>() {

			@Override
			public <T> DataResult<T> encode(Nothing input, DynamicOps<T> ops, T prefix) {
				return DataResult.success(prefix);
			}

			@Override
			public <T> DataResult<Pair<Nothing, T>> decode(DynamicOps<T> ops, T input) {
				return DataResult.success(Pair.of(INSTANCE, input));
			}
		};

		@Override
		public void addInformation(List<ClientTooltipComponent> components) {}
	}

	record Jacobs(List<String> crops) implements ExtraEventData {
		public static final Codec<Jacobs> CODEC = Codec.STRING.listOf(3, 3).xmap(Jacobs::new, Jacobs::crops);

		@Override
		public void addInformation(List<ClientTooltipComponent> components) {
			components.add(new JacobsTooltip(crops));
		}

		@Override
		public Toast createToast(EventInstance instance) {
			return new JacobEventToast(instance, this);
		}

		private record JacobsTooltip(List<String> crops) implements ClientTooltipComponent {

			@Override
			public int getHeight(Font textRenderer) {
				return 20;
			}

			@Override
			public int getWidth(Font textRenderer) {
				return 16 * 3 + 4;
			}

			@Override
			public void extractImage(Font textRenderer, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
				for (int i = 0; i < this.crops.size(); i++) {
					String crop = this.crops.get(i);

					graphics.fakeItem(JacobsContestWidget.FARM_DATA.getOrDefault(crop, Ico.BARRIER).getStackOrThrow(), x + 18 * i, y + 2);
				}
			}
		}
	}

	record JerryPerks(String mayorName, List<String> perks) implements ExtraEventData {
		public static final Codec<JerryPerks> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("name").forGetter(JerryPerks::mayorName),
				Codec.STRING.listOf().fieldOf("perks").forGetter(JerryPerks::perks)
		).apply(instance, JerryPerks::new));

		@Override
		public void addInformation(List<ClientTooltipComponent> components) {
			Component mayorText = Component.translatable("skyblocker.events.tab.currentJerryMayor", mayorName).withStyle(ChatFormatting.DARK_GREEN);
			components.add(ClientTooltipComponent.create(mayorText.getVisualOrderText()));

			for (String perk : perks) {
				Component perkText = Component.literal("- ").withStyle(ChatFormatting.GRAY).append(Component.literal(perk).withStyle(ChatFormatting.GREEN));
				components.add(ClientTooltipComponent.create(perkText.getVisualOrderText()));
			}
		}
	}
}
