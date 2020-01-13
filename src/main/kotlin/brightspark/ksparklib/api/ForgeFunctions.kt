package brightspark.ksparklib.api

import net.alexwells.kottle.FMLKotlinModLoadingContext
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.Ingredient
import net.minecraft.util.NonNullList
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.GenericEvent
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.fml.InterModComms
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.config.ModConfig
import java.util.concurrent.Callable
import java.util.function.Supplier

/**
 * Registers the [target] object to the event bus
 */
fun forgeEventBusRegister(target: Any) = MinecraftForge.EVENT_BUS.register(target)

/**
 * Registers the class [T] to the event bus
 */
inline fun <reified T : Any> forgeEventBusRegister() = forgeEventBusRegister(T::class.run { objectInstance ?: java })

/**
 * Registers the [function] as an event listener for the event type [T] with the given optional [priority] and
 * [receiveCancelled] properties to the mod event bus
 */
inline fun <reified T : Event> addModListener(
	priority: EventPriority = EventPriority.NORMAL,
	receiveCancelled: Boolean = false,
	noinline function: (T) -> Unit
) = FMLKotlinModLoadingContext.get().modEventBus.addListener(priority, receiveCancelled, T::class.java, function)

/**
 * Registers the [function] as a generic event listener for the event type [T] with class filter type [F] with the given
 * optional [priority] and [receiveCancelled] properties to the mod event bus
 */
inline fun <reified T : GenericEvent<out F>, reified F> addModGenericListener(
	priority: EventPriority = EventPriority.NORMAL,
	receiveCancelled: Boolean = false,
	noinline function: (T) -> Unit
) = FMLKotlinModLoadingContext.get().modEventBus.addGenericListener(F::class.java, priority, receiveCancelled, T::class.java, function)

/**
 * Registers the [function] as an event listener for the event type [T] with the given optional [priority] and
 * [receiveCancelled] properties to the Forge event bus
 */
inline fun <reified T : Event> addForgeListener(
	priority: EventPriority = EventPriority.NORMAL,
	receiveCancelled: Boolean = false,
	noinline function: (T) -> Unit
) = MinecraftForge.EVENT_BUS.addListener(priority, receiveCancelled, T::class.java, function)

/**
 * Registers the [function] as a generic event listener for the event type [T] with class filter type [F] with the given
 * optional [priority] and [receiveCancelled] properties to the Forge event bus
 */
inline fun <reified T : GenericEvent<out F>, reified F> addForgeGenericListener(
	priority: EventPriority = EventPriority.NORMAL,
	receiveCancelled: Boolean = false,
	noinline function: (T) -> Unit
) = MinecraftForge.EVENT_BUS.addGenericListener(F::class.java, priority, receiveCancelled, T::class.java, function)

/**
 * Sends an IMC to the mod [modId]
 * @see InterModComms.sendTo
 */
fun <T : Any?> sendIMC(modId: String, method: String, thing: () -> T): Boolean = InterModComms.sendTo(modId, method, thing)

/**
 * Registers [ForgeConfigSpec] instances for [client], [common] and [server]
 */
fun registerConfig(client: ForgeConfigSpec? = null, common: ForgeConfigSpec? = null, server: ForgeConfigSpec? = null) {
	ModLoadingContext.get().apply {
		client?.let { registerConfig(ModConfig.Type.CLIENT, it) }
		common?.let { registerConfig(ModConfig.Type.COMMON, it) }
		server?.let { registerConfig(ModConfig.Type.SERVER, it) }
	}
}

/**
 * Creates a new [NonNullList] with the given [stacks]
 */
fun stackList(vararg stacks: ItemStack) = NonNullList.from(ItemStack.EMPTY, *stacks)

/**
 * Creates a new [NonNullList] with the given [stacks]
 */
fun stackList(stacks: Collection<ItemStack>) = stackList(*stacks.toTypedArray())

/**
 * Creates a new [NonNullList] with the given [ingredients]
 */
fun ingredientList(vararg ingredients: Ingredient) = NonNullList.from(Ingredient.EMPTY, *ingredients)

/**
 * Creates a new [NonNullList] with the given [ingredients]
 */
fun ingredientList(ingredients: Collection<Ingredient>) = ingredientList(*ingredients.toTypedArray())

/**
 * Runs the [op] when on the [dist]
 */
fun runWhenOn(dist: Dist, op: () -> Unit) = DistExecutor.runWhenOn(dist) { Runnable(op) }

/**
 * Runs the [op] when on [Dist.CLIENT]
 */
fun runWhenOnClient(op: () -> Unit) = DistExecutor.runWhenOn(Dist.CLIENT) { Runnable(op) }

/**
 * Runs the [op] when on [Dist.DEDICATED_SERVER]
 */
fun runWhenOnServer(op: () -> Unit) = DistExecutor.runWhenOn(Dist.DEDICATED_SERVER) { Runnable(op) }

/**
 * Runs and returns the result of the [op] when on the [dist]
 */
fun <R> callWhenOn(dist: Dist, op: () -> R): R = DistExecutor.callWhenOn(dist) { Callable(op) }

/**
 * Runs and returns the result of the [op] when on [Dist.CLIENT]
 */
fun <R> callWhenOnClient(op: () -> R): R = DistExecutor.callWhenOn(Dist.CLIENT) { Callable(op) }

/**
 * Runs and returns the result of the [op] when on [Dist.DEDICATED_SERVER]
 */
fun <R> callWhenOnServer(op: () -> R): R = DistExecutor.callWhenOn(Dist.DEDICATED_SERVER) { Callable(op) }

/**
 * Runs and returns the result of the appropriate operation ([clientOp] or [serverOp]) depending on the distribution
 */
fun <R> runForDist(clientOp: () -> R, serverOp: () -> R): R =
	DistExecutor.runForDist({ Supplier(clientOp) }, { Supplier(serverOp) })
