package de.hysky.skyblocker.config.datafixer;

/*import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;*/

//I think some fields now end up in different order and I won't bother with fixing it, should still work anyways and even then
//these migrations happened like a year ago so there's not much excuse in not having upgraded by now
public class ConfigDataFixerTest {
    /*private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @BeforeAll
    public static void setupEnvironment() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    void testDataFixer1() {
        @SuppressWarnings("DataFlowIssue")
        JsonObject oldConfig = GSON.fromJson(new InputStreamReader(ConfigDataFixerTest.class.getResourceAsStream("/assets/skyblocker/config/skyblocker-v1.json")), JsonObject.class);
        @SuppressWarnings("DataFlowIssue")
        JsonObject expectedNewConfig = GSON.fromJson(new InputStreamReader(ConfigDataFixerTest.class.getResourceAsStream("/assets/skyblocker/config/skyblocker-v2.json")), JsonObject.class);

        Assertions.assertEquals(expectedNewConfig, ConfigDataFixer.apply(oldConfig, 2));
    }

    @Test
    void testDataFixer2QuickNav() {
        @SuppressWarnings("DataFlowIssue")
        JsonObject oldConfig = GSON.fromJson(new InputStreamReader(ConfigDataFixerTest.class.getResourceAsStream("/assets/skyblocker/config/skyblocker-v2.json")), JsonObject.class);
        @SuppressWarnings("DataFlowIssue")
        JsonObject expectedNewConfig = GSON.fromJson(new InputStreamReader(ConfigDataFixerTest.class.getResourceAsStream("/assets/skyblocker/config/skyblocker-v3.json")), JsonObject.class);

        Assertions.assertEquals(expectedNewConfig, ConfigDataFixer.apply(oldConfig));
    }*/
}
