package de.hysky.skyblocker.utils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.DataResult.Error;

import de.hysky.skyblocker.injected.SkyblockerStack;
import de.hysky.skyblocker.mixins.accessors.DataComponentPatchAccessor;
import de.hysky.skyblocker.skyblock.item.PetInfo;
import de.hysky.skyblocker.skyblock.item.SkyblockItemRarity;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;

/// Allows for the flexibility when working with {@link ItemStack ItemStacks} in any situation.
public final class FlexibleItemStack implements ItemInstance, SkyblockerStack {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final MapCodec<FlexibleItemStack> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Item.CODEC.fieldOf(FIELD_ID).forGetter(FlexibleItemStack::typeHolder),
			ExtraCodecs.intRange(1, Item.ABSOLUTE_MAX_STACK_SIZE).optionalFieldOf(FIELD_COUNT, 1).forGetter(FlexibleItemStack::count),
			DataComponentPatch.CODEC.optionalFieldOf(FIELD_COMPONENTS, DataComponentPatch.EMPTY).forGetter(FlexibleItemStack::components)
			).apply(instance, FlexibleItemStack::new));
	public static final Codec<FlexibleItemStack> CODEC = Codec.withAlternative(MAP_CODEC.codec(), Item.CODEC, item -> new FlexibleItemStack(item.value()));
	public static final FlexibleItemStack EMPTY = new FlexibleItemStack((Void) null);
	private static final DataComponentGetter FALLBACK_COMPONENT_GETTER = new FallbackComponentGetter();
	private final Holder<Item> item;
	private final int count;
	private final DataComponentPatch components;
	private @Nullable ItemStack itemStack;
	private @Nullable String skyblockId;
	private @Nullable String skyblockApiId;
	private @Nullable String neuName;
	private @Nullable String uuid;
	private @Nullable List<String> loreStrings;
	private @Nullable List<ItemAbility> abilities;
	private @Nullable PetInfo petInfo;
	private @Nullable SkyblockItemRarity skyblockRarity;

	/// Creates a new {@code FlexibleItemStack} and copies the {@code components}.
	public FlexibleItemStack(Holder<Item> item, int count, @Nullable DataComponentPatch components) {
		DataComponentPatch copy = DataComponentPatchAccessor.invokeInit((components == null || components == DataComponentPatch.EMPTY) ? new Reference2ObjectArrayMap<>() : new Reference2ObjectArrayMap<>(((DataComponentPatchAccessor) (Object) components).getMap()));
		this.item = item;
		this.count = count;
		this.components = copy;
	}

	@SuppressWarnings("deprecation")
	public FlexibleItemStack(Item item) {
		this(item.builtInRegistryHolder(), 1, null);
	}

	public FlexibleItemStack(ItemStackTemplate template) {
		this(template.item(), template.count(), template.components());
	}

	public FlexibleItemStack(ItemStack stack) {
		this(stack.typeHolder(), stack.count(), stack.getComponentsPatch());

		// This statement would be first however Checkstyle does not support flexible constructors and instead errors while parsing the file
		// https://github.com/checkstyle/checkstyle/issues/17052
		if (stack.isEmpty()) {
			throw new IllegalStateException("Stack must be non-empty");
		}
	}

	@SuppressWarnings("deprecation")
	private FlexibleItemStack(@Nullable Void voidMarker) {
		this(Items.AIR.builtInRegistryHolder(), 0, null);
	}

	@Override
	public Holder<Item> typeHolder() {
		return this.item;
	}

	@Override
	public int count() {
		return this.count;
	}

	public DataComponentPatch components() {
		return this.components;
	}

	/// {@return the associated component value, if available}
	@Override
	public <T> @Nullable T get(DataComponentType<? extends T> type) {
		if (this.itemStack != null) {
			return this.itemStack.get(type);
		} else {
			return this.components.get(FALLBACK_COMPONENT_GETTER, type);
		}
	}

	/// Updates the associated data component value.
	public <T> void set(DataComponentType<T> type, T value) {
		if (this.itemStack != null) {
			this.itemStack.set(type, value);
		}

		((DataComponentPatchAccessor) (Object) this.components).getMap().put(type, Optional.of(value));
	}

	/// Applies the {@code patch} components on this instance.
	@SuppressWarnings("unchecked")
	public void applyComponents(DataComponentPatch patch) {
		for (Map.Entry<DataComponentType<?>, Optional<?>> entry : patch.entrySet()) {
			if (entry.getValue().isPresent()) {
				this.set((DataComponentType<Object>) entry.getKey(), (Object) entry.getValue().get());
			}
		}
	}

	/// {@return whether this instance represents the {@link #EMPTY} value}
	public boolean isEmpty() {
		return this == EMPTY || this.item.value() == Items.AIR;
	}

	public FlexibleItemStack copy() {
		return this.copyWithCount(this.count());
	}

	public FlexibleItemStack copyWithCount(int count) {
		return new FlexibleItemStack(this.item, count, this.components);
	}

	/// Converts this instance to a {@code ItemStackTemplate}.
	public ItemStackTemplate toTemplate() {
		return new ItemStackTemplate(this.item, this.count, this.components);
	}

	/// {@return the {@code ItemStack} or null}
	public @Nullable ItemStack getStack() {
		this.tryCreateStack();

		return this.itemStack;
	}

	/// {@return the {@code ItemStack} or throws an exception}
	///
	/// @throws NullPointerException if the player is not currently in a world, or if the item or its components are not bound
	public ItemStack getStackOrThrow() {
		this.tryCreateStack();

		return Objects.requireNonNull(this.itemStack, "Not in a world yet (no components bound)");
	}

	/// {@return the {@code ItemStack} or {@link ItemStack#EMPTY}}
	public ItemStack getStackOrEmpty() {
		this.tryCreateStack();

		return this.itemStack == null ? ItemStack.EMPTY : this.itemStack;
	}

	private void tryCreateStack() {
		Holder<Item> typeHolder = this.typeHolder();

		if (this.isEmpty()) {
			this.itemStack = ItemStack.EMPTY;
		} else if (typeHolder.isBound() && typeHolder.areComponentsBound()) {
			if (this.itemStack == null) {
				ItemStack result = new ItemStack(this.item, this.count, this.components);
				Optional<Error<ItemStack>> error = ItemStack.validateStrict(result).error();

				if (error.isPresent()) {
					LOGGER.warn("[Skyblocker Flexible Item Stack] Can't create item stack with properties {}, error: {}", this, error.get().message());
				} else {
					this.itemStack = result;
				}
			}
		} else {
			this.itemStack = null;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public String getSkyblockId() {
		if (this.skyblockId != null && !this.skyblockId.isEmpty()) return this.skyblockId;
		return this.skyblockId = ItemUtils.getItemId(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	public String getSkyblockApiId() {
		if (this.skyblockApiId != null && !this.skyblockApiId.isEmpty()) return this.skyblockApiId;
		return this.skyblockApiId = ItemUtils.getSkyblockApiId(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	public String getNeuName() {
		if (this.neuName != null && !this.neuName.isEmpty()) return this.neuName;
		return this.neuName = ItemUtils.getNeuId(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	public String getUuid() {
		if (this.uuid != null) return this.uuid;
		return this.uuid = ItemUtils.getItemUuid(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	public List<String> skyblocker$getLoreStrings() {
		if (this.loreStrings != null) return this.loreStrings;
		return this.loreStrings = ItemUtils.getLore(this).stream().map(Component::getString).toList();
	}

	@SuppressWarnings("deprecation")
	@Override
	public List<ItemAbility> skyblocker$getAbilities() {
		if (this.abilities != null) return this.abilities;
		return this.abilities = ItemAbility.getAbilities(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	public PetInfo getPetInfo() {
		if (this.petInfo != null) return this.petInfo;
		return this.petInfo = ItemUtils.getPetInfo(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	public SkyblockItemRarity getSkyblockRarity() {
		if (this.skyblockRarity != null) return this.skyblockRarity;
		return this.skyblockRarity = ItemUtils.getItemRarity(this);
	}

	private record FallbackComponentGetter() implements DataComponentGetter {
		@Override
		public <T> @Nullable T get(DataComponentType<? extends T> type) {
			return null;
		}
	}
}
