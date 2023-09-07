package me.xmrvizzy.skyblocker.compatibility.modmenu;

import me.shedaniel.autoconfig.AutoConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ModMenuEntry implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screen -> AutoConfig.getConfigScreen(SkyblockerConfig.class, screen).get();
    }
}