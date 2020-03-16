package brightspark.ksparklib.api

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.command.CommandSource

/**
 * Interface for a command that can be registered using the custom overload of [CommandDispatcher.register]
 */
interface Command {
	/**
	 * The command builder which will actually be registered to vanilla's [CommandDispatcher.register]
	 */
	val builder: LiteralArgumentBuilder<CommandSource>
}
