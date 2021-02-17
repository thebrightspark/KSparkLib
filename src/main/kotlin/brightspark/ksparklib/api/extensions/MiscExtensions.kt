package brightspark.ksparklib.api.extensions

import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType

/**
 * Return true if this [KType] is a subtype of [T]
 */
inline fun <reified T> KType.isSubtypeOf(): Boolean = isSubtypeOf(T::class.starProjectedType)
