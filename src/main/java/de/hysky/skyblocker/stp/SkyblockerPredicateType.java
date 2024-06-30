package de.hysky.skyblocker.stp;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.stp.predicates.SkyblockerTexturePredicate;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;

public record SkyblockerPredicateType<T extends SkyblockerTexturePredicate>(Codec<T> codec) {
	public static final Registry<SkyblockerPredicateType<?>> REGISTRY = new SimpleRegistry<>(
			RegistryKey.ofRegistry(Identifier.of(SkyblockerMod.NAMESPACE, "predicate_types")), Lifecycle.stable());
}
