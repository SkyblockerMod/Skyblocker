package me.xmrvizzy.skyblocker.config;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer;

@Config(name = "skyblocker")
public class SkyblockerConfig implements ConfigData {

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.TransitiveObject
    public General general = new General();

    @ConfigEntry.Category("bars")
    @ConfigEntry.Gui.TransitiveObject
    public Bars bars = new Bars();

    @ConfigEntry.Category("messages")
    @ConfigEntry.Gui.TransitiveObject
    public Messages messages = new Messages();

    public static class General {
        public String apiKey;
    }

    public static class Bars {
        public boolean enableBars = true;
        public boolean enableAbsorption = true;
        @ConfigEntry.ColorPicker()
        public int absorbedHealthColor = 0xffaa00;
        @ConfigEntry.ColorPicker()
        public int healthColor = 0xff5555;
        @ConfigEntry.ColorPicker()
        public int manaColor = 0x55ffff;
    }

    public static class Messages {
        public boolean hideAbility = false;
        public boolean hideHeal = false;
        public boolean hideAOTE = false;
        public boolean hideMidasStaff = false;
    }

    public static void init() {
        AutoConfig.register(SkyblockerConfig.class, GsonConfigSerializer::new);
    }

    public static SkyblockerConfig get() {
        return AutoConfig.getConfigHolder(SkyblockerConfig.class).getConfig();
    }
}