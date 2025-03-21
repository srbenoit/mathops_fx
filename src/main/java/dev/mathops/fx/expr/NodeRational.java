package dev.mathops.fx.expr;

import dev.mathops.commons.CoreConstants;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/**
 * A node that represents a rational number.  Supported forms (any of which could include a leading unary negation)
 * include the following  (where "POSINT" means a sequence of one or more decimal digits):
 * <ul>
 *     <li>A POSINT, "/" token, and a second POSINT.</li>
 * </ul>
 * <p>
 * Note that when such a node is converted to a value in an expression, if there is no slash or if the denominator is
 * 1, the result should be an integer.
 */
final class NodeRational extends AbstractExpressionNode {

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

    /** The index of a slash symbol; -1 if there is no such symbol. */
    private int slashIndex = -1;

    /**
     * Creates an empty {@code NodeRational}.
     */
    NodeRational() {

        this.symbols = new ExprSymbol[10];

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
            // Invalid forms have no digits before or after the slash, or only '0' digits after the slash.

            if (this.slashIndex == -1) {
                // No slash - treat as integer.
                valid = this.symbols[0] != ExprSymbol.UNARY_NEGATION || this.numSymbols > 1;
            } else if (this.slashIndex < this.numSymbols - 1
                       && (this.symbols[0] == ExprSymbol.UNARY_NEGATION ? this.slashIndex > 2 : this.slashIndex > 1)) {
                // Slash is not at the start or the end
                for (int i = this.slashIndex + 1; i < this.numSymbols; ++i) {
                    if (this.symbols[i] != ExprSymbol.DIGIT_0) {
                        valid = true;
                        break;
                    }
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
                result = EAddSymbolOutcome.ACCEPTED;
            }
        } else if (symbol == ExprSymbol.SLASH) {
            if (this.slashIndex == -1) {
                if (position == 0) {
                    result = this.symbols[0] == ExprSymbol.UNARY_NEGATION ? EAddSymbolOutcome.REJECTED :
                            EAddSymbolOutcome.ACCEPTED;
                } else {
                    result = EAddSymbolOutcome.ACCEPTED;
                }
            }
        } else if (symbol == ExprSymbol.RADIX) {
            if (this.slashIndex == -1) {
                if (position == 0) {
                    result = this.symbols[0] == ExprSymbol.UNARY_NEGATION ? EAddSymbolOutcome.REJECTED :
                            EAddSymbolOutcome.CONVERT_TO_REAL_DECIMAL;
                } else {
                    result = EAddSymbolOutcome.CONVERT_TO_REAL_DECIMAL;
                }
            }
        } else if (symbol == ExprSymbol.E_PLUS || symbol == ExprSymbol.E_MINUS) {
            if (this.slashIndex == -1) {
                if (position == 1) {
                    result = this.symbols[0] == ExprSymbol.UNARY_NEGATION ? EAddSymbolOutcome.REJECTED :
                            EAddSymbolOutcome.CONVERT_TO_REAL_DECIMAL;
                } else if (position > 1) {
                    result = EAddSymbolOutcome.CONVERT_TO_REAL_DECIMAL;
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
        } else if (this.slashIndex > position) {
            --this.slashIndex;
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

        if (symbol == ExprSymbol.SLASH) {
            this.slashIndex = position;
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
     * @param codePoint     the code point of the key pressed
     * @param position the cursor position within this node's tokens
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
