package de.hysky.skyblocker.utils.render.gui;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Same as {@link HandlerBackedScreen} but with support for a sign. Useful for when a Hypixel screen has a button to set a value
 */
public abstract class HandlerSignBackedScreen extends HandlerBackedScreen{

    public HandlerSignBackedScreen(Text title, Text inventoryName, GenericContainerScreenHandler handler) {
        super(title, inventoryName, handler);
    }

    protected boolean signFront = true;
    protected @Nullable SignBlockEntity sign = null;
    protected boolean packetSent = true;

    protected Backend currentBackend = Backend.INVENTORY;

    public void changeSign(@NotNull SignBlockEntity newSign, boolean isFront) {
        this.sign = newSign;
        this.signFront = isFront;
        currentBackend = Backend.SIGN;
        packetSent = false;
        onSignUpdate();
        setWaitingForServer(false);

    }

    protected abstract void onSignUpdate();

    @Override
    public void changeHandlerAndMarkDirty(GenericContainerScreenHandler newHandler, Text newTitle) {
        super.changeHandlerAndMarkDirty(newHandler, newTitle);
        currentBackend = Backend.INVENTORY;
    }

    @Override
    public void removed() {
        if (currentBackend == Backend.INVENTORY) super.removed();
        else sendSignPacket(new String[]{});
    }

    @Override
    public void close() {
        if (currentBackend == Backend.INVENTORY)
            super.close();
        else {
            this.client.setScreen(null);
            sendSignPacket(new String[]{});
        }
    }

    @Override
    public void tick() {
        if (currentBackend == Backend.INVENTORY) super.tick();
    }

    /**
     * Sends the packet to update the sign. The packet will only be sent once, future calls (while in the same Sign handler state) will be ignored
     * @param lines an array of at most 4 strings. If less than 4 strings the missing strings will be set from the sign's original text.
     */
    public void sendSignPacket(String[] lines) {
        if (packetSent || sign == null) return;
        List<String> strings = new ArrayList<>(4);
        Text[] messages = sign.getText(signFront).getMessages(MinecraftClient.getInstance().shouldFilterText());
        for (int i = 0; i < 4; i++) {
            if (i >= lines.length || lines[i] == null) {
                strings.add(messages[i].getString());
                continue;
            }
            strings.add(lines[i]);
        }
        assert MinecraftClient.getInstance().player != null;
        MinecraftClient.getInstance().player.networkHandler.sendPacket(new UpdateSignC2SPacket(sign.getPos(), signFront,
                strings.get(0),
                strings.get(1),
                strings.get(2),
                strings.get(3)
        ));
        packetSent = true;

    }

    public void sendSignPacket(String line1) {
        sendSignPacket(new String[]{line1});
    }

    public enum Backend {
        INVENTORY,
        SIGN
    }
}
