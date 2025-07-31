package de.hysky.skyblocker.skyblock.profileviewer.rework;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public interface ProfileViewerWidget {
	void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, float deltaTicks);

	default void click(int x, int y, int mouseX, int mouseY) {}

	int getHeight();

	int getWidth();

	class Instance extends ClickableWidget {

		private final int originalX;
		private final int originalY;
		private final ProfileViewerWidget widget;

		public Instance(int x, int y, ProfileViewerWidget widget) {
			super(x, y, widget.getWidth(), widget.getHeight(), Text.empty()); // TODO: figure out the messages
			originalX = x;
			originalY = y;
			this.widget = widget;
		}

		public int getOriginalX() {
			return originalX;
		}

		public int getOriginalY() {
			return originalY;
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
			widget.render(context, this.getX(), this.getY(), mouseX, mouseY, deltaTicks);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			widget.click(getX(), getY(), (int) mouseX, (int) mouseY);
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		}

		public void setPositionFromRoot(int rootX, int rootY) {
			setX(rootX + originalX);
			setY(rootY + originalY);
		}
	}

	static Instance widget(int x, int y, ProfileViewerWidget widget) {
		return new Instance(x, y, widget);
	}
}
