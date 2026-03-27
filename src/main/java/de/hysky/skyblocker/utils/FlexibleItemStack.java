package de.hysky.skyblocker.utils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;

/**
 * Acts as a wrapper around {@code ItemStackTemplates}, allowing more flexible use of them and
 * for performant conversions into ItemStacks.
 */
public final class FlexibleItemStack implements ItemInstance, SkyblockerStack {
	public static final MapCodec<FlexibleItemStack> MAP_CODEC = ItemStackTemplate.MAP_CODEC.xmap(FlexibleItemStack::new, FlexibleItemStack::getTemplate);
	public static final Codec<FlexibleItemStack> CODEC = ItemStackTemplate.CODEC.xmap(FlexibleItemStack::new, FlexibleItemStack::getTemplate);
	@SuppressWarnings("deprecation")
	// TODO make this an air item sometime later, mainly for usage with recipe air spaces
	public static final FlexibleItemStack EMPTY = new FlexibleItemStack(new ItemStackTemplate(Items.BARRIER.builtInRegistryHolder(), 1, DataComponentPatchAccessor.invokeInit(new Reference2ObjectArrayMap<>())));
	private static final DataComponentGetter FALLBACK_COMPONENT_GETTER = new FallbackComponentGetter();
	private final ItemStackTemplate template;
	private @Nullable ItemStack itemStack;
	private @Nullable String skyblockId;
	private @Nullable String skyblockApiId;
	private @Nullable String neuName;
	private @Nullable String uuid;
	private @Nullable List<String> loreStrings;
	private @Nullable List<ItemAbility> abilities;
	private @Nullable PetInfo petInfo;
	private @Nullable SkyblockItemRarity skyblockRarity;

	public FlexibleItemStack(ItemStackTemplate template) {
		this.template = Objects.requireNonNull(template);
	}

	@SuppressWarnings("deprecation")
	public FlexibleItemStack(Item item) {
		this(new ItemStackTemplate(item.builtInRegistryHolder(), 1, DataComponentPatchAccessor.invokeInit(new Reference2ObjectArrayMap<>())));
	}

	public FlexibleItemStack(ItemStack stack) {
		this(ItemStackTemplate.fromNonEmptyStack(stack));
	}


	@Override
	public Holder<Item> typeHolder() {
		return this.template.typeHolder();
	}

	/**
	 * {@return the associated component value, if available}
	 */
	@Override
	public <T> @Nullable T get(DataComponentType<? extends T> type) {
		if (this.itemStack != null) {
			return this.itemStack.get(type);
		} else {
			return this.template.components().get(FALLBACK_COMPONENT_GETTER, type);
		}
	}

	@Override
	public int count() {
		return this.template.count();
	}

	/**
	 * Updates the associated data component value.
	 */
	public <T> void set(DataComponentType<T> type, T value) {
		if (this.itemStack != null) {
			this.itemStack.set(type, value);
		}

		((DataComponentPatchAccessor) (Object) this.template.components()).getMap().put(type, Optional.of(value));
	}

	/**
	 * Applies the {@code patch} components on this instance.
	 */
	@SuppressWarnings("unchecked")
	public void applyComponents(DataComponentPatch patch) {
		for (Map.Entry<DataComponentType<?>, Optional<?>> entry : patch.entrySet()) {
			if (entry.getValue().isPresent()) {
				this.set((DataComponentType<Object>) entry.getKey(), (Object) entry.getValue().get());
			}
		}
	}

	public boolean isEmpty() {
		return this == EMPTY;
	}

	public FlexibleItemStack copy() {
		return this.copyWithCount(this.count());
	}

	// FIXME does not do a copy of the component patch
	public FlexibleItemStack copyWithCount(int count) {
		return new FlexibleItemStack(new ItemStackTemplate(this.template.typeHolder(), count, this.template.components()));
	}

	/**
	 * {@return the backing template}
	 */
	public ItemStackTemplate getTemplate() {
		return this.template;
	}

	/**
	 * {@return the {@code ItemStack} or null}
	 */
	public @Nullable ItemStack getStack() {
		this.tryCreateStack();

		return this.itemStack;
	}

	/**
	 * {@return the {@code ItemStack} or throws an exception}
	 *
	 * @throws NullPointerException if the player is not currently in a world or, if the item or its components are not bound
	 */
	public ItemStack getStackOrThrow() {
		this.tryCreateStack();

		return Objects.requireNonNull(this.itemStack, "Not in a world yet (no components bound)");
	}

	private void tryCreateStack() {
		Holder<Item> typeHolder = this.typeHolder();

		if (typeHolder.isBound() && typeHolder.areComponentsBound()) {
			if (this.itemStack == null) {
				this.itemStack = this.template.create();
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
