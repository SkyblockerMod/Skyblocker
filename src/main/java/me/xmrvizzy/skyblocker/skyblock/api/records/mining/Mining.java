package me.xmrvizzy.skyblocker.skyblock.api.records.mining;

import com.google.gson.annotations.SerializedName;
import me.xmrvizzy.skyblocker.skyblock.api.records.PlayerProfiles;

import java.util.HashMap;

public record Mining(
        Commissions commissions,
        Forge forge,
        Core core
){
    public record Forge(Process[] processes){
        public record Process(
                String id,
                int slot,
                long timeFinished,
                String timeFinishedText,
                String name
        ){}
    }
    public record Commissions(int milestone){}
}
