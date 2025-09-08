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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class SkyblockInfoCategory implements DisplayCategory<SkyblockInfoDisplay> {
	private static final int REI_SLOT_HEIGHT = 18;
	private static final int OFFSET = 10;
	private static final int RED_ERROR_COLOR = 0xFFFF5555;
	private static final EntryStack<ItemStack> ICON = EntryStacks.of(new ItemStack(Items.CHEST));

	public static final Identifier IDENTIFIER = Identifier.of(SkyblockerMod.NAMESPACE, "skyblock_info");

	@Override
	public CategoryIdentifier<? extends SkyblockInfoDisplay> getCategoryIdentifier() {
		return CategoryIdentifier.of(IDENTIFIER);
	}

	@Override
	public Text getTitle() {
		return Text.translatable("emi.category.skyblocker.skyblock_info");
	}

	@Override
	public Renderer getIcon() {
		return ICON;
	}

	private ButtonWidget getWikiLookupButton(Text text, boolean isOfficial, ItemStack itemStack, ClientPlayerEntity player) {
		ButtonWidget btn = ButtonWidget.builder(text, (button) -> WikiLookupManager.openWiki(itemStack, player, isOfficial)).build();

		if (ItemRepository.getWikiLink(itemStack.getNeuName(), isOfficial) == null) {
			btn.setMessage(btn.getMessage().copy().withColor(RED_ERROR_COLOR));
			btn.active = false;
		}

		return btn;
	}

	private boolean checkScreen() {
		Screen currentScreen = MinecraftClient.getInstance().currentScreen;
		return currentScreen instanceof GenericContainerScreen || currentScreen instanceof AbstractCustomHypixelGUI<?>;
	}

	@Override
	public List<Widget> setupDisplay(SkyblockInfoDisplay display, Rectangle bounds) {
		List<Widget> widgets = new ArrayList<>();
		EntryStack<?> entryStack = display.getInputEntries().getFirst().getFirst();
		if (!(entryStack.getValue() instanceof ItemStack itemStack)) return widgets;

		widgets.add(Widgets.createRecipeBase(bounds));
		Slot slot = Widgets.createSlot(new Point(bounds.getCenterX() - 9 + 1, bounds.y + 1 + OFFSET / 2)).entry(entryStack);
		widgets.add(slot);

		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		DirectionalLayoutWidget layoutWidget = DirectionalLayoutWidget.vertical();
		layoutWidget.setPosition(bounds.x + OFFSET, bounds.y + OFFSET + REI_SLOT_HEIGHT);

		layoutWidget.add(ButtonWidget.builder(Text.translatable("key.itemPriceLookup"), (button) -> {
			ItemPrice.itemPriceLookup(player, itemStack);

			Scheduler.INSTANCE.schedule(() -> {
				if (checkScreen()) return;
				button.setMessage(Text.translatable("skyblocker.rei.skyblockInfo.failedToFind").withColor(RED_ERROR_COLOR));
				button.active = false;
			}, 10);
		}).build());

		layoutWidget.add(getWikiLookupButton(Text.translatable("key.wikiLookup.official"), true, itemStack, player));
		layoutWidget.add(getWikiLookupButton(Text.translatable("key.wikiLookup.fandom"), false, itemStack, player));

		layoutWidget.forEachChild(child -> widgets.add(Widgets.wrapVanillaWidget(child)));
		layoutWidget.refreshPositions();

		return widgets;
	}

	@Override
	public int getDisplayHeight() {
		return 3 * ButtonWidget.DEFAULT_HEIGHT + REI_SLOT_HEIGHT + 2 * OFFSET;
	}

	@Override
	public int getDisplayWidth(SkyblockInfoDisplay display) {
		return 150 + 20;
	}
}
