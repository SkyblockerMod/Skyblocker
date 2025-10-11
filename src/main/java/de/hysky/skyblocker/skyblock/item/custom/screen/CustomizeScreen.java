package de.hysky.skyblocker.skyblock.item.custom.screen;

import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.GeneralConfig;
import de.hysky.skyblocker.injected.RecipeBookHolder;
import de.hysky.skyblocker.mixins.accessors.HandledScreenAccessor;
import de.hysky.skyblocker.skyblock.item.custom.CustomArmorAnimatedDyes;
import de.hysky.skyblocker.skyblock.item.custom.CustomArmorTrims;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.TabNavigationWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

public class CustomizeScreen extends Screen {
	static final Logger LOGGER = LogUtils.getLogger();
	static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	private final Screen previousScreen;
	private final Map<String, PreviousConfig> previousConfigs = new Object2ObjectOpenHashMap<>();

	private final TabManager tabManager = new TabManager(this::addDrawableChild, this::remove);
	private final boolean item;
	private TabNavigationWidget tabNavigation;

	private final DirectionalLayoutWidget footerLayout = DirectionalLayoutWidget.horizontal().spacing(5);
	private ArmorTab armorTab;


	@Init
	public static void initThings() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
				ClientCommandManager.literal(SkyblockerMod.NAMESPACE).then(ClientCommandManager.literal("custom").executes(Scheduler.queueOpenScreenCommand(() -> new CustomizeScreen(null, false))))
		));
		ScreenEvents.AFTER_INIT.register((client1, screen, scaledWidth, scaledHeight) -> {
			if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.showCustomizeButton && screen instanceof InventoryScreen inventoryScreen) {
				CustomizeButton button = new CustomizeButton(
						((HandledScreenAccessor) inventoryScreen).getX() + 63,
						((HandledScreenAccessor) inventoryScreen).getY() + 10
				);
				Screens.getButtons(inventoryScreen).add(button);
				((RecipeBookHolder) inventoryScreen).registerRecipeBookToggleCallback(() -> button.setPosition(
						((HandledScreenAccessor) inventoryScreen).getX() + 63,
						((HandledScreenAccessor) inventoryScreen).getY() + 10
				));
			}
		});
	}

	public CustomizeScreen(Screen previousScreen, boolean item) {
		super((Math.random() < 0.01 ? Text.translatable("skyblocker.customization.titleSecret") : Text.translatable("skyblocker.customization.title")).formatted(Formatting.GRAY).styled(style -> style.withShadowColor(0)));
		this.previousScreen = previousScreen;
		this.item = item;
	}

	public void backupConfigs(ItemStack stack) {
		String uuid = ItemUtils.getItemUuid(stack);
		if (uuid.isEmpty() || previousConfigs.containsKey(uuid)) return;
		GeneralConfig general = SkyblockerConfigManager.get().general;
		PreviousConfig previousConfig = new PreviousConfig(
				general.customArmorTrims.containsKey(uuid) ? Optional.of(general.customArmorTrims.get(uuid)) : Optional.empty(),
				general.customDyeColors.containsKey(uuid) ? OptionalInt.of(general.customDyeColors.getInt(uuid)) : OptionalInt.empty(),
				general.customAnimatedDyes.containsKey(uuid) ? Optional.of(general.customAnimatedDyes.get(uuid)) : Optional.empty(),
				general.customHelmetTextures.containsKey(uuid) ? Optional.of(general.customHelmetTextures.get(uuid)) : Optional.empty(),
				general.customItemNames.containsKey(uuid) ? Optional.of(general.customItemNames.get(uuid)) : Optional.empty(),
				general.customGlint.containsKey(uuid) ? Optional.of(general.customGlint.getBoolean(uuid)) : Optional.empty(),
				general.customItemModel.containsKey(uuid) ? Optional.of(general.customItemModel.get(uuid)) : Optional.empty(),
				general.customArmorModel.containsKey(uuid) ? Optional.of(general.customArmorModel.get(uuid)) : Optional.empty()
				);
		previousConfigs.put(uuid, previousConfig);
	}

	@Override
	public void tick() {
		armorTab.tick();
	}

	@Override
	protected void init() {
		super.init();

		armorTab = new ArmorTab(this);
		tabNavigation = TabNavigationWidget.builder(tabManager, width)
				.tabs(armorTab, new ItemTab(this))
				.build();
		int i = tabNavigation.getNavigationFocus().getBottom();
		tabNavigation.init();
		tabManager.setTabArea(new ScreenRect(0, i, width, height - i - 30));
		tabNavigation.selectTab(item ? 1 : 0, false);
		addDrawableChild(tabNavigation);

		addDrawableChild(footerLayout.add(ButtonWidget.builder(Text.translatable("gui.cancel"), b -> cancel()).build()));
		addDrawableChild(footerLayout.add(ButtonWidget.builder(Text.translatable("gui.done"), b -> close()).build()));
		footerLayout.refreshPositions();
		refreshWidgetPositions();
	}

	@Override
	public void onDisplayed() {
		super.onDisplayed();
		if (armorTab != null) {
			armorTab.recreate();
		}
	}

	private void cancel() {
		previousConfigs.forEach((uuid, previousConfig) -> {
			previousConfig.armorTrimId().ifPresentOrElse(
					trim -> SkyblockerConfigManager.get().general.customArmorTrims.put(uuid, trim),
					() -> SkyblockerConfigManager.get().general.customArmorTrims.remove(uuid)
			);
			previousConfig.color().ifPresentOrElse(
					i -> SkyblockerConfigManager.get().general.customDyeColors.put(uuid, i),
					() -> SkyblockerConfigManager.get().general.customDyeColors.removeInt(uuid)
			);
			previousConfig.animatedDye().ifPresentOrElse(
					animatedDye -> SkyblockerConfigManager.get().general.customAnimatedDyes.put(uuid, animatedDye),
					() -> SkyblockerConfigManager.get().general.customAnimatedDyes.remove(uuid)
			);
			previousConfig.helmetTexture().ifPresentOrElse(
					tex -> SkyblockerConfigManager.get().general.customHelmetTextures.put(uuid, tex),
					() -> SkyblockerConfigManager.get().general.customHelmetTextures.remove(uuid)
			);
			previousConfig.itemName().ifPresentOrElse(
					text -> SkyblockerConfigManager.get().general.customItemNames.put(uuid, text),
					() -> SkyblockerConfigManager.get().general.customItemNames.remove(uuid)
			);
			previousConfig.glint().ifPresentOrElse(
					b -> SkyblockerConfigManager.get().general.customGlint.put(uuid, b.booleanValue()),
					() -> SkyblockerConfigManager.get().general.customGlint.removeBoolean(uuid)
			);
			previousConfig.itemModel().ifPresentOrElse(
					identifier -> SkyblockerConfigManager.get().general.customItemModel.put(uuid, identifier),
					() -> SkyblockerConfigManager.get().general.customItemModel.remove(uuid)
			);
			previousConfig.armorModel().ifPresentOrElse(
					identifier -> SkyblockerConfigManager.get().general.customArmorModel.put(uuid, identifier),
					() -> SkyblockerConfigManager.get().general.customArmorModel.remove(uuid)
			);
		});
		close();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		boolean b = super.mouseClicked(mouseX, mouseY, button);
		if (!b) setFocused(null);
		return b;
	}

	@Override
	protected void refreshWidgetPositions() {
		int i = tabNavigation.getNavigationFocus().getBottom();
		tabNavigation.setWidth(width);
		tabNavigation.init();
		footerLayout.setPosition((width - footerLayout.getWidth()) / 2, height - footerLayout.getHeight() - 5);
		tabManager.setTabArea(new ScreenRect(0, i, width, footerLayout.getY() - i - 2));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		//context.drawCenteredTextWithShadow(textRenderer, getTitle(), this.width / 2, footerLayout.getY() + footerLayout.getHeight() + 2, Colors.WHITE);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void removed() {
		super.removed();
		armorTab.close();
		CustomArmorAnimatedDyes.cleanTrackers();
	}

	@Override
	public void close() {
		assert client != null;
		SkyblockerConfigManager.update(config -> {});
		client.setScreen(previousScreen);
	}

	private record PreviousConfig(Optional<CustomArmorTrims.ArmorTrimId> armorTrimId,
								  OptionalInt color,
								  Optional<CustomArmorAnimatedDyes.AnimatedDye> animatedDye,
								  Optional<String> helmetTexture,
								  Optional<Text> itemName,
								  Optional<Boolean> glint,
								  Optional<Identifier> itemModel,
								  Optional<Identifier> armorModel
								  ) {}

	private static class CustomizeButton extends ClickableWidget {
		// thanks to @yuflow
		private static final Identifier TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "armor_customization_screen/button");

		private CustomizeButton(int x, int y) {
			super(x, y, 10, 10, Text.empty());
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
			context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, getX(), getY(), getWidth(), getHeight(), isHovered() ? 0xFFfafa96 : 0x80FFFFFF);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			CLIENT.setScreen(new CustomizeScreen(CLIENT.currentScreen, false));
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
	}
}
