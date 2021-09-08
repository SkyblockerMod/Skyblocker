package me.xmrvizzy.skyblocker.skyblock;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ChatFilter {
    private static final Pattern ability;
    private static final Pattern heal;
    private static final Pattern aote;
    private static final Pattern implosion;
    private static final Pattern moltenWave;
    private static final Pattern playerMessage;
    private static final Pattern adverts;

    public boolean shouldFilter(String message) {
        SkyblockerConfig.Messages settings = SkyblockerConfig.get().messages;
        if (settings.hideAbility && ability.matcher(message).matches())
            return true;

        if (settings.hideHeal && heal.matcher(message).matches())
            return true;

        if (settings.hideAOTE && aote.matcher(message).matches())
            return true;

        if (settings.hideImplosion && implosion.matcher(message).matches())
            return true;

        if (settings.hideMoltenWave && moltenWave.matcher(message).matches())
            return true;

        if (settings.hideAds) {
            Matcher m = playerMessage.matcher(message);
            if(m.matches() && adverts.matcher(m.group(2)).find()) {
                System.out.println(m.group(2));
                return true;
            }
        }
        return false;
    }

    static {
        String number = "-?[0-9]{1,3}(?>,[0-9]{3})*(?:\\.[1-9])?";
        ability = Pattern.compile("^(?:This ability is on cooldown for " + number + "s\\.|No more charges, next one in " + number + "s!)$");
        heal = Pattern.compile("^(?:You healed yourself for " + number + " health!|[a-zA-Z0-9_]{2,16} healed you for " + number + " health!)$");
        aote = Pattern.compile("^There are blocks in the way!$");
        implosion = Pattern.compile("^Your Implosion hit " + number + " enem(?:y|ies) for " + number + " damage\\.$");
        moltenWave = Pattern.compile("^Your Molten Wave hit " + number + " enemy(?:y|ies) for " + number + " damage\\.$");
        playerMessage = Pattern.compile("^§[67ab](?:\\[(?:MVP|VIP)(?:§[0-9a-f]\\+{1,2}§[6ab])?] )?([a-zA-Z0-9_]{2,16})§[7f]: (.*)$");
        adverts = Pattern.compile("(?i:^(?:i(?:m|'m| am)? |(?:is )?any(?: ?one|1) )?(?:buy|sell|lowball|trade?)(?:ing)?\\W|(?:\\W|^)(?:on|in|check|at) my ah(?:\\W|$)|(.)\\1{7,})");
    }
}
