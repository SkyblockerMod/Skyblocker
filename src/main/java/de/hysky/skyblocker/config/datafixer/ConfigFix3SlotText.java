package de.hysky.skyblocker.config.datafixer;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class ConfigFix3SlotText extends ConfigDataFix {
    public ConfigFix3SlotText(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return fixTypeEverywhereTyped(
                "ConfigFix3SlotText",
                getInputSchema().getType(ConfigDataFixer.CONFIG_TYPE),
                typed -> typed.update(DSL.remainderFinder(), this::fix)
        );
    }

    private <T> Dynamic<T> fix(Dynamic<T> dynamic) {
        return fixVersion(dynamic).update("general", general -> general.update("itemInfoDisplay", itemInfoDisplay -> itemInfoDisplay.update("slotText", slotText -> slotText.createString(slotText.asBoolean(true) ? "ENABLED" : "DISABLED"))));
    }
}
