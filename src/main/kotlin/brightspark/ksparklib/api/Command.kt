package brightspark.ksparklib.api

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands

/**
 * Base class for a command that can be registered using the custom overload of [CommandDispatcher.register]
 */
abstract class Command(
	name: String,
	builderBlock: LiteralArgumentBuilder<CommandSource>.() -> Unit
) {
	/**
	 * The command builder which will actually be registered to vanilla's [CommandDispatcher.register]
	 */
	val builder: LiteralArgumentBuilder<CommandSource> = Commands.literal(name).apply(builderBlock)
}
