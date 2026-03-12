package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.skyblock.tabchat.ChatTabButton;
import de.hysky.skyblocker.skyblock.tabchat.TabChat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {
		@Shadow
		protected EditBox input;

		@Unique
		private final List<ChatTabButton> tabchat$buttons = new ArrayList<>();

		protected ChatScreenMixin(Component title) {
				super(title);
		}

		@Inject(method = "init", at = @At("TAIL"))
		private void tabchat$addButtons(CallbackInfo ci) {
				tabchat$buttons.clear();
				if (!TabChat.isEnabled()) return;

				int x = TabChat.getButtonX(this.width);
				int y = TabChat.getButtonY(this.height);
				int w = TabChat.BUTTON_WIDTH;
				int h = TabChat.BUTTON_HEIGHT;
				int gap = TabChat.BUTTON_GAP;

				tabchat$buttons.add(new ChatTabButton(x, y, w, h, "skyblocker.tabChat.guild", "guild", "chat guild"));
				tabchat$buttons.add(new ChatTabButton(x + w + gap, y, w, h, "skyblocker.tabChat.all", "all", "chat all"));
				tabchat$buttons.add(new ChatTabButton(x + 2 * (w + gap), y, w, h, "skyblocker.tabChat.party", "party", "chat party"));
		}

		@Inject(method = "render", at = @At("TAIL"))
		private void tabchat$renderButtons(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
				for (ChatTabButton btn : tabchat$buttons) {
						btn.render(graphics, mouseX, mouseY);
				}
		}

		@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
		private void tabchat$handleClick(MouseButtonEvent click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
				if (click.button() != 0) return;
				for (ChatTabButton btn : tabchat$buttons) {
						if (btn.isMouseOver(click.x(), click.y())) {
								btn.handleClick();
								cir.setReturnValue(true);
								return;
						}
				}
		}
}
