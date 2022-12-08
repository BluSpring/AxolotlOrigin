package xyz.bluspring.axolotlorigin.mixin;

import net.minecraft.item.EntityBucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.axolotlorigin.powers.AxolotlBucketManager;

@Mixin(EntityBucketItem.class)
public class EntityBucketItemMixin {
    @Inject(at = @At("HEAD"), method = "spawnEntity", cancellable = true)
    public void replaceAxolotlSpawnWithPlayer(ServerWorld world, ItemStack stack, BlockPos pos, CallbackInfo ci) {
        if (stack.hasNbt() && stack.getNbt().contains("BucketedPlayer")) {
            var tickCreated = stack.getNbt().getInt("BucketedPlayerCreated");

            if (world.getServer().getTicks() - tickCreated <= 3) {
                ci.cancel();
                return;
            }

            var uuid = stack.getNbt().getUuid("BucketedPlayer");

            AxolotlBucketManager.INSTANCE.removeBucketed(uuid, world, Vec3d.of(pos));
            stack.getNbt().remove("BucketedPlayer");
            stack.getNbt().remove("BucketedPlayerCreated");

            ci.cancel();
        }
    }
}
