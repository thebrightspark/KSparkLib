package brightspark.ksparklib.api

import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.inventory.EquipmentSlotType
import net.minecraft.item.ItemStack
import net.minecraft.network.NetworkManager
import net.minecraft.network.PacketBuffer
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TextFormatting
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.dimension.DimensionType
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent
import net.minecraftforge.fml.network.PacketDistributor
import net.minecraftforge.fml.network.simple.SimpleChannel
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.function.Supplier
import java.util.stream.Stream
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.streams.toList

/**
 * Gets a logger for this class
 */
fun Any.getLogger(): Logger = LogManager.getLogger(this)

/**
 * Registers this object to the event bus
 */
fun Any.forgeEventBusRegister() = MinecraftForge.EVENT_BUS.register(this)

/**
 * Processes each [net.minecraftforge.fml.InterModComms.IMCMessage] with the [function] after filtering them using the
 * [methodFilter] if provided
 */
fun InterModProcessEvent.processEach(methodFilter: ((String) -> Boolean)? = null, function: (sender: String, thing: Supplier<Any?>) -> Unit) =
	(methodFilter?.let { this.getIMCStream(methodFilter) } ?: this.imcStream)
		.forEach { function(it.senderModId, it.getMessageSupplier()) }

/**
 * Damages this [ItemStack] by the given [amount] by the [entity]
 * If the stack is broken, this calls [LivingEntity.sendBreakAnimation] with the [breakSlotType]
 */
fun ItemStack.damageItem(entity: LivingEntity, amount: Int = 1, breakSlotType: EquipmentSlotType = EquipmentSlotType.MAINHAND) =
	this.damageItem(amount, entity) { it.sendBreakAnimation(breakSlotType) }

/**
 * Damages this [ItemStack] by the given [amount]
 * This is adapted from vanilla's [ItemStack.damageItem] and has all entity related logic removed
 */
fun ItemStack.damageItem(world: World, amount: Int = 1) {
	if (world.isRemote || !this.isDamageable)
		return
	val item = this.item
	val amount2 = item.damageItem(this, amount, null, {})
	if (this.attemptDamageItem(amount2, world.random, null)) {
		this.shrink(1)
		this.damage = 0
	}
}

/**
 * Damages this [ItemStack] by the given [amount]
 * This will call the appropriate [damageItem] method overload depending on whether [entity] is null
 */
fun ItemStack.damageItem(world: World, entity: LivingEntity?, amount: Int = 1) =
	if (entity == null) stack.damageItem(world, amount) else stack.damageItem(entity, amount)

/**
 * Converts this [Stream] of [BlockPos] to a [List] safely
 */
fun Stream<BlockPos>.toBlockPosList(): List<BlockPos> = this.map { it.toImmutable() }.toList()

/**
 * Runs the [op] if this [World] is on the client side
 */
inline infix fun <R> World.onClient(op: World.() -> R): R? = if (this.isRemote) op(this) else null

/**
 * Runs the [op] if this [World] is on the server side
 */
inline infix fun <R> World.onServer(op: World.() -> R): R? = if (!this.isRemote) op(this) else null

/**
 * Gets the capability instance [T] from [capable] with the optional [side] and throws an error if it does not exist
 */
fun <T> Capability<T>.get(capable: ICapabilityProvider, side: Direction? = null): T =
	capable.getCapability(this, side).orElseThrow { RuntimeException("Capability $name does not exist in provider $capable") }

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
 * Send the [message] to all clients in the [dimensionType]
 */
fun SimpleChannel.sendToDimension(message: Message, dimensionType: DimensionType): Unit =
	this.send(PacketDistributor.DIMENSION.with { dimensionType }, message)

/**
 * Send the [message] to all clients near the [targetPoint]
 */
fun SimpleChannel.sendToNear(message: Message, targetPoint: PacketDistributor.TargetPoint): Unit =
	this.send(PacketDistributor.NEAR.with { targetPoint }, message)

/**
 * Send the [message] to all clients near the [pos] and [dimensionType] within the [range] and optionally excluding the
 * given [excluded] player
 */
fun SimpleChannel.sendToNear(message: Message, pos: Vec3d, range: Double, dimensionType: DimensionType, excluded: ServerPlayerEntity? = null): Unit =
	sendToNear(message, PacketDistributor.TargetPoint(excluded, pos.x, pos.y, pos.z, range, dimensionType))

/**
 * Send the [message] to all clients near the [pos] and [dimensionType] within the [range] and optionally excluding the
 * given [excluded] player
 */
fun SimpleChannel.sendToNear(message: Message, pos: Vec3i, range: Double, dimensionType: DimensionType, excluded: ServerPlayerEntity? = null): Unit =
	sendToNear(message, PacketDistributor.TargetPoint(excluded, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), range, dimensionType))

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

/**
 * Adds a new [StringTextComponent] to the end of the sibling list, with the specified [obj] Same as calling
 * [ITextComponent.appendText] and giving it the result of calling [Any.toString] on [obj].
 */
fun ITextComponent.appendText(obj: Any): ITextComponent = this.appendText(obj.toString())

/**
 * Adds a new [TranslationTextComponent] to the end of the sibling list, with the specified translation key and
 * arguments. Same as calling [ITextComponent.appendSibling] with a new [TranslationTextComponent].
 */
fun ITextComponent.appendTranslation(translationKey: String, vararg args: Any): ITextComponent =
	this.appendSibling(TranslationTextComponent(translationKey, args))

/**
 * Adds a new [StringTextComponent] to the end of the sibling list, with the specified [text] and [styles].
 * Same as calling [ITextComponent.appendSibling] with a new [StringTextComponent] and calling
 * [ITextComponent.applyTextStyles] on that.
 */
fun ITextComponent.appendStyledText(text: String, vararg styles: TextFormatting): ITextComponent =
	this.appendSibling(StringTextComponent(text).applyTextStyles(*styles))

/**
 * Reads an [Enum] value of type [T] from this [PacketBuffer]
 */
inline fun <reified T : Enum<T>> PacketBuffer.readEnumValue(): T = this.readEnumValue(T::class.java)
