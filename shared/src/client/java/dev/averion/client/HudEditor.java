package dev.averion.client;

import java.util.List;

/** One draft of the active gameplay HUD layout. */
final class HudEditor {
    private static final int WHITE = 0xFFF0F5FA;
    private static final int MUTED = 0xFFA6B5C4;
    private static final int ACCENT = 0xFF8DB6FF;
    private static final int PANEL_HEIGHT = 64;
    private static final int BUTTON_HEIGHT = 15;
    private static final String[] HORIZONTAL = {"Left", "Center X", "Right"};
    private static final String[] VERTICAL = {"Top", "Center Y", "Bottom"};

    private final ClientRuntime runtime;
    private final List<HudElement> elements;
    private final HudPlacement[] placements;
    private final boolean[] visible;
    private int width;
    private int height;
    private int selected;
    private boolean measured;
    private boolean dragging;
    private boolean panelAtTop;
    private int grabX;
    private int grabY;

    HudEditor(ClientRuntime runtime, int width, int height) {
        this.runtime = runtime;
        elements = runtime.hudElements();
        placements = new HudPlacement[elements.size()];
        visible = new boolean[elements.size()];
        for (int i = 0; i < elements.size(); i++) {
            placements[i] = elements.get(i).placement;
            visible[i] = elements.get(i).isEnabled();
        }
        selected = nextVisible(-1);
        resize(width, height);
    }

    void resize(int width, int height) {
        this.width = width;
        this.height = height;
        dragging = false;
        measured = false;
    }

    void render(GuiCanvas gui, HudCanvas canvas) {
        runtime.recordFrame();
        panelAtTop = false;
        if (selected >= 0) {
            for (int i = 0; i < elements.size(); i++) {
                if (visible[i] && i != selected) {
                    renderElement(gui, canvas, i);
                }
            }
            renderElement(gui, canvas, selected);
            HudRenderer bounds = elements.get(selected).renderer;
            panelAtTop = bounds.top + bounds.height + 2 > bottomPanelTop()
                    && bounds.left < panelLeft() + panelWidth()
                    && bounds.left + bounds.width > panelLeft();
        }
        measured = selected >= 0;

        int top = panelTop();
        gui.fill(panelLeft(), top, panelLeft() + panelWidth(), top + PANEL_HEIGHT, 0xF0182330);
        button(gui, 2, 3, 0, "Save & back");
        if (selected >= 0) {
            gui.text("HUD Editor · Drag to move", panelLeft() + 4, top + 4, WHITE, panelWidth() - 8);
            button(gui, 0, 3, 0, elements.get(selected).name + " >");
            button(gui, 1, 3, 0, "Delete");
            HudPlacement placement = placements[selected];
            button(gui, 0, 5, 1, HORIZONTAL[placement.horizontal.ordinal()]);
            button(gui, 1, 5, 1, VERTICAL[placement.vertical.ordinal()]);
            button(gui, 2, 5, 1, "−");
            button(gui, 3, 5, 1, placement.scale + "%");
            button(gui, 4, 5, 1, "+");
        } else {
            gui.text("No active HUD elements", panelLeft() + 4, top + 4, WHITE, panelWidth() - 8);
            gui.text("Enable HUD elements in ClickGUI.", panelLeft() + 4, top + 36, MUTED, panelWidth() - 8);
        }
        String status = runtime.configurationStatus();
        gui.text(status.isEmpty() ? "Esc cancels" : status, panelLeft() + 4, top + 51, MUTED, panelWidth() - 8);
    }

    private void renderElement(GuiCanvas gui, HudCanvas canvas, int index) {
        HudElement element = elements.get(index);
        HudRenderer renderer = element.renderer;
        renderer.measure(canvas, element, placements[index], width, height);
        int left = renderer.left;
        int top = renderer.top;
        int right = left + renderer.width;
        int bottom = top + renderer.height;
        int border = index == selected ? ACCENT : MUTED;
        gui.fill(left, top, right, bottom, 0x80202B3C);
        renderer.draw(canvas, element);
        gui.fill(left, top - 1, right, top, border);
        gui.fill(left, bottom, right, bottom + 1, border);
        gui.fill(left - 1, top, left, bottom, border);
        gui.fill(right, top, right + 1, bottom, border);
    }

