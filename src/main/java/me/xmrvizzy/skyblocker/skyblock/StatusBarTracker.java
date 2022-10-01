package me.xmrvizzy.skyblocker.skyblock;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

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
        int overflow = 0;
        ClientPlayerEntity player = null;
        try {
            player = MinecraftClient.getInstance().player;
        }
        // Is triggered by tests. Couldn't come up with a better solution.
        catch (NullPointerException ignored) {
        }
        if (player != null) {
            int hp = (int) (player.getHealth() * max / player.getMaxHealth());
            overflow = value - hp;
            value = hp;
        } else if (value > max) {
            overflow = value - max;
            value = max;
        }
        if (overflow > max)
            overflow = max;
        this.health = new Resource(value, max, overflow);
    }

    private String reset(String str, Matcher m) {
        str = str.substring(m.end());
        m.reset(str);
        return str;
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

    public record Resource(
            int value,
            int max,
            int overflow
    ) {
    }
}
