package de.hysky.skyblocker.config.datafixer;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.Identifier;

public class ConfigFix2QuickNav extends ConfigDataFix {
    public ConfigFix2QuickNav(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return fixTypeEverywhereTyped(
                "ConfigFix2QuickNav",
                getInputSchema().getType(ConfigDataFixer.CONFIG_TYPE),
                typed -> typed.update(DSL.remainderFinder(), this::fix)
        );
    }

    private <T> Dynamic<T> fix(Dynamic<T> dynamic) {
        return fixVersion(dynamic).update("quickNav", quickNav -> quickNav
                .renameField("button12", "button13")
                .renameField("button11", "button12")
                .renameField("button10", "button11")
                .renameField("button9", "button10")
                .renameField("button8", "button9")
                .renameField("button7", "button8")
                .updateMapValues(button -> button.getFirst().asString().getOrThrow().startsWith("button") ? button.mapSecond(this::fixButton) : button)
        );
    }

    private <T> Dynamic<T> fixButton(Dynamic<T> button) {
        return button.renameAndFixField("item", "itemData", itemData -> itemData.renameAndFixField("id", "item", id -> id.createString(Identifier.of(id.asString().getOrThrow()).toString())));
    }
}
