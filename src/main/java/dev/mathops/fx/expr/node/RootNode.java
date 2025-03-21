package dev.mathops.fx.expr.node;

import dev.mathops.commons.log.Log;
import javafx.collections.ObservableList;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;

/**
 * A node that represents an n-th root, with a base expression and optional root expression.
 *
 * <p>
 * This node presents itself as a radical symbol containing the base node with the root node (if present) drawn above
 * the surd.  The radical sizes itself to the base node, and the overall node sizes itself to contain all child nodes
 * (it may extend above the top of the radical if the root expression is tall).  The baseline of this node is set to
 * match the baseline of the base expression.
 */
public final class RootNode extends ExpressionNodeBase {

    /** A label with the function name. */
    private final ExpressionNodeBase base;

    /** A label with the opening parenthesis (this will size itself to the content node). */
    private final ExpressionNodeBase root;

    /** The radical symbol. */
    private final Path radical;

    /** The baseline offset. */
    private double baselineOffset;

    /**
     * Constructs a new {@code RootNode}.
     *
     * @param theContext the expression rendering context
     * @param theBase    the base expression name
     * @param theRoot    the root expression ({@code null} if none
     */
    RootNode(final ExpressionRenderingContext theContext, final ExpressionNodeBase theBase,
             final ExpressionNodeBase theRoot) {

        super(theContext);

        if (theBase == null) {
            throw new IllegalArgumentException("Root node must have non-null base expression");
        }

        this.base = theBase;
        this.root = theRoot;
        this.radical = new Path();
        this.radical.setFill(theContext.decorationPaint);

        final ObservableList<Node> children = getChildren();
        children.addAll(this.base, this.root, this.radical);

        this.baselineOffset = 0.0;
    }

    /**
     * Gets the number of tokens in the node (the cursor lies in the gaps between tokens).
     *
     * @return the number of tokens
     */
    @Override
    public int getNumTokens() {

        int count = 1 + this.base.getNumTokens();

        if (this.root != null) {
            count += this.root.getNumTokens();
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

        // TODO:
    }

    /**
     * Sets the range of selected nodes in this node.
     *
     * @param start the start of a selected range (-1 if no selection)
     * @param end   the end of a selected range (-1 if no selection)
     */
    @Override
    public void setSelectedRange(final int start, final int end) {

        // TODO:
    }

    /**
     * Lays out child nodes.
     */
    @Override
    protected void layoutChildren() {

        Log.info("Laying out children of root node");

        if (this.base.isNeedsLayout()) {
            this.base.layout();
        }

        if (this.root != null && this.root.isNeedsLayout()) {
            this.root.layout();
        }

        final double w = this.context.lineWidth;

        final Bounds baseBounds = this.base.getLayoutBounds();
        final double baseWidth = baseBounds.getWidth();
        final double baseHeight = baseBounds.getHeight();

        final Bounds rootBounds = this.root == null ? new BoundingBox(0.0, 0.0, 0.0, 0.0) : this.root.getLayoutBounds();
        final double rootWidth = rootBounds.getWidth();
        final double rootHeight = rootBounds.getHeight();

        Log.info("Base: (", baseWidth, "x", baseHeight, ")  Root: ", rootBounds);

        final double surdBottomPointX = Math.max(4.0 * w, rootWidth);
        final double surdHeight = baseHeight + 5.0 * w;
        final double surdTopY = rootHeight > (surdHeight * 0.5) ? rootHeight - surdHeight * 0.5 : 0.0;
        final double surdBottomY = surdHeight + surdTopY;
        final double surdSlope = (baseHeight + 4.0 * w) / (4.0 * w);

        final ObservableList<PathElement> elements = this.radical.getElements();
        elements.clear();

        final PathElement start = new MoveTo(surdBottomPointX, surdBottomY);
        final PathElement line1 = new LineTo(surdBottomPointX + 4.0 * w, surdTopY + w);
        final PathElement line2 = new LineTo(surdBottomPointX + 5.0 * w + baseWidth, surdTopY + w);
        final PathElement line3 = new LineTo(surdBottomPointX + 5.0 * w + baseWidth, surdTopY);
        final PathElement line4 = new LineTo(surdBottomPointX + 3.0 * w + w / surdSlope, surdTopY);
        final double innerPointDy = Math.sqrt(1.0 + surdSlope * surdSlope);
        final PathElement line5 = new LineTo(surdBottomPointX, surdBottomY - innerPointDy);
        final PathElement line6 = new LineTo(surdBottomPointX - 2.0 * w, surdBottomY - innerPointDy * 2.0);
        final PathElement line7 = new LineTo(surdBottomPointX - 4.0 * w, surdBottomY - innerPointDy);
        final PathElement line8 = new LineTo(surdBottomPointX - 2.0 * w, surdBottomY - innerPointDy);
        final PathElement close = new ClosePath();

        elements.addAll(start, line1, line2, line3, line4, line5, line6, line7, line8, close);

        this.base.setLayoutX(surdBottomPointX + 5.0 * w);
        this.base.setLayoutY(surdTopY + 3.0 * w);

        if (this.root != null) {
            final double rootLeft = rootWidth < 4.0 * w ? (4.0 * w - rootWidth) : 0.0;
            this.root.setLayoutX(rootLeft);
            this.root.setLayoutY(0.0);
        }

        this.baselineOffset = surdTopY + 3.0;

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
