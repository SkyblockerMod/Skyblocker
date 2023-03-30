package me.xmrvizzy.skyblocker.skyblock.api.records.mining;

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
