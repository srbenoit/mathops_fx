package dev.mathops.fx.expr;

import javafx.application.ColorScheme;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Settings that affect rendering of an expression.
 *
 * @param colorScheme      the current color scheme (LIGHT or DARK), used to select rendering colors
 * @param font             the font
 * @param fontSize         the font size for "unscaled" text
 * @param fontWeight       the font weight
 * @param shapeWeight      the weight for shapes like fraction lines, radicals, sigma and integral symbols, etc.
 * @param cursorColor      the cursor color
 * @param cursorWidth      the width of the cursor
 * @param errorDecorations decorations to render for errors and invalid nodes (null for no decoration)
 */
record ExpressionRenderContext(ColorScheme colorScheme, Font font, double fontSize, FontWeight fontWeight,
                               double shapeWeight, Color symbolColor, Color shapeColor, Color cursorColor,
                               double cursorWidth, EExpressionErrorDecoration errorDecorations) {
}
