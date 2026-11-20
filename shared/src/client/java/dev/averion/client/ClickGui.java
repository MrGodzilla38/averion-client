package dev.averion.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

/** Owns one ClickGUI session. Minecraft screens supply size, drawing and input events. */
final class ClickGui {
    private static final int PANEL = 0x98212B39;
    private static final int RAIL = 0x7813212E;
    private static final int INSET = 0xBF202B3C;
    private static final int HOVER = 0xA0303E50;
    private static final int SELECTED = 0xA337475C;
    private static final int DIVIDER = 0xFF2A3947;
    private static final int WHITE = 0xFFF0F5FA;
    private static final int MUTED = 0xFFA6B5C4;
    private static final int ACCENT = 0xFF8DB6FF;
    private static final int BLUE = 0xFF355FE9;
    private static final int PADDING = 8;
    private static final int ROW_HEIGHT = 30;
    private static final int HEADER = 30;
    private static final int COLUMN_HEADER = 20;
    private static final int FOOTER = 18;
    private static final int HUD_BUTTON_HEIGHT = 23;
    private static final int QUERY_LIMIT = 64;
    private static final String[] CHANNELS = {"Red", "Green", "Blue"};
    private static final int[] CHANNEL_COLORS = {0xFFF09398, 0xFF83CCAA, 0xFF83B9EC};

    private final ClientRuntime runtime;
    private final List<Feature.Category> categories;
    private final List<Feature> features;
    private final Column navigation = new Column();
    private final Column modules = new Column();
    private final Column settings = new Column();
    private List<Feature> results = Collections.emptyList();
    private List<Setting<?>> selectedSettings = Collections.emptyList();
    private Feature.Category category;
    private Feature selected;
    private Setting.Color expandedColor;
    private String query = "";
    private boolean searchFocused;
    private boolean querySelected;
    private DoubleConsumer activeSlider;
    private int width;
    private int height;
    private int left;
    private int top;
    private int right;
    private int bottom;
    private int searchLeft;
    private int cardColumns = 1;

    ClickGui(ClientRuntime runtime) {
        this.runtime = runtime;
        features = runtime.features();
        EnumSet<Feature.Category> populated = EnumSet.noneOf(Feature.Category.class);
        for (Feature feature : features) {
            populated.add(feature.category);
        }
        categories = new ArrayList<>(populated);
        category = categories.isEmpty() ? null : categories.get(0);
        filter();
    }

    void resize(int width, int height) {
        this.width = width;
        this.height = height;
        int panelWidth = Math.min(632, width - Math.min(24, width / 12));
        int panelHeight = Math.min(332, height - Math.min(24, height / 12));
        left = (width - panelWidth) / 2;
        right = left + panelWidth;
        top = (height - panelHeight) / 2;
        bottom = top + panelHeight;
        int body = top + HEADER + COLUMN_HEADER;
        int rows = Math.max(0, (bottom - FOOTER - body) / ROW_HEIGHT);
        int railEnd = left + Math.min(126, panelWidth / 4);
        int listEnd = selected == null ? right : railEnd + (panelWidth - (railEnd - left)) * 43 / 100;
        int available = bottom - FOOTER - body;
        int categoryRows = Math.max(0, (available - HUD_BUTTON_HEIGHT) / ROW_HEIGHT);
        navigation.layout(left, railEnd, body, categoryRows, categories.size());
        modules.stride = Math.max(1, Math.min(123, available));
        cardColumns = Math.max(1, Math.min(3, (listEnd - railEnd) / 112));
        modules.layout(railEnd, listEnd, body, available / modules.stride, cardRows());
        settings.layout(listEnd, right, body, rows, settingRows());
        searchLeft = right - 26 - Math.min(212, panelWidth * 33 / 100);
        activeSlider = null;
    }

