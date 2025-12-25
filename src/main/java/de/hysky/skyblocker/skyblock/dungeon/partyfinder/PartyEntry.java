package de.hysky.skyblocker.skyblock.dungeon.partyfinder;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import de.hysky.skyblocker.SkyblockerMod;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import org.joml.Matrix3x2fStack;

public class PartyEntry extends ContainerObjectSelectionList.Entry<PartyEntry> {
	private static final ResourceLocation PARTY_CARD_TEXTURE = SkyblockerMod.id("textures/gui/party_card.png");
	private static final ResourceLocation PARTY_CARD_TEXTURE_HOVER = SkyblockerMod.id("textures/gui/party_card_hover.png");
	private static final Map<String, ResolvableProfile> SKULL_CACHE = new Object2ObjectOpenHashMap<>();
	private static final Pattern NUMBERS_PATTERN = Pattern.compile("\\d+$");

	public static final Component JOIN_TEXT = Component.translatable("skyblocker.partyFinder.join");
	protected final PartyFinderScreen screen;
	protected final int slotID;

	Player partyLeader;
	String floor = "???";
	String dungeon = "???";
	String note = "";
	PropertyMap floorSkullProperties = PropertyMap.EMPTY;
	ResourceLocation partyLeaderSkin = DefaultPlayerSkin.getDefaultTexture();
	Player[] partyMembers = new Player[4];

	int minClassLevel = -1;
	int minCatacombsLevel = -1;

	public boolean isLocked() {
		return isLocked;
	}

	boolean isLocked = false;
	Component lockReason = Component.empty();


