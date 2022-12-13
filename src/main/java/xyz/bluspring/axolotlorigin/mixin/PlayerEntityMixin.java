package xyz.bluspring.axolotlorigin.mixin;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.axolotlorigin.powers.AxolotlBucketManager;
import xyz.bluspring.axolotlorigin.powers.AxolotlPowerTypes;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("RETURN"), method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;")
    public void axolotlHandleItemDropped(ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> cir) {
        if (stack.hasNbt() && stack.getNbt().contains("BucketedPlayer")) {
            var thrownEntity = cir.getReturnValue();
            var uuid = stack.getNbt().getUuid("BucketedPlayer");

            var waterBucket = new ItemStack(Items.WATER_BUCKET, stack.getCount());

            var nbtCopy = stack.getNbt().copy();
            nbtCopy.remove("BucketedPlayer");
            waterBucket.setNbt(nbtCopy);

            thrownEntity.setStack(waterBucket);

            if (!thrownEntity.world.isClient && AxolotlBucketManager.INSTANCE.isBucketed(uuid)) {
                AxolotlBucketManager.INSTANCE.removeBucketed(uuid, (ServerWorld) thrownEntity.world, thrownEntity.getPos());

                var player = thrownEntity.world.getServer().getPlayerManager().getPlayer(uuid);
                if (player != null) {
                    player.setVelocity(thrownEntity.getVelocity().multiply(1.0, 1.43, 1.0));
                }
            }
        }
    }

    @Intrinsic
    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) {
        if (this.world.isClient) // Absolutely do NOT handle this on the client whatsoever.
            return super.interactAt(player, hitPos, hand);

        if (AxolotlPowerTypes.INSTANCE.getAxolotlBucket().isActive(this)) {
            if (AxolotlBucketManager.INSTANCE.isBucketed((PlayerEntity) (Object)this))
                return super.interactAt(player, hitPos, hand);

            if (player.getStackInHand(hand).getItem().equals(Items.WATER_BUCKET)) {
                var entity = (PlayerEntity) (Object) this;

                entity.playSound(SoundEvents.ITEM_BUCKET_FILL_AXOLOTL, 1.0F, 1.0F);
                var itemStack = new ItemStack(Items.AXOLOTL_BUCKET);
                itemStack.getOrCreateNbt().putUuid("BucketedPlayer", entity.getUuid());
                // Workaround to a bug where the water bucket just gets placed whenever you right click a player.
                itemStack.getOrCreateNbt().putInt("BucketedPlayerCreated", entity.getWorld().getServer().getTicks());

                itemStack.setCustomName(Text.literal("Bucket of ").append(entity.getDisplayName()));

                var itemStack2 = ItemUsage.exchangeStack(player.getStackInHand(hand), player, itemStack, true);
                player.setStackInHand(hand, itemStack2);

                Criteria.FILLED_BUCKET.trigger((ServerPlayerEntity) player, itemStack2);

                AxolotlBucketManager.INSTANCE.addBucketed((PlayerEntity) (Object)this, player);

                return ActionResult.success(true);
            }
        }

        return super.interactAt(player, hitPos, hand);
    }
}
