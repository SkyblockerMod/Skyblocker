package me.xmrvizzy.skyblocker.skyblock.tabhud.screens;

import java.util.ArrayList;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.skyblock.tabhud.TabHud;
import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.genericInfo.GardenInfoScreen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.genericInfo.GenericInfoScreen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.genericInfo.GenericRiftInfoScreen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.main.CrimsonIsleScreen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.main.DungeonHubScreen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.main.DungeonScreen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.main.FarmingServerScreen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.main.GardenScreen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.main.GenericServerScreen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.main.GuestServerScreen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.main.HomeServerScreen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.main.HubServerScreen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.main.MineServerScreen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.main.ParkServerScreen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.main.RiftScreen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.playerList.DungeonPlayerScreen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.playerList.GuestPlayerScreen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.playerList.HomePlayerScreen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.playerList.PlayerListScreen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.PlayerLocator;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.Widget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class Screen {

    private ArrayList<Widget> widgets = new ArrayList<>();
    public int w, h;

    public Screen(int w, int h) {
        float scale = SkyblockerConfig.get().general.tabHud.tabHudScale / 100f;
        this.w = (int) (w / scale);
        this.h = (int) (h / scale);
    }

    public static Screen getCorrect(int w, int h, Text footer) {

        if (TabHud.genericTgl.isPressed()) {
            return Screen.correctGenericScrn(w, h, footer);
        } else if (TabHud.playerTgl.isPressed()) {
            return Screen.correctPlayerScrn(w, h, footer);
        } else {
            return Screen.correctMainScrn(w, h, footer);
        }
    }

    private static Screen correctGenericScrn(int w, int h, Text footer) {
        return switch (PlayerLocator.getPlayerLocation()) {
            case GARDEN -> new GardenInfoScreen(w, h, footer); // ok
            case THE_RIFT -> new GenericRiftInfoScreen(w, h, footer);
            case UNKNOWN -> new EmptyScreen(w, h, footer); // ok
            default -> new GenericInfoScreen(w, h, footer); // ok
        };
    }

    private static Screen correctPlayerScrn(int w, int h, Text footer) {
        return switch (PlayerLocator.getPlayerLocation()) {
            case GUEST_ISLAND -> new GuestPlayerScreen(w, h, footer); // ok
            case HOME_ISLAND, GARDEN -> new HomePlayerScreen(w, h, footer); // ok for 1 player
            case DUNGEON -> new DungeonPlayerScreen(w, h, footer);
            case UNKNOWN -> new EmptyScreen(w, h, footer); // ok
            default -> new PlayerListScreen(w, h, footer); // ok
        };
    }

    private static Screen correctMainScrn(int w, int h, Text footer) {
        return switch (PlayerLocator.getPlayerLocation()) {
            case PARK -> new ParkServerScreen(w, h, footer); // ok
            case HUB -> new HubServerScreen(w, h, footer); // ok when fire sale incoming
            case HOME_ISLAND -> new HomeServerScreen(w, h, footer); // ok
            case GUEST_ISLAND -> new GuestServerScreen(w, h, footer); // ok
            case CRYSTAL_HOLLOWS, DWARVEN_MINES -> new MineServerScreen(w, h, footer); // ok, TODO active forge
            case FARMING_ISLAND -> new FarmingServerScreen(w, h, footer);
            case DUNGEON_HUB -> new DungeonHubScreen(w, h, footer); // ok
            case DUNGEON -> new DungeonScreen(w, h, footer); // ok
            case CRIMSON_ISLE -> new CrimsonIsleScreen(w, h, footer);
            case GARDEN -> new GardenScreen(w, h, footer); // ok
            case THE_RIFT -> new RiftScreen(w, h, footer);
            case UNKNOWN -> new EmptyScreen(w, h, footer); // ok
            default -> new GenericServerScreen(w, h, footer); // ok
        };
    }

    /**
     * Add a widget to this screen
     */
    public void addWidget(Widget w) {
        widgets.add(w);
    }

    /**
     * Add many widgets to this screen
     */
    public void addWidgets(Widget... ws) {
        for (Widget w : ws) {
            widgets.add(w);
        }
    }

    public void render(DrawContext context) {
        for (Widget w : widgets) {
            w.render(context);
        }
    }

    /**
     * Stack these widgets on top of each other as determined by the lists's order
     */
    public void stackWidgetsH(Widget... list) {
        int compHeight = -5;
        for (Widget wid : list) {
            compHeight += wid.getHeight() + 5;
        }

        int y = (h - compHeight) / 2;
        for (Widget wid : list) {
            wid.setY(y);
            y += wid.getHeight() + 5;
        }
    }

    /**
     * Arrange these widgets next to each other as determined by the lists's order
     */
    public void stackWidgetsW(Widget... list) {
        // TODO not centered
        int compWidth = -5;
        for (Widget wid : list) {
            compWidth += wid.getWidth() + 5;
        }

        int x = (w - compWidth) / 2;
        for (Widget wid : list) {
            wid.setX(x);
            x += wid.getWidth() + 5;
        }
    }

    /**
     * Center a widget vertically, keeping X pos
     */
    public void centerH(Widget wid) {
        wid.setY((h - wid.getHeight()) / 2);
    }

    /**
     * Center a widget horizontally, keeping Y pos
     */
    public void centerW(Widget wid) {
        wid.setX((w - wid.getWidth()) / 2);
    }

    /**
     * Center a widget vertically and horizontally
     */
    public void center(Widget wid) {
        this.centerH(wid);
        this.centerW(wid);
    }

    /**
     * Let a widget's left border be on the screen's center, keeping Y pos
     */
    public void offCenterL(Widget wid) {
        int wHalf = this.w / 2;
        wid.setX(wHalf - 3 - wid.getWidth());
    }

    /**
     * Let a widget's right border be on the screen's center, keeping Y pos
     */
    public void offCenterR(Widget wid) {
        int wHalf = this.w / 2;
        wid.setX(wHalf + 3);
    }

    public void collideAgainstL(Widget w, Widget... others) {
        int yMin = w.getY();
        int yMax = w.getY() + w.getHeight();

        int xCor = this.w / 2;

        // assume others to be sorted top-bottom.
        for (Widget other : others) {
            if (other.getY() + other.getHeight() + 5 < yMin) {
                // too high, next one
                continue;
            }

            if (other.getY() - 5 > yMax) {
                // too low, no more collisions possible
                break;
            }

            int xPos = other.getX() - 5 - w.getWidth();
            xCor = Math.min(xCor, xPos);
        }
        w.setX(xCor);
    }

    public void collideAgainstR(Widget w, Widget... others) {
        int yMin = w.getY();
        int yMax = w.getY() + w.getHeight();

        int xCor = this.w / 2;

        // assume others to be sorted top-bottom.
        for (Widget other : others) {
            if (other.getY() + other.getHeight() + 5 < yMin) {
                // too high, next one
                continue;
            }

            if (other.getY() - 5 > yMax) {
                // too low, no more collisions possible
                break;
            }

            int xPos = other.getX() + other.getWidth() + 5;
            xCor = Math.max(xCor, xPos);
        }
        w.setX(xCor);
    }
}
