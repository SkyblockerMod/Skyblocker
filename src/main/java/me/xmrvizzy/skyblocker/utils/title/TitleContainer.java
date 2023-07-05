package me.xmrvizzy.skyblocker.utils.title;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.skyblock.FairySouls;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class TitleContainer {
    public static List<Title> titles = new ArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger("skyblocker");
    public static void init() {
        HudRenderCallback.EVENT.register(TitleContainer::draw);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE)
                .then(literal("title")
                        .then(literal("ice").executes(context -> {
                            titles.add(new Title(Text.translatable("skyblocker.rift.iceNow").getString(), Formatting.AQUA.getColorValue()));
                            return 1;
                        }))
                        .then(literal("stake").executes(context -> {
                            titles.add(new Title("Stake", Formatting.RED.getColorValue()));
                            return 1;
                        }))
                        .then(literal("heal").executes(context -> {
                            titles.add(new Title("Heal", Formatting.DARK_RED.getColorValue()));
                            return 1;
                        }))
                )));
    }
    public static void addTitle(Title title)
    {
        title.active = true;
        title.lastX = 0;
        titles.add(title);
    }
    public static void draw(DrawContext context, float tickDelta) {
        var client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        List<Title> toRemove = new ArrayList<>();

        float x;
        float width = 0;
        context.getMatrices().push();
        context.getMatrices().scale(3.0F, 3.0F, 3.0F);
        for (Title title : titles) {
            width += textRenderer.getWidth(title.text) * 3F + 10;
        }
        context.getMatrices().pop();

        x = (client.getWindow().getScaledWidth() / 2) - width / 2;

        for (Title title : titles) {
            context.getMatrices().push();
            context.getMatrices().translate(title.lastX, client.getWindow().getScaledHeight() * 0.7F, 0);
            context.getMatrices().scale(3.0F, 3.0F, 3.0F);

            title.lastX = MathHelper.lerp(tickDelta * 0.5F, title.lastX, x);
            x += textRenderer.getWidth(title.text) * 3F + 10;
            context.drawText(textRenderer, title.text, 0, 0, title.color, true
            );
            context.getMatrices().pop();
            if (!title.active) {
                toRemove.add(title);
            }
        }
        titles.removeAll(toRemove);
    }
}