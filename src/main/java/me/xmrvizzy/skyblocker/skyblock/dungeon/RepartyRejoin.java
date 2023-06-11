package me.xmrvizzy.skyblocker.skyblock.dungeon;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.chat.ChatFilterResult;
import me.xmrvizzy.skyblocker.chat.ChatPatternListener;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.regex.Matcher;

public class RepartyRejoin extends ChatPatternListener {

    private static final SkyblockerMod skyblocker = SkyblockerMod.getInstance();
    private static final MinecraftClient client = MinecraftClient.getInstance();

    private boolean repartying;


    public RepartyRejoin(){
        super("^(?:That party has been disbanded\\.|([\\[A-z+\\]]* )?(?<name>[A-z0-9_]*) has disbanded the party!)");
        this.repartying = false;
    }

    @Override
    protected ChatFilterResult state() {
        return SkyblockerConfig.get().locations.dungeons.repartyRejoin ? ChatFilterResult.FILTER : ChatFilterResult.PASS;
    }

    @Override
    protected boolean onMatch(Text message, Matcher matcher) {
        if (matcher.group("name") != null && !matcher.group("name").equals(client.getSession().getUsername())) {
            this.repartying = true;
            join(matcher.group("name"));
            return false;
        } else if ( repartying ) {
            repartying = false;
            return true;
        }
        return false;
    }

    private void join(String player){
        String command = "/party accept " + player;
        sendCommand(command);
        skyblocker.scheduler.schedule(() -> this.repartying = false, 150);
    }

    private void sendCommand(String command) {
        skyblocker.messageScheduler.queueMessage(command, 15 );
    }
}
