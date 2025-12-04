package de.hysky.skyblocker.config.datafixer;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public abstract class ConfigDataFix extends DataFix {
	public ConfigDataFix(Schema outputSchema, boolean changesType) {
		super(outputSchema, changesType);
	}

	protected <T> Dynamic<T> fixVersion(Dynamic<T> dynamic) {
		return dynamic.set("version", dynamic.createInt(DataFixUtils.getVersion(getVersionKey())));
	}
}
