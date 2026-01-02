package de.hysky.skyblocker.skyblock.profileviewer;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import de.hysky.skyblocker.skyblock.profileviewer.utils.ProfileViewerUtils;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class ProfileViewerNavButton extends AbstractWidget {
	private static final Identifier BUTTON_TEXTURES_TOGGLED = Identifier.parse("container/creative_inventory/tab_top_selected_2");
	private static final Identifier BUTTON_TEXTURES = Identifier.parse("container/creative_inventory/tab_top_unselected_2");
	private boolean toggled;
	private final int index;
	private final ProfileViewerScreen screen;
	private final ItemStack icon;

	private static final Map<String, ItemStack> HEAD_ICON = Map.ofEntries(
			Map.entry("Skills", Ico.IRON_SWORD),
			Map.entry("Slayers", ProfileViewerUtils.createSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzkzMzZkN2NjOTVjYmY2Njg5ZjVlOGM5NTQyOTRlYzhkMWVmYzQ5NGE0MDMxMzI1YmI0MjdiYzgxZDU2YTQ4NGQifX19")),
			Map.entry("Pets", Ico.BONE),
			Map.entry("Dungeons", ProfileViewerUtils.createSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzliNTY4OTViOTY1OTg5NmFkNjQ3ZjU4NTk5MjM4YWY1MzJkNDZkYjljMWIwMzg5YjhiYmViNzA5OTlkYWIzM2QifX19")),
			Map.entry("Inventories", Ico.E_CHEST),
			Map.entry("Collections", Ico.PAINTING)
	);

	public ProfileViewerNavButton(ProfileViewerScreen screen, String tabName, int index, boolean toggled) {
		super(-100, -100, 28, 32, Component.empty());
		this.screen = screen;
		this.toggled = toggled;
		this.index = index;
		this.icon = HEAD_ICON.getOrDefault(tabName, Ico.BARRIER);
	}

	@Override
	protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
		context.blitSprite(RenderPipelines.GUI_TEXTURED, toggled ? BUTTON_TEXTURES_TOGGLED : BUTTON_TEXTURES, this.getX(), this.getY(), this.width, this.height - ((this.toggled) ? 0 : 4));
		context.renderItem(this.icon, this.getX() + 6, this.getY() + (this.toggled ? 7 : 9));

		if (this.isHovered()) {
			context.requestCursor(CursorTypes.POINTING_HAND);
		}
	}

	@Override
	public void onClick(MouseButtonEvent click, boolean doubled) {
		screen.onNavButtonClick(this);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {}

	public void setToggled(boolean toggled) {
		this.toggled = toggled;
	}

	public int getIndex() {
		return index;
	}
}
