# assignment.json
Assigns the screens to a type (standard, screenA, screenB). standard is shown when just pressing tab, A and B are shown when using other keys/the modifiers.
For every type, the concrete screens are mapped to the areas they are supposed to be shown in. See Appendix A for a list of valid areas.
To find the screen definition, ".json" is appended to the screen key, so specifying `"foo": "generic_info_screen"` will try to find `generic_info_screen.json` and assign this definition to the "foo" area.

# Screen definition JSON
This file consists of one widgets section and one layout section.

## Widget section
The widget section defines all of the widgets that a screen uses. It also defines an alias that is used in the layout section and possibly a widget's argument if its behaviour can be changed by one.
For a full list of widgets, their names and their arguments see Appendix B

## Layout section
The layout section defines how the widgets are arranged on the screen. This is done using a pipeline; an ordered list of stages where each stage moves the widgets in some way. The initial position of the widgets is undefined. As a rule of thumb, every stage (except "Place", see below) only moves the widget in one direction, so at least two operations need to be done executed on each widget for it to be in a well defined position. A full list of operations and their arguments is found in Appendix C.

# Appendices

## Appendix A: Area identifiers
- default: Any area not explicitly listed in the type.

## Appendix B: Widget identifiers and arguments

- EmptyWidget: Generic "No data available"
- SkillsWidget: The player's skill levels and stats
- EventWidget: Info about current events. Arg `inGarden: true|false`: Is this widget displayed in the garden area?
- UpgradeWidget: Currently running upgrades
- ProfileWidget: Data about the player's profile
- EffectWidget: Currently active effects
- ElectionWidget: Info about the current mayor election
- CookieWidget: Info about your super cookie

## Appendix C: Pipeline stages

#### Align
- op: `align`
- reference: One of `horizontalCenter`, `verticalCenter`, `leftOfCenter`, `rightOfCenter`, `topOfCenter`, `botOfCenter`
- apply_to: List of widgets to individually apply this operation to.
- Moves a widget in *one* direction (up/down OR left/right) until it's positioned in the way described by `reference`. This reference may be thought of as a straight line, with some describing the screen's borders and others referring to it's two center axis.

Example: align A and B with "horizontalCenter"
```
+-----------+       +-----------+
|           |       |           |
|  A        |       | ->  A     |
|           |  -->  |           |
|           |       |           |
|          B|       |     B  <- |
+-----------+       +-----------+
```

#### Collide
- op: `collideAgainst`
- direction: One of `left`, `right`
- widgets: List of widgets to individually move.
- colliders: List static reference widgets to "collide against".
- Moves a widget in the `direction` until it would overlap with any one of the colliders. Doesn't move the widget in the other direction, doesn't move the widget if it wouldn't collide with anything.

Example: Collide A and B from the right with X, Y and Z
```
+-----------+       +-----------+
|  A        |       |      A    |
|  A    XX  |       |   -> AXX  |
|       XX  |  -->  |       XX  |
|      Y    |       |      Y    |
|      Y  BB|       | -> BBY    |
+-----------+       +-----------+
```

#### Stack
- op: `stack`
- direction: One of `vertical`, `horizontal`
- align: One of `center`, `top`, `bot`, `left`, `right`
- apply_to: List of widgets to stack, order of the list is important (top -> bot or left -> right)
- Move the widgets vertically or horizontally as indicated by the `direction`. `align` indicates if the list is touching a screen border or is centered on the screen. This only works with borders in the direction of the stacking; combinations such as `vertical`/`left` are considered invalid and act like `align` was set to `center`. This only moves the widget in one direction so in order to get a clean list, an Align operation must be executed as well.

Example: Stack A, B, C vertically/center-aligned, then align with leftOfCenter
```
+-----------+       +-----------+       +-----------+
|  A        |       |  v        |       |           |
|           | stack |  A        | align | -> A      |
|     C     |  -->  |     v   B |  -->  |    B  <-  |
|           |       |     C   ^ |       |    C  <-  |
|         B |       |           |       |           |
+-----------+       +-----------+       +-----------+
```

#### Place
- op: `place`
- where: One of `center`
- apply_to: Widget to place an the indicated position.
- Places a widget at some position, moving it in any direction as needed. Only makes sense to use on one widget at a time, as applying the same Place op to multiple widgtes causes them to be on top of each other.

Example: Place A at "center"
+-----------+       +-----------+
|           |       |           |
|  A        |       | ->  v     |
|           |  -->  |     A     |
|           |       |           |
|           |       |           |
+-----------+       +-----------+