    void render(GuiCanvas canvas, int mouseX, int mouseY) {
        runtime.recordFrame();
        canvas.fill(0, 0, width, height, 0x24071019);
        if (!usable()) {
            canvas.text("Averion", 4, 4, WHITE, width - 8);
            return;
        }
        canvas.fill(left + 6, bottom, right - 6, bottom + 2, 0x32070C15);
        rounded(canvas, left, top, right, bottom, 0x94101823);
        rounded(canvas, left, top, modules.left, bottom, RAIL);
        canvas.fill(modules.left, top + 8, modules.left + 1, bottom - 8, 0x66506177);

        int brandWidth = navigation.contentWidth();
        int markX = left + (modules.left - left - 21) / 2;
        MenuArtwork.emblem(canvas, markX, top + 7, ACCENT);
        if (brandWidth >= MenuArtwork.WORDMARK.width) {
            MenuArtwork.WORDMARK.draw(canvas, left + (modules.left - left - MenuArtwork.WORDMARK.width) / 2,
                    top + 23, WHITE);
        } else {
            canvas.text("AVERION", left + PADDING, top + 23, WHITE, brandWidth);
        }
        if (searchLeft - modules.left > MenuArtwork.MODULES.width + 16) {
            MenuArtwork.MODULES.draw(canvas, modules.left + 9, top + 9, WHITE);
        } else {
            canvas.text("Mods", modules.left + 6, top + 11, WHITE, searchLeft - modules.left - 8);
        }
        renderSearch(canvas);
        int closeColor = hovered(mouseX, mouseY, right - 22, top + 5, right - 4, top + 25) ? WHITE : MUTED;
        canvas.text("×", right - 15, top + 11, closeColor);

        canvas.text("LIBRARY", left + PADDING, navigation.top - 12, MUTED, navigation.contentWidth());
        String section = query.trim().isEmpty() ? "Features" : "Search results";
        canvas.text(section, modules.left + 9, modules.top - 12, MUTED, modules.contentWidth());
        if (selected != null) {
            rounded(canvas, settings.left + 1, top + HEADER, right - 4, bottom - 5, PANEL);
            canvas.text(selected.name, settings.left + PADDING, settings.top - 13, WHITE, settings.contentWidth() - 12);
            canvas.text("×", right - 13, settings.top - 13, MUTED);
        }
        for (int index = navigation.offset; index < navigation.end(categories.size()); index++) {
            Feature.Category item = categories.get(index);
            int y = navigation.y(index);
            boolean current = item == category && query.trim().isEmpty();
            boolean over = navigation.hit(mouseX, mouseY, categories.size()) == index;
            if (current || over) {
                rounded(canvas, left + 5, y, modules.left - 5, y + 23, current ? BLUE : HOVER);
            }
            int gx = left + 10;
            canvas.fill(gx, y + 7, gx + 4, y + 16, MUTED);
            canvas.fill(gx + 5, y + 7, gx + 10, y + 11, WHITE);
            canvas.fill(gx + 5, y + 12, gx + 10, y + 16, MUTED);
            canvas.text(item.label, gx + 15, y + 8, WHITE, modules.left - gx - 22);
        }
        int editorTop = hudEditorTop();
        rounded(canvas, left + 5, editorTop, navigation.right - 5, editorTop + HUD_BUTTON_HEIGHT,
                hudEditorHit(mouseX, mouseY, 0) ? HOVER : INSET);
        canvas.text("HUD Editor", left + 10, editorTop + 8, WHITE, navigation.right - left - 16);

        for (int index = modules.offset * cardColumns;
                index < Math.min(results.size(), modules.end(cardRows()) * cardColumns); index++) {
            drawCard(canvas, index, mouseX, mouseY);
        }
        if (results.isEmpty()) {
            canvas.text("No matches", modules.left + PADDING, modules.top + 5, MUTED, modules.contentWidth());
        }
        if (selected != null) {
            renderSettings(canvas, mouseX, mouseY);
        }
        scrollbar(canvas, navigation, categories.size());
        scrollbar(canvas, modules, cardRows());
        scrollbar(canvas, settings, settingRows());

        String status = runtime.configurationStatus();
        String footer = status.isEmpty() ? "Esc  ·  Close" : status;
        canvas.text(footer, left + PADDING, bottom - 12, status.isEmpty() ? MUTED : 0xFFF1C592,
                right - left - PADDING * 2);
    }

    private int cardRows() {
        return (results.size() + cardColumns - 1) / cardColumns;
    }

    private int cardEdge(int column) {
        return modules.left + column * (modules.right - modules.left) / cardColumns;
    }

    private static void rounded(GuiCanvas canvas, int x, int y, int end, int base, int color) {
        int radius = Math.min(6, Math.min((end - x) / 2, (base - y) / 2));
        if (radius <= 0) {
            return;
        }
        for (int i = 0; i < radius; i++) {
            int inset = cornerInset(radius, i);
            canvas.fill(x + inset, y + i, end - inset, y + i + 1, color);
            canvas.fill(x + inset, base - i - 1, end - inset, base - i, color);
        }
        canvas.fill(x, y + radius, end, base - radius, color);
    }

