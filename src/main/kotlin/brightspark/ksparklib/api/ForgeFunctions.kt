package brightspark.ksparklib.api

import net.alexwells.kottle.FMLKotlinModLoadingContext
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.GenericEvent
import net.minecraftforge.fml.InterModComms
import kotlin.reflect.full.createInstance

/**
 * Registers the [target] object to the event bus
 */
fun eventBusRegister(target: Any) = MinecraftForge.EVENT_BUS.register(target)

/**
 * Registers the class [T] to the event bus
 */
inline fun <reified T : Any> eventBusRegister() = eventBusRegister(T::class.run { objectInstance ?: createInstance() })

/**
 * Registers the [function] as an event listener for the event type [T] with the given optional [priority] and
 * [receiveCancelled] properties
 */
inline fun <reified T : Event> addListener(
	priority: EventPriority = EventPriority.NORMAL,
	receiveCancelled: Boolean = false,
	noinline function: (T) -> Unit
) = FMLKotlinModLoadingContext.get().modEventBus.addListener(priority, receiveCancelled, T::class.java, function)

/**
 * Registers the [function] as a generic event listener for the event type [T] with class filter type [F] with the given
 * optional [priority] and [receiveCancelled] properties
 */
inline fun <reified T : GenericEvent<out F>, reified F> addGenericListener(
	priority: EventPriority = EventPriority.NORMAL,
	receiveCancelled: Boolean = false,
	noinline function: (T) -> Unit
) = FMLKotlinModLoadingContext.get().modEventBus.addGenericListener(F::class.java, priority, receiveCancelled, T::class.java, function)

/**
 * Sends an IMC to the mod [modId]
 * @see InterModComms.sendTo
 */
fun <T : Any?> sendIMC(modId: String, method: String, thing: () -> T): Boolean = InterModComms.sendTo(modId, method, thing)
