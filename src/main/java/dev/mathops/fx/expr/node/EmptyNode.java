package dev.mathops.fx.expr.node;

import dev.mathops.commons.log.Log;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

/**
 * A node that represents an empty "slot" where a sub-expression can be created.
 *
 * <p>
 * This node presents itself as an empty dotted box.  The box represents "zero tokens" in the stream of tokens in an
 * expression, so when the cursor is rendered, the entire node is highlighted in the selection color.
 */
public final class EmptyNode extends ExpressionNodeBase {

    /** A label with two spaces, used to set the size of the node. */
    private final Text space1;

    /** A label with two spaces, used to set the size of the node. */
    private final Text space2;

    /** A rectangle to show the insertion outline. */
    private final Rectangle rect;

    /** The baseline offset. */
    private double baselineOffset = 0.0;

    /**
     * Constructs a new {@code EmptyNode}.
     *
     * @param theContext the expression rendering context
     */
    EmptyNode(final ExpressionRenderingContext theContext) {

        super(theContext);

        this.space1 = new Text("X");
        this.space1.setFill(Color.TRANSPARENT);
        this.space1.setBoundsType(TextBoundsType.LOGICAL);
        this.space1.setFont(theContext.romanFont);

        this.space2 = new Text("X");
        this.space2.setFill(Color.TRANSPARENT);
        this.space2.setBoundsType(TextBoundsType.VISUAL);
        this.space2.setFont(theContext.romanFont);

        this.rect = new Rectangle();
        this.rect.setFill(null);
        this.rect.setStroke(Color.GRAY);
        this.rect.setStrokeWidth(1.4);
        this.rect.getStrokeDashArray().addAll(Double.valueOf(2.0), Double.valueOf(4.0));
        this.rect.setStrokeType(StrokeType.INSIDE);

        final ObservableList<Node> children = getChildren();
        children.addAll(this.space1, this.space2, this.rect);

        this.space1.setLayoutX(0.0);
        this.space1.setLayoutY(0.0);
        this.space2.setLayoutX(0.0);
        this.space2.setLayoutY(0.0);
        this.rect.setLayoutX(0.0);
        this.rect.setLayoutY(0.0);
    }

    /**
     * Gets the number of tokens in the node (the cursor lies in the gaps between tokens).
     *
     * @return the number of tokens
     */
    @Override
    public int getNumTokens() {

        return 0;
    }

    /**
     * Sets the cursor position within the node.
     *
     * @param position the cursor position (-1 if cursor is not visible within this node)
     */
    @Override
    public void setCursorPosition(final int position) {

        if (position == -1) {
            this.rect.setFill(null);
        } else {
            this.rect.setFill(SELECTION_COLOR);
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

        if (start == -1 || end == -1) {
            this.rect.setFill(null);
        } else {
            this.rect.setFill(SELECTION_COLOR);
        }
    }

    /**
     * Lays out child nodes.
     */
    @Override
    protected void layoutChildren() {

//        Log.info("Laying out empty node");

        final Bounds bounds1 = this.space1.getLayoutBounds();

        final Bounds bounds2 = this.space2.getLayoutBounds();
        final double width = bounds2.getWidth();
        final double height = bounds2.getHeight();

        Log.info("Space: ", bounds1);

        this.space1.setLayoutX(0.0);
        this.space1.setLayoutY(0.0);

        this.rect.setLayoutX(bounds2.getMinX());
        this.rect.setLayoutY(bounds2.getMinY());
        this.rect.setWidth(width);
        this.rect.setHeight(height);

        this.baselineOffset = -bounds1.getMinY();

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
