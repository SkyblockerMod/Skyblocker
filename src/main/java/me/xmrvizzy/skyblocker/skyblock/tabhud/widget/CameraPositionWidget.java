package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

public class CameraPositionWidget extends Widget {
	private static final MutableText TITLE = Text.literal("Camera Pos").formatted(Formatting.DARK_PURPLE, Formatting.BOLD);
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	public CameraPositionWidget() {
		super(TITLE, Formatting.DARK_PURPLE.getColorValue());
		
		double yaw = CLIENT.getCameraEntity().getYaw();
		double pitch = CLIENT.getCameraEntity().getPitch();
		
		this.addComponent(new PlainTextComponent(Text.literal("Yaw: " + roundToDecimalPlaces(MathHelper.wrapDegrees(yaw), 3))));
		this.addComponent(new PlainTextComponent(Text.literal("Pitch: " + roundToDecimalPlaces(MathHelper.wrapDegrees(pitch), 3))));
		
		this.pack();
	}
	
	//https://stackoverflow.com/a/33889423
	private static double roundToDecimalPlaces(double value, int decimalPlaces) {
		double shift = Math.pow(10, decimalPlaces);
		
		return Math.round(value * shift) / shift;
	}
}
