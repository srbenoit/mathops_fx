package dev.mathops.fx.expr.node;

import dev.mathops.commons.log.Log;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

import java.util.ArrayList;
import java.util.List;

/**
 * A node that represents an invocation of a function.
 *
 * <p>
 * This node presents itself as a function name followed by a left parenthesis, then its list of arguments, separated by
 * commas, then a right parenthesis.
 */
public final class FunctionNode extends ExpressionNodeBase {

    /** A label with the function name. */
    private final Text function;

    /** A rectangle to display fill behind the function name. */
    private final Rectangle functionFill;

    /** A label with the opening parenthesis (this will size itself to the content node). */
    private final Text opening;

    /** A rectangle to display fill behind the opening paren. */
    private final Rectangle openingFill;

    /** A label with the closing parenthesis. */
    private final Text closing;

    /** A rectangle to display fill behind the closing paren. */
    private final Rectangle closingFill;

    /** A rectangle to display the cursor. */
    private final Rectangle cursor;

    /** The argument nodes (never empty). */
    private final List<ExpressionNodeBase> arguments;

    /** The list of commas, each followed by a space (length one less than arguments list). */
    private final List<Text> commas;

    /** The baseline height. */
    private double baselineOffset = 0.0;

    /**
     * Constructs a new {@code FunctionNode}.
     *
     * @param theContext         the expression rendering context
     * @param theFunctionName    the function name
     * @param theInitialArgument the initial argument node (could be an {@code EmptyNode})
     */
    FunctionNode(final ExpressionRenderingContext theContext, final String theFunctionName,
                 final ExpressionNodeBase theInitialArgument) {

        super(theContext);

        if (theFunctionName == null) {
            throw new IllegalArgumentException("Function name may not be null");
        }
        if (theInitialArgument == null) {
            throw new IllegalArgumentException("Function argument may not be null");
        }

        this.function = new Text(theFunctionName);
        this.function.setBoundsType(TextBoundsType.LOGICAL);
        this.opening = new Text("(");
        this.opening.setBoundsType(TextBoundsType.LOGICAL);
        this.closing = new Text(")");
        this.closing.setBoundsType(TextBoundsType.LOGICAL);

        this.arguments = new ArrayList<>(4);
        this.commas = new ArrayList<>(3);

        this.arguments.add(theInitialArgument);

        this.function.setFont(theContext.romanFont);
        this.opening.setFont(theContext.romanFont);
        this.closing.setFont(theContext.romanFont);

        this.functionFill = new Rectangle(0.0, 0.0, 0.0, 0.0);
        this.functionFill.setFill(SELECTION_COLOR);
        this.openingFill = new Rectangle(0.0, 0.0, 0.0, 0.0);
        this.openingFill.setFill(SELECTION_COLOR);
        this.closingFill = new Rectangle(0.0, 0.0, 0.0, 0.0);
        this.closingFill.setFill(SELECTION_COLOR);
        this.cursor = new Rectangle(0.0, 0.0, 0.0, 0.0);
        this.cursor.setFill(CURSOR_COLOR);

        final ObservableList<Node> children = getChildren();
        children.addAll(this.functionFill, this.openingFill, this.closingFill, this.function, this.opening,
                this.closing, theInitialArgument, this.cursor);
    }

    /**
     * Gets the number of tokens in the node (the cursor lies in the gaps between tokens).
     *
     * @return the number of tokens
     */
    @Override
    public int getNumTokens() {

        int count = 2 + this.commas.size();

        for (final ExpressionNodeBase inner : this.arguments) {
            count += inner.getNumTokens();
        }

        return count;
    }

