package de.hysky.skyblocker.skyblock.profileviewer.rework.pages;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.profileviewer.rework.ProfileLoadState;
import de.hysky.skyblocker.skyblock.profileviewer.rework.ProfileViewerWidget;
import de.hysky.skyblocker.skyblock.profileviewer.utils.ProfileViewerUtils;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class PlayerMetaWidget implements ProfileViewerWidget {
	public static final Identifier BACKGROUND = Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/profile_viewer/basic_page_body_widget.png");
	public static final int WIDTH = 82, HEIGHT = 54;
	TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
	private static final int ROW_GAP = 9;

	final int skyblockExperience;
	final Text profileName;
	final int skyblockLevel;
	final double purse;
	final @Nullable Double personalBank;
	final double coopBank;
	final double totalBank;

	public PlayerMetaWidget(ProfileLoadState.SuccessfulLoad load) {
		this.profileName = Text.literal(load.profile().cuteName).formatted(Formatting.UNDERLINE);
		this.skyblockExperience = load.member().leveling.experience;
		this.skyblockLevel = skyblockExperience / 100;
		this.purse = load.member().currencies.coinsInPurse;
		this.personalBank = load.member().profile.personalBankAccount;
		this.coopBank = load.profile().banking.balance;
		this.totalBank = personalBank != null ? personalBank + coopBank : coopBank;
		// TODO: networth
	}

	@Override
	public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, float deltaTicks) {
		drawContext.drawTexture(RenderPipelines.GUI_TEXTURED, BACKGROUND, x, y, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);
		var matrices = drawContext.getMatrices();
		matrices.pushMatrix();
		matrices.translate(x, y);
		matrices.scale(0.75F, 0.75F);
		drawContext.drawItem(Ico.PAINTING, 0, 0);
		matrices.popMatrix();
		drawContext.drawText(textRenderer, profileName, x + 14, y + 3, Colors.WHITE, true);
		drawContext.drawText(textRenderer, Text.literal("Level: ")
						.formatted(Formatting.GREEN)
						.append(Text.literal(skyblockLevel + "")
								.formatted(Formatting.WHITE)),
				x + 2, y + 6 + ROW_GAP, Colors.WHITE, true);
		drawContext.drawText(textRenderer, Text.literal("Purse: ")
						.formatted(Formatting.GOLD)
						.append(Text.literal(ProfileViewerUtils.numLetterFormat(purse))
								.formatted(Formatting.WHITE)),
				x + 2, y + 6 + ROW_GAP * 2, Colors.WHITE, true);
		var bankText = Text.literal("Bank: ")
				.formatted(Formatting.GOLD)
				.append(Text.literal(ProfileViewerUtils.numLetterFormat(totalBank))
						.formatted(Formatting.WHITE));
		drawContext.drawText(textRenderer, bankText, x + 2, y + 6 + ROW_GAP * 3, Colors.WHITE, true);
		if (isHovered(x + 2, y + 6 + ROW_GAP * 3, textRenderer.getWidth(bankText), textRenderer.fontHeight, mouseX, mouseY)) {
			var tooltip = new ArrayList<Text>();
			tooltip.add(Text.literal("Main Bank: ")
					.formatted(Formatting.GOLD)
					.append(Text.literal(ProfileViewerUtils.numLetterFormat(totalBank))
							.formatted(Formatting.WHITE)));
			if (personalBank != null) // TODO: bank upgrades (completed_tasks.BANK_UPGRADE_SUPER_DELUXE), interest!
				tooltip.add(Text.literal("Personal Bank: ")
						.formatted(Formatting.GOLD)
						.append(Text.literal(ProfileViewerUtils.numLetterFormat(personalBank))
								.formatted(Formatting.WHITE)));
			drawContext.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
		}
		// TODO: networth
	}

	@Override
	public int getHeight() {
		return HEIGHT;
	}

	@Override
	public int getWidth() {
		return WIDTH;
	}
}
