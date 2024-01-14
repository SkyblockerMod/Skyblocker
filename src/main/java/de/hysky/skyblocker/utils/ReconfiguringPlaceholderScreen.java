package de.hysky.skyblocker.utils;

import net.minecraft.network.ClientConnection;
import net.minecraft.text.Text;

public final class ReconfiguringPlaceholderScreen extends BasePlaceholderScreen {
    private final ClientConnection connection;

    public ReconfiguringPlaceholderScreen(final ClientConnection connection) {
        super(Text.translatable("connect.reconfiguring"));
        this.connection = connection;
    }

    @Override
    public void tick() {
        if (this.connection.isOpen()) {
            this.connection.tick();
        } else {
            this.connection.handleDisconnection();
        }
    }
}
