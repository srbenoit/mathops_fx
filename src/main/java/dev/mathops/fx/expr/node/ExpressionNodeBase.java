package dev.mathops.fx.expr.node;

import javafx.scene.Group;
import javafx.scene.paint.Color;

/**
 * The base class for nodes that represent parts of an expression.
 */
public abstract class ExpressionNodeBase extends Group {

    /** The color for selected nodes; "LightSteelBlue2". */
    static final Color SELECTION_COLOR = Color.rgb(188, 210, 238);

    /** The color for the cursor, based on M<S Word "Blue", closest named color is "DodgerBlue3". */
    static final Color CURSOR_COLOR = Color.rgb(0, 112, 192);

    /** The expression rendering context. */
    final ExpressionRenderingContext context;

    /**
     * Constructs a new {@code AbstractExpressionNode}.
     *
     * @param theContext the expression rendering context
     */
    ExpressionNodeBase(final ExpressionRenderingContext theContext) {

        super();

        this.context = theContext;
        setAutoSizeChildren(false);
    }

    /**
     * Gets the number of tokens in the node (the cursor lies in the gaps between tokens).
     *
     * @return the number of tokens
     */
    public abstract int getNumTokens();

    /**
     * Sets the cursor position within the node.
     *
     * @param position the cursor position (-1 if cursor is not visible within this node)
     */
    public abstract void setCursorPosition(int position);

    /**
     * Sets the range of selected nodes in this node.
     *
     * @param start the start of a selected range (-1 if no selection)
     * @param end   the end of a selected range (-1 if no selection)
     */
    public abstract void setSelectedRange(int start, int end);
}
