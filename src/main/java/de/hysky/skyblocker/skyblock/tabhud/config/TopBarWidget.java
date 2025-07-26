package de.hysky.skyblocker.skyblock.tabhud.config;

import com.google.common.collect.Lists;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetManager;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.function.Consumer;

public class TopBarWidget extends ContainerWidget {
	private static final Identifier TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "menu_outer_space");
	private final DropdownThing<Location> locationDropdown;
	private final DropdownThing<WidgetManager.ScreenLayer> screenLayerDropdown;

	public TopBarWidget(int width, Consumer<Location> onLocationSelected, Consumer<WidgetManager.ScreenLayer> onLayerSelected) {
		super(0, 0, width, 15, Text.literal("hi"));
		List<Location> locations = Lists.newArrayList(Location.values());
		// move UNKNOWN to be first
		locations.remove(Location.UNKNOWN);
		locations.addFirst(Location.UNKNOWN);
		locationDropdown = new DropdownThing<>(width / 2 - 100 - 5, 0, 100, 200, locations, onLocationSelected, Utils.getLocation());
		locationDropdown.setFormatter(location -> location == Location.UNKNOWN ? Text.literal("Everywhere").formatted(Formatting.YELLOW) : Text.literal(location.toString()));
		screenLayerDropdown = new DropdownThing<>(width / 2 + 5, 0, 100, 200, List.of(WidgetManager.ScreenLayer.values()), onLayerSelected, WidgetManager.ScreenLayer.HUD);
	}

	@Override
	public int getHeight() {
		return Math.max(super.getHeight(), Math.max(locationDropdown.getHeight(), screenLayerDropdown.getHeight()));
	}

	@Override
	public List<? extends Element> children() {
		return List.of(locationDropdown, screenLayerDropdown);
	}

	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		locationDropdown.setX(width / 2 - 100 - 5);
		screenLayerDropdown.setX(width / 2 + 5);
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		context.drawGuiTexture(RenderLayer::getGuiTextured, TEXTURE, getX() - 2, getY() - 2, getWidth() + 4, 15 + 2);
		locationDropdown.render(context, mouseX, mouseY, deltaTicks);
		screenLayerDropdown.render(context, mouseX, mouseY, deltaTicks);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (!visible) return false;
		if (this.hoveredElement(mouseX, mouseY).filter(element -> element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)).isPresent()) return true;
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

	@Override
	protected int getContentsHeightWithPadding() {
		return 0;
	}

	@Override
	protected double getDeltaYPerScroll() {
		return 0;
	}
}
