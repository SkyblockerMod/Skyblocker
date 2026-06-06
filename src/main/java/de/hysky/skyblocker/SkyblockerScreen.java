package de.hysky.skyblocker;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.Tips;
import de.hysky.skyblocker.utils.FunUtils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FormattedCharSequence;

public class SkyblockerScreen extends Screen {
	private static final int SPACING = 8;
	private static final int BUTTON_WIDTH = 210;
	private static final int HALF_BUTTON_WIDTH = (BUTTON_WIDTH - SPACING) / 2;
	private static final Component TITLE;
	private static final Identifier ICON;
	private static final Component CONFIGURATION_TEXT = Component.translatable("text.skyblocker.config");
	private static final Component SOURCE_TEXT = Component.translatable("text.skyblocker.source");
	private static final Component REPORT_BUGS_TEXT = Component.translatable("menu.reportBugs");
	private static final Component WEBSITE_TEXT = Component.translatable("text.skyblocker.website");
	private static final Component TRANSLATE_TEXT = Component.translatable("text.skyblocker.translate");
	private static final Component MODRINTH_TEXT = Component.translatable("text.skyblocker.modrinth");
	private static final Component DISCORD_TEXT = Component.translatable("text.skyblocker.discord");
	private static final Component SUPPORT_US_TEXT = Component.translatable("text.skyblocker.supportUs");
	private static final Component CREDITS_TEXT = Component.translatable("credits_and_attribution.button.credits");
	private HeaderAndFooterLayout layout;
	private MultiLineTextWidget tip;

	static {
		if (FunUtils.shouldEnableFun()) {
			TITLE = Component.literal("Skibidiblocker " + SkyblockerMod.VERSION);
			ICON = SkyblockerMod.id("icons.png");
		} else {
			TITLE = Component.literal("Skyblocker " + SkyblockerMod.VERSION);
			ICON = SkyblockerMod.id("icon.png");
		}
	}

	public SkyblockerScreen() {
		super(TITLE);
	}

