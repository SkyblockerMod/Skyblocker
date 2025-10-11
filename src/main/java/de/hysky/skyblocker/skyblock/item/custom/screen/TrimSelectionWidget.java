package de.hysky.skyblocker.skyblock.item.custom.screen;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.custom.CustomArmorTrims;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.*;
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
	private static final int PADDING = 3;

	private static final Identifier INNER_SPACE_TEXTURE = SkyblockerMod.id("menu_inner_space");
	private static final int MAX_BUTTONS_PER_ROW_PATTERN = 7;
	private static final int MAX_BUTTONS_PER_ROW_MATERIAL = 6;

	private final List<TrimElementButton.Pattern> patternButtons = new ArrayList<>();
	private final List<TrimElementButton> materialButtons = new ArrayList<>();
	private SimplePositioningWidget layout;
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

		positionButtons(width, height);
		layout.setPosition(x + PADDING, y + PADDING);
	}

	private void positionButtons(int width, int height) {
		// Layout button again and accommodate for the scrollbar if overflows
		// We need to do this since overflows always return false during the first calculation since the buttons per row hasn't been calculated yet

		// Calculate buttons per row
		// minus 9 because 3 pixels of left padding, right padding, and gap
		int buttonsPerRow = (width - 9) / 20;
		// Try to allocate more buttons to patterns since there are more patterns than materials
		int patternButtonsPerRow = Math.min(Math.ceilDiv(buttonsPerRow, 2), MAX_BUTTONS_PER_ROW_PATTERN);
		int materialButtonsPerRow = Math.min(Math.floorDiv(buttonsPerRow, 2), MAX_BUTTONS_PER_ROW_MATERIAL);

		int maxHeight = getHeight() - PADDING * 2;
		boolean overflow = (patternButtons.size() / patternButtonsPerRow + 1) * 20 > maxHeight || (materialButtons.size() / materialButtonsPerRow + 1) * 20 > maxHeight;
		if (overflow) {
			// subtract 6 extra pixels for the scrollbar
			buttonsPerRow = (width - 15) / 20;
			patternButtonsPerRow = Math.min(Math.ceilDiv(buttonsPerRow, 2), MAX_BUTTONS_PER_ROW_PATTERN);
			materialButtonsPerRow = Math.min(Math.floorDiv(buttonsPerRow, 2), MAX_BUTTONS_PER_ROW_MATERIAL);
		}

		// Set button positions
		GridWidget patternsGrid = new GridWidget();
		GridWidget.Adder patternAdder = patternsGrid.createAdder(patternButtonsPerRow);
		patternButtons.forEach(patternAdder::add);

		GridWidget materialsGrid = new GridWidget();
		for (int i = 0; i < materialButtons.size(); i++) {
			TrimElementButton button = materialButtons.get(i);
			int row = i / materialButtonsPerRow;
			int column = materialButtonsPerRow - (i % materialButtonsPerRow) - 1;
			materialsGrid.add(button, row, column);
		}

		layout = new SimplePositioningWidget(width - PADDING * 2 - (overflow ? 6 : 0), height - PADDING * 2);
		layout.getMainPositioner().alignTop();
		layout.add(patternsGrid, Positioner::alignLeft);
		layout.add(materialsGrid, Positioner::alignRight);
		layout.refreshPositions();
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
		String itemUuid = currentItem.getUuid();
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
		// 3 pixels of padding on top and bottom
		return layout.getHeight() + PADDING * 2;
	}

	@Override
	public void setX(int x) {
		super.setX(x);
		layout.setX(getX() + PADDING);
	}

	@Override
	public void setY(int y) {
		super.setY(y);
		layout.setY(getY() + PADDING);
	}

	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		positionButtons(getWidth(), getHeight());
	}

	@Override
	public void setHeight(int height) {
		super.setHeight(height);
		positionButtons(getWidth(), getHeight());
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
		context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, INNER_SPACE_TEXTURE, getX(), getY(), getWidth(), getHeight());
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
		String itemUuid = currentItem.getUuid();
		for (TrimElementButton.Pattern button : patternButtons) {
			button.setStack(currentItem);
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
