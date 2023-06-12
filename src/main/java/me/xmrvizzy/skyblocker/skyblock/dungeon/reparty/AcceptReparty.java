package me.xmrvizzy.skyblocker.skyblock.dungeon.reparty;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.chat.ChatFilterResult;
import me.xmrvizzy.skyblocker.chat.ChatPatternListener;
import net.minecraft.text.Text;

import java.util.regex.Matcher;

import static me.xmrvizzy.skyblocker.skyblock.dungeon.reparty.Reparty.partyLeader;

public class AcceptReparty extends ChatPatternListener {

    private static final SkyblockerMod skyblocker = SkyblockerMod.getInstance();

    public AcceptReparty() {
        super("-----------------------------------------------------" +
                "\n([\\[A-z+\\]]* )?(?<name>[A-z0-9_]*) has invited you to join their party!" +
                "\nYou have 60 seconds to accept. Click here to join!" +
                "\n-----------------------------------------------------");
    }

    @Override
    protected ChatFilterResult state() {
        return (partyLeader != null) ? ChatFilterResult.FILTER : ChatFilterResult.PASS;
    }

    @Override
    protected boolean onMatch(Text message, Matcher matcher) {
        try {
            if (matcher.group("name").equals(partyLeader)) {
                skyblocker.messageScheduler.sendMessageAfterCooldown("/party accept " + partyLeader);
            }
        } catch (NullPointerException e) {
            // In case if block executes after setting "partyLeader" variable to null
        }
        partyLeader = null;
        return false;
    }
}
