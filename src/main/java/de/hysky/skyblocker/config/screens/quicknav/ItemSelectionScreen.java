package de.hysky.skyblocker.config.screens.quicknav;

import de.hysky.skyblocker.config.configs.QuickNavigationConfig;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.datafixer.ItemStackComponentizationFixer;
import de.hysky.skyblocker.utils.render.gui.AbstractPopupScreen;
import de.hysky.skyblocker.utils.render.gui.SearchableGridWidget;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class ItemSelectionScreen extends AbstractPopupScreen {

	private final List<Pair<String, String>> skullIcons = List.of(
			Pair.of("Accessory Bag", "1a11a7f11bcd5784903c5201d08261c4df8379109d6e611c1cd3ededf031afed"),
			Pair.of("Sack of Sacks", "80a077e248d142772ea800864f8c578b9d36885b29daf836b64a706882b6ec10"),
			Pair.of("Fishing Bag", "eab810b1b4ff598d1d99336e71da4e356d3ad321e169dbd1fa9d506aae61717d"),
			Pair.of("Potion Bag", "2fc626b3a12d099c44da04b5f15c95f2e360e0789bccecf842ef09c72b93d842"),
			Pair.of("Quiver", "44e1843df8f91a7033e4c810f10b003e07ade5100079ac44df74a4e46a489447"),
			Pair.of("Personal Bank", "e36e94f6c34a35465fce4a90f2e25976389eb9709a12273574ff70fd4daa6852"),
			Pair.of("Skyblock Hub", "d7cc6687423d0570d556ac53e0676cb563bbdd9717cd8269bdebed6f6d4e7bf8"),
			Pair.of("Private Island", "c9c8881e42915a9d29bb61a16fb26d059913204d265df5b439b3d792acd56"),
			Pair.of("Castle", "f4559d75464b2e40a518e4de8e6cf3085f0a3ca0b1b7012614c4cd96fed60378"),
			Pair.of("Sirius' Shack", "7ab83858ebc8ee85c3e54ab13aabfcc1ef2ad446d6a900e471c3f33b78906a5b"),
			Pair.of("Crypts", "25d2f31ba162fe6272e831aed17f53213db6fa1c4cbe4fc827f3963cc98b9"),
			Pair.of("Spider's Den", "c754318a3376f470e481dfcd6c83a59aa690ad4b4dd7577fdad1c2ef08d8aee6"),
			Pair.of("Top of the Nest", "9d7e3b19ac4f3dee9c5677c135333b9d35a7f568b63d1ef4ada4b068b5a25"),
			Pair.of("The End", "7840b87d52271d2a755dedc82877e0ed3df67dcc42ea479ec146176b02779a5"),
			Pair.of("The End Dragon's Nest", "a1cd6d2d03f135e7c6b5d6cdae1b3a68743db4eb749faf7341e9fb347aa283b"),
			Pair.of("The Park", "a221f813dacee0fef8c59f76894dbb26415478d9ddfc44c2e708a6d3b7549b"),
			Pair.of("The Park - Jungle", "79ca3540621c1c79c32bf42438708ff1f5f7d0af9b14a074731107edfeb691c"),
			Pair.of("Howling Cave", "1832d53997b451635c9cf9004b0f22bb3d99ab5a093942b5b5f6bb4e4de47065"),
			Pair.of("Gold Mines", "73bc965d579c3c6039f0a17eb7c2e6faf538c7a5de8e60ec7a719360d0a857a9"),
			Pair.of("Deep Caverns", "569a1f114151b4521373f34bc14c2963a5011cdc25a6554c48c708cd96ebfc"),
			Pair.of("The Barn", "4d3a6bd98ac1833c664c4909ff8d2dc62ce887bdcf3cc5b3848651ae5af6b"),
			Pair.of("Mushroom Desert", "6b20b23c1aa2be0270f016b4c90d6ee6b8330a17cfef87869d6ad60b2ffbf3b5"),
			Pair.of("Dungeon Hub", "9b56895b9659896ad647f58599238af532d46db9c1b0389b8bbeb70999dab33d"),
			Pair.of("Dwarven Mines", "51539dddf9ed255ece6348193cd75012c82c93aec381f05572cecf7379711b3b"),
			Pair.of("Heart Of The Mountain (HOTM)", "86f06eaa3004aeed09b3d5b45d976de584e691c0e9cade133635de93d23b9edb"),
			Pair.of("Bazaar", "c232e3820897429157619b0ee099fec0628f602fff12b695de54aef11d923ad7"),
			Pair.of("Museum", "438cf3f8e54afc3b3f91d20a49f324dca1486007fe545399055524c17941f4dc"),
			Pair.of("Crystal Hollows", "21dbe30b027acbceb612563bd877cd7ebb719ea6ed1399027dcee58bb9049d4a"),
			Pair.of("Dwarven Forge", "5cbd9f5ec1ed007259996491e69ff649a3106cf920227b1bb3a71ee7a89863f"),
			Pair.of("Forgotten Skull", "6becc645f129c8bc2faa4d8145481fab11ad2ee75749d628dcd999aa94e7"),
			Pair.of("Crystal Nucleus", "34d42f9c461cee1997b67bf3610c6411bf852b9e5db607bbf626527cfb42912c"),
			Pair.of("Void Sepulture", "eb07594e2df273921a77c101d0bfdfa1115abed5b9b2029eb496ceba9bdbb4b3"),
			Pair.of("Crimson Isle", "c3687e25c632bce8aa61e0d64c24e694c3eea629ea944f4cf30dcfb4fbce071"),
			Pair.of("Trapper's Den", "6102f82148461ced1f7b62e326eb2db3a94a33cba81d4281452af4d8aeca4991"),
			Pair.of("Arachne's Sanctuary", "35e248da2e108f09813a6b848a0fcef111300978180eda41d3d1a7a8e4dba3c3"),
			Pair.of("Garden", "f4880d2c1e7b86e87522e20882656f45bafd42f94932b2c5e0d6ecaa490cb4c"),
			Pair.of("Winter", "6dd663136cafa11806fdbca6b596afd85166b4ec02142c8d5ac8941d89ab7"),
			Pair.of("Wizard Tower", "838564e28aba98301dbda5fafd86d1da4e2eaeef12ea94dcf440b883e559311c"),
			Pair.of("Base Camp", "2461ec3bd654f62ca9a393a32629e21b4e497c877d3f3380bcf2db0e20fc0244")
	);

	private final QuickNavigationConfig.ItemData target;
	private @Nullable ItemWidget selectedItem = null;

	private final GridWidget gridWidget = new GridWidget();
	private ButtonWidget doneButton;

	public ItemSelectionScreen(Screen backgroundScreen, QuickNavigationConfig.ItemData target) {
		super(Text.literal("Select Item"), backgroundScreen);
		this.target = target;
	}

	private void setSelectedItem(@NotNull ItemWidget selectedItem) {
		this.selectedItem = selectedItem;
		doneButton.active = true;
	}

	@Override
	protected void init() {
		GridWidget.Adder adder = gridWidget.createAdder(2);
		addDrawableChild(adder.add(new ItemList(300, (int) (height * 0.8f)), 2));
		addDrawableChild(adder.add(ButtonWidget.builder(ScreenTexts.CANCEL, b -> close()).build()));
		doneButton = ButtonWidget.builder(ScreenTexts.DONE, b -> {
			if (selectedItem == null) return;
			target.item = selectedItem.item.getItem();
			target.components = ItemStackComponentizationFixer.componentsAsString(selectedItem.item);
			close();
		}).build();
		doneButton.active = false;
		addDrawableChild(adder.add(doneButton));
		gridWidget.refreshPositions();
		refreshWidgetPositions();
	}

	@Override
	protected void refreshWidgetPositions() {
		gridWidget.setPosition((width - gridWidget.getWidth()) / 2, (height - gridWidget.getHeight()) / 2);
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		super.renderBackground(context, mouseX, mouseY, delta);
		drawPopupBackground(context, gridWidget.getX(), gridWidget.getY(), gridWidget.getWidth(), gridWidget.getHeight());
	}

	private class ItemList extends SearchableGridWidget {
		private final List<ItemWidget> items;

		private ItemList(int width, int height) {
			super(0, 0, width, height, Text.literal("Item List"), 20);
			Stream<ItemStack> icons = skullIcons.stream().map(pair -> {
						ItemStack skull = ItemUtils.createSkull(ItemUtils.toTextureBase64(pair.right()));
						skull.set(DataComponentTypes.CUSTOM_NAME, Text.literal(pair.left()).styled(style -> style.withItalic(false)));
						return skull;
			});
			items = Stream.concat(icons, ItemRepository.getItemsStream()).map(ItemWidget::new).toList();
			setSearch("");
		}

		@Override
		protected Collection<? extends ClickableWidget> filterWidgets(String input) {
			return items.stream().filter(w -> w.getMessage().getString().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH))).toList();
		}

		@Override
		protected double getDeltaYPerScroll() {
			return 15;
		}
	}

	private class ItemWidget extends ClickableWidget {
		private final ItemStack item;

		private ItemWidget(ItemStack stack) {
			super(0, 0, 20, 20, Text.literal(stack.getName().getString()));
			item = new ItemStack(stack.getItem());
			item.copy(DataComponentTypes.PROFILE, stack);
			String itemId = ItemUtils.getItemId(stack);
			NbtCompound customData = new NbtCompound();
			customData.putString(ItemUtils.ID, itemId);
			item.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(customData));
			setTooltip(Tooltip.of(getMessage()));
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
			context.drawItem(item, getX() + 2, getY() + 2);
			if (selectedItem == this) {
				context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x3000FF00);
			}
			if (isHovered()) {
				context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x20FFFFFF);
			}
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			super.onClick(mouseX, mouseY);
			setSelectedItem(this);
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
	}
}