    private static int cornerInset(int radius, int row) {
        double dy = radius - row - 0.5;
        return radius - (int) Math.round(Math.sqrt(radius * radius - dy * dy));
    }

    private void drawCard(GuiCanvas canvas, int index, int mouseX, int mouseY) {
        Feature feature = results.get(index);
        int col = index % cardColumns;
        int x = cardEdge(col) + 4;
        int end = cardEdge(col + 1) - 4;
        int y = modules.y(index / cardColumns);
        int base = y + modules.stride - 5;
        int center = (x + end) / 2;
        boolean active = feature.isEnabled();
        boolean hover = hovered(mouseX, mouseY, x, y, end, base);
        int strip = active ? 0xE0355FE9 : 0xAC38465A;
        int face = hover || feature == selected ? SELECTED : 0x72253140;
        int band = Math.max(y + 1, base - 22);
        // Draw each scanline once so translucent surfaces have uniform opacity.
        int radius = Math.min(6, (base - y) / 2);
        for (int i = 0; i < radius; i++) {
            int inset = cornerInset(radius, i);
            canvas.fill(x + inset, y + i, end - inset, y + i + 1, face);
            canvas.fill(x + inset, base - i - 1, end - inset, base - i, strip);
        }
        if (band > y + radius) {
            canvas.fill(x, y + radius, end, band, face);
        }
        canvas.fill(x, Math.max(band, y + radius), end, base - radius, strip);

        if (band - y >= 46) {
            int iconY = y + Math.max(7, (band - y - 54) / 2);
            if (feature instanceof FpsFeature) {
                MenuArtwork.fpsIcon(canvas, center - 14, iconY, 0xFFAAC7FF);
            } else {
                MenuArtwork.hudIcon(canvas, center - 14, iconY, 0xFFBDD0FF);
            }
            feature.preview((line, px, py, color, shadow) -> {
                int ty = iconY + 29 + py - 8;
                if (ty + 9 <= band - 2) {
                    int tx = Math.max(x + 5, center - canvas.textWidth(line) / 2);
                    if (shadow) {
                        canvas.text(line, tx + 1, ty + 1, 0xFF131925, end - tx - 5);
                    }
                    canvas.text(line, tx, ty, color, end - tx - 5);
                }
            });
        }
        rounded(canvas, end - 14, y + 6, end - 6, y + 14, active ? 0xFF4C75FF : 0xFF495768);
        canvas.text(feature.name, x + 7, base - 14, WHITE, end - x - 34);
        toggleMark(canvas, end - 23, base - 16, active);
    }

    private void renderSearch(GuiCanvas canvas) {
        int x = searchLeft + 17;
        int end = right - 26;
        rounded(canvas, searchLeft, top + 5, end, top + 25, searchFocused ? BLUE : DIVIDER);
        rounded(canvas, searchLeft + 1, top + 6, end - 1, top + 24, INSET);
        rounded(canvas, searchLeft + 5, top + 10, searchLeft + 12, top + 17, MUTED);
        rounded(canvas, searchLeft + 6, top + 11, searchLeft + 11, top + 16, INSET);
        canvas.fill(searchLeft + 11, top + 16, searchLeft + 14, top + 18, MUTED);
        if (searchFocused) {
            canvas.fill(searchLeft, top + 23, end, top + 24, ACCENT);
        }
        if (query.isEmpty()) {
            canvas.text("Search features", x, top + 11, MUTED, end - x - 6);
            return;
        }
        String visible = query;
        int available = end - x - 6;
        while (!visible.isEmpty() && canvas.textWidth(visible) > available) {
            visible = visible.substring(visible.offsetByCodePoints(0, 1));
        }
        int textEnd = x + canvas.textWidth(visible);
        if (querySelected) {
            canvas.fill(x - 1, top + 9, textEnd + 1, top + 21, SELECTED);
        }
        canvas.text(visible, x, top + 11, WHITE);
        if (searchFocused && !querySelected) {
            canvas.fill(textEnd + 1, top + 10, textEnd + 2, top + 20, ACCENT);
        }
    }

