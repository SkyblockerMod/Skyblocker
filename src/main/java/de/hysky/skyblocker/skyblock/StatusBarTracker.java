package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StatusBarTracker {
    private static final Pattern STATUS_HEALTH = Pattern.compile("§[6c](\\d+(,\\d\\d\\d)*)/(\\d+(,\\d\\d\\d)*)❤(?:(\\+§c(\\d+(,\\d\\d\\d)*). *)| *)");
    private static final Pattern DEFENSE_STATUS = Pattern.compile("§a(\\d+(,\\d\\d\\d)*)§a❈ Defense *");
    private static final Pattern MANA_USE = Pattern.compile("§b-(\\d+(,\\d\\d\\d)*) Mana \\(§\\S+(?:\\s\\S+)* *");
    private static final Pattern MANA_STATUS = Pattern.compile("§b(\\d+(,\\d\\d\\d)*)/(\\d+(,\\d\\d\\d)*)✎ (?:Mana|§3(\\d+(,\\d\\d\\d)*)ʬ) *");

    private Resource health = new Resource(100, 100, 0);
    private Resource mana = new Resource(100, 100, 0);
    private int defense = 0;

    public void init() {
        ClientReceiveMessageEvents.ALLOW_GAME.register(this::allowOverlayMessage);
        ClientReceiveMessageEvents.MODIFY_GAME.register(this::onOverlayMessage);
    }

    public Resource getHealth() {
        return this.health;
    }

    public Resource getMana() {
        return this.mana;
    }

    public int getDefense() {
        return this.defense;
    }

    private int parseInt(Matcher m, int group) {
        return Integer.parseInt(m.group(group).replace(",", ""));
    }

    private void updateMana(Matcher m) {
        int value = parseInt(m, 1);
        int max = parseInt(m, 3);
        int overflow = m.group(5) == null ? 0 : parseInt(m, 5);
        this.mana = new Resource(value, max, overflow);
    }

    private void updateHealth(Matcher m) {
        int value = parseInt(m, 1);
        int max = parseInt(m, 3);
        int overflow = Math.max(0, value - max);
        if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().player != null) {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            value = (int) (player.getHealth() * max / player.getMaxHealth());
            overflow = (int) (player.getAbsorptionAmount() * max / player.getMaxHealth());
        }
        this.health = new Resource(Math.min(value, max), max, Math.min(overflow, max));
    }

    private String reset(String str, Matcher m) {
        str = str.substring(m.end());
        m.reset(str);
        return str;
    }

    private boolean allowOverlayMessage(Text text, boolean overlay) {
        onOverlayMessage(text, overlay);
        return true;
    }

    private Text onOverlayMessage(Text text, boolean overlay) {
        if (!overlay || !Utils.isOnSkyblock() || !SkyblockerConfigManager.get().general.bars.enableBars || Utils.isInTheRift()) {
            return text;
        }
        return Text.of(update(text.getString(), SkyblockerConfigManager.get().messages.hideMana));
    }

    public String update(String actionBar, boolean filterManaUse) {
        var sb = new StringBuilder();
        Matcher matcher = STATUS_HEALTH.matcher(actionBar);
        if (!matcher.lookingAt())
            return actionBar;
        updateHealth(matcher);
        if (matcher.group(5) != null) {
            sb.append("§c❤");
            sb.append(matcher.group(5));
        }
        actionBar = reset(actionBar, matcher);
        if (matcher.usePattern(MANA_STATUS).lookingAt()) {
            defense = 0;
            updateMana(matcher);
            actionBar = reset(actionBar, matcher);
        } else {
            if (matcher.usePattern(DEFENSE_STATUS).lookingAt()) {
                defense = parseInt(matcher, 1);
                actionBar = reset(actionBar, matcher);
            } else if (filterManaUse && matcher.usePattern(MANA_USE).lookingAt()) {
                actionBar = reset(actionBar, matcher);
            }
            if (matcher.usePattern(MANA_STATUS).find()) {
                updateMana(matcher);
                matcher.appendReplacement(sb, "");
            }
        }
        matcher.appendTail(sb);
        String res = sb.toString().trim();
        return res.isEmpty() ? null : res;
    }

    public record Resource(int value, int max, int overflow) {
    }
}
