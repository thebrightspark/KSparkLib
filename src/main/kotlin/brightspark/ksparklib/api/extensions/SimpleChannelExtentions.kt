package brightspark.ksparklib.api.extensions

import brightspark.ksparklib.api.Message
import net.minecraft.entity.Entity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.network.NetworkManager
import net.minecraft.util.RegistryKey
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.util.math.vector.Vector3i
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.fml.network.PacketDistributor
import net.minecraftforge.fml.network.simple.SimpleChannel
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

/**
 * Registers a [Message] with the given [index]
 */
@Suppress("INACCESSIBLE_TYPE")
fun <T : Message> SimpleChannel.registerMessage(messageClass: KClass<T>, index: Int) {
	this.registerMessage(
		index,
		messageClass.java,
		{ message, buffer -> message.encode(buffer) },
		{ message -> messageClass.createInstance().apply { decode(message) } },
		{ message, context -> message.consume(context) }
	)
}

/**
 * Registers a [Message] with the given [index]
 */
@Suppress("INACCESSIBLE_TYPE")
inline fun <reified T : Message> SimpleChannel.registerMessage(index: Int) {
	this.registerMessage(
		index,
		T::class.java,
		{ message, buffer -> message.encode(buffer) },
		{ message -> T::class.createInstance().apply { decode(message) } },
		{ message, context -> message.consume(context) }
	)
}

/**
 * Sends the [message] to the [player] client
 */
fun SimpleChannel.sendToPlayer(message: Message, player: ServerPlayerEntity): Unit =
	this.send(PacketDistributor.PLAYER.with { player }, message)

/**
 * Send the [message] to all clients in the [worldKey]
 */
fun SimpleChannel.sendToDimension(message: Message, worldKey: RegistryKey<World>): Unit =
	this.send(PacketDistributor.DIMENSION.with { worldKey }, message)

/**
 * Send the [message] to all clients near the [targetPoint]
 */
fun SimpleChannel.sendToNear(message: Message, targetPoint: PacketDistributor.TargetPoint): Unit =
	this.send(PacketDistributor.NEAR.with { targetPoint }, message)

/**
 * Send the [message] to all clients near the [pos] and [worldKey] within the [range] and optionally excluding the
 * given [excluded] player
 */
fun SimpleChannel.sendToNear(
	message: Message,
	pos: Vector3d,
	range: Double,
	worldKey: RegistryKey<World>,
	excluded: ServerPlayerEntity? = null
): Unit =
	sendToNear(message, PacketDistributor.TargetPoint(excluded, pos.x, pos.y, pos.z, range, worldKey))

/**
 * Send the [message] to all clients near the [pos] and [worldKey] within the [range] and optionally excluding the
 * given [excluded] player
 */
fun SimpleChannel.sendToNear(
	message: Message,
	pos: Vector3i,
	range: Double,
	worldKey: RegistryKey<World>,
	excluded: ServerPlayerEntity? = null
): Unit =
	sendToNear(
		message,
		PacketDistributor.TargetPoint(excluded, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), range, worldKey)
	)

/**
 * Sends the [message] to all clients tracking the [entity]
 */
fun SimpleChannel.sendToTrackingEntity(message: Message, entity: Entity): Unit =
	this.send(PacketDistributor.TRACKING_ENTITY.with { entity }, message)

/**
 * Sends the [message] to all clients tracking the [entity] and to the entity itself if it's a [ServerPlayerEntity]
 */
fun SimpleChannel.sendToTrackingEntityAndSelf(message: Message, entity: Entity): Unit =
	this.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with { entity }, message)

/**
 * Sends the [message] to all clients tracking the [chunk]
 */
fun SimpleChannel.sendToTrackingChunk(message: Message, chunk: Chunk): Unit =
	this.send(PacketDistributor.TRACKING_CHUNK.with { chunk }, message)

/**
 * Sends the [message] to the given [networkManagers]
 */
fun SimpleChannel.sendToNetworkManagers(message: Message, vararg networkManagers: NetworkManager): Unit =
	this.send(PacketDistributor.NMLIST.with { networkManagers.toList() }, message)

/**
 * Sends the [message] to all clients
 */
fun SimpleChannel.sendToAll(message: Message): Unit = this.send(PacketDistributor.ALL.noArg(), message)