	public PartyEntry(Component title, List<Component> tooltips, PartyFinderScreen screen, int slotID) {
		this.screen = screen;
		this.slotID = slotID;

		Arrays.fill(partyMembers, null);
		if (tooltips.isEmpty()) return;
		//System.out.println(tooltips);

		Minecraft client = Minecraft.getInstance();
		String partyHost = title.getString().split("'s")[0];

		int membersIndex = -1;
		for (int i = 0; i < tooltips.size(); i++) {
			Component text = tooltips.get(i);
			String tooltipText = ChatFormatting.stripFormatting(text.getString());
			assert tooltipText != null;
			String lowerCase = tooltipText.toLowerCase(Locale.ENGLISH);
			//System.out.println("TOOLTIP"+i);
			//System.out.println(text.getSiblings());

			if (lowerCase.contains("members:") && membersIndex == -1) {
				membersIndex = i + 1;
			} else if (lowerCase.contains("class level")) {
				Matcher matcher = NUMBERS_PATTERN.matcher(lowerCase);
				if (matcher.find()) minClassLevel = Integer.parseInt(matcher.group());
			} else if (lowerCase.contains("dungeon level")) {
				Matcher matcher = NUMBERS_PATTERN.matcher(lowerCase);
				if (matcher.find()) minCatacombsLevel = Integer.parseInt(matcher.group());
			} else if (lowerCase.contains("floor:")) {
				floor = tooltipText.split(":")[1].trim();
				if (dungeon.equals("???")) continue;
				if (PartyFinderScreen.floorIconsMaster == null || PartyFinderScreen.floorIconsNormal == null) continue;
				if (dungeon.contains("Master Mode")) {
					try {
						floorSkullProperties = PartyFinderScreen.floorIconsMaster.getOrDefault(floor.toLowerCase(Locale.ENGLISH), PropertyMap.EMPTY);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				} else {
					try {
						floorSkullProperties = PartyFinderScreen.floorIconsNormal.getOrDefault(floor.toLowerCase(Locale.ENGLISH), PropertyMap.EMPTY);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}

			} else if (lowerCase.contains("dungeon:")) {
				dungeon = tooltipText.split(":")[1].trim();
			} else if (!text.getSiblings().isEmpty() && Objects.equals(text.getSiblings().getFirst().getStyle().getColor(), TextColor.fromRgb(ChatFormatting.RED.getColor())) && !lowerCase.startsWith(" ")) {
				isLocked = true;
				lockReason = text;
			} else if (lowerCase.contains("note:")) {
				String[] split = tooltipText.split(":", 2);
				if (split.length == 2) {
					note = split[1].trim();
				} else {
					note = "???";
				}
			}
		}

		if (membersIndex != -1) {
			for (int i = membersIndex, j = 0; i < membersIndex + 5; i++, j++) {
				if (i >= tooltips.size()) continue;

				Component text = tooltips.get(i);
				String memberText = text.getString();
				if (!memberText.startsWith(" ")) continue; // Member thingamajigs start with a space

				String[] parts = memberText.split(":", 2);
				String playerNameTrim = parts[0].trim();

				if (playerNameTrim.equals("Empty")) continue; // Don't care about these idiots lol

				List<Component> siblings = text.getSiblings();
				Style nameStyle = !siblings.isEmpty() ? siblings.get(Math.min(1, siblings.size() - 1)).getStyle() : text.getStyle();
				Component playerName = Component.literal(playerNameTrim).setStyle(nameStyle);
				String className = parts[1].trim().split(" ")[0];
				int classLevel = -1;
				Matcher matcher = Pattern.compile("\\((\\d+)\\)").matcher(parts[1]);
				if (matcher.find()) classLevel = Integer.parseInt(matcher.group(1));
				Player player = new Player(playerName, className, classLevel);

				client.playerSkinRenderCache().lookup(ResolvableProfile.createUnresolved(playerNameTrim)).thenAccept(
						gameProfile -> gameProfile.ifPresent(entry -> player.skinTexture = entry.playerSkin().body().texturePath()));

				if (playerNameTrim.equals(partyHost)) {
					partyLeader = player;
					j--;
				} else if (j > 3) {
					partyLeader = player;
				} else partyMembers[j] = player;
			}
		}

		if (partyLeader == null) {
			for (int i = partyMembers.length - 1; i >= 0; i--) {
				if (partyMembers[i] != null) {
					partyLeader = partyMembers[i];
					partyMembers[i] = null;
					break;
				}
			}
		}
		if (partyLeader == null) {
			partyLeader = new Player(Component.literal("Error"), "Error", -1);
		}

		client.playerSkinRenderCache().lookup(ResolvableProfile.createUnresolved(partyLeader.name.getString())).thenAccept(
				gameProfile -> gameProfile.ifPresent(entry -> partyLeaderSkin = entry.playerSkin().body().texturePath()));
	}

	@Override
	public List<? extends NarratableEntry> narratables() {
		return List.of();
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return List.of();
	}

	@Override
	public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
		int x = this.getX();
		int y = this.getY();
		int entryWidth = this.getWidth();
		int entryHeight = this.getHeight();
		Matrix3x2fStack matrices = context.pose();
		matrices.pushMatrix();
		matrices.translate(x, y);

		Font textRenderer = Minecraft.getInstance().font;
		if (hovered && !isLocked) {
			context.blit(RenderPipelines.GUI_TEXTURED, PARTY_CARD_TEXTURE_HOVER, 0, 0, 0, 0, 336, 64, 336, 64);
			if (!(this instanceof YourParty)) context.drawString(textRenderer, JOIN_TEXT, 148, 6, CommonColors.WHITE, false);
		} else context.blit(RenderPipelines.GUI_TEXTURED, PARTY_CARD_TEXTURE, 0, 0, 0, 0, 336, 64, 336, 64);
		int mouseXLocal = mouseX - x;
		int mouseYLocal = mouseY - y;

		context.drawString(textRenderer, this.partyLeader.toText(), 18, 6, CommonColors.WHITE, true);

		if (PartyFinderScreen.DEBUG) {
			context.drawString(textRenderer, String.valueOf(slotID), 166, 6, CommonColors.WHITE, true);
			if (hovered) {
				context.drawString(textRenderer, "H", 160, 6, CommonColors.WHITE, true);
			}
		}
		PlayerFaceRenderer.draw(context, partyLeaderSkin, 6, 6, 8, true, false, -1);
		for (int i = 0; i < partyMembers.length; i++) {
			Player partyMember = partyMembers[i];
			if (partyMember == null) continue;
			context.drawString(textRenderer, partyMember.toText(), 17 + 136 * (i % 2), 24 + 14 * (i / 2), CommonColors.WHITE);
			PlayerFaceRenderer.draw(context, partyMember.skinTexture, 6 + 136 * (i % 2), 24 + 14 * (i / 2), 8, true, false, -1);
		}

		if (minClassLevel > 0) {
			context.drawString(textRenderer, Component.nullToEmpty("Class " + minClassLevel), 278, 25, CommonColors.WHITE);
			if (!isLocked && hovered && mouseXLocal >= 276 && mouseXLocal <= 331 && mouseYLocal >= 22 && mouseYLocal <= 35) {
				context.setTooltipForNextFrame(textRenderer, Component.translatable("skyblocker.partyFinder.partyCard.minClassLevel", minClassLevel), mouseX, mouseY);
			}
		}

		if (minCatacombsLevel > 0) {
			context.drawString(textRenderer, Component.nullToEmpty("Cata " + minCatacombsLevel), 278, 43, CommonColors.WHITE);
			if (!isLocked && hovered && mouseXLocal >= 276 && mouseXLocal <= 331 && mouseYLocal >= 40 && mouseYLocal <= 53) {
				context.setTooltipForNextFrame(textRenderer, Component.translatable("skyblocker.partyFinder.partyCard.minDungeonLevel", minCatacombsLevel), mouseX, mouseY);
			}
		}
		ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
		stack.set(DataComponents.PROFILE, SKULL_CACHE.computeIfAbsent("SkyblockerCustomPFSkull" + dungeon + floor, name -> ResolvableProfile.createResolved(new GameProfile(UUID.randomUUID(), name, floorSkullProperties))));
		context.renderItem(stack, 317, 3);

		int textWidth = textRenderer.width(floor);
		context.drawString(textRenderer, floor, 314 - textWidth, 7, 0xA0000000, false);

		context.drawString(textRenderer, note, 5, 52, CommonColors.WHITE, true);

		if (isLocked) {
			context.fill(0, 0, entryWidth, entryHeight, 0x90000000); // darken
			matrices.pushMatrix();
			matrices.translate((float) entryWidth / 2, (float) entryHeight / 2);

			int lockWidth = textRenderer.width(lockReason) + 6; // 3 px padding on both sides
			int textHeight = textRenderer.lineHeight;

			// The locked text can sometimes overlap with player names, so a background is drawn to make keep it visible.
			context.fill(-lockWidth / 2, -2, lockWidth / 2, textHeight, 0x7F000000); // Colors.BLACK with 1/2 alpha
			context.drawCenteredString(textRenderer, lockReason, 0, 0, CommonColors.SOFT_RED);

			matrices.popMatrix();
		}

		matrices.popMatrix();

	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		//System.out.println("To be clicked" + slotID);
		if (slotID == -1) {
			PartyFinderScreen.LOGGER.error("[Skyblocker] Slot ID is null for " + partyLeader.name.getString() + "'s party");
		}
		if (click.button() == 0 && !screen.isWaitingForServer() && slotID != -1) {
			screen.clickAndWaitForServer(slotID);
			return true;
		}
		return super.mouseClicked(click, doubled);
	}

	public static class Player {
		public final Component name;
		public final String dungeonClass;
		public final int classLevel;
		public ResourceLocation skinTexture = DefaultPlayerSkin.getDefaultTexture();

		Player(Component name, String dungeonClass, int classLevel) {
			this.name = name;
			this.dungeonClass = dungeonClass;
			this.classLevel = classLevel;
		}

		public Component toText() {
			char dClass = dungeonClass.isEmpty() ? '?' : dungeonClass.charAt(0);
			return name.copy().append(Component.literal(" " + dClass + " " + classLevel).withStyle(ChatFormatting.YELLOW));
		}
	}

	public static class NoParties extends PartyEntry {

		public NoParties() {
			super(Component.empty(), List.of(), null, -1);
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
			return false;
		}

		@Override
		public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			Font textRenderer = Minecraft.getInstance().font;
			context.drawCenteredString(textRenderer, Component.translatable("skyblocker.partyFinder.noParties"), this.getX() + this.getWidth() / 2, this.getY() + this.getHeight() / 2 - textRenderer.lineHeight / 2, CommonColors.WHITE);
		}
	}

	public static class YourParty extends PartyEntry {
		public static final Component DE_LIST_TEXT = Component.translatable("skyblocker.partyFinder.deList");
		public static final Component YOUR_PARTY_TEXT = Component.translatable("skyblocker.partyFinder.yourParty");

		public YourParty(Component title, List<Component> tooltips, PartyFinderScreen screen, int deListSlotId) {
			super(title, tooltips, screen, deListSlotId);
		}

		@Override
		public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			super.renderContent(context, mouseX, mouseY, hovered, deltaTicks);

			Matrix3x2fStack matrices = context.pose();
			matrices.pushMatrix();
			matrices.translate(this.getX(), this.getY());

			hovered = hovered & slotID != -1;

			Font textRenderer = Minecraft.getInstance().font;
			context.drawString(textRenderer, hovered ? DE_LIST_TEXT : YOUR_PARTY_TEXT, 148, 6, CommonColors.WHITE, false);

			matrices.popMatrix();
		}
	}
}