    private void renderSettings(GuiCanvas canvas, int mouseX, int mouseY) {
        if (selected == null) {
            canvas.text("Select a feature", settings.left + PADDING, settings.top + 5, MUTED,
                    settings.contentWidth());
            return;
        }
        for (int row = settings.offset; row < settings.end(settingRows()); row++) {
            int y = settings.y(row);
            int x = settings.left + PADDING;
            int end = settings.right - PADDING;
            if (row == 0) {
                rowBackground(canvas, settings, y, false, mouseX, mouseY);
                canvas.text("Feature enabled", x, y + 10, WHITE, end - x - 24);
                toggleMark(canvas, end - 19, y + 9, selected.isEnabled());
                continue;
            }
            Setting<?> setting = settingAt(row);
            int channel = channelAt(row);
            if (channel >= 0 && setting instanceof Setting.Color) {
                int value = channelValue((Setting.Color) setting, channel);
                slider(canvas, x, y, end, CHANNELS[channel], value, 0, 255, CHANNEL_COLORS[channel]);
            } else if (setting instanceof Setting.Slider) {
                Setting.Slider value = (Setting.Slider) setting;
                slider(canvas, x, y, end, value.label, value.value(), value.min, value.max, ACCENT);
            } else {
                rowBackground(canvas, settings, y, false, mouseX, mouseY);
                canvas.text(setting.label, x, y + 4, WHITE, end - x);
                if (setting instanceof Setting.Toggle) {
                    boolean enabled = ((Setting.Toggle) setting).value();
                    canvas.text(enabled ? "On" : "Off", x, y + 16, enabled ? ACCENT : MUTED, end - x - 24);
                    toggleMark(canvas, end - 19, y + 15, enabled);
                } else if (setting instanceof Setting.Choice) {
                    canvas.text(((Setting.Choice<?>) setting).selectedLabel(), x, y + 16, MUTED, end - x - 12);
                    canvas.text("›", end - 6, y + 16, ACCENT);
                } else if (setting instanceof Setting.Color) {
                    int rgb = ((Setting.Color) setting).value();
                    canvas.text(String.format(Locale.ROOT, "#%06X", rgb), x, y + 16, MUTED, end - x - 18);
                    canvas.fill(end - 13, y + 14, end, y + 24, DIVIDER);
                    canvas.fill(end - 12, y + 15, end - 1, y + 23, 0xFF000000 | rgb);
                }
            }
        }
    }

    private static void heading(GuiCanvas canvas, Column column, String label) {
        canvas.text(label, column.left + PADDING, column.top - 13, MUTED, column.contentWidth());
    }

    private static void rowBackground(GuiCanvas canvas, Column column, int y, boolean selected, int x, int mouseY) {
        if (selected || hovered(x, mouseY, column.left, y, column.right, y + ROW_HEIGHT)) {
            canvas.fill(column.left + 3, y, column.right - 4, y + ROW_HEIGHT - 1, selected ? SELECTED : HOVER);
        }
        if (selected) {
            canvas.fill(column.left + 3, y + 3, column.left + 4, y + ROW_HEIGHT - 4, ACCENT);
        }
    }

    private static void toggleMark(GuiCanvas canvas, int x, int y, boolean enabled) {
        rounded(canvas, x, y, x + 19, y + 10, enabled ? 0xFF2349AE : 0xBF202B3C);
        int handle = enabled ? x + 10 : x + 2;
        rounded(canvas, handle, y + 2, handle + 7, y + 8, enabled ? WHITE : MUTED);
    }

    private static void slider(GuiCanvas canvas, int x, int y, int end, String label,
                               int value, int min, int max, int color) {
        String number = Integer.toString(value);
        int numberWidth = canvas.textWidth(number);
        int trackEnd = end - 2;
        int handle = x + (int) Math.round((trackEnd - x) * ((value - (double) min) / (max - (double) min)));
        canvas.text(label, x, y + 4, WHITE, end - x - numberWidth - 4);
        canvas.text(number, end - numberWidth, y + 4, MUTED);
        canvas.fill(x, y + 20, trackEnd, y + 22, DIVIDER);
        canvas.fill(x, y + 20, handle, y + 22, color);
        canvas.fill(handle - 1, y + 17, handle + 2, y + 25, color);
    }

    private static void scrollbar(GuiCanvas canvas, Column column, int count) {
        if (column.rows == 0 || count <= column.rows) {
            return;
        }
        int height = column.rows * column.stride;
        int size = Math.max(6, height * column.rows / count);
        int y = column.top + (height - size) * column.offset / (count - column.rows);
        canvas.fill(column.right - 3, column.top, column.right - 2, column.top + height, DIVIDER);
        canvas.fill(column.right - 3, y, column.right - 2, y + size, ACCENT);
    }

    boolean closeHit(double x, double y, int button) {
        return button == 0 && usable() && hovered(x, y, right - 22, top + 5, right - 4, top + 25);
    }

