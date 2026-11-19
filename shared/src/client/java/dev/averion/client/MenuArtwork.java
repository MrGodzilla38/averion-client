package dev.averion.client;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import javax.imageio.ImageIO;

/** Small alpha masks baked into horizontal runs once, shared by the native canvas adapters. */
final class MenuArtwork {
    static final MenuArtwork WORDMARK = new MenuArtwork("wordmark");
    static final MenuArtwork MODULES = new MenuArtwork("modules");

    final int width;
    private final int[] runs;

    private MenuArtwork(String name) {
        String path = "/assets/averion_client/gui/" + name + ".png";
        try (InputStream stream = MenuArtwork.class.getResourceAsStream(path)) {
            BufferedImage mask = ImageIO.read(stream);
            width = mask.getWidth();
            int[] buffer = new int[width * mask.getHeight()];
            int count = 0;
            for (int y = 0; y < mask.getHeight(); y++) {
                int x = 0;
                while (x < width) {
                    int start = x++;
                    int alpha = (mask.getRGB(start, y) >>> 29) * 255 / 7;
                    while (x < width && (mask.getRGB(x, y) >>> 29) * 255 / 7 == alpha) {
                        x++;
                    }
                    if (alpha != 0) {
                        buffer[count++] = (alpha << 24) | (y << 16) | (start << 8) | x;
                    }
                }
            }
            runs = Arrays.copyOf(buffer, count);
        } catch (IOException ex) {
            throw new IllegalStateException(path, ex);
        }
    }

    void draw(GuiCanvas canvas, int x, int y, int rgb) {
        for (int run : runs) {
            int row = y + ((run >>> 16) & 255);
            canvas.fill(x + ((run >>> 8) & 255), row, x + (run & 255), row + 1,
                    (run & 0xFF000000) | (rgb & 0xFFFFFF));
        }
    }

    static void emblem(GuiCanvas c, int x, int y, int color) {
        for (int i = 0; i < 9; i++) {
            c.fill(x + 8 - i, y + i, x + 11 - i, y + i + 1, color);
            c.fill(x + 10 + i, y + i, x + 13 + i, y + i + 1, color);
        }
        c.fill(x + 8, y + 9, x + 13, y + 11, color);
    }

    static void hudIcon(GuiCanvas c, int x, int y, int ink) {
        c.fill(x + 2, y, x + 27, y + 2, ink);
        c.fill(x, y + 2, x + 2, y + 17, ink);
        c.fill(x + 27, y + 2, x + 29, y + 17, ink);
        c.fill(x + 2, y + 17, x + 27, y + 19, ink);
        c.fill(x + 5, y + 5, x + 10, y + 9, ink);
        c.fill(x + 12, y + 5, x + 22, y + 7, ink);
        c.fill(x + 12, y + 9, x + 18, y + 11, ink);
        c.fill(x + 11, y + 21, x + 18, y + 23, ink);
    }

    static void fpsIcon(GuiCanvas c, int x, int y, int ink) {
        c.fill(x, y + 2, x + 2, y + 22, ink);
        c.fill(x + 2, y + 20, x + 29, y + 22, ink);
        c.fill(x + 6, y + 12, x + 10, y + 17, ink);
        c.fill(x + 13, y + 7, x + 17, y + 17, ink);
        c.fill(x + 20, y + 1, x + 24, y + 17, ink);
    }
}
