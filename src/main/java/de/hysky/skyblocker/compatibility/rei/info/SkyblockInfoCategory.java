package de.hysky.skyblocker.compatibility.rei.info;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.item.ItemPrice;
import de.hysky.skyblocker.skyblock.item.wikilookup.WikiLookupManager;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.render.gui.AbstractCustomHypixelGUI;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.ArrayList;
import java.util.List;

public class SkyblockInfoCategory implements DisplayCategory<SkyblockInfoDisplay> {
	private static final int REI_SLOT_HEIGHT = 18;
	private static final int OFFSET = 10;
	private static final int RED_ERROR_COLOR = 0xFFFF5555;
	private static final EntryStack<ItemStack> ICON = EntryStacks.of(new ItemStack(Items.CHEST));

	public static final ResourceLocation IDENTIFIER = SkyblockerMod.id("skyblock_info");

	@Override
	public CategoryIdentifier<? extends SkyblockInfoDisplay> getCategoryIdentifier() {
		return CategoryIdentifier.of(IDENTIFIER);
	}

	@Override
	public Component getTitle() {
		return Component.translatable("emi.category.skyblocker.skyblock_info");
	}

	@Override
	public Renderer getIcon() {
		return ICON;
	}

	private Button getWikiLookupButton(Component text, boolean isOfficial, ItemStack itemStack, LocalPlayer player) {
		Button btn = Button.builder(text, (button) -> WikiLookupManager.openWiki(itemStack, player, isOfficial)).build();

		if (ItemRepository.getWikiLink(itemStack.getNeuName(), isOfficial) == null) {
			btn.setMessage(btn.getMessage().copy().withColor(RED_ERROR_COLOR));
			btn.active = false;
		}

		return btn;
	}

	private boolean checkScreen() {
		Screen currentScreen = Minecraft.getInstance().screen;
		return currentScreen instanceof ContainerScreen || currentScreen instanceof AbstractCustomHypixelGUI<?>;
	}

	@Override
	public List<Widget> setupDisplay(SkyblockInfoDisplay display, Rectangle bounds) {
		List<Widget> widgets = new ArrayList<>();
		EntryStack<?> entryStack = display.getInputEntries().getFirst().getFirst();
		if (!(entryStack.getValue() instanceof ItemStack itemStack)) return widgets;

		widgets.add(Widgets.createRecipeBase(bounds));
		Slot slot = Widgets.createSlot(new Point(bounds.getCenterX() - 9 + 1, bounds.y + 1 + OFFSET / 2)).entry(entryStack);
		widgets.add(slot);

		LocalPlayer player = Minecraft.getInstance().player;
		LinearLayout layoutWidget = LinearLayout.vertical();
		layoutWidget.setPosition(bounds.x + OFFSET, bounds.y + OFFSET + REI_SLOT_HEIGHT);

		layoutWidget.addChild(Button.builder(Component.translatable("key.skyblocker.itemPriceLookup"), (button) -> {
			ItemPrice.itemPriceLookup(player, itemStack);

			Scheduler.INSTANCE.schedule(() -> {
				if (checkScreen()) return;
				button.setMessage(Component.translatable("skyblocker.rei.skyblockInfo.failedToFind").withColor(RED_ERROR_COLOR));
				button.active = false;
			}, 10);
		}).build());

		layoutWidget.addChild(getWikiLookupButton(Component.translatable("key.skyblocker.wikiLookup.official"), true, itemStack, player));
		layoutWidget.addChild(getWikiLookupButton(Component.translatable("key.skyblocker.wikiLookup.fandom"), false, itemStack, player));

		layoutWidget.visitWidgets(child -> widgets.add(Widgets.wrapVanillaWidget(child)));
		layoutWidget.arrangeElements();

		return widgets;
	}

	@Override
	public int getDisplayHeight() {
		return 3 * Button.DEFAULT_HEIGHT + REI_SLOT_HEIGHT + 2 * OFFSET;
	}

	@Override
	public int getDisplayWidth(SkyblockInfoDisplay display) {
		return 150 + 20;
	}
}
