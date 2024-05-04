package de.hysky.skyblocker.skyblock.dungeon.partyfinder;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PartyEntryListWidget extends ElementListWidget<PartyEntry> {
    protected List<PartyEntry> partyEntries;

    protected boolean isActive = true;

    private String search = "";

    public static String BASE_SKULL_NBT = """
              {
              "SkullOwner": {
                "Id": [
                        1215241996,
                        -1849412511,
                        -1161255720,
                        -889217537
                      ],
                "Properties": {
                  "textures": [
                    {
                      "Value": "%TEXTURE%"
                    }
                  ]
                }
              }
            }
            """;

    public PartyEntryListWidget(MinecraftClient minecraftClient, int width, int height, int y, int itemHeight) {
        super(minecraftClient, width, height, y, itemHeight);
    }

    @Override
    public int getRowWidth() {
        return 336;
    }

    public void setEntries(List<PartyEntry> partyEntries) {
        this.partyEntries = partyEntries;
        updateDisplay();
    }

    public void updateDisplay() {
        List<PartyEntry> entries = new ArrayList<>(partyEntries);
        entries.removeIf(partyEntry -> !partyEntry.note.toLowerCase().contains(search) && !(partyEntry instanceof PartyEntry.YourParty));
        entries.sort(Comparator.comparing(PartyEntry::isLocked));
        entries.sort(Comparator.comparing(partyEntry -> !(partyEntry instanceof PartyEntry.YourParty)));
        if (entries.isEmpty() && !partyEntries.isEmpty()) {
            entries.add(new PartyEntry.NoParties());
        }
        replaceEntries(entries);
    }

    public void setSearch(String s) {
        search = s.toLowerCase();
        updateDisplay();
    }

    @Override
    protected int getScrollbarX() {
        return this.width / 2 + getRowWidth() / 2 + 2;
    }


    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return false;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        //context.drawGuiTexture(BACKGROUND_TEXTURE, x, top-8, getRowWidth()+16+6, bottom-top+16);

        if (children().isEmpty()) {
            Text string = Text.translatable("skyblocker.partyFinder.loadingError");
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            context.drawTextWrapped(textRenderer, string, getRowLeft(), getY() + 10, getRowWidth(), 0xFFFFFFFF);
        } else super.renderWidget(context, mouseX, mouseY, delta);
    }

    @Override
    protected void drawHeaderAndFooterSeparators(DrawContext context) {
    }

    @Override
    protected void drawMenuListBackground(DrawContext context) {
    }
}
