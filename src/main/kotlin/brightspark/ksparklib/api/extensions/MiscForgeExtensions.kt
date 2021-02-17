package brightspark.ksparklib.api.extensions

import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.inventory.EquipmentSlotType
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketBuffer
import net.minecraft.util.Direction
import net.minecraft.util.Util
import net.minecraft.util.text.ITextComponent
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.function.Supplier

/**
 * Gets a logger for this class
 */
fun Any.getLogger(): Logger = LogManager.getLogger(this)

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
	capable.getCapability(this, side)
		.orElseThrow { RuntimeException("Capability $name does not exist in provider $capable") }

/**
 * Reads an [Enum] value of type [T] from this [PacketBuffer]
 */
inline fun <reified T : Enum<T>> PacketBuffer.readEnumValue(): T = this.readEnumValue(T::class.java)

/**
 * Overload for [Entity.sendMessage] which uses [Util.DUMMY_UUID] instead of an explicit UUID
 */
fun Entity.sendMessage(textComponent: ITextComponent): Unit = this.sendMessage(textComponent, Util.DUMMY_UUID)
