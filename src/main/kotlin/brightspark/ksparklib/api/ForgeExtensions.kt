package brightspark.ksparklib.api

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.function.Supplier

/**
 * Gets a logger for this class
 */
fun Any.getLogger(): Logger = LogManager.getLogger(this)

/**
 * Registers this object to the event bus
 */
fun Any.eventBusRegister() = MinecraftForge.EVENT_BUS.register(this)

/**
 * Processes each [net.minecraftforge.fml.InterModComms.IMCMessage] with the [function] after filtering them using the
 * [methodFilter] if provided
 */
fun InterModProcessEvent.processEach(methodFilter: ((String) -> Boolean)? = null, function: (sender: String, thing: Supplier<Any?>) -> Unit) =
	(methodFilter?.let { this.getIMCStream(methodFilter) } ?: this.imcStream)
		.forEach { function(it.senderModId, it.getMessageSupplier()) }
