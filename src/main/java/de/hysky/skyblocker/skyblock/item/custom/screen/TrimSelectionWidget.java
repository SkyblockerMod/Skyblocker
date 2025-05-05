package de.hysky.skyblocker.skyblock.item.custom.screen;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.custom.CustomArmorTrims;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class TrimSelectionWidget extends ContainerWidget {

	private static final Identifier INNER_SPACE_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "menu_inner_space");
	private static final int BUTTONS_PER_ROW_PATTERN = 7;
	private static final int BUTTONS_PER_ROW_MATERIAL = 5;

	private final List<TrimElementButton.Pattern> patternButtons = new ArrayList<>();
	private final List<TrimElementButton> materialButtons = new ArrayList<>();
	private final List<ClickableWidget> children = new ArrayList<>();
	
	private ItemStack currentItem = null;
	private Identifier selectedPattern = null;
	private Identifier selectedMaterial = null;

	public TrimSelectionWidget(int x, int y, int width, int height) {
		super(x, y, width, height, Text.of("Trim Selection"));
		// Patterns
		TrimElementButton.Pattern patternNoneButton = new TrimElementButton.Pattern(null, null, this::onClickPattern);
		patternNoneButton.setMessage(Text.translatable("gui.none"));
		patternButtons.add(patternNoneButton);

		Utils.getRegistryWrapperLookup().getOrThrow(RegistryKeys.TRIM_PATTERN).streamEntries()
				// Sort them in alphabetical order
				.sorted(Comparator.comparing(reference -> reference.value().description().getString()))
				.map(reference -> new TrimElementButton.Pattern(
						reference.registryKey().getValue(),
						reference.value(),
						this::onClickPattern
				)).forEachOrdered(patternButtons::add);
		children.addAll(patternButtons);
		for (int i = 0; i < patternButtons.size(); i++) {
			TrimElementButton button = patternButtons.get(i);
			button.setPosition(x + 5 + (i % BUTTONS_PER_ROW_PATTERN) * 20, y + 5 + (i / BUTTONS_PER_ROW_PATTERN) * 20);
		}

		// Materials
		Utils.getRegistryWrapperLookup().getOrThrow(RegistryKeys.TRIM_MATERIAL).streamEntries()
				// Sort them in alphabetical order
				.sorted(Comparator.comparing(reference -> reference.value().description().getString()))
				.map(reference -> new TrimElementButton.Material(
						reference.registryKey().getValue(),
						reference.value(),
						this::onClickMaterial
				)).forEachOrdered(materialButtons::add);
		children.addAll(materialButtons);
		for (int i = 0; i < materialButtons.size(); i++) {
			TrimElementButton button = materialButtons.get(i);
			button.setPosition(x + getWidth() - 11 - BUTTONS_PER_ROW_MATERIAL * 20 + (i % BUTTONS_PER_ROW_MATERIAL) * 20, y + 5 + (i / BUTTONS_PER_ROW_MATERIAL) * 20);
		}
	}

	private void onClickPattern(TrimElementButton button) {
		for (TrimElementButton patternButton : patternButtons) {
			patternButton.active = true;
		}
		button.active = false;
		selectedPattern = button.getElement();
		updateConfig();
	}

	private void onClickMaterial(TrimElementButton button) {
		for (TrimElementButton materialButton : materialButtons) {
			materialButton.active = true;
		}
		button.active = false;
		selectedMaterial = button.getElement();
		updateConfig();
	}

	private void updateConfig() {
		if (currentItem == null) return;
		Map<String, CustomArmorTrims.ArmorTrimId> trims = SkyblockerConfigManager.get().general.customArmorTrims;
		String itemUuid = ItemUtils.getItemUuid(currentItem);
		if (selectedPattern == null) {
			trims.remove(itemUuid);
		} else {
			trims.put(itemUuid, new CustomArmorTrims.ArmorTrimId(selectedMaterial, selectedPattern));
		}
	}

	@Override
	public List<? extends Element> children() {
		return children;
	}

	@Override
	protected int getContentsHeightWithPadding() {
		return (Math.max(patternButtons.size(), materialButtons.size()) / BUTTONS_PER_ROW_PATTERN + 1) * 20 + 10;
	}

	@Override
	protected double getDeltaYPerScroll() {
		return 10;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return super.mouseClicked(mouseX, mouseY + this.getScrollY(), button);
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		context.drawGuiTexture(RenderLayer::getGuiTextured, INNER_SPACE_TEXTURE, getX(), getY(), getWidth(), getHeight());
		context.enableScissor(getX() + 2, getY() + 2, getX() + getWidth() - 2, getY() + getHeight() - 2);

		int scrollY = (int) this.getScrollY();
		for (ClickableWidget widget : this.children) {
			widget.setY(widget.getY() - scrollY);
			widget.render(context, mouseX, mouseY, delta);
			widget.setY(widget.getY() + scrollY);
		}

		drawScrollbar(context);
		context.disableScissor();
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {

	}

	public void setCurrentItem(@NotNull ItemStack currentItem) {
		this.currentItem = currentItem;
		Map<String, CustomArmorTrims.ArmorTrimId> trims = SkyblockerConfigManager.get().general.customArmorTrims;
		String itemUuid = ItemUtils.getItemUuid(currentItem);
		for (TrimElementButton.Pattern button : patternButtons) {
			button.setItem(currentItem);
		}
		if (!trims.containsKey(itemUuid)) {
			selectedPattern = null;
			selectedMaterial = materialButtons.getFirst().getElement();
			for (int i = 0; i < materialButtons.size(); i++) {
				materialButtons.get(i).active = i != 0;
			}
			for (int i = 0; i < patternButtons.size(); i++) {
				patternButtons.get(i).active = i != 0;
			}
		} else {
			CustomArmorTrims.ArmorTrimId id = trims.get(itemUuid);
			selectedMaterial = id.material();
			selectedPattern = id.pattern();
			for (TrimElementButton materialButton : materialButtons) {
				materialButton.active = !selectedMaterial.equals(materialButton.getElement());
			}
			for (TrimElementButton patternButton : patternButtons) {
				patternButton.active = !selectedPattern.equals(patternButton.getElement());
			}
		}
	}
}
