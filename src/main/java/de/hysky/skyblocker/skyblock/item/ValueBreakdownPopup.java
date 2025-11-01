package de.hysky.skyblocker.skyblock.item;

import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.mixins.accessors.HandledScreenAccessor;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.TextTransformer;
import de.hysky.skyblocker.utils.networth.NetworthCalculator;
import de.hysky.skyblocker.utils.render.gui.AbstractPopupScreen;
import io.github.moulberry.repo.data.NEUItem;
import net.azureaaron.networth.Calculation;
import net.azureaaron.networth.NetworthResult;
import net.azureaaron.networth.utils.ItemConstants;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Function;

public class ValueBreakdownPopup extends AbstractPopupScreen {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Function<String, Text> EMPTY = s -> Text.empty();

    private static final Map<Calculation.Type, LayoutAppender> FORMATTERS = Map.ofEntries(
            Map.entry(Calculation.Type.REFORGE, new BasicSingleAppender(
                    Text.literal("Reforge"),
                    s -> {
                        //noinspection UnstableApiUsage
                        String neuId = ItemConstants.REFORGES.get(s);
                        if (neuId == null) return Text.literal(s);
                        NEUItem neuItem = NEURepoManager.getItemByNeuId(neuId);
                        if (neuItem == null) return Text.literal(s);
                        return TextTransformer.fromLegacy(neuItem.getDisplayName());
                    },
                    true
            )),
            Map.entry(Calculation.Type.ENCHANTMENT, new BasicListAppender(
                    Text.literal("Enchantments"),
                    s -> {
                        String neuId = ItemRepository.getBazaarStocks().get("ENCHANTMENT_" + s);
                        if (neuId == null) return Text.literal(s);
                        NEUItem neuItem = NEURepoManager.getItemByNeuId(neuId);
                        if (neuItem == null) return Text.literal(s);
                        return TextTransformer.fromLegacy(neuItem.getLore().getFirst());
                    }
            )),
            Map.entry(Calculation.Type.GEMSTONE, new BasicListAppender(
                    Text.literal("Gemstones"),
                    s -> {
                        NEUItem neuItem = NEURepoManager.getItemByNeuId(s);
                        if (neuItem == null) return Text.literal(s);
                        return TextTransformer.fromLegacy(neuItem.getDisplayName());
                    }
            )),
            Map.entry(Calculation.Type.WITHER_BLADE_SCROLL, new BasicListAppender(
                    Text.literal("Wither Blade Scrolls"),
                    s -> {
                        NEUItem neuItem = NEURepoManager.getItemByNeuId(s);
                        if (neuItem == null) return Text.literal(s);
                        return TextTransformer.fromLegacy(neuItem.getDisplayName());
                    }
            )),
            Map.entry(Calculation.Type.STAR, new BasicListAppender(
                    Text.literal("Star Upgrades"),
                    s -> {
                        NEUItem neuItem = NEURepoManager.getItemByNeuId(s);
                        if (neuItem == null) return Text.literal(s);
                        return TextTransformer.fromLegacy(neuItem.getDisplayName());
                    }
            )),
            Map.entry(Calculation.Type.MASTER_STAR, new BasicListAppender(
                    Text.literal("Master Stars"),
                    s -> {
                        NEUItem neuItem = NEURepoManager.getItemByNeuId(s);
                        if (neuItem == null) return Text.literal(s);
                        return TextTransformer.fromLegacy(neuItem.getDisplayName());
                    }
            )),
            Map.entry(Calculation.Type.HOT_POTATO_BOOK, new BasicSingleAppender(
                    Text.literal("Hot Potato Book"),
                    EMPTY,
                    true
            )),
            Map.entry(Calculation.Type.FUMING_POTATO_BOOK, new BasicSingleAppender(
                    Text.literal("Fuming Potato Book"),
                    EMPTY,
                    true
            )),
            Map.entry(Calculation.Type.ART_OF_WAR, new BasicSingleAppender(
                    Text.literal("Art of War"),
                    EMPTY,
                    true
            )),
            Map.entry(Calculation.Type.RECOMBOBULATOR, new BasicSingleAppender(
                    Text.literal("Recombobulator"),
                    EMPTY,
                    true
            )),
            Map.entry(Calculation.Type.POWER_SCROLL, new BasicSingleAppender(
                    Text.literal("Power Scroll"),
                    s -> {
                        NEUItem neuItem = NEURepoManager.getItemByNeuId(s);
                        if (neuItem == null) return Text.literal(s);
                        return TextTransformer.fromLegacy(neuItem.getDisplayName());
                    },
                    true
            ))
    );

    private final NetworthResult networthResult;
    private final EnumMap<Calculation.Type, List<Calculation>> map;
    private ScrollableLayoutWidget scrollable;

