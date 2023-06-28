package me.xmrvizzy.skyblocker.skyblock.rift;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.utils.SlayerUtils;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class StakeIndicator {
    private static final Logger LOGGER = LoggerFactory.getLogger(StakeIndicator.class);

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(StakeIndicator::UpdateStake);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE)
                .then(literal("vampire")
                        .then(literal("logDisplayName").executes(context -> {
                            context.getSource().sendFeedback(SlayerUtils.GetSlayerEntity().getDisplayName());
                            LOGGER.info(SlayerUtils.GetSlayerEntity().getDisplayName().toString());

                            return 1;
                        }))
                        .then(literal("logHearts").executes(context -> {
                            context.getSource().sendFeedback(Text.of("Hearts:" + MinecraftClient.getInstance().player.getHealth()));

                            return 1;
                        }))
                        .then(literal("logALlArmorStands").executes(context -> {
                            var slayerEntity = SlayerUtils.GetSlayerEntity();
                            for (var entity : slayerEntity.getEntityWorld().getOtherEntities(slayerEntity, slayerEntity.getBoundingBox().expand(1F, 2.5F, 1F), x-> x instanceof ArmorStandEntity && x.hasCustomName())) {
                                context.getSource().sendFeedback(entity.getDisplayName());
                                LOGGER.info(entity.getDisplayName().toString());

                                if(entity.hasCustomName())
                                {
                                    LOGGER.info(entity.getCustomName().toString());
                                    context.getSource().sendFeedback(entity.getCustomName());
                                }
                            }
                            return 1;
                        })))));
    }
    private static long lastDisplayTime = 0;
    public static void UpdateStake(MinecraftClient client) {
        if(!Utils.isOnSkyblock()) return;
        if(!(Utils.getLocation().contains("Stillgore Château"))) return;
        //if(!SlayerUtils.getIsInSlayer()) return;
        var slayerEntity = SlayerUtils.GetSlayerEntity();
        if(slayerEntity != null) {
            LOGGER.info(slayerEntity.getDisplayName().toString());
            if(slayerEntity.getDisplayName().toString().contains("҉"))
            {
                if (System.currentTimeMillis() - lastDisplayTime > 2500) {
                    lastDisplayTime = System.currentTimeMillis();
                    client.inGameHud.setTitleTicks(0, 25, 5);
                    client.inGameHud.setTitle(Text.translatable("skyblocker.rift.stakeNow").formatted(Formatting.RED));
                    client.player.playSound(SoundEvent.of(new Identifier("minecraft", "entity.experience_orb.pickup")), 100f, 0.1f);
                }
            }
        }
    }
}
