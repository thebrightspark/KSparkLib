package brightspark.ksparklib.api

import com.mojang.brigadier.arguments.ArgumentType
import net.alexwells.kottle.FMLKotlinModLoadingContext
import net.minecraft.command.arguments.ArgumentSerializer
import net.minecraft.command.arguments.ArgumentTypes
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.Ingredient
import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.NonNullList
import net.minecraft.util.ResourceLocation
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.GenericEvent
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.fml.InterModComms
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.network.NetworkRegistry
import net.minecraftforge.fml.network.simple.SimpleChannel
import java.util.concurrent.Callable
import java.util.function.Supplier
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

/**
 * Registers the [target] object to the event bus
 */
fun forgeEventBusRegister(target: Any): Unit = MinecraftForge.EVENT_BUS.register(target)

/**
 * Registers the class [T] to the event bus
 */
inline fun <reified T : Any> forgeEventBusRegister(): Unit =
	forgeEventBusRegister(T::class.run { objectInstance ?: java })

/**
 * Registers the [function] as an event listener for the event type [T] with the given optional [priority] and
 * [receiveCancelled] properties to the mod event bus
 */
inline fun <reified T : Event> addModListener(
	priority: EventPriority = EventPriority.NORMAL,
	receiveCancelled: Boolean = false,
	noinline function: (T) -> Unit
): Unit = FMLKotlinModLoadingContext.get().modEventBus.addListener(priority, receiveCancelled, T::class.java, function)

/**
 * Registers the [function] as a generic event listener for the event type [T] with class filter type [F] with the given
 * optional [priority] and [receiveCancelled] properties to the mod event bus
 */
inline fun <reified T : GenericEvent<out F>, reified F> addModGenericListener(
	priority: EventPriority = EventPriority.NORMAL,
	receiveCancelled: Boolean = false,
	noinline function: (T) -> Unit
): Unit = FMLKotlinModLoadingContext.get().modEventBus.addGenericListener(F::class.java, priority, receiveCancelled, T::class.java, function)

/**
 * Registers the [function] as an event listener for the event type [T] with the given optional [priority] and
 * [receiveCancelled] properties to the Forge event bus
 */
inline fun <reified T : Event> addForgeListener(
	priority: EventPriority = EventPriority.NORMAL,
	receiveCancelled: Boolean = false,
	noinline function: (T) -> Unit
): Unit = MinecraftForge.EVENT_BUS.addListener(priority, receiveCancelled, T::class.java, function)

/**
 * Registers the [function] as a generic event listener for the event type [T] with class filter type [F] with the given
 * optional [priority] and [receiveCancelled] properties to the Forge event bus
 */
inline fun <reified T : GenericEvent<out F>, reified F> addForgeGenericListener(
	priority: EventPriority = EventPriority.NORMAL,
	receiveCancelled: Boolean = false,
	noinline function: (T) -> Unit
): Unit = MinecraftForge.EVENT_BUS.addGenericListener(F::class.java, priority, receiveCancelled, T::class.java, function)

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
 * Adds a listener for [AttachCapabilitiesEvent] to attach the capability provided by the [PROVIDER] to objects of the
 * type [ATTACH] if it doesn't already have the capability and [attachPredicate] returns true
 */
inline fun <reified ATTACH, reified PROVIDER : ICapabilityProvider> regAttachCapability(
	key: ResourceLocation,
	crossinline attachPredicate: (AttachCapabilitiesEvent<ATTACH>) -> Boolean = { true }
): Unit = addForgeListener<AttachCapabilitiesEvent<ATTACH>> {
	if (!it.capabilities.containsKey(key) && attachPredicate(it))
		it.addCapability(key, PROVIDER::class.createInstance())
}

/**
 * Registers a capability of type [T] with the [storage] and [factory]
 */
inline fun <reified T> regCapability(storage: Capability.IStorage<T>, noinline factory: () -> T): Unit =
	CapabilityManager.INSTANCE.register(T::class.java, storage, factory)

/**
 * Registers a capability of type [CAP] with the [storage] and [factory].
 * Also adds a listener for [AttachCapabilitiesEvent] to attach to types of [ATTACH].
 */
