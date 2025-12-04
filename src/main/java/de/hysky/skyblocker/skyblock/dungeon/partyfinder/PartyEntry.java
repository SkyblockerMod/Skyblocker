package de.hysky.skyblocker.skyblock.dungeon.partyfinder;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import de.hysky.skyblocker.SkyblockerMod;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joml.Matrix3x2fStack;

public class PartyEntry extends ElementListWidget.Entry<PartyEntry> {
	private static final Identifier PARTY_CARD_TEXTURE = SkyblockerMod.id("textures/gui/party_card.png");
	private static final Identifier PARTY_CARD_TEXTURE_HOVER = SkyblockerMod.id("textures/gui/party_card_hover.png");
	private static final Map<String, ProfileComponent> SKULL_CACHE = new Object2ObjectOpenHashMap<>();
	private static final Pattern NUMBERS_PATTERN = Pattern.compile("\\d+$");

	public static final Text JOIN_TEXT = Text.translatable("skyblocker.partyFinder.join");
	protected final PartyFinderScreen screen;
	protected final int slotID;

	Player partyLeader;
	String floor = "???";
	String dungeon = "???";
	String note = "";
	PropertyMap floorSkullProperties = PropertyMap.EMPTY;
	Identifier partyLeaderSkin = DefaultSkinHelper.getTexture();
	Player[] partyMembers = new Player[4];

	int minClassLevel = -1;
	int minCatacombsLevel = -1;

	public boolean isLocked() {
		return isLocked;
	}

	boolean isLocked = false;
	Text lockReason = Text.empty();


	public PartyEntry(Text title, List<Text> tooltips, PartyFinderScreen screen, int slotID) {
		this.screen = screen;
		this.slotID = slotID;

		Arrays.fill(partyMembers, null);
		if (tooltips.isEmpty()) return;
		//System.out.println(tooltips);

		MinecraftClient client = MinecraftClient.getInstance();
		String partyHost = title.getString().split("'s")[0];

		int membersIndex = -1;
		for (int i = 0; i < tooltips.size(); i++) {
			Text text = tooltips.get(i);
			String tooltipText = Formatting.strip(text.getString());
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
			} else if (!text.getSiblings().isEmpty() && Objects.equals(text.getSiblings().getFirst().getStyle().getColor(), TextColor.fromRgb(Formatting.RED.getColorValue())) && !lowerCase.startsWith(" ")) {
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

				Text text = tooltips.get(i);
				String memberText = text.getString();
				if (!memberText.startsWith(" ")) continue; // Member thingamajigs start with a space

				String[] parts = memberText.split(":", 2);
				String playerNameTrim = parts[0].trim();

				if (playerNameTrim.equals("Empty")) continue; // Don't care about these idiots lol

				List<Text> siblings = text.getSiblings();
				Style nameStyle = !siblings.isEmpty() ? siblings.get(Math.min(1, siblings.size() - 1)).getStyle() : text.getStyle();
				Text playerName = Text.literal(playerNameTrim).setStyle(nameStyle);
				String className = parts[1].trim().split(" ")[0];
				int classLevel = -1;
				Matcher matcher = Pattern.compile("\\((\\d+)\\)").matcher(parts[1]);
				if (matcher.find()) classLevel = Integer.parseInt(matcher.group(1));
				Player player = new Player(playerName, className, classLevel);

				client.getPlayerSkinCache().getFuture(ProfileComponent.ofDynamic(playerNameTrim)).thenAccept(
						gameProfile -> gameProfile.ifPresent(entry -> player.skinTexture = entry.getTextures().body().texturePath()));

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
			partyLeader = new Player(Text.literal("Error"), "Error", -1);
		}

		client.getPlayerSkinCache().getFuture(ProfileComponent.ofDynamic(partyLeader.name.getString())).thenAccept(
				gameProfile -> gameProfile.ifPresent(entry -> partyLeaderSkin = entry.getTextures().body().texturePath()));
	}

	@Override
	public List<? extends Selectable> selectableChildren() {
		return List.of();
	}

