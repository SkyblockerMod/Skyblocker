package de.hysky.skyblocker.skyblock.item.custom.screen;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.GeneralConfig;
import de.hysky.skyblocker.mixins.accessors.AbstractContainerScreenAccessor;
import de.hysky.skyblocker.skyblock.item.custom.CustomArmorAnimatedDyes;
import de.hysky.skyblocker.skyblock.item.custom.CustomArmorTrims;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

public class CustomizeScreen extends Screen {
	static final Logger LOGGER = LogUtils.getLogger();
	static final Minecraft CLIENT = Minecraft.getInstance();

	private final Screen previousScreen;
	private final Map<String, PreviousConfig> previousConfigs = new Object2ObjectOpenHashMap<>();

	private final TabManager tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
	private final boolean item;
	private TabNavigationBar tabNavigation;

	private final LinearLayout footerLayout = LinearLayout.horizontal().spacing(5);
	private ArmorTab armorTab;


	@Init
	public static void initThings() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
				ClientCommandManager.literal(SkyblockerMod.NAMESPACE).then(ClientCommandManager.literal("custom").executes(Scheduler.queueOpenScreenCommand(() -> new CustomizeScreen(null, false))))
		));
		ScreenEvents.AFTER_INIT.register((client1, screen, scaledWidth, scaledHeight) -> {
			if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.showCustomizeButton && screen instanceof InventoryScreen inventoryScreen) {
				CustomizeButton button = new CustomizeButton(
						((AbstractContainerScreenAccessor) inventoryScreen).getX() + 63,
						((AbstractContainerScreenAccessor) inventoryScreen).getY() + 10
				);
				Screens.getButtons(inventoryScreen).add(button);
				inventoryScreen.registerRecipeBookToggleCallback(() -> button.setPosition(
						((AbstractContainerScreenAccessor) inventoryScreen).getX() + 63,
						((AbstractContainerScreenAccessor) inventoryScreen).getY() + 10
				));
			}
		});
	}

	public CustomizeScreen(Screen previousScreen, boolean item) {
		super((Math.random() < 0.01 ? Component.translatable("skyblocker.customization.titleSecret") : Component.translatable("skyblocker.customization.title")).withStyle(ChatFormatting.GRAY).withStyle(style -> style.withShadowColor(0)));
		this.previousScreen = previousScreen;
		this.item = item;
	}

	public void backupConfigs(ItemStack stack) {
		String uuid = stack.getUuid();
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
		tabNavigation = TabNavigationBar.builder(tabManager, width)
				.addTabs(armorTab, new ItemTab(this))
				.build();
		int i = tabNavigation.getRectangle().bottom();
		tabNavigation.arrangeElements();
		tabManager.setTabArea(new ScreenRectangle(0, i, width, height - i - 30));
		tabNavigation.selectTab(item ? 1 : 0, false);
		addRenderableWidget(tabNavigation);

		addRenderableWidget(footerLayout.addChild(Button.builder(Component.translatable("gui.cancel"), b -> cancel()).build()));
		addRenderableWidget(footerLayout.addChild(Button.builder(Component.translatable("gui.done"), b -> onClose()).build()));
		footerLayout.arrangeElements();
		repositionElements();
	}

	@Override
	public void added() {
		super.added();
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
		onClose();
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		boolean b = super.mouseClicked(click, doubled);
		if (!b) setFocused(null);
		return b;
	}

	@Override
	protected void repositionElements() {
		int i = tabNavigation.getRectangle().bottom();
		tabNavigation.setWidth(width);
		tabNavigation.arrangeElements();
		footerLayout.setPosition((width - footerLayout.getWidth()) / 2, height - footerLayout.getHeight() - 5);
		tabManager.setTabArea(new ScreenRectangle(0, i, width, footerLayout.getY() - i - 2));
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		//context.drawCenteredTextWithShadow(textRenderer, getTitle(), this.width / 2, footerLayout.getY() + footerLayout.getHeight() + 2, Colors.WHITE);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void removed() {
		super.removed();
		armorTab.close();
		CustomArmorAnimatedDyes.cleanTrackers();
	}

	@Override
	public void onClose() {
		assert minecraft != null;
		SkyblockerConfigManager.update(config -> {});
		minecraft.setScreen(previousScreen);
	}

	private record PreviousConfig(Optional<CustomArmorTrims.ArmorTrimId> armorTrimId,
								OptionalInt color,
								Optional<CustomArmorAnimatedDyes.AnimatedDye> animatedDye,
								Optional<String> helmetTexture,
								Optional<Component> itemName,
								Optional<Boolean> glint,
								Optional<Identifier> itemModel,
								Optional<Identifier> armorModel
								) {}

	private static class CustomizeButton extends AbstractWidget {
		// thanks to @yuflow
		private static final Identifier TEXTURE = SkyblockerMod.id("armor_customization_screen/button");

		private CustomizeButton(int x, int y) {
			super(x, y, 10, 10, Component.empty());
		}

		@Override
		protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
			context.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURE, getX(), getY(), getWidth(), getHeight(), isHovered() ? 0xFFFAFA96 : 0x80FFFFFF);

			if (this.isHovered()) {
				context.requestCursor(CursorTypes.POINTING_HAND);
			}
		}

		@Override
		public void onClick(MouseButtonEvent click, boolean doubled) {
			CLIENT.setScreen(new CustomizeScreen(CLIENT.screen, false));
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput builder) {}
	}
}
