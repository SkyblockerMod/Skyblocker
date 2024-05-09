package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.skyblock.garden.FarmingHudConfigScreen;
import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionGroup;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class FarmingCategory {

    public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("skyblocker.config.farming"))

                //Garden
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.farming.garden"))
                        .collapsed(false)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.farming.garden.enableHud"))
                                .binding(defaults.farming.garden.farmingHud.enableHud,
                                        () -> config.farming.garden.farmingHud.enableHud,
                                        newValue -> config.farming.garden.farmingHud.enableHud = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(ButtonOption.createBuilder()
                                .name(Text.translatable("skyblocker.config.farming.garden.farmingHud"))
                                .text(Text.translatable("text.skyblocker.open"))
                                .action((screen, opt) -> MinecraftClient.getInstance().setScreen(new FarmingHudConfigScreen(screen)))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.farming.garden.dicerTitlePrevent"))
                                .binding(defaults.farming.garden.dicerTitlePrevent,
                                        () -> config.farming.garden.dicerTitlePrevent,
                                        newValue -> config.farming.garden.dicerTitlePrevent = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.farming.garden.visitorHelper"))
                                .binding(defaults.farming.garden.visitorHelper,
                                        () -> config.farming.garden.visitorHelper,
                                        newValue -> config.farming.garden.visitorHelper = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.farming.garden.lockMouseTool"))
                                .binding(defaults.farming.garden.lockMouseTool,
                                        () -> config.farming.garden.lockMouseTool,
                                        newValue -> config.farming.garden.lockMouseTool = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.farming.garden.lockMouseGround"))
                                .binding(defaults.farming.garden.lockMouseGroundOnly,
                                        () -> config.farming.garden.lockMouseGroundOnly,
                                        newValue -> config.farming.garden.lockMouseGroundOnly = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())
                .build();
    }
}
