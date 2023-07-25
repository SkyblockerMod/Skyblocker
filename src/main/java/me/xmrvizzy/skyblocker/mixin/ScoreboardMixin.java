package me.xmrvizzy.skyblocker.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.scoreboard.Scoreboard;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Scoreboard.class)
public abstract class ScoreboardMixin {
    @WrapWithCondition(method = "addTeam", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V"))
    private boolean skyblocker$cancelTeamWarning(Logger instance, String format, Object arg) {
        return !Utils.isOnHypixel();
    }
}