    /**
     * Sets the cursor position within the node.
     *
     * @param position the cursor position (-1 if cursor is not visible within this node)
     */
    @Override
    public void setCursorPosition(final int position) {

        clearCursorAndSelection();

        final int numTokens = getNumTokens();

        if (position < 0 || position > numTokens) {
            this.cursor.setLayoutX(0.0);
            this.cursor.setLayoutY(0.0);
            this.cursor.setWidth(0.0);
            this.cursor.setHeight(0.0);
        } else {
            final double halfWidth = this.context.cursorWidth * 0.5;
            final Bounds nameBounds = this.function.getLayoutBounds();

            if (position == 0) {
                this.cursor.setLayoutX(-halfWidth);
                this.cursor.setWidth(this.context.cursorWidth);
                this.cursor.setLayoutY(nameBounds.getMinY());
                this.cursor.setHeight(nameBounds.getHeight());
            } else if (position == numTokens) {
                final Bounds bounds = getLayoutBounds();
                this.cursor.setLayoutX(bounds.getMaxX() - halfWidth);
                this.cursor.setWidth(this.context.cursorWidth);
                this.cursor.setLayoutY(nameBounds.getMinY());
                this.cursor.setHeight(nameBounds.getHeight());
            } else {
                int indexInArg = position - 1;
                for (final ExpressionNodeBase inner : this.arguments) {
                    final int innerCount = inner.getNumTokens();
                    if (indexInArg <= innerCount) {
                        inner.setCursorPosition(indexInArg);
                        indexInArg -= (innerCount + 1);
                    }
                }
            }
        }
    }

    /**
     * Sets the range of selected nodes in this node.
     *
     * @param start the start of a selected range (-1 if no selection)
     * @param end   the end of a selected range (-1 if no selection)
     */
    @Override
    public void setSelectedRange(final int start, final int end) {

        clearCursorAndSelection();

        final int numTokens = getNumTokens();

        if (start == 0 && end > 0) {
            // Function name and open paren are included in selection
            final Bounds functionBounds = this.function.getLayoutBounds();
            setRectToBounds(this.functionFill, functionBounds);

            final Bounds openingBounds = this.opening.getLayoutBounds();
            setRectToBounds(this.openingFill, openingBounds);
        }

        if (start < numTokens && end >= numTokens) {
            // Closing paren is included in selection
            final Bounds closingBounds = this.closing.getLayoutBounds();
            setRectToBounds(this.closingFill, closingBounds);
        }

        // Note: all arguments will have had their selections cleared above, so we only need to update an argument
        // if the selection overlaps that argument's token range

        int selectionStart = start - 1;
        int selectionEnd = end - 1;
        for (final ExpressionNodeBase inner : this.arguments) {
            final int innerCount = inner.getNumTokens();

            if (selectionStart < innerCount) {
                inner.setSelectedRange(selectionStart, selectionEnd);
            }

            // TODO: show selection on commas

            selectionStart -= innerCount - 1;
            selectionEnd -= innerCount - 1;

            if (selectionEnd < 0) {
                break;
            }
        }
    }

    /**
     * Clears the cursor and selection markers.
     */
    private void clearCursorAndSelection() {

        for (final ExpressionNodeBase inner : this.arguments) {
            inner.setCursorPosition(-1);
        }

        this.cursor.setWidth(0.0);
        this.cursor.setHeight(0.0);

        this.functionFill.setWidth(0.0);
        this.functionFill.setHeight(0.0);

        this.openingFill.setWidth(0.0);
        this.openingFill.setHeight(0.0);

        this.closingFill.setWidth(0.0);
        this.closingFill.setHeight(0.0);
    }

    /**
     * Sets the layout position and size of a rectangle to match provided bounds.
     *
     * @param rect   the rectangle to adjust
     * @param bounds the bounds
     */
    private static void setRectToBounds(final Rectangle rect, final Bounds bounds) {

        rect.setLayoutX(bounds.getMinX());
        rect.setLayoutY(bounds.getMinY());
        rect.setWidth(bounds.getWidth());
        rect.setHeight(bounds.getHeight());
    }

