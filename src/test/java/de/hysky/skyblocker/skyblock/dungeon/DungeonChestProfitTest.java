package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.config.SkyblockerConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DungeonChestProfitTest {
    @Test
    void testProfitText() {
        SkyblockerConfig.DungeonChestProfit config = new SkyblockerConfig.DungeonChestProfit();
        Assertions.assertEquals("literal{ 0}[style={color=dark_gray}]", DungeonChestProfit.getProfitText(0, false, config.neutralThreshold, config.neutralColor.formatting, config.profitColor.formatting, config.lossColor.formatting, config.incompleteColor.formatting).toString());
        Assertions.assertEquals("literal{ 0}[style={color=blue}]", DungeonChestProfit.getProfitText(0, true, config.neutralThreshold, config.neutralColor.formatting, config.profitColor.formatting, config.lossColor.formatting, config.incompleteColor.formatting).toString());
        Assertions.assertEquals("literal{ +10}[style={color=dark_gray}]", DungeonChestProfit.getProfitText(10, false, config.neutralThreshold, config.neutralColor.formatting, config.profitColor.formatting, config.lossColor.formatting, config.incompleteColor.formatting).toString());
        Assertions.assertEquals("literal{ +10}[style={color=blue}]", DungeonChestProfit.getProfitText(10, true, config.neutralThreshold, config.neutralColor.formatting, config.profitColor.formatting, config.lossColor.formatting, config.incompleteColor.formatting).toString());
        Assertions.assertEquals("literal{ -10}[style={color=dark_gray}]", DungeonChestProfit.getProfitText(-10, false, config.neutralThreshold, config.neutralColor.formatting, config.profitColor.formatting, config.lossColor.formatting, config.incompleteColor.formatting).toString());
        Assertions.assertEquals("literal{ -10}[style={color=blue}]", DungeonChestProfit.getProfitText(-10, true, config.neutralThreshold, config.neutralColor.formatting, config.profitColor.formatting, config.lossColor.formatting, config.incompleteColor.formatting).toString());
        Assertions.assertEquals("literal{ +10,000}[style={color=dark_green}]", DungeonChestProfit.getProfitText(10000, false, config.neutralThreshold, config.neutralColor.formatting, config.profitColor.formatting, config.lossColor.formatting, config.incompleteColor.formatting).toString());
        Assertions.assertEquals("literal{ +10,000}[style={color=blue}]", DungeonChestProfit.getProfitText(10000, true, config.neutralThreshold, config.neutralColor.formatting, config.profitColor.formatting, config.lossColor.formatting, config.incompleteColor.formatting).toString());
        Assertions.assertEquals("literal{ -10,000}[style={color=red}]", DungeonChestProfit.getProfitText(-10000, false, config.neutralThreshold, config.neutralColor.formatting, config.profitColor.formatting, config.lossColor.formatting, config.incompleteColor.formatting).toString());
        Assertions.assertEquals("literal{ -10,000}[style={color=blue}]", DungeonChestProfit.getProfitText(-10000, true, config.neutralThreshold, config.neutralColor.formatting, config.profitColor.formatting, config.lossColor.formatting, config.incompleteColor.formatting).toString());
    }
}
