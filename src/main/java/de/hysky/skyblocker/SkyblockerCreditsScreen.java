package de.hysky.skyblocker;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.CodecUtils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.objects.PlayerSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.component.ResolvableProfile;

public class SkyblockerCreditsScreen extends Screen {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Component TITLE = Component.literal("Skyblocker Credits");
	private static final Identifier CREDITS_FILE = SkyblockerMod.id("credits.json");
	private static final Identifier LOGO = SkyblockerMod.id("logo.png");
	private static final Identifier VIGNETTE = Identifier.withDefaultNamespace("textures/misc/credits_vignette.png");
	private static final Music SKYBLOCKER_CREDITS_MUSIC = new Music(SoundEvents.MUSIC_DISC_CREATOR, 0, 0, true);
	private static final Component SECTION_HEADING = Component.literal("================").withStyle(ChatFormatting.WHITE);
	private static final float SPEEDUP_FACTOR = 5f;
	private static final float SPEEDUP_FACTOR_FAST = 15f;
	private static final float UNMODIFIED_SCROLL_SPEED = 0.75f;
	private float scroll;
	private final List<FormattedCharSequence> lines = new ArrayList<>();
	private final List<Component> narratorComponents = new ArrayList<>();
	private final IntSet centredLines = new IntOpenHashSet();
	private int totalScrollLength;
	private boolean speedupActive;
	private final IntSet speedupModifiers = new IntOpenHashSet();
	private float scrollSpeed;
	private int direction;
	private @Nullable Screen parent;

	protected SkyblockerCreditsScreen(@Nullable Screen parent) {
		super(TITLE);
		this.direction = 1;
		this.scrollSpeed = UNMODIFIED_SCROLL_SPEED;
		this.parent = parent;
	}

