package dev.mathops.fx.expr.node;

import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

/**
 * A container for setting related to expression rendering.
 */
public class ExpressionRenderingContext {

    /** The paint to use for decorations like fraction dividers, radical symbols, etc. */
    public Paint decorationPaint;

    /** The paint to use for function names and related parentheses. */
    public Paint functionNamePaint;

    /** The "main" font size before applying adjustments for superscript/subscript, etc. */
    public double fontSize;

    /** The "Roman" font. */
    public Font romanFont;

    /** The "Italic" font. */
    public Font italicFont;

    /** The line width for decorations like fraction dividers, radical symbols, etc. */
    public double lineWidth;

    /** The cursor width. */
    public double cursorWidth;
}
