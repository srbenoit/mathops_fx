package dev.mathops.fx.expr;

import dev.mathops.commons.CoreConstants;
import javafx.scene.Node;
import javafx.scene.text.Text;

/**
 * A node that represents an empty space into which students can type or insert content..
 *
 * <p>
 * This node presents as an empty rectangle with a cursor.
 */
final class NodeEmpty extends AbstractExpressionNode {

    /** The text node that represents this object. */
    private final Text node;

    /**
     * Creates an empty {@code NodeEmpty}.
     */
    NodeEmpty() {

        this.node = new Text(" ");
    }

    /**
     * Tests whether the node is in a valid state.
     *
     * @return true if the node is in a valid state; false if not
     */
    @Override
    boolean isValid() {

        return true;
    }

    /**
     * Gets the number of "tokens" in this node (zero for empty nodes).
     *
     * @return the number of tokens (0)
     */
    @Override
    int getNumTokens() {

        return 0;
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

        EAddSymbolOutcome result = EAddSymbolOutcome.REJECTED;

        if (symbol == ExprSymbol.UNARY_NEGATION || symbol.isDigit()) {
            result = EAddSymbolOutcome.CONVERT_TO_INTEGER;
        } else if (symbol == ExprSymbol.RADIX) {
            result = EAddSymbolOutcome.CONVERT_TO_REAL_DECIMAL;
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

        return false;
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

        return CoreConstants.EMPTY;
    }

    /**
     * Handles a key press.
     *
     * @param codePoint the code point of the key pressed
     * @param position  the cursor position within this node's tokens
     */
    EAddSymbolOutcome handleKey(final int codePoint, final int position) {

        EAddSymbolOutcome result = EAddSymbolOutcome.REJECTED;

        if (codePoint == '-' || (codePoint >= '0' && codePoint <= '9')) {
            result = EAddSymbolOutcome.CONVERT_TO_INTEGER;
        } else if (codePoint == '.') {
            result = EAddSymbolOutcome.CONVERT_TO_REAL_DECIMAL;
        }

        return result;
    }

    /**
     * Sets the caret position within this node.
     *
     * @param position the caret position; -1 if the cursor is not within this node
     */
    void setCaretPosition(final int position) {

        this.node.setCaretPosition(position);
    }
}