	@Init
	public static void initClass() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) -> {
			dispatcher.register(ClientCommands.literal(SkyblockerMod.NAMESPACE)
					.executes(Scheduler.queueOpenScreenCommand(SkyblockerScreen::new)));
		});
	}

	@Override
	protected void init() {
		// Slightly larger footer than header to move body content higher
		// because the body content is positioned at footer height + 30 for some reason
		this.layout = new HeaderAndFooterLayout(this, height < 280 ? 48 : 64, height < 280 ? 64 : 100);
		this.layout.addToHeader(new IconTextWidget(this.getTitle(), this.font, ICON));

		GridLayout gridWidget = this.layout.addToContents(new GridLayout()).spacing(SPACING);
		if (height < 320) gridWidget = gridWidget.rowSpacing(4); // 320 is default height at gui scale 3
		gridWidget.defaultCellSetting().alignHorizontallyCenter();
		GridLayout.RowHelper adder = gridWidget.createRowHelper(2);

		adder.addChild(Button.builder(CONFIGURATION_TEXT, _ -> this.openConfig()).width(BUTTON_WIDTH).build(), 2);
		adder.addChild(Button.builder(SOURCE_TEXT, ConfirmLinkScreen.confirmLink(this, "https://github.com/SkyblockerMod/Skyblocker")).width(HALF_BUTTON_WIDTH).build());
		adder.addChild(Button.builder(REPORT_BUGS_TEXT, ConfirmLinkScreen.confirmLink(this, "https://github.com/SkyblockerMod/Skyblocker/issues")).width(HALF_BUTTON_WIDTH).build());
		adder.addChild(Button.builder(WEBSITE_TEXT, ConfirmLinkScreen.confirmLink(this, "https://hysky.de/")).width(HALF_BUTTON_WIDTH).build());
		adder.addChild(Button.builder(TRANSLATE_TEXT, ConfirmLinkScreen.confirmLink(this, "https://translate.hysky.de/")).width(HALF_BUTTON_WIDTH).build());
		adder.addChild(Button.builder(MODRINTH_TEXT, ConfirmLinkScreen.confirmLink(this, "https://modrinth.com/mod/skyblocker-liap")).width(HALF_BUTTON_WIDTH).build());
		adder.addChild(Button.builder(DISCORD_TEXT, ConfirmLinkScreen.confirmLink(this, "https://discord.gg/aNNJHQykck")).width(HALF_BUTTON_WIDTH).build());
		adder.addChild(Button.builder(SUPPORT_US_TEXT, ConfirmLinkScreen.confirmLink(this, "https://hysky.de/skyblocker/team")).width(HALF_BUTTON_WIDTH).build());
		adder.addChild(Button.builder(CREDITS_TEXT, _ -> this.minecraft.setScreen(new SkyblockerCreditsScreen(this))).width(HALF_BUTTON_WIDTH).build());
		adder.addChild(Button.builder(CommonComponents.GUI_DONE, _ -> this.onClose()).width(BUTTON_WIDTH).build(), 2);

		GridLayout footerGridWidget = this.layout.addToFooter(new GridLayout()).columnSpacing(SPACING).rowSpacing(0);
		footerGridWidget.defaultCellSetting().alignHorizontallyCenter();
		GridLayout.RowHelper footerAdder = footerGridWidget.createRowHelper(2);
		footerAdder.addChild(tip = new MultiLineTextWidget(Tips.nextTip(), this.font).setCentered(true).setMaxWidth((int) (this.width * 0.8)), 2);
		footerAdder.addChild(Button.builder(Component.translatable("skyblocker.tips.previous"), _ -> {
			tip.setMessage(Tips.previousTip());
			layout.arrangeElements();
		}).width(HALF_BUTTON_WIDTH).build());
		footerAdder.addChild(Button.builder(Component.translatable("skyblocker.tips.next"), _ -> {
			tip.setMessage(Tips.nextTip());
			layout.arrangeElements();
		}).width(HALF_BUTTON_WIDTH).build());

		this.layout.arrangeElements();
		this.layout.visitWidgets(this::addRenderableWidget);
	}

	@Override
	protected void repositionElements() {
		super.repositionElements();
		this.layout.arrangeElements();
	}

	private void openConfig() {
		this.minecraft.setScreen(SkyblockerConfigManager.createGUI(this));
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		super.extractRenderState(context, mouseX, mouseY, delta);
	}

	private static class IconTextWidget extends StringWidget {
		private static final int ICON_SIZE = 32;
		private static final int SPACING = 2;
		private final Identifier icon;

		IconTextWidget(Component message, Font textRenderer, Identifier icon) {
			super(textRenderer.width(message.getVisualOrderText()) + ICON_SIZE + SPACING, ICON_SIZE, message, textRenderer);
			this.icon = icon;
		}

		@Override
		public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
			Component text = this.getMessage();
			Font textRenderer = this.getFont();

			int width = this.getWidth();
			int textWidth = textRenderer.width(text.getVisualOrderText());
			float horizontalAlignment = 0.5f; // default
			//17 = (32 + 2) / 2 • 32 + 2 is the width of the icon + spacing between icon and text
			int x = this.getX() + (ICON_SIZE + SPACING) / 2 + Math.round(horizontalAlignment * (float) (width - textWidth));
			int y = this.getY() + (this.getHeight() - textRenderer.lineHeight) / 2;
			FormattedCharSequence orderedText = textWidth > width ? this.trim(text, width) : text.getVisualOrderText();

			int iconX = x - ICON_SIZE - SPACING;
			int iconY = y - 13;

			graphics.text(textRenderer, orderedText, x, y, CommonColors.WHITE);
			graphics.blit(RenderPipelines.GUI_TEXTURED, this.icon, iconX, iconY, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
		}

		private FormattedCharSequence trim(Component text, int width) {
			Font textRenderer = this.getFont();
			FormattedText stringVisitable = textRenderer.substrByWidth(text, width - textRenderer.width(CommonComponents.ELLIPSIS));
			return Language.getInstance().getVisualOrder(FormattedText.composite(stringVisitable, CommonComponents.ELLIPSIS));
		}
	}
}