inline fun <reified CAP, reified ATTACH, reified PROVIDER : ICapabilityProvider> regCapability(
	storage: Capability.IStorage<CAP>,
	noinline factory: () -> CAP,
	key: ResourceLocation,
	crossinline attachPredicate: (AttachCapabilitiesEvent<ATTACH>) -> Boolean = { true }
) {
	regCapability(storage, factory)
	regAttachCapability<ATTACH, PROVIDER>(key, attachPredicate)
}

/**
 * Registers a capability of type [T] which implements [INBTSerializable] with the [factory] and a default
 * [net.minecraftforge.common.capabilities.Capability.IStorage] implementation that delegates to the capability
 */
inline fun <reified T : INBTSerializable<CompoundNBT>> regCapability(noinline factory: () -> T): Unit =
	CapabilityManager.INSTANCE.register(T::class.java, DelegatingCapabilityStorage<T>(), factory)

/**
 * Registers a capability of type [CAP] which implements [INBTSerializable] with the [factory] and a default
 * [net.minecraftforge.common.capabilities.Capability.IStorage] implementation that delegates to the capability.
 *
 * Also adds a listener for [AttachCapabilitiesEvent] to attach to types of [ATTACH].
 */
inline fun <reified CAP : INBTSerializable<CompoundNBT>, reified ATTACH, reified PROVIDER : ICapabilityProvider> regCapability(
	noinline factory: () -> CAP,
	key: ResourceLocation,
	crossinline attachPredicate: (AttachCapabilitiesEvent<ATTACH>) -> Boolean = { true }
) {
	regCapability(factory)
	regAttachCapability<ATTACH, PROVIDER>(key, attachPredicate)
}

/**
 * Registers and returns a new [SimpleChannel] with the given [name] and [protocolVersion], then registers all
 * [messages] supplied
 */
fun regSimpleChannel(
	name: ResourceLocation,
	protocolVersion: String,
	clientAcceptedVersions: (String) -> Boolean = { it == protocolVersion },
	serverAcceptedVersions: (String) -> Boolean = { it == protocolVersion },
	messages: Array<KClass<out Message>>
): SimpleChannel =
	NetworkRegistry.newSimpleChannel(name, { protocolVersion }, clientAcceptedVersions, serverAcceptedVersions).apply {
		messages.forEachIndexed { index, kClass -> registerMessage(kClass, index) }
	}

/**
 * Registers a new [ArgumentType] with the given [id]
 * Note that this method requires the [ArgumentType] [T] to be a Kotlin Object
 */
inline fun <reified T : ArgumentType<out Any>> regCommandArgType(id: String) {
	val instance = T::class.objectInstance
		?: throw RuntimeException("The argument type ${T::class.qualifiedName} must be a Kotlin Object!")
	ArgumentTypes.register(id, T::class.java, ArgumentSerializer { instance })
}

/**
 * Registers a new [ArgumentType] with the given [id] which creates an [ArgumentSerializer] using the [instance]
 */
fun regCommandArgType(id: String, instance: ArgumentType<out Any>): Unit =
	ArgumentTypes.register(id, instance.javaClass, ArgumentSerializer { instance })

/**
 * Registers a new [ArgumentType] with the given [id] and [serializer]
 */
inline fun <reified T : ArgumentType<out Any>> regCommandArgType(id: String, serializer: ArgumentSerializer<T>): Unit =
	ArgumentTypes.register(id, T::class.java, serializer)

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
fun runWhenOn(dist: Dist, op: () -> Unit): Unit = DistExecutor.runWhenOn(dist) { Runnable(op) }

/**
 * Runs the [op] when on [Dist.CLIENT]
 */
fun runWhenOnClient(op: () -> Unit): Unit = DistExecutor.runWhenOn(Dist.CLIENT) { Runnable(op) }

/**
 * Runs the [op] when on [Dist.DEDICATED_SERVER]
 */
fun runWhenOnServer(op: () -> Unit): Unit = DistExecutor.runWhenOn(Dist.DEDICATED_SERVER) { Runnable(op) }

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
