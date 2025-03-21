package dev.mathops.fx.expr;

import dev.mathops.commons.CoreConstants;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/**
 * A node that represents a real number expressed as a rational multiple of a square root of a positive integer like "5
 * square root 3 over 2". Supported forms (any of which could include a leading unary negation) include the following
 * (where "POSINT" means a sequence of one or more decimal digits):
 *
 * <ul>
 *     <li>A "radical" token and a POSINT optionally followed by (a "/" token followed by a nonzero POSINT)</li>
 *     <li>A POSINT, then a "radical" token and a POSINT optionally followed by (a "/" token followed by a nonzero
 *     POSINT)</li>
 * </ul>
 */
final class NodeIrrationalRoot extends AbstractExpressionNode {

    /** The node that represents this object. */
    private final BorderPane node;

    /** The node that represents this object. */
    private final Text numerator;

    /** The node that represents this object. */
    private final Text denominator;

    /** The node that represents this object. */
    private final Text negation;

    /** The sequence of symbols in the node (length will be at least {@code numSymbols}). */
    private ExprSymbol[] symbols;

    /** The number of symbols currently in the node. */
    private int numSymbols = 0;

    /** The index of the radical symbol; -1 if there is no such symbol. */
    private int radicalIndex = -1;

    /** The index of a slash symbol; -1 if there is no such symbol. */
    private int slashIndex = -1;

    /**
     * Creates an empty {@code NodeIrrationalRoot}.
     */
    NodeIrrationalRoot() {

        this.symbols = new ExprSymbol[10];

        // TODO: To show the overline after the surd, we could do a Text for the "pre-surd" content and the surd, then
        //  a second Text (with a top border) for the "post-surd: content

        this.node = new BorderPane();
        this.numerator = new Text();
        this.denominator = new Text();
        this.negation = new Text();

        final Rectangle rect = new Rectangle(1.0, 1.0);
        final NumberBinding maxWidth = Bindings.max(this.numerator.wrappingWidthProperty(),
                this.denominator.wrappingWidthProperty());
        rect.widthProperty().bind(maxWidth);

        this.node.setLeft(this.negation);
        this.node.setTop(this.numerator);
        this.node.setCenter(rect);
        this.node.setBottom(this.denominator);
    }

