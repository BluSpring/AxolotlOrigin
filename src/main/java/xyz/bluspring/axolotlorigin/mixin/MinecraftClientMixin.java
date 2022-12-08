package xyz.bluspring.axolotlorigin.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.axolotlorigin.powers.AxolotlPowerTypes;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow @Nullable public ClientPlayerEntity player;

    @Inject(at = @At("HEAD"), method = "hasOutline", cancellable = true)
    public void ancientSensePower(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (AxolotlPowerTypes.INSTANCE.getAncientSense().isActive(this.player) && entity.getType() == EntityType.PLAYER) {
            cir.setReturnValue(true);
        }
    }
}
