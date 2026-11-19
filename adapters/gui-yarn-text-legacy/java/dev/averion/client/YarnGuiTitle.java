package dev.averion.client;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

final class YarnGuiTitle {
    private YarnGuiTitle() {
    }

    static Text title() {
        return new LiteralText("Averion");
    }
}