    private void button(GuiCanvas gui, int column, int columns, int row, String label) {
        int left = edge(column, columns);
        int right = edge(column + 1, columns);
        int top = buttonTop(row);
        gui.fill(left + 1, top, right - 1, top + BUTTON_HEIGHT, 0xFF31425A);
        int textLeft = left + Math.max(2, (right - left - gui.textWidth(label)) / 2);
        gui.text(label, textLeft, top + 4, WHITE, right - textLeft - 2);
    }

    /** Returns true when the draft has been saved and the editor should close. */
    boolean click(double x, double y) {
        if (x >= panelLeft() && x < panelLeft() + panelWidth() && y >= panelTop() && y < panelTop() + PANEL_HEIGHT) {
            if (buttonHit(y, 0)) {
                int column = column(x, 3);
                if (column == 2) {
                    runtime.applyHudLayout(placements, visible);
                    return true;
                }
                if (selected >= 0) {
                    if (column == 1) {
                        visible[selected] = false;
                        dragging = false;
                        measured = false;
                    }
                    selected = nextVisible(selected);
                }
            } else if (selected >= 0 && buttonHit(y, 1)) {
                changePlacement(column(x, 5));
            }
            return false;
        }
        if (measured) {
            if (elements.get(selected).renderer.contains(x, y)) {
                startDrag(selected, x, y);
            } else {
                for (int i = elements.size() - 1; i >= 0; i--) {
                    if (visible[i] && elements.get(i).renderer.contains(x, y)) {
                        startDrag(i, x, y);
                        break;
                    }
                }
            }
        }
        return false;
    }

    private int nextVisible(int index) {
        for (int i = 0; i < visible.length; i++) {
            index = (index + 1) % visible.length;
            if (visible[index]) {
                return index;
            }
        }
        return -1;
    }

    private void changePlacement(int column) {
        HudPlacement current = placements[selected];
        if (column == 0) {
            HudPlacement.Anchor anchor = current.horizontal.next();
            placements[selected] = new HudPlacement(anchor, current.vertical, anchor.margin(), current.y, current.scale);
        } else if (column == 1) {
            HudPlacement.Anchor anchor = current.vertical.next();
            placements[selected] = new HudPlacement(current.horizontal, anchor, current.x, anchor.margin(), current.scale);
        } else if (column == 2 || column == 4) {
            int scale = current.scale + (column == 2 ? -10 : 10);
            scale = Math.max(HudPlacement.MIN_SCALE, Math.min(scale, HudPlacement.MAX_SCALE));
            placements[selected] = new HudPlacement(current.horizontal, current.vertical, current.x, current.y, scale);
        }
        measured = false;
    }

    private void startDrag(int index, double x, double y) {
        selected = index;
        HudRenderer renderer = elements.get(index).renderer;
        grabX = (int) Math.round(x) - renderer.left;
        grabY = (int) Math.round(y) - renderer.top;
        dragging = true;
    }

    boolean drag(double x, double y) {
        if (!dragging) {
            return false;
        }
        HudRenderer renderer = elements.get(selected).renderer;
        HudPlacement current = placements[selected];
        int availableX = width - renderer.width;
        int availableY = height - renderer.height;
        int left = HudRenderer.clamp((int) Math.round(x) - grabX, availableX);
        int top = HudRenderer.clamp((int) Math.round(y) - grabY, availableY);
        placements[selected] = new HudPlacement(current.horizontal, current.vertical,
                left - current.horizontal.origin(availableX), top - current.vertical.origin(availableY), current.scale);
        return true;
    }

    boolean release() {
        boolean handled = dragging;
        dragging = false;
        return handled;
    }

    private int panelWidth() {
        return Math.min(310, width);
    }

    private int panelLeft() {
        return (width - panelWidth()) / 2;
    }

    private int panelTop() {
        return panelAtTop ? 4 : bottomPanelTop();
    }

    private int bottomPanelTop() {
        return Math.max(0, height - PANEL_HEIGHT - 4);
    }

    private int edge(int column, int columns) {
        return panelLeft() + column * panelWidth() / columns;
    }

    private int column(double x, int columns) {
        int column = 0;
        while (column + 1 < columns && x >= edge(column + 1, columns)) {
            column++;
        }
        return column;
    }

    private int buttonTop(int row) {
        return panelTop() + 16 + row * 17;
    }

    private boolean buttonHit(double y, int row) {
        int top = buttonTop(row);
        return y >= top && y < top + BUTTON_HEIGHT;
    }
}
