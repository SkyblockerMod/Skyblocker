package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import java.util.Arrays;
import java.util.Comparator;

import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows a list of obtained dungeon buffs
// TODO: could be more pretty, can't be arsed atm

public class DungeonBuffWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Dungeon Buffs").formatted(Formatting.DARK_PURPLE,
            Formatting.BOLD);

    public DungeonBuffWidget(String footertext) {
        super(TITLE, Formatting.DARK_PURPLE.getColorValue());

        if (footertext == null || !footertext.contains("Dungeon Buffs")) {
            this.addComponent(new PlainTextComponent(Text.literal("No data").formatted(Formatting.GRAY)));
            this.pack();
            return;
        }

        String interesting = footertext.split("Dungeon Buffs")[1];
        String[] lines = interesting.split("\n");

        if (!lines[1].startsWith("Blessing")) {
            this.addComponent(new PlainTextComponent(Text.literal("No buffs found!").formatted(Formatting.GRAY)));
            this.pack();
            return;
        }
        
        //Filter out text unrelated to blessings
        lines = Arrays.stream(lines).filter(s -> s.contains("Blessing")).toArray(String[]::new);
        
        //Alphabetically sort the blessings
        Arrays.sort(lines, new Comparator<String>() {
        	@Override
        	public int compare(String o1, String o2) {
        		return o1.toLowerCase().compareTo(o2.toLowerCase());
        	}
        });

        for (int i = 0; i < lines.length; i++) {
            if (lines[i].length() < 3) { // empty line is §s
                break;
            }
            
            int color = getBlessingColor(lines[i]);
            
            this.addComponent(new PlainTextComponent(Text.literal(lines[i]).styled(style -> style.withColor(color))));
        }

        this.pack();
    }
    
    public int getBlessingColor(String blessing) {
    	if (blessing.contains("Life")) return Formatting.LIGHT_PURPLE.getColorValue().intValue();
    	if (blessing.contains("Power")) return Formatting.RED.getColorValue().intValue();
    	if (blessing.contains("Stone")) return Formatting.GREEN.getColorValue().intValue();
    	if (blessing.contains("Time")) return 0xafb8c1;
    	if (blessing.contains("Wisdom")) return Formatting.AQUA.getColorValue().intValue();
    	
    	return 0xffffff;
    }

}