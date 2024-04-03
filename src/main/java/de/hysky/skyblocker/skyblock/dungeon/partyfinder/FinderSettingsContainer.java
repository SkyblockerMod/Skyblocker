package de.hysky.skyblocker.skyblock.dungeon.partyfinder;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

import de.hysky.skyblocker.utils.ItemUtils;

public class FinderSettingsContainer extends ContainerWidget {
    private boolean isInitialized = false;
    private OptionDropdownWidget floorSelector;
    private OptionDropdownWidget dungeonTypeSelector;
    private OptionDropdownWidget sortGroupsSelector;

    private RangedValueWidget classLevelRange;
    private RangedValueWidget dungeonLevelRange;

    private ContainerWidget currentlyOpenedOption = null;

    private final List<ContainerWidget> initializedWidgets = new ArrayList<>();


    public FinderSettingsContainer(int x, int y, int height) {
        super(x, y, 336, height, Text.empty());
    }

    @Override
    public void setDimensionsAndPosition(int width, int height, int x, int y) {
        super.setDimensionsAndPosition(width, height, x, y);
        if (this.floorSelector != null) floorSelector.setPosition(x + width / 4 - 70, y + 20);
        if (this.dungeonTypeSelector != null) dungeonTypeSelector.setPosition(x + 3 * width / 4 - 70, y + 20);
        if (this.sortGroupsSelector != null) sortGroupsSelector.setPosition(x + width / 2 - 70, y + 120);
        if (this.classLevelRange != null) classLevelRange.setPosition(x + width / 4 - 50, y + 70);
        if (this.dungeonLevelRange != null) dungeonLevelRange.setPosition(x + 3 * width / 4 - 50, y + 70);

    }

    /**
     * Handles everything in the Settings page
     * @param screen the parent Party Finder screen
     * @param inventoryName le inventory name
     * @return returns false if it doesn't know what's happening
     */
    public boolean handle(PartyFinderScreen screen, String inventoryName) {
        String nameLowerCase = inventoryName.toLowerCase();
        GenericContainerScreenHandler handler = screen.getHandler();
        if (!isInitialized) {
            if (!nameLowerCase.contains("search settings")) return false;
            isInitialized = true;
            //System.out.println("initializing");
            for (Slot slot : handler.slots) {
                if (slot.id > handler.getRows() * 9 - 1) break;
                if (!slot.hasStack()) continue;
                ItemStack stack = slot.getStack();
                //System.out.println(stack.toString());
                String name = stack.getName().getString().toLowerCase();
                if (name.contains("floor")) {

                    //System.out.println("Floor selector created");
                    this.floorSelector = new OptionDropdownWidget(screen, stack.getName(), null, getX() + getWidth() / 4 - 70, getY() + 20, 140, 170, slot.id);
                    if (!setSelectedElementFromTooltip(slot, stack, floorSelector)) return false;

                    initializedWidgets.add(floorSelector);

                } else if (name.contains("dungeon type")) {

                    this.dungeonTypeSelector = new OptionDropdownWidget(screen, stack.getName(), null, getX() + (3 * getWidth()) / 4 - 70, getY() + 20, 140, 100, slot.id);
                    if (!setSelectedElementFromTooltip(slot, stack, dungeonTypeSelector)) return false;

                    initializedWidgets.add(dungeonTypeSelector);

                } else if (name.contains("groups")) {

                    this.sortGroupsSelector = new OptionDropdownWidget(screen, stack.getName(), null, getX() + getWidth() / 2 - 70, getY() + 120, 140, 100, slot.id);
                    if (!setSelectedElementFromTooltip(slot, stack, sortGroupsSelector)) return false;

                    initializedWidgets.add(sortGroupsSelector);

                } else if (name.contains("class level")) {

                    this.classLevelRange = new RangedValueWidget(screen, stack.getName(), getX() + getWidth() / 4 - 50, getY() + 70, 100, slot.id);
                    if (!setRangeFromTooltip(stack, classLevelRange)) return false;

                    initializedWidgets.add(classLevelRange);

                } else if (name.contains("dungeon level")) {

                    this.dungeonLevelRange = new RangedValueWidget(screen, stack.getName(), getX() + 3 * (getWidth()) / 4 - 50, getY() + 70, 100, slot.id);
                    if (!setRangeFromTooltip(stack, dungeonLevelRange)) return false;

                    initializedWidgets.add(dungeonLevelRange);

                }
            }
        }
        if (nameLowerCase.contains("search settings")) {
            if (floorSelector != null) floorSelector.close();
            if (dungeonTypeSelector != null) dungeonTypeSelector.close();
            if (sortGroupsSelector != null) sortGroupsSelector.close();
            if (classLevelRange != null) classLevelRange.setState(RangedValueWidget.State.CLOSED);
            if (dungeonLevelRange != null) dungeonLevelRange.setState(RangedValueWidget.State.CLOSED);

            screen.partyFinderButton.active = true;
            currentlyOpenedOption = null;

            for (int i = (handler.getRows() - 1) * 9; i < handler.getRows() * 9; i++) {
                Slot slot = handler.slots.get(i);
                if (slot.hasStack() && slot.getStack().isOf(Items.ARROW)) {
                    screen.partyButtonSlotId = slot.id;
                }
            }
            return true;
        } else {
            screen.partyFinderButton.active = false;

            if (nameLowerCase.contains("floor")) {
                updateDropdownOptionWidget(handler, floorSelector);
                currentlyOpenedOption = floorSelector;
                return true;
            } else if (nameLowerCase.contains("select type")) {
                updateDropdownOptionWidget(handler, dungeonTypeSelector);
                currentlyOpenedOption = dungeonTypeSelector;
                return true;
            } else if (nameLowerCase.contains("class level range")) {
                updateRangedValue(handler, classLevelRange);
                return true;
            } else if (nameLowerCase.contains("dungeon level range")) {
                updateRangedValue(handler, dungeonLevelRange);
                return true;
            } else if (nameLowerCase.contains("sort")) {
                updateDropdownOptionWidget(handler, sortGroupsSelector);
                currentlyOpenedOption = sortGroupsSelector;
                return true;
            }
        }
        return false;
    }

