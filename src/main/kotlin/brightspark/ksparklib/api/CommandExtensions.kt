package brightspark.ksparklib.api

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands

/**
 * Registers all [commands] to this [CommandDispatcher]
 */
fun CommandDispatcher<CommandSource>.register(vararg commands: Command): Unit =
	commands.forEach { this.register(it.builder) }

fun Command.literal(name: String, block: LiteralArgumentBuilder<CommandSource>.() -> Unit): LiteralArgumentBuilder<CommandSource> =
	Commands.literal(name).apply(block)

fun <T : ArgumentBuilder<CommandSource, T>> T.thenLiteral(
	name: String,
	literalBlock: LiteralArgumentBuilder<CommandSource>.() -> Unit,
	thenBlock: T.() -> Unit = {}
): T = this.then(Commands.literal(name).apply(literalBlock)).apply(thenBlock)

fun <T : ArgumentBuilder<CommandSource, T>, ARG> T.thenArgument(
	argumentName: String,
	argument: ArgumentType<ARG>,
	argumentBlock: RequiredArgumentBuilder<CommandSource, ARG>.() -> Unit,
	thenBlock: T.() -> Unit = {}
): T = this.then(Commands.argument(argumentName, argument).apply(argumentBlock)).apply(thenBlock)

fun <T : ArgumentBuilder<CommandSource, T>> T.thenCommand(command: Command, block: T.() -> Unit = {}): T =
	this.then(command.builder).apply(block)
