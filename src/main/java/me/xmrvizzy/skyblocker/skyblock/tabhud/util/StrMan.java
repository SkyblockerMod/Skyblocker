package me.xmrvizzy.skyblocker.skyblock.tabhud.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class StrMan {

    private static final Logger LOGGER = LoggerFactory.getLogger(StrMan.class.getName());
    private static final Text ERROR_TXT = Text.literal("[ERROR]").formatted(Formatting.RED, Formatting.BOLD);

    public static Text stdEntry(List<PlayerListEntry> ple, int idx, String entryName, Formatting contentFmt) {
        Text txt = ple.get(idx).getDisplayName();
        if (txt == null) {
            return ERROR_TXT;
        }
        String src = txt.getString();
        src = src.substring(src.indexOf(':') + 1);
        return StrMan.stdEntry(src, entryName, contentFmt);
    }

    public static Text stdEntry(String entryContent, String entryName, Formatting contentFmt) {
        return Text.literal(entryName).append(Text.literal(entryContent).formatted(contentFmt));
    }

    public static Text plainEntry(List<PlayerListEntry> ple, int idx) {
        Text txt = ple.get(idx).getDisplayName();
        if (txt == null) {
            return ERROR_TXT;
        }
        return Text.of(txt.getString().trim());
    }

    public static Matcher regexAt(List<PlayerListEntry> ple, int idx, Pattern p) {
        Text txt = ple.get(idx).getDisplayName();
        if (txt == null) {
            return null;
        }
        String str = txt.getString();
        Matcher m = p.matcher(str);
        if (!m.matches()) {
            LOGGER.error("ERROR: Regex {} failed for input \"{}\"", p.pattern(), str);
            return null;
        } else {
            return m;
        }
    }

    public static String strAt(List<PlayerListEntry> ple, int idx) {
        Text txt = ple.get(idx).getDisplayName();
        if (txt == null) {
            return null;
        }
        return txt.getString();
    }

}