    private int findBackSlotId(GenericContainerScreenHandler handler) {
        int backId = -1;
        for (int i = (handler.getRows() - 1) * 9; i < handler.getRows() * 9; i++) {
            Slot slot = handler.slots.get(i);
            if (slot.hasStack() && slot.getStack().isOf(Items.ARROW)) {
                backId = slot.id;
                break;
            }
        }
        return backId;
    }

    /**
     * @return true if all goes well
     */
    private boolean setRangeFromTooltip(ItemStack stack, RangedValueWidget widget) {
        for (Text text : ItemUtils.getLore(stack)) {
            String textLowerCase = text.getString().toLowerCase();
            if (textLowerCase.contains("selected:")) {
                String[] split = text.getString().split(":");
                if (split.length < 2) return false;
                String[] minAndMax = split[1].split("-");
                if (minAndMax.length < 2) return false;
                //System.out.println(textLowerCase);
                //System.out.println("Min and max: " + minAndMax[0] + " " + minAndMax[1]);
                int leMin = -1;
                int leMax = -1;
                try {leMin = Integer.parseInt(minAndMax[0].trim());} catch (NumberFormatException ignored) {}
                try {leMax = Integer.parseInt(minAndMax[1].trim());} catch (NumberFormatException ignored) {}

                widget.setMinAndMax(leMin, leMax);
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if all goes well
     */
    private boolean setSelectedElementFromTooltip(Slot slot, ItemStack stack, OptionDropdownWidget dropdownWidget) {
        for (Text text : ItemUtils.getLore(stack)) {
            String textLowerCase = text.getString().toLowerCase();
            if (textLowerCase.contains("selected:")) {
                String[] split = text.getString().split(":");
                if (split.length < 2) return false;
                String floorName = split[1].trim();
                dropdownWidget.setSelectedOption(dropdownWidget.new Option(floorName, stack, slot.id));
                return true;
            }
        }
        return false;
    }

    public boolean handleSign(SignBlockEntity sign, boolean front) {
        if (!isInitialized) return false;
        if (currentlyOpenedOption == classLevelRange) {
            return updateValues(sign, front, classLevelRange);
        } else if (currentlyOpenedOption == dungeonLevelRange) {
            return updateValues(sign, front, dungeonLevelRange);
        }
        return false;
    }

    private boolean updateValues(SignBlockEntity sign, boolean front, RangedValueWidget valueWidget) {
        RangedValueWidget.State state;
        String lowerCase = sign.getText(front).getMessage(3, false).getString().toLowerCase();
        if (lowerCase.contains("max")) {
            state = RangedValueWidget.State.MODIFYING_MAX;
        } else if (lowerCase.contains("min")) {
            state = RangedValueWidget.State.MODIFYING_MIN;
        } else return false;
        valueWidget.setState(state);
        this.setFocused(valueWidget);
        return true;
    }

    private void updateDropdownOptionWidget(GenericContainerScreenHandler handler, OptionDropdownWidget dropdownWidget) {
        List<OptionDropdownWidget.Option> entries = new ArrayList<>();
        for (Slot slot : handler.slots) {
            if (slot.id > (handler.getRows() - 1) * 9 - 1) break;
            if (slot.hasStack() && !slot.getStack().isOf(Items.BLACK_STAINED_GLASS_PANE)) {
                entries.add(dropdownWidget.new Option(slot.getStack().getName().getString(), slot.getStack(), slot.id));
            }
        }
        int backId = findBackSlotId(handler);
        dropdownWidget.open(entries, backId);
    }

    private void updateRangedValue(GenericContainerScreenHandler handler, RangedValueWidget valueWidget) {
        currentlyOpenedOption = valueWidget;
        int min = -1;
        int max = -1;
        for (Slot slot : handler.slots) {
            if (slot.id > (handler.getRows() - 1) * 9 - 1) break;
            if (slot.hasStack() && slot.getStack().getName().getString().toLowerCase().contains("min")) {
                min = slot.id;
            } else if (slot.hasStack() && slot.getStack().getName().getString().toLowerCase().contains("max")) {
                max = slot.id;
            }
        }
        int backId = findBackSlotId(handler);

        valueWidget.setStateAndSlots(RangedValueWidget.State.OPEN, min, max, backId);
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        if (floorSelector != null) this.floorSelector.visible = visible;
        if (dungeonTypeSelector != null) this.dungeonTypeSelector.visible = visible;
        if (classLevelRange != null) this.classLevelRange.visible = visible;
        if (dungeonLevelRange != null) this.dungeonLevelRange.visible = visible;
        if (sortGroupsSelector != null) this.sortGroupsSelector.visible = visible;
    }

    public boolean canInteract(ContainerWidget widget) {
        return currentlyOpenedOption == null || currentlyOpenedOption == widget;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) return;
        for (ContainerWidget initializedWidget : initializedWidgets) {
            initializedWidget.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public List<? extends Element> children() {
        return initializedWidgets;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}