    boolean hudEditorHit(double x, double y, int button) {
        int editorTop = hudEditorTop();
        return button == 0 && usable()
                && hovered(x, y, left + 5, editorTop, navigation.right - 5, editorTop + HUD_BUTTON_HEIGHT);
    }

    private int hudEditorTop() {
        return navigation.y(navigation.end(categories.size()));
    }

    boolean click(double x, double y, int button) {
        if (!usable() || (button != 0 && button != 1)) {
            return false;
        }
        searchFocused = button == 0 && hovered(x, y, searchLeft, top + 6, right - 26, top + 24);
        querySelected = false;
        if (searchFocused) {
            return true;
        }
        int row = navigation.hit(x, y, categories.size());
        if (row >= 0 && button == 0) {
            category = categories.get(row);
            setQuery("");
            return true;
        }
        if (selected != null && button == 0 && hovered(x, y, right - 20, settings.top - COLUMN_HEADER,
                right, settings.top)) {
            select(null);
            return true;
        }
        row = modules.hit(x, y, cardRows());
        if (row >= 0 && button == 0) {
            for (int col = 0; col < cardColumns; col++) {
                int index = row * cardColumns + col;
                int end = cardEdge(col + 1) - 4;
                int base = modules.y(row) + modules.stride - 5;
                if (index < results.size() && hovered(x, y, cardEdge(col) + 4, modules.y(row), end, base)) {
                    Feature feature = results.get(index);
                    if (x >= end - 27 && y >= base - 22) {
                        return runtime.toggle(feature);
                    }
                    select(feature);
                    return true;
                }
            }
        }
        row = settings.hit(x, y, settingRows());
        if (row >= 0) {
            return clickSetting(row, x, y, button);
        }
        return false;
    }

    private boolean clickSetting(int row, double x, double y, int button) {
        if (row == 0) {
            return button == 0 && runtime.toggle(selected);
        }
        Setting<?> setting = settingAt(row);
        int channel = channelAt(row);
        if (setting instanceof Setting.Choice) {
            cycle((Setting.Choice<?>) setting, button == 0 ? 1 : -1);
            return true;
        }
        if (button != 0) {
            return false;
        }
        if (channel >= 0) {
            Setting.Color color = (Setting.Color) setting;
            if (y >= settings.y(row) + 14) {
                int shift = (2 - channel) * 8;
                startSlider(x, 0, 255, value -> runtime.update(selected, color,
                        (color.value() & ~(0xFF << shift)) | (value << shift)));
            }
        } else if (setting instanceof Setting.Slider) {
            Setting.Slider slider = (Setting.Slider) setting;
            if (y >= settings.y(row) + 14) {
                startSlider(x, slider.min, slider.max, value -> runtime.update(selected, slider, value));
            }
        } else if (setting instanceof Setting.Toggle) {
            Setting.Toggle toggle = (Setting.Toggle) setting;
            runtime.update(selected, toggle, !toggle.value());
        } else if (setting instanceof Setting.Color) {
            expandedColor = expandedColor == setting ? null : (Setting.Color) setting;
            settings.clamp(settingRows());
            if (expandedColor != null && settings.rows > 1 && row == settings.end(settingRows()) - 1) {
                settings.offset = Math.min(settings.offset + 1, Math.max(0, settingRows() - settings.rows));
            }
        }
        return true;
    }

    private <E extends Enum<E>> void cycle(Setting.Choice<E> setting, int direction) {
        runtime.update(selected, setting, setting.next(direction));
    }

    private void startSlider(double x, int min, int max, IntConsumer change) {
        activeSlider = mouseX -> {
            int start = settings.left + PADDING;
            int end = settings.right - PADDING - 2;
            double fraction = Math.max(0, Math.min(1, (mouseX - start) / (end - start)));
            change.accept((int) Math.round(min + fraction * (max - (double) min)));
        };
        activeSlider.accept(x);
    }

    boolean drag(double x, int button) {
        if (activeSlider == null || button != 0) {
            return false;
        }
        activeSlider.accept(x);
        return true;
    }

    boolean release(int button) {
        if (activeSlider == null || button != 0) {
            return false;
        }
        activeSlider = null;
        return true;
    }

    boolean scroll(double x, double y, double amount) {
        if (!usable() || !Double.isFinite(amount)) {
            return false;
        }
        if (navigation.contains(x, y)) {
            return navigation.scroll(amount, categories.size());
        }
        if (modules.contains(x, y)) {
            return modules.scroll(amount, cardRows());
        }
        return settings.contains(x, y) && settings.scroll(amount, settingRows());
    }

