package de.hysky.skyblocker.skyblock.events;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class SkyblockEvents {
	public static final Codec<SkyblockEvent> CODEC = Codec.STRING.xmap(SkyblockEvents::getOrNew, SkyblockEvent::name);
	private static final List<SkyblockEvent> ALL_EVENTS = new ArrayList<>();
	private static final Map<String, SkyblockEvent> BY_NAME = new HashMap<>();

	private static SkyblockEvent register(SkyblockEvent event) {
		ALL_EVENTS.add(event);
		BY_NAME.put(event.name(), event);
		return event;
	}

	private static SkyblockEvent register(String name, ItemStackTemplate icon, Codec<? extends ExtraEventData> extraDataDecoder) {
		return register(new SkyblockEvent(name, icon, extraDataDecoder));
	}

	private static SkyblockEvent register(String name, ItemLike icon, Codec<? extends ExtraEventData> extraDataDecoder) {
		CompoundTag tag = new CompoundTag();
		tag.putString("event", name.toLowerCase(Locale.ENGLISH).replace(' ', '_').replace("'", ""));
		ItemStackTemplate template = new ItemStackTemplate(icon.asItem(), DataComponentPatch.builder().set(DataComponents.CUSTOM_DATA, CustomData.of(tag)).build());
		return register(name, template, extraDataDecoder);
	}

	private static SkyblockEvent register(String name, ItemStackTemplate icon) {
		return register(name, icon, ExtraEventData.Nothing.CODEC);
	}

	private static SkyblockEvent register(String name, ItemLike icon) {
		return register(name, icon, ExtraEventData.Nothing.CODEC);
	}

	public static final SkyblockEvent DARK_AUCTION = register("Dark Auction", Items.NETHER_BRICK);
	public static final SkyblockEvent BONUS_FISHING_FESTIVAL = register("Bonus Fishing Festival", Items.FISHING_ROD);
	public static final SkyblockEvent BONUS_FISHING_FIESTA = register("Bonus Fishing Fiesta", Items.IRON_PICKAXE);
	public static final SkyblockEvent JACOBS_FARMING_CONTEST = register("Jacob's Farming Contest", Items.IRON_HOE, ExtraEventData.Jacobs.CODEC);
	public static final SkyblockEvent NEW_YEAR_CELEBRATION = register("New Year Celebration", Items.CAKE);
	public static final SkyblockEvent MAYOR_ELECTION = register("Mayor Election", Items.JUKEBOX);
	public static final SkyblockEvent MAYOR_JERRY = register("Mayor Jerry", Items.VILLAGER_SPAWN_EGG, ExtraEventData.JerryPerks.CODEC);
	public static final SkyblockEvent SPOOKY_FESTIVAL = register("Spooky Festival", Items.CARVED_PUMPKIN);
	public static final SkyblockEvent SEASON_OF_JERRY = register("Season of Jerry", Items.SNOWBALL);
	public static final SkyblockEvent JERRYS_WORKSHOP = register("Jerry's Workshop", Items.SNOW_BLOCK);
	public static final SkyblockEvent TRAVELING_ZOO = register("Traveling Zoo", Items.HAY_BLOCK);
	public static final SkyblockEvent HARVEST_FEAST = register("Harvest Feast", Items.GOLDEN_HOE);

	public static final SkyblockEvent DUMMY = register("", Items.POISONOUS_POTATO);

	public static Optional<SkyblockEvent> get(String name) {
		return Optional.ofNullable(BY_NAME.get(name));
	}

	public static SkyblockEvent getOrNew(String name) {
		return get(name).orElseGet(() -> register(name, Items.PAPER));
	}

	public static Collection<SkyblockEvent> getAllEvents() {
		return ALL_EVENTS;
	}

}
