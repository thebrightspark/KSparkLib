package brightspark.ksparklib.api.extensions

import net.minecraft.util.text.*

/**
 * Adds a new [StringTextComponent] to the end of the sibling list, with the specified [obj]. Same as calling
 * [IFormattableTextComponent.appendString] and giving it the result of calling [Any.toString] on [obj].
 */
fun IFormattableTextComponent.appendString(obj: Any): IFormattableTextComponent = this.appendString(obj.toString())

/**
 * Adds a new [TranslationTextComponent] to the end of the sibling list, with the specified translation key and
 * arguments. Same as calling [IFormattableTextComponent.append] with a new [TranslationTextComponent].
 */
fun IFormattableTextComponent.appendTranslation(translationKey: String, vararg args: Any): IFormattableTextComponent =
	this.append(TranslationTextComponent(translationKey, args))

/**
 * Adds a new [StringTextComponent] to the end of the sibling list, with the specified [text] and [style].
 * Same as calling [IFormattableTextComponent.append] with a new [StringTextComponent] and calling
 * [IFormattableTextComponent.setStyle] on that.
 */
fun IFormattableTextComponent.appendStyledString(text: String, style: Style): IFormattableTextComponent =
	this.append(StringTextComponent(text).setStyle(style))

/**
 * Adds a new [StringTextComponent] to the end of the sibling list, with the specified [text] and [styles].
 * Same as calling [IFormattableTextComponent.append] with a new [StringTextComponent] and calling
 * [IFormattableTextComponent.mergeStyle] on that.
 */
fun IFormattableTextComponent.appendStyledString(
	text: String,
	vararg styles: TextFormatting
): IFormattableTextComponent =
	this.append(StringTextComponent(text).mergeStyle(*styles))