    /**
     * Tests whether the node is in a valid state.
     *
     * @return true if the node is in a valid state; false if not
     */
    @Override
    boolean isValid() {

        boolean valid = false;

        if (this.numSymbols > 0) {
            // The string is non-empty (an empty string is invalid)

            // Invalid forms have no symbols or digits before or after the slash, no digits between the radical and
            // the slash, or only '0' digits after the slash.

            if (this.slashIndex == -1) {
                // Valid forms are "R2" or "3R2" or "3", or any of these with unary minus
                if (this.radicalIndex == -1) {
                    // No slash and no radical symbol - treat as integer
                    valid = this.symbols[0] != ExprSymbol.UNARY_NEGATION || this.numSymbols > 1;
                } else {
                    // No slash; radical may not be the last symbol
                    valid = this.radicalIndex != this.numSymbols - 1;
                }
            } else if (this.slashIndex < this.numSymbols - 1
                       && (this.symbols[0] == ExprSymbol.UNARY_NEGATION ? this.slashIndex > 2 : this.slashIndex > 1)) {

                for (int i = this.slashIndex + 1; i < this.numSymbols; ++i) {
                    if (this.symbols[i] != ExprSymbol.DIGIT_0) {
                        valid = true;
                        break;
                    }
                }

                if (valid && this.radicalIndex > -1) {
                    // Slash is not at the start or the end, and we don't have "divide by zero".
                    // Valid forms are "R2/5" or "3R2/5", or any of these with unary minus.
                    valid = this.slashIndex - this.radicalIndex > 0;
                }
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

        return this.numSymbols;
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

        if (symbol == ExprSymbol.UNARY_NEGATION) {
            result = position == 0 && (this.numSymbols == 0 || this.symbols[0] != ExprSymbol.UNARY_NEGATION)
                    ? EAddSymbolOutcome.ACCEPTED : EAddSymbolOutcome.REJECTED;
        } else if (symbol.isDigit()) {
            if (position == 0) {
                result = this.symbols[0] == ExprSymbol.UNARY_NEGATION ? EAddSymbolOutcome.REJECTED :
                        EAddSymbolOutcome.ACCEPTED;
            } else {
                // We accept digits when:
                // - There is no radical symbol, whether there is a slash or not
                // - There is a radical and no slash, either before or after the radical
                // - There are both radical and slash, either before, after, or between
                // The net effect is that we always accept a digit, just not before the unary minus
                result = EAddSymbolOutcome.ACCEPTED;
            }
        } else if (symbol == ExprSymbol.SLASH) {
            if (this.slashIndex == -1) {
                if (this.radicalIndex == -1) {
                    // There is no slash or radical.  In theory this should not happen since the object should have
                    // been converted to an Integer node before we get in this situation.
                    if (position == 0) {
                        result = this.symbols[0] == ExprSymbol.UNARY_NEGATION ? EAddSymbolOutcome.REJECTED :
                                EAddSymbolOutcome.CONVERT_TO_RATIONAL;
                    } else {
                        result = EAddSymbolOutcome.CONVERT_TO_RATIONAL;
                    }
                } else {
                    // There is a radical but no slash - the slash MUST fall after the radical
                    result = position > this.radicalIndex ? EAddSymbolOutcome.ACCEPTED : EAddSymbolOutcome.REJECTED;
                }
            }
        } else if (symbol == ExprSymbol.RADICAL) {
            if (this.radicalIndex == -1) {
                if (this.slashIndex == -1) {
                    // Adding a radical symbol when there is no slash.
                    if (position == 0) {
                        result = this.symbols[0] == ExprSymbol.UNARY_NEGATION ? EAddSymbolOutcome.REJECTED :
                                EAddSymbolOutcome.ACCEPTED;
                    } else {
                        result = EAddSymbolOutcome.ACCEPTED;
                    }
                } else {
                    // Adding a radical when there is already a slash - the radical must fall before the slash
                    result = position <= this.slashIndex ? EAddSymbolOutcome.ACCEPTED : EAddSymbolOutcome.REJECTED;
                }
            }
        }

        if (result == EAddSymbolOutcome.ACCEPTED) {
            increaseSizeIfNeeded();
            insertSymbol(symbol, position);
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

        final ExprSymbol toRemove = this.symbols[position];

        if (toRemove == ExprSymbol.SLASH) {
            this.slashIndex = -1;
        } else if (toRemove == ExprSymbol.RADICAL) {
            this.radicalIndex = -1;
        }

        if (this.slashIndex > position) {
            --this.slashIndex;
        }
        if (this.radicalIndex > position) {
            --this.radicalIndex;
        }

        if (position <= this.numSymbols - 1) {
            final int numToMove = this.numSymbols - position - 1;
            System.arraycopy(this.symbols, position + 1, this.symbols, position, numToMove);
        }

        --this.numSymbols;

        updateNode();

        return true;
    }

    /**
     * Increases the size of the symbol buffer if needed to allow it to accommodate one more symbol.
     */
    private void increaseSizeIfNeeded() {

        final int newSize = this.numSymbols + 1;
        if (this.symbols.length < newSize) {
            final int newCapacity = newSize + 6;
            final ExprSymbol[] newArray = new ExprSymbol[newCapacity];
            System.arraycopy(this.symbols, 0, newArray, 0, this.numSymbols);
            this.symbols = newArray;
        }
    }

    /**
     * Inserts a symbol at a specified position and increments {@code numSymbols}.  It is assumed that the symbol array
     * is large enough to accommodate the new symbol.
     *
     * @param symbol   the symbol to insert
     * @param position the position
     */
    private void insertSymbol(final ExprSymbol symbol, final int position) {

        if (position < this.numSymbols) {
            final int numToMove = this.numSymbols - position;
            System.arraycopy(this.symbols, position, this.symbols, position + 1, numToMove);
        }

        this.symbols[position] = symbol;
        ++this.numSymbols;

        if (this.slashIndex >= position) {
            ++this.slashIndex;
        }
        if (this.radicalIndex >= position) {
            ++this.radicalIndex;
        }

        if (symbol == ExprSymbol.SLASH) {
            this.slashIndex = position;
        } else if (symbol == ExprSymbol.RADICAL) {
            this.radicalIndex = position;
        }

        updateNode();
    }

    /**
     * Updates the text content in the {@code Text} node representation of the object.
     */
    private void updateNode() {

        if (this.numSymbols == 0 || this.symbols[0] != ExprSymbol.UNARY_NEGATION) {
            this.negation.setText(CoreConstants.EMPTY);
        } else {
            this.negation.setText("(-)");
        }

        final StringBuilder num = new StringBuilder(10);
        final StringBuilder den = new StringBuilder(10);
        StringBuilder active = num;

        for (int i = 0; i < this.numSymbols; ++i) {
            switch (this.symbols[i]) {
                case DIGIT_0 -> active.append('0');
                case DIGIT_1 -> active.append('1');
                case DIGIT_2 -> active.append('2');
                case DIGIT_3 -> active.append('3');
                case DIGIT_4 -> active.append('4');
                case DIGIT_5 -> active.append('5');
                case DIGIT_6 -> active.append('6');
                case DIGIT_7 -> active.append('7');
                case DIGIT_8 -> active.append('8');
                case DIGIT_9 -> active.append('9');
                case RADICAL -> active.append('\u221a');
                case SLASH -> active = den;
            }
        }

        if (num.isEmpty()) {
            this.numerator.setText("_");
        } else {
            this.numerator.setText(num.toString());
        }

        if (den.isEmpty()) {
            this.denominator.setText("_");
        } else {
            this.denominator.setText(den.toString());
        }
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

        final StringBuilder builder = new StringBuilder(this.numSymbols * 2);

        boolean didSpace = false;

        for (int i = 0; i < this.numSymbols; ++i) {
            final ExprSymbol sym = this.symbols[i];
            final boolean spc = sym.isSpaceAroundAltText();

            if (spc && !didSpace) {
                builder.append(' ');
            }
            final String alt = sym.getAltText();
            builder.append(alt);
            if (spc) {
                builder.append(' ');
            }
            didSpace = spc;
        }

        return builder.toString();
    }

    /**
     * Handles a key press.
     *
     * @param codePoint the code point of the key pressed
     * @param position  the cursor position within this node's tokens
     */
    EAddSymbolOutcome handleKey(final int codePoint, final int position) {

        // FIXME:

        return EAddSymbolOutcome.REJECTED;
    }

    /**
     * Sets the caret position within this node.
     *
     * @param position the caret position; -1 if the cursor is not within this node
     */
    void setCaretPosition(final int position) {

        if (position < 0 || position > this.numSymbols) {
            this.negation.setCaretPosition(-1);
            this.numerator.setCaretPosition(-1);
            this.denominator.setCaretPosition(-1);
        } else if (this.numSymbols > 0 && this.symbols[0] == ExprSymbol.UNARY_NEGATION) {
            // We have a leading (-) symbol
            if (position == 0) {
                this.negation.setCaretPosition(0);
                this.numerator.setCaretPosition(-1);
                this.denominator.setCaretPosition(-1);
            } else if (this.slashIndex == -1 || position < this.slashIndex) {
                this.negation.setCaretPosition(-1);
                this.numerator.setCaretPosition(position - 1);
                this.denominator.setCaretPosition(-1);
            } else {
                this.negation.setCaretPosition(-1);
                this.numerator.setCaretPosition(-1);
                this.denominator.setCaretPosition(position - this.slashIndex);
            }
        } else {
            this.negation.setCaretPosition(-1);
            // We do NOT have a leading (-) symbol
            if (this.slashIndex == -1 || position < this.slashIndex) {
                this.numerator.setCaretPosition(position);
                this.denominator.setCaretPosition(-1);
            } else {
                this.numerator.setCaretPosition(-1);
                this.denominator.setCaretPosition(position - this.slashIndex);
            }
        }
    }
}
