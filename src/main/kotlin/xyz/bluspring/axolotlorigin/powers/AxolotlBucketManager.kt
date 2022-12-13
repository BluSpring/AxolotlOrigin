package xyz.bluspring.axolotlorigin.powers

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import net.minecraft.world.Heightmap
import java.io.File
import java.util.*

object AxolotlBucketManager {
    // Map<Bucketed, Holder>
    private val bucketedPlayers = mutableMapOf<UUID, UUID>()

    // Knowing my code, players are probably gonna get stuck being invisible.
    // Let's not allow that easily.
    private val queueForUninvis = mutableListOf<UUID>()

    private val file = File(FabricLoader.getInstance().configDir.toFile(), "haiken_axolotl_uninvis.json")

    fun init() {
        ServerPlayConnectionEvents.JOIN.register { player, _, _ ->
            if (player.player.inventory.containsAny { it.hasNbt() && it.nbt!!.contains("BucketedPlayer") }) {
                player.player.inventory.remove({
                    it.hasNbt() && it.nbt!!.contains("BucketedPlayer")
                }, 64, null)
            }

            if (queueForUninvis.contains(player.player.uuid)) {
                player.player.isInvisible = false
                player.player.stopRiding()

                val topPos = player.player.getWorld().getTopPosition(Heightmap.Type.MOTION_BLOCKING, player.player.blockPos.subtract(Vec3i(0, 450, 0)))
                val pos = Vec3d.ofCenter(topPos)
                player.player.teleport(pos.x, pos.y, pos.z)
                player.player.velocity = Vec3d.ZERO
                player.player.removeScoreboardTag("haiken_axolotlbucketed")

                queueForUninvis.remove(player.player.uuid)
            }
        }

        ServerPlayConnectionEvents.DISCONNECT.register { player, server ->
            if (bucketedPlayers.containsValue(player.player.uuid)) {
                bucketedPlayers.filter { it.value == player.player.uuid }.keys.forEach { uuid ->
                    bucketedPlayers.remove(uuid)

                    val bucketed = server.playerManager.getPlayer(uuid)

                    if (bucketed == null) {
                        queueForUninvis.add(uuid)
                        return@register
                    }

                    bucketed.removeScoreboardTag("haiken_axolotlbucketed")
                    bucketed.isInvisible = false
                    bucketed.stopRiding()
                }
            } else if (bucketedPlayers.contains(player.player.uuid)) {
                val holderUUID = bucketedPlayers[player.player.uuid]!!
                val holder = server.playerManager.getPlayer(holderUUID)

                if (holder == null) {
                    queueForUninvis.add(player.player.uuid)
                    return@register
                }

                queueForUninvis.add(player.player.uuid)
                bucketedPlayers.remove(player.player.uuid)

                // try to do it anyway
                player.player.removeScoreboardTag("haiken_axolotlbucketed")
                player.player.isInvisible = false
                player.player.stopRiding()
            }
        }

        ServerLifecycleEvents.SERVER_STOPPING.register {
            if (queueForUninvis.isEmpty())
                return@register

            val json = JsonArray()
            queueForUninvis.forEach {
                json.add(it.toString())
            }

            if (!file.exists())
                file.createNewFile()

            file.writeText(json.toString())
        }

        ServerLifecycleEvents.SERVER_STARTED.register {
            if (file.exists()) {
                val json = JsonParser.parseString(file.readText()).asJsonObject

                json.entrySet().forEach {
                    val pos = it.value.asString.split(";")
                    queueForUninvis.add(UUID.fromString(it.key))
                }

                file.delete()
            }
        }

        ServerTickEvents.END_SERVER_TICK.register {
            it.playerManager.playerList.forEach { player ->
                if (!AxolotlPowerTypes.axolotlBucket.isActive(player))
                    return@forEach

                if (bucketedPlayers.contains(player.uuid)) {
                    val holderPlayer = it.playerManager.getPlayer(bucketedPlayers[player.uuid]!!) ?: return@forEach

                    player.isInvisible = true
                    if (holderPlayer.world != player.world)
                        player.moveToWorld(holderPlayer.getWorld())

                    player.teleport(holderPlayer.pos.x, 500.0, holderPlayer.pos.z)
                    player.velocity = Vec3d.ZERO
                    player.addScoreboardTag("haiken_axolotlbucketed")
                } else {
                    player.removeScoreboardTag("haiken_axolotlbucketed")
                }
            }
        }
    }

    fun isBucketed(uuid: UUID): Boolean {
        return bucketedPlayers.contains(uuid)
    }

    fun isBucketed(entity: PlayerEntity): Boolean {
        return bucketedPlayers.contains(entity.uuid)
    }

    fun getHolder(entity: PlayerEntity): PlayerEntity? {
        if (!bucketedPlayers.contains(entity.uuid))
            return null

        return entity.world.getPlayerByUuid(bucketedPlayers[entity.uuid])
    }

    fun addBucketed(entity: PlayerEntity, holder: PlayerEntity) {
        bucketedPlayers[entity.uuid] = holder.uuid

        if (entity is ServerPlayerEntity)
            entity.networkHandler.sendPacket(EntityPassengersSetS2CPacket(holder))

        if (holder is ServerPlayerEntity)
            holder.networkHandler.sendPacket(EntityPassengersSetS2CPacket(holder))

        entity.teleport(0.0, 500.0, 0.0)
        entity.addScoreboardTag("haiken_axolotlbucketed")
        entity.isInvisible = true
    }

    fun removeBucketed(entity: ServerPlayerEntity, world: ServerWorld, pos: Vec3d) {
        bucketedPlayers.remove(entity.uuid)
        entity.isInvisible = false

        entity.removeScoreboardTag("haiken_axolotlbucketed")
        entity.stopRiding()
        entity.teleport(world, pos.x, pos.y, pos.z, 0F, 0F)
        entity.velocity = Vec3d.ZERO
    }

    fun removeBucketed(uuid: UUID, world: ServerWorld, pos: Vec3d) {
        bucketedPlayers.remove(uuid)

        if (world.server.playerManager.getPlayer(uuid) != null) {
            removeBucketed(world.server.playerManager.getPlayer(uuid)!!, world, pos)
        } else {
            queueForUninvis.add(uuid)
        }
    }
}