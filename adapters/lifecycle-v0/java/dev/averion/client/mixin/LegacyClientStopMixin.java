package dev.averion.client.mixin;

import dev.averion.client.TargetLifecycle;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
abstract class LegacyClientStopMixin {
    @Inject(method = "stop", at = @At("HEAD"))
    private void averion$stop(CallbackInfo callback) {
        TargetLifecycle.stop();
    }
}
