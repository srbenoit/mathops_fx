package dev.mathops.fx.expr;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;

/**
 * A node that represents a grouping.
 *
 * <p>
 * The tokens in this node include one token for the open parenthesis, one token for the closing parenthesis, all tokens
 * of the argument node.
 */
final class NodeGrouping extends AbstractExpressionNode {

    /** The overall node that contains all nodes in this object's representation. */
    private final FlowPane node;

    /** The text node that presents the opening parenthesis, sized to hold the argument's nodes. */
    private final Text openingParen;

    /** The text node that presents the closing parenthesis, sized to hold the argument's nodes. */
    private final Text closingParen;

    /** The argument node. */
    private AbstractExpressionNode argument;

    /**
     * Creates an empty {@code NodeGrouping}.
     */
    NodeGrouping() {

        super();

        this.openingParen = new Text("(");
        this.closingParen = new Text(")");
        this.argument = new NodeEmpty();

        this.node = new FlowPane();
        final ObservableList<Node> children = this.node.getChildren();
        final Node emptyArgNode = this.argument.getNode();
        children.addAll(this.openingParen, emptyArgNode, this.closingParen);
    }

    /**
     * Tests whether the node is in a valid state.
     *
     * @return true if the node is in a valid state; false if not
     */
    @Override
    boolean isValid() {

        return this.argument.isValid();
    }

    /**
     * Gets the number of "tokens" in this node and its descendants, where a "token" is an atomic object that the cursor
     * can move across in one "step".  The cursor is always in a gap between adjacent tokens, or before the first or
     * after the last token.
     *
     * @return the number of tokens
     */
    @Override
    int getNumTokens() {

        return 2 + this.argument.getNumTokens();
    }

    /**
     * Adds a symbol at a specified position.
     *
     * @param symbol   the symbols to add
     * @param position the position at which to add the symbol
     * @return the outcome
     */
    @Override
    EAddSymbolOutcome addSymbol(final ExprSymbol symbol, final int position) {

        final EAddSymbolOutcome result;

        // We can't add a symbol at [0] or at the end.
        if (position > 0 || position < getNumTokens()) {
            result = this.argument.addSymbol(symbol, position - 1);
        } else {
            result = EAddSymbolOutcome.REJECTED;
        }

        return result;
    }

    /**
     * Deletes a symbol at a specified position.
     *
     * @param position the position
     * @return boolean if the symbol was deleted
     */
    @Override
    boolean deleteSymbol(final int position) {

        boolean deleted = false;

        // We can't delete the first or last symbol
        if (position > 0 && position < (getNumTokens() - 1)) {
            deleted = this.argument.deleteSymbol(position - 1);
        }

        return deleted;
    }

    /**
     * Gets the {@code Node} that represents this object.
     *
     * @return the node
     */
    Node getNode() {

        return this.node;
    }

    /**
     * Generates an "alt-text" string that represents node contents.
     *
     * @return the alt-text string
     */
    String generateAltText() {

        return "(" + this.argument.generateAltText() + ")";
    }

    /**
     * Handles a key press.
     *
     * @param codePoint the code point of the key pressed
     * @param position  the cursor position within this node's tokens
     */
    EAddSymbolOutcome handleKey(final int codePoint, final int position) {

        final EAddSymbolOutcome result;

        if (position > 0 && position > getNumTokens()) {
            result = this.argument.handleKey(codePoint, position - 1);
        } else {
            result = EAddSymbolOutcome.REJECTED;
        }

        return result;
    }

    /**
     * Sets the caret position within this node.
     *
     * @param position the caret position; -1 if the cursor is not within this node
     */
    void setCaretPosition(final int position) {

        if (position == 0) {
            this.openingParen.setCaretPosition(0);
        } else if (position == getNumTokens()) {
            this.closingParen.setCaretPosition(1);
        } else {
            this.argument.setCaretPosition(position - 1);
        }
    }
}
