package dev.mathops.fx.expr;

import dev.mathops.assessment.formula.EFunction;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * A node that represents a function invocation.
 *
 * <p>
 * The tokens in this node include one token for the function name and open parenthesis, one token for the closing
 * parenthesis, all tokens of the argument node(s), and a token for a "comma" between each argument.
 *
 * <p>
 * A user can type a backslash to start TeX entry mode, then the function name and opening parenthesis, which will then
 * convert that entry into a function invocation with an Empty node as the argument.
 */
final class NodeFunction extends AbstractExpressionNode {

    /** The overall node that contains all nodes in this object's representation. */
    private final FlowPane node;

    /** The text node that presents the function name. */
    private final Text functionName;

    /** The text node that presents the opening parenthesis, sized to hold the argument's nodes. */
    private final Text openingParen;

    /** The text node that presents the closing parenthesis, sized to hold the argument's nodes. */
    private final Text closingParen;

    /** The list of argument nodes (the number allowed depends on the function). */
    private final List<AbstractExpressionNode> arguments;

    /** The text nodes that present commas. */
    private final List<Text> commas;

    /**
     * Creates an empty {@code NodeFunction}.
     *
     * @param function the function this node represents
     */
    NodeFunction(final EFunction function) {

        super();

        this.functionName = new Text(function.name);
        this.openingParen = new Text("(");
        this.closingParen = new Text(")");
        this.arguments = new ArrayList<>(2);
        final NodeEmpty emptyArg = new NodeEmpty();
        this.arguments.add(emptyArg);
        this.commas = new ArrayList<>(1);

        this.node = new FlowPane();
        final ObservableList<Node> children = this.node.getChildren();
        final Node emptyArgNode = emptyArg.getNode();
        children.addAll(this.functionName, this.openingParen, emptyArgNode, this.closingParen);
    }

    /**
     * Tests whether the node is in a valid state.
     *
     * @return true if the node is in a valid state; false if not
     */
    @Override
    boolean isValid() {

        boolean valid = true;

        for (final AbstractExpressionNode test : this.arguments) {
            if (!test.isValid()) {
                valid = false;
                break;
            }
        }

        return valid;
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

        int numTokens = 2;

        // Add "comma" tokens
        final int numArgs = this.arguments.size();
        if (numArgs > 1) {
            numTokens += numArgs - 1;
        }

        for (final AbstractExpressionNode test : this.arguments) {
            numTokens += test.getNumTokens();
        }

        return numTokens;
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

        // We can't add a symbol at [0] or at the end.  Any other position will be adjacent to an argument.
        if (position > 0 || position < getNumTokens()) {

            int current = 1;
            for (final AbstractExpressionNode test : this.arguments) {
                final int len = test.getNumTokens();
                if (position <= (current + len)) {
                    result = test.addSymbol(symbol, position - current);
                    break;
                }
                current += len + 1;
            }
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

            final int count = this.arguments.size();
            int current = 1;
            for (int i = 0; i < count; ++i) {
                final AbstractExpressionNode test = this.arguments.get(i);

                final int len = test.getNumTokens();
                if (position < (current + len)) {
                    deleted = test.deleteSymbol(position - current);
                    break;
                } else if (position == current + len) {
                    // Asking to delete the comma - this works only if there is an "Empty" node before or after the
                    // comma, in which case we delete the comma and the empty node
                    if (test instanceof NodeEmpty) {
                        this.arguments.remove(i);
                        if (count > 1) {
                            this.commas.remove(i);
                        }
                        deleted = true;
                    } else if (i + 1 < count) {
                        final AbstractExpressionNode next = this.arguments.get(i + 1);

                        if (next instanceof NodeEmpty) {
                            this.arguments.remove(i + 1);
                            this.commas.remove(i);
                            deleted = true;
                        }
                    }

                    break;
                }
                current += len + 1;
            }
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

        final StringBuilder builder = new StringBuilder(40);

        final String nameText = this.functionName.getText();
        builder.append(nameText);
        builder.append("(");

        boolean comma = false;
        for (final AbstractExpressionNode test : this.arguments) {
            if (comma) {
                builder.append(", ");
            }
            final String argAltText = test.generateAltText();
            builder.append(argAltText);
            comma = true;
        }

        builder.append(")");

        return builder.toString();
    }

    /**
     * Handles a key press.
     *
     * @param codePoint the code point of the key pressed
     * @param position  the cursor position within this node's tokens
     */
    EAddSymbolOutcome handleKey(final int codePoint, final int position) {

        EAddSymbolOutcome result = EAddSymbolOutcome.REJECTED;

        if (position > 0 && position > getNumTokens()) {

            int current = 1;
            for (final AbstractExpressionNode test : this.arguments) {
                final int len = test.getNumTokens();
                if (position <= (current + len)) {
                    result = test.handleKey(codePoint, position - current);
                    break;
                }
                current += len + 1;
            }
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
            this.functionName.setCaretPosition(0);
        } else if (position == getNumTokens()) {
            this.closingParen.setCaretPosition(1);
        } else {
            int current = 1;
            for (final AbstractExpressionNode test : this.arguments) {
                final int len = test.getNumTokens();
                if (position <= (current + len)) {
                    test.setCaretPosition(position - current);
                    break;
                }
                current += len + 1;
            }
        }
    }
}