	@Override
	public List<? extends Element> children() {
		return List.of();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
		int x = this.getX();
		int y = this.getY();
		int entryWidth = this.getWidth();
		int entryHeight = this.getHeight();
		Matrix3x2fStack matrices = context.getMatrices();
		matrices.pushMatrix();
		matrices.translate(x, y);

		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		if (hovered && !isLocked) {
			context.drawTexture(RenderPipelines.GUI_TEXTURED, PARTY_CARD_TEXTURE_HOVER, 0, 0, 0, 0, 336, 64, 336, 64);
			if (!(this instanceof YourParty)) context.drawText(textRenderer, JOIN_TEXT, 148, 6, Colors.WHITE, false);
		} else context.drawTexture(RenderPipelines.GUI_TEXTURED, PARTY_CARD_TEXTURE, 0, 0, 0, 0, 336, 64, 336, 64);
		int mouseXLocal = mouseX - x;
		int mouseYLocal = mouseY - y;

		context.drawText(textRenderer, this.partyLeader.toText(), 18, 6, Colors.WHITE, true);

		if (PartyFinderScreen.DEBUG) {
			context.drawText(textRenderer, String.valueOf(slotID), 166, 6, Colors.WHITE, true);
			if (hovered) {
				context.drawText(textRenderer, "H", 160, 6, Colors.WHITE, true);
			}
		}
		PlayerSkinDrawer.draw(context, partyLeaderSkin, 6, 6, 8, true, false, -1);
		for (int i = 0; i < partyMembers.length; i++) {
			Player partyMember = partyMembers[i];
			if (partyMember == null) continue;
			context.drawTextWithShadow(textRenderer, partyMember.toText(), 17 + 136 * (i % 2), 24 + 14 * (i / 2), Colors.WHITE);
			PlayerSkinDrawer.draw(context, partyMember.skinTexture, 6 + 136 * (i % 2), 24 + 14 * (i / 2), 8, true, false, -1);
		}

		if (minClassLevel > 0) {
			context.drawTextWithShadow(textRenderer, Text.of("Class " + minClassLevel), 278, 25, Colors.WHITE);
			if (!isLocked && hovered && mouseXLocal >= 276 && mouseXLocal <= 331 && mouseYLocal >= 22 && mouseYLocal <= 35) {
				context.drawTooltip(textRenderer, Text.translatable("skyblocker.partyFinder.partyCard.minClassLevel", minClassLevel), mouseX, mouseY);
			}
		}

		if (minCatacombsLevel > 0) {
			context.drawTextWithShadow(textRenderer, Text.of("Cata " + minCatacombsLevel), 278, 43, Colors.WHITE);
			if (!isLocked && hovered && mouseXLocal >= 276 && mouseXLocal <= 331 && mouseYLocal >= 40 && mouseYLocal <= 53) {
				context.drawTooltip(textRenderer, Text.translatable("skyblocker.partyFinder.partyCard.minDungeonLevel", minCatacombsLevel), mouseX, mouseY);
			}
		}
		ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
		stack.set(DataComponentTypes.PROFILE, SKULL_CACHE.computeIfAbsent("SkyblockerCustomPFSkull" + dungeon + floor, name -> ProfileComponent.ofStatic(new GameProfile(UUID.randomUUID(), name, floorSkullProperties))));
		context.drawItem(stack, 317, 3);

		int textWidth = textRenderer.getWidth(floor);
		context.drawText(textRenderer, floor, 314 - textWidth, 7, 0xA0000000, false);

		context.drawText(textRenderer, note, 5, 52, Colors.WHITE, true);

		if (isLocked) {
			context.fill(0, 0, entryWidth, entryHeight, 0x90000000); // darken
			matrices.pushMatrix();
			matrices.translate((float) entryWidth / 2, (float) entryHeight / 2);

			int lockWidth = textRenderer.getWidth(lockReason) + 6; // 3 px padding on both sides
			int textHeight = textRenderer.fontHeight;

			// The locked text can sometimes overlap with player names, so a background is drawn to make keep it visible.
			context.fill(-lockWidth / 2, -2, lockWidth / 2, textHeight, 0x7F000000); // Colors.BLACK with 1/2 alpha
			context.drawCenteredTextWithShadow(textRenderer, lockReason, 0, 0, Colors.LIGHT_RED);

			matrices.popMatrix();
		}

		matrices.popMatrix();

	}

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
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
		public final Text name;
		public final String dungeonClass;
		public final int classLevel;
		public Identifier skinTexture = DefaultSkinHelper.getTexture();

		Player(Text name, String dungeonClass, int classLevel) {
			this.name = name;
			this.dungeonClass = dungeonClass;
			this.classLevel = classLevel;
		}

		public Text toText() {
			char dClass = dungeonClass.isEmpty() ? '?' : dungeonClass.charAt(0);
			return name.copy().append(Text.literal(" " + dClass + " " + classLevel).formatted(Formatting.YELLOW));
		}
	}

	public static class NoParties extends PartyEntry {

		public NoParties() {
			super(Text.empty(), List.of(), null, -1);
		}

		@Override
		public boolean mouseClicked(Click click, boolean doubled) {
			return false;
		}

		@Override
		public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
			context.drawCenteredTextWithShadow(textRenderer, Text.translatable("skyblocker.partyFinder.noParties"), this.getX() + this.getWidth() / 2, this.getY() + this.getHeight() / 2 - textRenderer.fontHeight / 2, Colors.WHITE);
		}
	}

	public static class YourParty extends PartyEntry {
		public static final Text DE_LIST_TEXT = Text.translatable("skyblocker.partyFinder.deList");
		public static final Text YOUR_PARTY_TEXT = Text.translatable("skyblocker.partyFinder.yourParty");

		public YourParty(Text title, List<Text> tooltips, PartyFinderScreen screen, int deListSlotId) {
			super(title, tooltips, screen, deListSlotId);
		}

		@Override
		public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			super.render(context, mouseX, mouseY, hovered, deltaTicks);

			Matrix3x2fStack matrices = context.getMatrices();
			matrices.pushMatrix();
			matrices.translate(this.getX(), this.getY());

			hovered = hovered & slotID != -1;

			TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
			context.drawText(textRenderer, hovered ? DE_LIST_TEXT : YOUR_PARTY_TEXT, 148, 6, Colors.WHITE, false);

			matrices.popMatrix();
		}
	}
}
