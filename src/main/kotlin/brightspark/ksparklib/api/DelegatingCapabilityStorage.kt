package brightspark.ksparklib.api

import net.minecraft.nbt.CompoundNBT
import net.minecraft.nbt.INBT
import net.minecraft.util.Direction
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.INBTSerializable

class DelegatingCapabilityStorage<T : INBTSerializable<CompoundNBT>> : Capability.IStorage<T> {
	override fun writeNBT(capability: Capability<T>, instance: T, side: Direction?): INBT = instance.serializeNBT()

	override fun readNBT(capability: Capability<T>?, instance: T, side: Direction?, nbt: INBT?) =
		instance.deserializeNBT(nbt as CompoundNBT)
}