    @Init
    public static void initClass() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof HandledScreen<?> handledScreen) {
                ScreenKeyboardEvents.afterKeyPress(screen).register((screen1, key, scancode, modifiers) -> {
                    if (key != GLFW.GLFW_KEY_I) return;
                    Slot slot = ((HandledScreenAccessor) handledScreen).getFocusedSlot();
                    if (slot == null || !slot.hasStack()) return;
                    NetworthResult networth = NetworthCalculator.getItemNetworth(slot.getStack());
                    if (networth.price() > 0) client.setScreen(new ValueBreakdownPopup(screen, networth));
                });
            }
        });
    }

    protected ValueBreakdownPopup(Screen backgroundScreen, NetworthResult networthResult) {
        super(Text.empty(), backgroundScreen);
        this.networthResult = networthResult;

        this.map = new EnumMap<>(Calculation.Type.class);
        for (Calculation calculation : networthResult.calculations()) {
            map.computeIfAbsent(calculation.type(), ignored -> new ArrayList<>()).add(calculation);
        }
    }

    @Override
    protected void init() {
        DirectionalLayoutWidget layout = DirectionalLayoutWidget.vertical();
        layout.add(new TextWidget(Text.literal("Base Item Price: ").append(getCoinsText(networthResult.base())), textRenderer));
        for (Map.Entry<Calculation.Type, List<Calculation>> entry : map.entrySet()) {
            layout.add(EmptyWidget.ofHeight(5));
            LayoutAppender appender = FORMATTERS.get(entry.getKey());
            if (appender != null) {
                appender.appendTo(entry.getValue(), layout);
                continue;
            }
            layout.add(new TextWidget(Text.literal(entry.getKey().toString()), textRenderer));
            for (Calculation calculation : entry.getValue()) {
                layout.add(new TextWidget(Text.literal(calculation.id() + ": " + calculation.price() + " coins"), textRenderer), p -> p.marginLeft(20));
            }
        }
        layout.add(EmptyWidget.ofHeight(10));
        layout.add(new TextWidget(Text.literal("Total: ").append(getCoinsText(networthResult.price())), textRenderer), Positioner::alignRight);
        scrollable = new ScrollableLayoutWidget(client, layout, 300);
        scrollable.setHeight(200);
        scrollable.forEachChild(this::addDrawableChild);
        super.init();
    }

    @Override
    protected void refreshWidgetPositions() {
        super.refreshWidgetPositions();
        scrollable.refreshPositions();
        scrollable.setPosition((width - scrollable.getWidth()) / 2, (height - scrollable.getHeight()) / 2);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        drawPopupBackground(context, scrollable.getX(), scrollable.getY(), scrollable.getWidth(), scrollable.getHeight());
    }

    private static Text getCoinsText(double price) {
        return Text.literal(Formatters.DOUBLE_NUMBERS.format(price) + " coins").formatted(Formatting.GOLD);
    }

    public interface LayoutAppender {
        void appendTo(List<Calculation> calculations, DirectionalLayoutWidget layout);

        default void appendCountAndPrice(Calculation calc, MutableText empty) {
            if (calc.count() > 1)
                empty.append(Text.literal(" x").formatted(Formatting.GRAY)).append(Text.literal(String.valueOf(calc.count())).formatted(Formatting.YELLOW));
            empty.append(Text.literal(" (").formatted(Formatting.GRAY));
            empty.append(getCoinsText(calc.price()));
            empty.append(Text.literal(")").formatted(Formatting.GRAY));
        }
    }

    private record BasicSingleAppender(Text displayName, Function<String, Text> idFormatter,
                                       boolean hideIfWorthNothing) implements LayoutAppender {
        @Override
        public void appendTo(List<Calculation> calculations, DirectionalLayoutWidget layout) {
            if (calculations.size() > 1) {
                LOGGER.warn("More than one calculation was found for type {}", calculations.getFirst().type());
            }
            Calculation calc = calculations.getFirst();
            if (calc.price() <= 0 && hideIfWorthNothing) return;
            MutableText empty = Text.empty();
            empty.append(displayName());
            Text apply = idFormatter.apply(calc.id());
            if (!apply.getString().isBlank()) empty.append(": ").append(apply);
            appendCountAndPrice(calc, empty);
            layout.add(new TextWidget(empty, MinecraftClient.getInstance().textRenderer));
        }
    }

    private record BasicListAppender(Text displayName, Function<String, Text> idFormatter) implements LayoutAppender {
        @Override
        public void appendTo(List<Calculation> calculations, DirectionalLayoutWidget layout) {
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            layout.add(new TextWidget(displayName, textRenderer));
            double total = 0;
            for (Calculation calc : calculations) {
                MutableText empty = Text.empty();
                Text apply = idFormatter.apply(calc.id());
                empty.append(apply);
                appendCountAndPrice(calc, empty);
                total += calc.price();
                layout.add(new TextWidget(empty, textRenderer), p -> p.marginLeft(15));
            }
            layout.add(new TextWidget(Text.literal("Total: ").append(getCoinsText(total)), textRenderer), p -> p.marginLeft(10));
        }
    }
}