    /**
     * Lays out child nodes.
     */
    @Override
    protected void layoutChildren() {

//        Log.info("Laying out children of function node");

        for (final ExpressionNodeBase arg : this.arguments) {
            if (arg.isNeedsLayout()) {
                arg.layout();
            }
        }

        double x = 0.0;

        final Bounds functionBounds = this.function.getLayoutBounds();
        final Bounds openingBounds = this.opening.getLayoutBounds();
        final Bounds closingBounds = this.closing.getLayoutBounds();

        final double functionHeight = functionBounds.getHeight();
        final double functionWidth = functionBounds.getWidth();

        double minY = Math.min(functionBounds.getMinY(),
                Math.min(openingBounds.getMinY(), closingBounds.getMinY()));
        double maxY = Math.max(functionBounds.getMaxY(),
                Math.max(openingBounds.getMaxY(), closingBounds.getMaxY()));
        for (final ExpressionNodeBase arg : this.arguments) {
            final Bounds argBounds = arg.getLayoutBounds();
            minY = Math.min(minY, argBounds.getMinY());
            maxY = Math.max(maxY, argBounds.getMaxY());
        }
        for (final Text comma : this.commas) {
            final Bounds commaBounds = comma.getLayoutBounds();
            minY = Math.min(minY, commaBounds.getMinY());
            maxY = Math.max(maxY, commaBounds.getMaxY());
        }

        this.baselineOffset = -minY;

        Log.info("Overall baseline offset: ", Double.toString(this.baselineOffset));
        Log.info("Maximum descent: ", Double.toString(maxY));

        // Determine the maximum ascent - this becomes the new baseline offset

        final double functionNameY = this.baselineOffset + functionBounds.getMinY();

        this.function.setLayoutX(x);
        this.function.setLayoutY(functionNameY);

        x += functionWidth;

        final double openingHeight = openingBounds.getHeight();
        final double openingWidth = openingBounds.getWidth();
        final double openingY = this.baselineOffset + openingBounds.getMinY();

        this.opening.setLayoutX(x);
        this.opening.setLayoutY(openingY);

        x += openingWidth;

        final ExpressionNodeBase arg0 = this.arguments.getFirst();

        final Bounds arg0Bounds = arg0.getLayoutBounds();
        final double arg0Height = arg0Bounds.getHeight();
        final double arg0Width = arg0Bounds.getWidth();
        final double arg0Y = this.baselineOffset + arg0Bounds.getMinY();

        arg0.setLayoutX(x);
        arg0.setLayoutY(arg0Y);

        x += arg0Width;

        final int numCommas = this.commas.size();
        for (int i = 0; i < numCommas; ++i) {
            final Text comma = this.commas.get(i);

            final Bounds commaBounds = comma.getLayoutBounds();
            final double commaHeight = commaBounds.getHeight();
            final double commaWidth = commaBounds.getWidth();
            final double commaY = this.baselineOffset + commaBounds.getMinY();

            comma.setLayoutX(x);
            comma.setLayoutY(commaY);

            x += commaWidth;

            final ExpressionNodeBase arg = this.arguments.get(i + 1);

            final Bounds argBounds = arg0.getLayoutBounds();
            final double argHeight = argBounds.getHeight();
            final double argWidth = argBounds.getWidth();
            final double argY = this.baselineOffset + argBounds.getMinY();

            arg.setLayoutX(x);
            arg.setLayoutY(argY);

            x += argWidth;
        }

        final double closingHeight = closingBounds.getHeight();
        final double closingWidth = closingBounds.getWidth();
        final double closingY = this.baselineOffset + closingBounds.getMinY();

        this.closing.setLayoutX(x);
        this.closing.setLayoutY(closingY);

        x += closingWidth;

        setNeedsLayout(false);
    }

    /**
     * Gets the baseline offset.
     *
     * @return the baseline offset
     */
    public double getBaselineOffset() {

        return this.baselineOffset;
    }
}
