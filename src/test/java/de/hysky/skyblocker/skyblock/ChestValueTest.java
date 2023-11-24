package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.config.SkyblockerConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ChestValueTest {
    @Test
    void testProfitText() {
        SkyblockerConfig.DungeonChestProfit dCPConfig = new SkyblockerConfig.DungeonChestProfit();
        Assertions.assertEquals("literal{ 0 Coins}[style={color=dark_gray}]", ChestValue.getProfitText(0, false, dCPConfig.neutralThreshold, dCPConfig.neutralColor, dCPConfig.profitColor, dCPConfig.lossColor, dCPConfig.incompleteColor).toString());
        Assertions.assertEquals("literal{ 0 Coins}[style={color=blue}]", ChestValue.getProfitText(0, true, dCPConfig.neutralThreshold, dCPConfig.neutralColor, dCPConfig.profitColor, dCPConfig.lossColor, dCPConfig.incompleteColor).toString());
        Assertions.assertEquals("literal{ +10,000 Coins}[style={color=dark_green}]", ChestValue.getProfitText(10000, false, dCPConfig.neutralThreshold, dCPConfig.neutralColor, dCPConfig.profitColor, dCPConfig.lossColor, dCPConfig.incompleteColor).toString());
        Assertions.assertEquals("literal{ +10,000 Coins}[style={color=blue}]", ChestValue.getProfitText(10000, true, dCPConfig.neutralThreshold, dCPConfig.neutralColor, dCPConfig.profitColor, dCPConfig.lossColor, dCPConfig.incompleteColor).toString());
        Assertions.assertEquals("literal{ -10,000 Coins}[style={color=red}]", ChestValue.getProfitText(-10000, false, dCPConfig.neutralThreshold, dCPConfig.neutralColor, dCPConfig.profitColor, dCPConfig.lossColor, dCPConfig.incompleteColor).toString());
        Assertions.assertEquals("literal{ -10,000 Coins}[style={color=blue}]", ChestValue.getProfitText(-10000, true, dCPConfig.neutralThreshold, dCPConfig.neutralColor, dCPConfig.profitColor, dCPConfig.lossColor, dCPConfig.incompleteColor).toString());
        SkyblockerConfig.ChestValue cVConfig = new SkyblockerConfig.ChestValue();
        Assertions.assertEquals("literal{ 10,000 Coins}[style={color=dark_green}]", ChestValue.getValueText(10000, false, cVConfig.color, cVConfig.incompleteColor).toString());
        Assertions.assertEquals("literal{ 10,000 Coins}[style={color=blue}]", ChestValue.getValueText(10000, true, cVConfig.color, cVConfig.incompleteColor).toString());
    }
}
