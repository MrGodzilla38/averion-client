package dev.averion.client;

/** A registered feature whose HUD content uses element-local GUI coordinates. */
abstract class HudElement extends Feature {
    final HudPlacement defaultPlacement;
    final HudRenderer renderer = new HudRenderer();
    HudPlacement placement;

    HudElement(String id, String name, String description, int x, int y) {
        super(id, name, description, Category.HUD, false);
        defaultPlacement = new HudPlacement(x, y);
        placement = defaultPlacement;
    }

    abstract void drawContent(HudText text);

    @Override
    final void preview(HudText text) {
        drawContent((line, x, y, color, shadow) -> text.draw(line, x + 8, y + 8, color, shadow));
    }
}
