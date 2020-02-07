package brightspark.ksparklib.api

import net.minecraft.entity.LivingEntity
import net.minecraft.inventory.EquipmentSlotType
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.function.Supplier
import java.util.stream.Stream
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
