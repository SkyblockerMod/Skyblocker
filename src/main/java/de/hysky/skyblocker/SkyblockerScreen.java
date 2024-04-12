package de.hysky.skyblocker;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.Tips;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;

public class SkyblockerScreen extends Screen {
	private static final int SPACING = 8;
	private static final int BUTTON_WIDTH = 210;
	private static final int HALF_BUTTON_WIDTH = 101; //Same as (210 - 8) / 2
	private static final Text TITLE = Text.literal("Skyblocker " + SkyblockerMod.VERSION);
	private static final Identifier ICON = new Identifier(SkyblockerMod.NAMESPACE, "icon.png");
	private static final Text CONFIGURATION_TEXT = Text.translatable("text.skyblocker.config");
	private static final Text SOURCE_TEXT = Text.translatable("text.skyblocker.source");
	private static final Text REPORT_BUGS_TEXT = Text.translatable("menu.reportBugs");
	private static final Text WEBSITE_TEXT = Text.translatable("text.skyblocker.website");
	private static final Text TRANSLATE_TEXT = Text.translatable("text.skyblocker.translate");
	private static final Text MODRINTH_TEXT = Text.translatable("text.skyblocker.modrinth");
	private static final Text DISCORD_TEXT = Text.translatable("text.skyblocker.discord");
	private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);

	private SkyblockerScreen() {
		super(TITLE);
	}

	public static void initClass() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(ClientCommandManager.literal(SkyblockerMod.NAMESPACE)
					.executes(Scheduler.queueOpenScreenCommand(SkyblockerScreen::new)));
		});
	}

	@Override
	protected void init() {
		this.layout.addHeader(new IconTextWidget(this.getTitle(), this.textRenderer, ICON));

		GridWidget gridWidget = this.layout.addBody(new GridWidget()).setSpacing(SPACING);
		gridWidget.getMainPositioner().alignHorizontalCenter();
		GridWidget.Adder adder = gridWidget.createAdder(2);

		adder.add(ButtonWidget.builder(CONFIGURATION_TEXT, button -> this.openConfig()).width(BUTTON_WIDTH).build(), 2);
		adder.add(ButtonWidget.builder(SOURCE_TEXT, ConfirmLinkScreen.opening(this, "https://github.com/SkyblockerMod/Skyblocker")).width(HALF_BUTTON_WIDTH).build());
		adder.add(ButtonWidget.builder(REPORT_BUGS_TEXT, ConfirmLinkScreen.opening(this, "https://github.com/SkyblockerMod/Skyblocker/issues")).width(HALF_BUTTON_WIDTH).build());
		adder.add(ButtonWidget.builder(WEBSITE_TEXT, ConfirmLinkScreen.opening(this, "https://hysky.de/")).width(HALF_BUTTON_WIDTH).build());
		adder.add(ButtonWidget.builder(TRANSLATE_TEXT, ConfirmLinkScreen.opening(this, "https://translate.hysky.de/")).width(HALF_BUTTON_WIDTH).build());
		adder.add(ButtonWidget.builder(MODRINTH_TEXT, ConfirmLinkScreen.opening(this, "https://modrinth.com/mod/skyblocker-liap")).width(HALF_BUTTON_WIDTH).build());
		adder.add(ButtonWidget.builder(DISCORD_TEXT, ConfirmLinkScreen.opening(this, "https://discord.gg/aNNJHQykck")).width(HALF_BUTTON_WIDTH).build());
		adder.add(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close()).width(BUTTON_WIDTH).build(), 2);

		MultilineTextWidget tip = new MultilineTextWidget(Text.translatable("skyblocker.tips.tip", Tips.nextTipInternal()), this.textRenderer)
		.setCentered(true)
		.setMaxWidth((int) (this.width * 0.7));

		this.layout.addFooter(tip);
		this.layout.refreshPositions();
		this.layout.forEachChild(this::addDrawableChild);
	}

	@Override
	protected void initTabNavigation() {
		this.layout.refreshPositions();
	}

	private void openConfig() {
		this.client.setScreen(SkyblockerConfigManager.createGUI(this));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderBackground(context, mouseX, mouseY, delta);
		super.render(context, mouseX, mouseY, delta);
	}

	private static class IconTextWidget extends TextWidget {
		private final Identifier icon;

		IconTextWidget(Text message, TextRenderer textRenderer, Identifier icon) {
			super(message, textRenderer);
			this.icon = icon;
		}

		@Override
		public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
			Text text = this.getMessage();
			TextRenderer textRenderer = this.getTextRenderer();

			int width = this.getWidth();
			int textWidth = textRenderer.getWidth(text);
			float horizontalAlignment = 0.5f; // default
			//17 = (32 + 2) / 2 • 32 + 2 is the width of the icon + spacing between icon and text
			int x = this.getX() + 17 + Math.round(horizontalAlignment * (float) (width - textWidth));
			int y = this.getY() + (this.getHeight() - textRenderer.fontHeight) / 2;
			OrderedText orderedText = textWidth > width ? this.trim(text, width) : text.asOrderedText();

			int iconX = x - 34;
			int iconY = y - 13;

			context.drawTextWithShadow(textRenderer, orderedText, x, y, this.getTextColor());
			context.drawTexture(this.icon, iconX, iconY, 0, 0, 32, 32, 32, 32);
		}

		private OrderedText trim(Text text, int width) {
			TextRenderer textRenderer = this.getTextRenderer();
			StringVisitable stringVisitable = textRenderer.trimToWidth(text, width - textRenderer.getWidth(ScreenTexts.ELLIPSIS));
			return Language.getInstance().reorder(StringVisitable.concat(stringVisitable, ScreenTexts.ELLIPSIS));
		}
	}
}