	@Init
	public static void initClass() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(ClientCommandManager.literal(SkyblockerMod.NAMESPACE)
					.then(ClientCommandManager.literal("credits")
							.executes(Scheduler.queueOpenScreenCommand(() -> new SkyblockerCreditsScreen(null)))));
		});
	}

	private float calculateScrollSpeed() {
		return this.speedupActive
				? UNMODIFIED_SCROLL_SPEED * (SPEEDUP_FACTOR + this.speedupModifiers.size() * SPEEDUP_FACTOR_FAST) * this.direction
						: UNMODIFIED_SCROLL_SPEED * this.direction;
	}

	@Override
	public void tick() {
		this.minecraft.getMusicManager().tick();
		this.minecraft.getSoundManager().tick(false);
		float maxScroll = this.totalScrollLength + this.height + this.height + 24;

		if (this.scroll > maxScroll) {
			this.onClose();
		}
	}

	@Override
	public boolean keyPressed(KeyEvent keyEvent) {
		if (keyEvent.isUp()) {
			this.direction = -1;
		} else if (keyEvent.key() == GLFW.GLFW_KEY_LEFT_CONTROL || keyEvent.key() == GLFW.GLFW_KEY_RIGHT_CONTROL) {
			this.speedupModifiers.add(keyEvent.key());
		} else if (keyEvent.key() == GLFW.GLFW_KEY_SPACE) {
			this.speedupActive = true;
		}

		this.scrollSpeed = this.calculateScrollSpeed();
		return super.keyPressed(keyEvent);
	}

	@Override
	public boolean keyReleased(KeyEvent keyEvent) {
		if (keyEvent.isUp()) {
			this.direction = 1;
		}

		if (keyEvent.key() == GLFW.GLFW_KEY_SPACE) {
			this.speedupActive = false;
		} else if (keyEvent.key() == GLFW.GLFW_KEY_LEFT_CONTROL || keyEvent.key() == GLFW.GLFW_KEY_RIGHT_CONTROL) {
			this.speedupModifiers.remove(keyEvent.key());
		}

		this.scrollSpeed = this.calculateScrollSpeed();
		return super.keyReleased(keyEvent);
	}

	@Override
	public void init() {
		if (this.lines.isEmpty()) {
			Credits credits = this.readCreditsFile();
			List<String> contributors = SkyblockerMod.SKYBLOCKER_MOD.getMetadata().getContributors().stream()
					.map(Person::getName)
					.toList();

			this.addCreditsLine(SECTION_HEADING, true, false);
			this.addCreditsLine(Component.literal("Authors").withStyle(ChatFormatting.YELLOW), true, true);
			this.addCreditsLine(SECTION_HEADING, true, false);
			this.addEmptyLine();
			this.addEmptyLine();

			for (Author author : credits.authors()) {
				this.addCreditsLine(author.asText(), false, true);
				this.addEmptyLine();
			}

			this.addEmptyLine();
			this.addEmptyLine();
			this.addCreditsLine(SECTION_HEADING, true, false);
			this.addCreditsLine(Component.literal("Creative Team").withStyle(ChatFormatting.YELLOW), true, true);
			this.addCreditsLine(SECTION_HEADING, true, false);
			this.addEmptyLine();
			this.addEmptyLine();

			for (String member : credits.creativeTeam()) {
				this.addCreditsLine(Component.literal(member).withStyle(ChatFormatting.WHITE), false, true);
				this.addEmptyLine();
			}

			this.addEmptyLine();
			this.addEmptyLine();
			this.addCreditsLine(SECTION_HEADING, true, false);
			this.addCreditsLine(Component.literal("Code Contributors").withStyle(ChatFormatting.YELLOW), true, true);
			this.addCreditsLine(SECTION_HEADING, true, false);
			this.addEmptyLine();
			this.addEmptyLine();

			for (String contributor : contributors) {
				this.addCreditsLine(Component.literal(contributor).withStyle(ChatFormatting.WHITE), false, true);
			}
			this.addCreditsLine(Component.literal("and any other code contributors not listed").withStyle(ChatFormatting.WHITE), false, true);
			this.addEmptyLine();

			this.addEmptyLine();
			this.addEmptyLine();
			this.addCreditsLine(SECTION_HEADING, true, false);
			this.addCreditsLine(Component.literal("Translations").withStyle(ChatFormatting.YELLOW), true, true);
			this.addCreditsLine(SECTION_HEADING, true, false);
			this.addEmptyLine();
			this.addEmptyLine();

			for (Translation translation : credits.translations()) {
				this.addCreditsLine(Component.literal(translation.name()).withStyle(ChatFormatting.GRAY), false, true);

				for (String translator : translation.translators()) {
					Component text = Component.empty()
							.append("           ")
							.append(Component.literal(translator).withStyle(ChatFormatting.WHITE));
					this.addCreditsLine(text, false, true);
				}

				this.addEmptyLine();
			}

			this.addEmptyLine();
			this.addEmptyLine();
			this.addCreditsLine(SECTION_HEADING, true, false);
			this.addCreditsLine(Component.literal("Libraries").withStyle(ChatFormatting.YELLOW), true, true);
			this.addCreditsLine(SECTION_HEADING, true, false);
			this.addEmptyLine();
			this.addEmptyLine();

			for (Library library : credits.libraries()) {
				Component libraryCreditText = Component.empty()
						.append("           ")
						.append(Component.literal(library.credit()).withStyle(ChatFormatting.WHITE));
				this.addCreditsLine(Component.literal(library.name()).withStyle(ChatFormatting.GRAY), false, true);
				this.addCreditsLine(libraryCreditText, false, true);
				this.addEmptyLine();
			}

			this.addEmptyLine();
			this.addEmptyLine();
			this.addCreditsLine(Component.literal("Special thanks to all of our users!").withStyle(ChatFormatting.YELLOW), true, true);

			this.totalScrollLength = this.lines.size() * 12;
		}
	}

	@Override
	public Component getNarrationMessage() {
		return CommonComponents.joinForNarration(this.narratorComponents.toArray(Component[]::new));
	}

	private Credits readCreditsFile() {
		try (BufferedReader reader = this.minecraft.getResourceManager().openAsReader(CREDITS_FILE)) {
			return Credits.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).getOrThrow();
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Credits] Failed to load credits file.", e);
		}

		return Credits.EMPTY;
	}

	private void addEmptyLine() {
		this.lines.add(FormattedCharSequence.EMPTY);
		this.narratorComponents.add(CommonComponents.EMPTY);
	}

	private void addCreditsLine(Component text, boolean centred, boolean narratable) {
		if (centred) {
			this.centredLines.add(this.lines.size());
		}

		this.lines.add(text.getVisualOrderText());

		if (narratable) {
			this.narratorComponents.add(text);
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float a) {
		super.render(graphics, mouseX, mouseY, a);
		this.renderVignette(graphics);

		this.scroll = Math.max(0f, this.scroll + a * this.scrollSpeed);
		int logoX = this.width / 2 - 128;
		int logoY = this.height + 50;
		float yOffs = -this.scroll;

		graphics.pose().pushMatrix();
		graphics.pose().translate(0f, yOffs);
		graphics.nextStratum();

		this.renderLogo(graphics, this.width, logoY);
		int yPos = logoY + 100;

		for (int i = 0; i < this.lines.size(); i++) {
			if (i == this.lines.size() - 1) {
				float diff = yPos + yOffs - (this.height / 2 - 6);

				if (diff < 0f) {
					graphics.pose().translate(0f, -diff);
				}
			}

			if (yPos + yOffs + 12f + 8f > 0f && yPos + yOffs < this.height) {
				FormattedCharSequence line = this.lines.get(i);

				if (this.centredLines.contains(i)) {
					graphics.drawCenteredString(this.font, line, logoX + 128, yPos, CommonColors.WHITE);
				} else {
					graphics.drawString(this.font, line, logoX, yPos, CommonColors.WHITE);
				}
			}

			yPos += 12;
		}

		graphics.pose().popMatrix();
	}

	private void renderVignette(GuiGraphics graphics) {
		graphics.blit(RenderPipelines.VIGNETTE, VIGNETTE, 0, 0, 0f, 0f, this.width, this.height, this.width, this.height);
	}

	private void renderLogo(GuiGraphics graphics, int width, int heightOffset) {
		int logoX = width / 2 - 128;
		graphics.blit(RenderPipelines.GUI_TEXTURED, LOGO, logoX, heightOffset, 0f, 0f, 256, 64, 256, 64, CommonColors.WHITE);
	}

	@Override
	protected void renderMenuBackground(GuiGraphics graphics, int x, int y, int width, int height) {
		float v = this.scroll * 0.5f;
		Screen.renderMenuBackgroundTexture(graphics, Screen.MENU_BACKGROUND, 0, 0, 0f, v, width, height);
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.parent);
	}

	@Override
	public void removed() {
		this.minecraft.getMusicManager().stopPlaying(SKYBLOCKER_CREDITS_MUSIC);
	}

	@Override
	public Music getBackgroundMusic() {
		return SKYBLOCKER_CREDITS_MUSIC;
	}

	private record Credits(List<Author> authors, List<String> creativeTeam, List<Translation> translations, List<Library> libraries) {
		private static final Codec<Credits> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Author.CODEC.listOf().fieldOf("authors").forGetter(Credits::authors),
				Codec.STRING.listOf().fieldOf("creativeTeam").forGetter(Credits::creativeTeam),
				Translation.CODEC.listOf().fieldOf("translations").forGetter(Credits::translations),
				Library.CODEC.listOf().fieldOf("libraries").forGetter(Credits::libraries)
				).apply(instance, Credits::new));
		private static final Credits EMPTY = new Credits(List.of(new Author("Error", UUID.fromString("647ffd4c-f99c-4b06-a8f8-66cf1a587e57"), CommonColors.RED)), List.of(), List.of(), List.of());
	}

	private record Author(String name, UUID uuid, int colour) {
		private static final Codec<Author> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("name").forGetter(Author::name),
				UUIDUtil.STRING_CODEC.fieldOf("uuid").forGetter(Author::uuid),
				CodecUtils.STRING_RGB_COLOR.optionalFieldOf("colour", CommonColors.WHITE).forGetter(Author::colour)
				).apply(instance, Author::new));

		public Component asText() {
			// Preload profile so the texture is immediately available
			ResolvableProfile profile = ResolvableProfile.createUnresolved(this.uuid);
			Minecraft.getInstance().playerSkinRenderCache().lookup(profile);

			return Component.empty()
					.append(Component.object(new PlayerSprite(profile, true)))
					.append(Component.literal(" " + this.name).withColor(this.colour));
		}
	}

	private record Translation(String code, String name, List<String> translators) {
		private static final Codec<Translation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("code").forGetter(Translation::code),
				Codec.STRING.fieldOf("name").forGetter(Translation::name),
				CodecUtils.sortedListCodec(Codec.STRING, String::compareToIgnoreCase).fieldOf("translators").forGetter(Translation::translators)
				).apply(instance, Translation::new));
	}

	private record Library(String name, String credit) {
		private static final Codec<Library> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("name").forGetter(Library::name),
				Codec.STRING.fieldOf("credit").forGetter(Library::credit)
				).apply(instance, Library::new));
	}
}