    boolean searchFocused() {
        return searchFocused;
    }

    void focusSearch() {
        searchFocused = true;
    }

    void selectQuery() {
        querySelected = !query.isEmpty();
    }

    void backspace() {
        if (!query.isEmpty()) {
            setQuery(querySelected ? "" : query.substring(0, query.offsetByCodePoints(query.length(), -1)));
        }
    }

    boolean type(int codePoint) {
        if (!searchFocused || Character.isISOControl(codePoint) || !Character.isValidCodePoint(codePoint)) {
            return false;
        }
        insert(new String(Character.toChars(codePoint)));
        return true;
    }

    void insert(String text) {
        StringBuilder next = new StringBuilder(querySelected ? "" : query);
        int length = next.codePointCount(0, next.length());
        for (int index = 0; index < text.length() && length < QUERY_LIMIT;) {
            int codePoint = text.codePointAt(index);
            index += Character.charCount(codePoint);
            if (!Character.isISOControl(codePoint)) {
                next.appendCodePoint(codePoint);
                length++;
            }
        }
        setQuery(next.toString());
    }

    private void setQuery(String value) {
        querySelected = false;
        query = value;
        filter();
    }

    private void filter() {
        String term = query.trim().toLowerCase(Locale.ROOT);
        List<Feature> matches = new ArrayList<>();
        for (Feature feature : features) {
            if (term.isEmpty() ? feature.category == category
                    : feature.name.toLowerCase(Locale.ROOT).contains(term) || feature.id.contains(term)
                    || feature.description.toLowerCase(Locale.ROOT).contains(term)
                    || feature.category.label.toLowerCase(Locale.ROOT).contains(term)) {
                matches.add(feature);
            }
        }
        results = matches;
        modules.reset(cardRows());
        if (!matches.contains(selected)) {
            select(null);
        }
    }

    private void select(Feature feature) {
        if (selected != feature) {
            selected = feature;
            expandedColor = null;
            activeSlider = null;
            selectedSettings = feature == null ? Collections.emptyList() : feature.settings();
            settings.reset(settingRows());
            if (width > 0) {
                resize(width, height);
            }
        }
    }

    private int settingRows() {
        return selected == null ? 0 : 1 + selectedSettings.size() + (expandedColor == null ? 0 : 3);
    }

    private Setting<?> settingAt(int row) {
        int index = row - 1;
        int color = selectedSettings.indexOf(expandedColor);
        if (color >= 0 && index > color) {
            index = Math.max(color, index - 3);
        }
        return selectedSettings.get(index);
    }

    private int channelAt(int row) {
        int color = selectedSettings.indexOf(expandedColor);
        int channel = row - color - 2;
        return color >= 0 && channel >= 0 && channel < 3 ? channel : -1;
    }

    private static int channelValue(Setting.Color color, int channel) {
        return (color.value() >> ((2 - channel) * 8)) & 0xFF;
    }

    private boolean usable() {
        return right - left >= 152 && settings.rows > 0;
    }

    private static boolean hovered(double x, double y, int left, int top, int right, int bottom) {
        return x >= left && x < right && y >= top && y < bottom;
    }

    private static final class Column {
        private int left;
        private int right;
        private int top;
        private int rows;
        private int stride = ROW_HEIGHT;
        private int offset;
        private double wheel;

        void layout(int left, int right, int top, int rows, int count) {
            this.left = left;
            this.right = right;
            this.top = top;
            this.rows = rows;
            clamp(count);
        }

        void reset(int count) {
            offset = 0;
            wheel = 0;
            clamp(count);
        }

        void clamp(int count) {
            offset = Math.max(0, Math.min(offset, Math.max(0, count - rows)));
        }

        int end(int count) {
            return Math.min(offset + rows, count);
        }

        int y(int row) {
            return top + (row - offset) * stride;
        }

        int contentWidth() {
            return right - left - PADDING * 2;
        }

        boolean contains(double x, double y) {
            return hovered(x, y, left, top, right, top + rows * stride);
        }

        int hit(double x, double y, int count) {
            int row = offset + (int) ((y - top) / stride);
            return contains(x, y) && row < count ? row : -1;
        }

        boolean scroll(double amount, int count) {
            if (count <= rows) {
                return false;
            }
            wheel = Math.signum(wheel) == Math.signum(-amount) ? wheel - amount : -amount;
            int steps = (int) wheel;
            wheel -= steps;
            offset += Math.max(-count, Math.min(steps, count));
            clamp(count);
            return true;
        }
    }
}
