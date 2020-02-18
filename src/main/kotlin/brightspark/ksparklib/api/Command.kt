package brightspark.ksparklib.api

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.command.CommandSource

interface Command {
	val builder: LiteralArgumentBuilder<CommandSource>
}
