package xyz.bluspring.axolotlorigin

import io.github.apace100.apoli.power.factory.condition.ConditionFactory
import io.github.apace100.apoli.registry.ApoliRegistries
import io.github.apace100.calio.data.SerializableData
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Identifier
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry
import xyz.bluspring.axolotlorigin.powers.AxolotlBucketManager
import xyz.bluspring.axolotlorigin.powers.AxolotlPowerTypes
import java.util.*

class AxolotlOrigin : ModInitializer {
    private val ancientAttackCharge = mutableMapOf<UUID, Int>()
    private val ancientAttackTotalCharge = 45
    private val hasFakeDeathReady = mutableMapOf<UUID, Int>()

    override fun onInitialize() {
        AxolotlBucketManager.init()

        registerApoliFactories()

        ServerPlayerEvents.ALLOW_DEATH.register { player, source, damage ->
            if (!hasFakeDeathReady.contains(player.uuid))
                return@register true

            if (player.server.ticks - hasFakeDeathReady[player.uuid]!! >= 75) {
                hasFakeDeathReady.remove(player.uuid)
                return@register true
            }

            hasFakeDeathReady.remove(player.uuid)
            player.health = 0.1f
            player.addStatusEffect(StatusEffectInstance(StatusEffects.SLOWNESS, 300, 4))
            player.addStatusEffect(StatusEffectInstance(StatusEffects.RESISTANCE, 300, 4))
            fakeDeathActiveSince[player.uuid] = player.world.server!!.ticks

            false
        }

        ServerTickEvents.END_WORLD_TICK.register { world ->
            val uuidsToRemove = mutableListOf<UUID>()
            fakeDeathActiveSince.forEach { (uuid, ticks) ->
                if (world.server.ticks - ticks >= 350)
                    uuidsToRemove.add(uuid)
            }

            uuidsToRemove.forEach {
                fakeDeathActiveSince.remove(it)
            }

            world.players.forEach { player ->
                if (AxolotlPowerTypes.ancientAttack.isActive(player)) {
                    if (!ancientAttackCharge.contains(player.uuid)) {
                        ancientAttackCharge[player.uuid] = 0
                        playSound(SoundEvents.ENTITY_WARDEN_SONIC_CHARGE, player.pos.x, player.pos.y, player.pos.z, player.getWorld())
                    } else {
                        ancientAttackCharge[player.uuid] = ancientAttackCharge[player.uuid]!! + 1

                        if (ancientAttackCharge[player.uuid]!! >= ancientAttackTotalCharge) {
                            runAncientAttack(player)

                            ancientAttackCharge.remove(player.uuid)
                        }
                    }
                }

                if (AxolotlPowerTypes.fakeDeath.isActive(player))
                    hasFakeDeathReady[player.uuid] = world.server.ticks
                else if (hasFakeDeathReady.contains(player.uuid) && world.server.ticks - hasFakeDeathReady[player.uuid]!! >= 45)
                    hasFakeDeathReady.remove(player.uuid)
            }
        }
    }

    private fun playSound(event: SoundEvent, x: Double, y: Double, z: Double, world: ServerWorld) {
        world.players.forEach {
            it.networkHandler.sendPacket(PlaySoundS2CPacket(event, SoundCategory.PLAYERS, x, y, z, 3F, 1F, 0L))
        }
    }

    private fun runAncientAttack(player: ServerPlayerEntity) {
        val serverWorld = player.getWorld()

        val vec3d: Vec3d = player.pos.add(0.0, 1.600000023841858, 0.0)
        val raycast = player.raycast(64.0, 0F, false)

        val vec3d2: Vec3d = raycast.pos.subtract(vec3d)
        val vec3d3 = vec3d2.normalize()

        for (i in 1 until MathHelper.floor(vec3d2.length()) + 7) {
            val vec3d4 = vec3d.add(vec3d3.multiply(i.toDouble()))
            serverWorld.spawnParticles(
                ParticleTypes.SONIC_BOOM,
                vec3d4.x,
                vec3d4.y,
                vec3d4.z,
                1,
                0.0,
                0.0,
                0.0,
                0.0
            )
        }

        val box = Box(player.pos, raycast.pos).expand(1.37)

        val entities = serverWorld.getOtherEntities(player, box) { it is LivingEntity && !it.isSpectator && !it.isInvulnerable }
        val holder = AxolotlBucketManager.getHolder(player)

        entities.forEach { target ->
            if (holder != null && holder.uuid == target.uuid)
                return@forEach

            target.damage(DamageSource.sonicBoom(player), 10.5f)

            var d = 1.0
            var e = 1.0

            if (target.type == EntityType.PLAYER) {
                d = 0.5 * (1.0 - (target as PlayerEntity).getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE))
                e = 2.5 * (1.0 - target.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE))
            }

            target.addVelocity(vec3d3.getX() * e, vec3d3.getY() * d, vec3d3.getZ() * e)
        }

        playSound(SoundEvents.ENTITY_WARDEN_SONIC_BOOM, player.pos.x, player.pos.y, player.pos.z, serverWorld)
    }

    private fun registerApoliFactories() {
        // in a perfect world, this would be server-sided only, allowing this to actually work.
        // today is not that world.

        /*registerEntityCondition(
            ConditionFactory(
                Identifier("haiken", "is_bucketed"),
                SerializableData()
            ) { _, entity ->
                if (entity !is PlayerEntity)
                    return@ConditionFactory false

                AxolotlBucketManager.isBucketed(entity)
            }
        )*/
    }

    private fun registerEntityCondition(factory: ConditionFactory<Entity>) {
        Registry.register(ApoliRegistries.ENTITY_CONDITION, factory.serializerId, factory)
    }

    companion object {
        val fakeDeathActiveSince = mutableMapOf<UUID, Int>()
    }
}