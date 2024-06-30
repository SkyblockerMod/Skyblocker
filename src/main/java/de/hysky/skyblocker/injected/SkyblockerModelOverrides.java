package de.hysky.skyblocker.injected;

import org.jetbrains.annotations.Nullable;

import de.hysky.skyblocker.stp.predicates.SkyblockerTexturePredicate;

public interface SkyblockerModelOverrides {

	default void setItemPredicates(SkyblockerTexturePredicate[] predicates) {
	}

	@Nullable
	default SkyblockerTexturePredicate[] getItemPredicates() {
		return null;
	}
}
