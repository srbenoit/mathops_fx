package dev.mathops.fx.expr;

import javafx.scene.Node;
import javafx.scene.text.Text;

/**
 * A node that represents an immutable Boolean TRUE value.
 */
final class NodeBooleanTrue extends AbstractExpressionNode {

    /** The text node that represents this object. */
    private final Text node;

    /**
     * Creates an empty {@code NodeBooleanTrue}.
     */
    NodeBooleanTrue() {

        super();

        this.node = new Text("TRUE");
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
     * Gets the number of "tokens" in this node and its descendants, where a "token" is an atomic object that the cursor
     * can move across in one "step".  The cursor is always in a gap between adjacent tokens, or before the first or
     * after the last token.
     *
     * @return the number of tokens
     */
    @Override
    int getNumTokens() {

        return 1;
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

        return EAddSymbolOutcome.REJECTED;
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

        return "TRUE";
    }

    /**
     * Handles a key press.
     *
     * @param codePoint     the code point of the key pressed
     * @param position the cursor position within this node's tokens
     */
    EAddSymbolOutcome handleKey(final int codePoint, final int position) {

        return EAddSymbolOutcome.REJECTED;
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
