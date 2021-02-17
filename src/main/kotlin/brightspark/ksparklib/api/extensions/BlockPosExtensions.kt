package brightspark.ksparklib.api.extensions

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.util.math.vector.Vector3f
import net.minecraft.util.math.vector.Vector3i
import java.util.stream.Stream
import kotlin.streams.toList

fun BlockPos.toVec3i(): Vector3i = Vector3i(x, y, z)

fun BlockPos.toVec3f(): Vector3f = Vector3f(x.toFloat(), y.toFloat(), z.toFloat())

fun BlockPos.toVec3d(): Vector3d = Vector3d(x.toDouble(), y.toDouble(), z.toDouble())

/**
 * Converts this [Stream] of [BlockPos] to a [List] safely
 */
fun Stream<BlockPos>.toBlockPosList(): List<BlockPos> = this.map { it.toImmutable() }.toList()
