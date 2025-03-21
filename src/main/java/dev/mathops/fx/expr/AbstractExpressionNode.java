package dev.mathops.fx.expr;

import javafx.scene.Node;

/**
 * A node in an expression tree.  Every node represents some number of "tokens" between which the cursor can fall. Nodes
 * that contain child nodes report their intrinsic tokens plus the number of tokens in all descendant nodes. The cursor
 * is then represented by an index along with an optional selection anchor index to define a selection range.  The
 * selection range is based on indexes, but visually, if the selection range includes a portion of a node, the entire
 * node is considered "selected" for purposes of cut/copy/paste/delete operations.
 *
 * <p>
 * A node may be in a valid or an invalid state depending on what tokens are present.  For example, within a function's
 * argument list, the user may enter a (-) sign to start the entry of a numeric value.  The editor will create an
 * Integer node with this leading sign, ready to accept new digits, but at this point, the Integer node is invalid.  It
 * would become valid if the user then enters a digit, and invalid again if that digit were deleted.
 */
public abstract class AbstractExpressionNode {

    AbstractExpressionNode() {

        // No action
    }

    /**
     * Tests whether the node is in a valid state.
     *
     * @return true if the node is in a valid state; false if not
     */
    abstract boolean isValid();

    /**
     * Gets the number of "tokens" in this node and its descendants, where a "token" is an atomic object that the cursor
     * can move across in one "step".  The cursor is always in a gap between adjacent tokens, or before the first or
     * after the last token.
     *
     * @return the number of tokens
     */
    abstract int getNumTokens();

    /**
     * Adds a symbol at a specified position.
     *
     * @param symbol   the symbols to add
     * @param position the position at which to add the symbol
     * @return the outcome
     */
    abstract EAddSymbolOutcome addSymbol(ExprSymbol symbol, int position);

    /**
     * Deletes a symbol at a specified position.
     *
     * @param position the position
     * @return boolean if the symbol was deleted
     */
    abstract boolean deleteSymbol(int position);

    /**
     * Gets the {@code Node} that represents this object.
     *
     * @return the node
     */
    abstract Node getNode();

    /**
     * Generates an "alt-text" string that represents node contents.
     *
     * @return the alt-text string
     */
    abstract String generateAltText();

    /**
     * Handles a key press.
     *
     * @param codePoint the code point of the key pressed
     * @param position  the cursor position within this node's tokens
     */
    abstract EAddSymbolOutcome handleKey(int codePoint, int position);

    /**
     * Sets the caret position within this node.
     *
     * @param position the caret position; -1 if the cursor is not within this node
     */
    abstract void setCaretPosition(int position);
}
