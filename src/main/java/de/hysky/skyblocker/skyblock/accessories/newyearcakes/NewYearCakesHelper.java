package de.hysky.skyblocker.skyblock.accessories.newyearcakes;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewYearCakesHelper extends SimpleContainerSolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(NewYearCakesHelper.class);
    private static final Pattern NEW_YEAR_CAKE = Pattern.compile("New Year Cake \\(Year (?<year>\\d+)\\)");
    private static final Pattern NEW_YEAR_CAKE_PURCHASE = Pattern.compile("You purchased New Year Cake \\(Year (?<year>\\d+)\\) for .+ coins!");
	public static final NewYearCakesHelper INSTANCE = new NewYearCakesHelper();
    private final Map<String, IntSet> cakes = new HashMap<>();

    private NewYearCakesHelper() {
        super("Auctions: \".*\"");
        ClientReceiveMessageEvents.ALLOW_GAME.register(this::onChatMessage);
    }

    public static int getCakeYear(ItemStack stack) {
        return getCakeYear(NEW_YEAR_CAKE, stack.getName().getString());
    }

    public static int getCakeYear(Pattern pattern, String name) {
        Matcher matcher = pattern.matcher(name);
        if (matcher.matches()) {
            try {
                return Formatters.INTEGER_NUMBERS.parse(matcher.group("year")).intValue();
            } catch (ParseException e) {
                LOGGER.info("Failed to parse year from New Year Cake: " + name, e);
            }
        }
        return -1;
    }

    @Override
    public boolean isEnabled() {
        return SkyblockerConfigManager.get().helpers.enableNewYearCakesHelper;
    }

    public boolean addCake(int year) {
        if (year < 0) return false;
        return cakes.computeIfAbsent(Utils.getProfile(), _profile -> new IntOpenHashSet()).add(year);
    }

    private boolean onChatMessage(Text message, boolean overlay) {
        if (isEnabled()) {
            addCake(getCakeYear(NEW_YEAR_CAKE_PURCHASE, message.getString()));
        }

        return true;
    }

    @Override
    public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
        String profile = Utils.getProfile();
        if (cakes.isEmpty() || !cakes.containsKey(profile) || cakes.containsKey(profile) && cakes.get(profile).isEmpty()) return List.of();
        List<ColorHighlight> highlights = new ArrayList<>();
        for (Int2ObjectMap.Entry<ItemStack> entry : slots.int2ObjectEntrySet()) {
            int year = getCakeYear(entry.getValue());
            if (year >= 0 && cakes.containsKey(profile)) {
                highlights.add(cakes.get(profile).contains(year) ? ColorHighlight.red(entry.getIntKey()) : ColorHighlight.green(entry.getIntKey()));
            }
        }
        return highlights;
    }
}
