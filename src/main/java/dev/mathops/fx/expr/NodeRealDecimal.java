package dev.mathops.fx.expr;

import javafx.scene.Node;
import javafx.scene.text.Text;

/**
 * A node that represents a real number in decimal format.  Supported forms (any of which could include a leading unary
 * negation) include
 * <ul>
 *     <li>An integer, like "98765".</li>
 *     <li>An integer followed by a radix, like "123.".</li>
 *     <li>A radix followed by one or more digits, like ".5".</li>
 *     <li>One or more digits, then a radix followed by one or more digits, like "2.52".</li>
 *     <li>Any of the above forms can be followed by "E+" or "E-" then one or more digits like "1.21E+9".</li>
 * </ul>
 */
final class NodeRealDecimal extends AbstractExpressionNode {

    /** The text node that represents this object. */
    private final Text node;

    /** The sequence of symbols in the node (length will be at least {@code numSymbols}). */
    private ExprSymbol[] symbols;

    /** The number of symbols currently in the node. */
    private int numSymbols = 0;

    /** The index of a radix symbol; -1 if there is no such symbol. */
    private int radixIndex = -1;

    /** The index of an E+ or E- symbol; -1 if there is no such symbol. */
    private int eeIndex = -1;

    /**
     * Creates an empty {@code NodeRealDecimal}.
     */
    NodeRealDecimal() {

        this.symbols = new ExprSymbol[10];
        this.node = new Text();
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
            if (this.radixIndex == -1) {
                if (this.eeIndex == -1) {
                    // No radix, no E+ or E-, this is an integer form.  Invalid form is "(-)".
                    valid = this.symbols[0] != ExprSymbol.UNARY_NEGATION || this.numSymbols > 1;
                } else {
                    // No radix, but there is an E+ or E-, so this is something like "123E+9".  Invalid forms are
                    // "1.5E+" (with E+ or E- at the end), or "E+4" or "(-)E+4" (with no digits before the E+ or E-).
                    valid = (this.eeIndex < this.numSymbols - 1) &&
                            (this.symbols[0] == ExprSymbol.UNARY_NEGATION ? this.eeIndex > 1 : this.eeIndex > 0);
                }
            } else if (this.eeIndex == -1) {
                // There is a radix but no E+ or E-.  Invalid form is "(-).".
                valid = this.symbols[0] == ExprSymbol.UNARY_NEGATION ? this.numSymbols > 2 : this.numSymbols > 1;
            } else {
                // There is a radix and an E+ or E-.  Invalid forms have E+/E- before the radix, "1.5E+" (with E+ or
                // E- at the end), or ".E+4" or "(-).E+4" (with only a radix before the E+ or E-).
                valid = (this.radixIndex < this.eeIndex)
                        && (this.eeIndex < this.numSymbols - 1)
                        && (this.symbols[0] == ExprSymbol.UNARY_NEGATION ? this.eeIndex > 2 : this.eeIndex > 1);
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
        } else if (symbol == ExprSymbol.RADIX) {
            if (this.radixIndex == -1) {
                if (position == 0) {
                    result = this.symbols[0] == ExprSymbol.UNARY_NEGATION ? EAddSymbolOutcome.REJECTED :
                            EAddSymbolOutcome.ACCEPTED;
                } else if (this.eeIndex >= 0) {
                    result = position <= this.eeIndex ? EAddSymbolOutcome.ACCEPTED : EAddSymbolOutcome.REJECTED;
                } else {
                    result = EAddSymbolOutcome.ACCEPTED;
                }
            }
        } else if (symbol == ExprSymbol.E_PLUS || symbol == ExprSymbol.E_MINUS) {
            if (this.eeIndex == -1 && position > 0) {
                // NOTE: We accept E+/E- at the end, even though it leaves an invalid state, but we don't allow the
                // E+/E- after a radix
                if (this.radixIndex == -1 || position > this.radixIndex) {
                    // Require something before the E+/E-, even if it is just a radix
                    if (position == 1) {
                        result = this.symbols[0] == ExprSymbol.UNARY_NEGATION ? EAddSymbolOutcome.REJECTED :
                                EAddSymbolOutcome.ACCEPTED;
                    } else {
                        result = EAddSymbolOutcome.ACCEPTED;
                    }
                }
            }
        } else if (symbol == ExprSymbol.SLASH) {
            if (this.eeIndex == -1 && this.radixIndex == -1) {
                // This node can be interpreted as a integer
                if (position == 1) {
                    result = this.symbols[0] == ExprSymbol.UNARY_NEGATION ? EAddSymbolOutcome.REJECTED :
                            EAddSymbolOutcome.CONVERT_TO_RATIONAL;
                } else if (position > 1) {
                    result = EAddSymbolOutcome.CONVERT_TO_RATIONAL;
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

        if (toRemove == ExprSymbol.RADIX) {
            this.radixIndex = -1;
        } else if (toRemove == ExprSymbol.E_PLUS || toRemove == ExprSymbol.E_MINUS) {
            this.eeIndex = -1;
        } else {
            if (this.radixIndex > position) {
                --this.radixIndex;
            }
            if (this.eeIndex > position) {
                --this.eeIndex;
            }
        }

        if (position <= this.numSymbols - 1) {
            final int numToMove = this.numSymbols - position - 1;
            System.arraycopy(this.symbols, position + 1, this.symbols, position, numToMove);
        }

        --this.numSymbols;

        updateText();

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

        if (this.eeIndex >= position) {
            ++this.eeIndex;
        }
        if (this.radixIndex >= position) {
            ++this.radixIndex;
        }

        if (symbol == ExprSymbol.RADIX) {
            this.radixIndex = position;
        } else if (symbol == ExprSymbol.E_PLUS || symbol == ExprSymbol.E_MINUS) {
            this.eeIndex = position;
        }

        updateText();
    }

    /**
     * Updates the text content in the {@code Text} node representation of the object.
     */
    private void updateText() {

        final StringBuilder builder = new StringBuilder(10);
        for (int i = 0; i < this.numSymbols; ++i) {
            switch (this.symbols[i]) {
                case DIGIT_0 -> builder.append('0');
                case DIGIT_1 -> builder.append('1');
                case DIGIT_2 -> builder.append('2');
                case DIGIT_3 -> builder.append('3');
                case DIGIT_4 -> builder.append('4');
                case DIGIT_5 -> builder.append('5');
                case DIGIT_6 -> builder.append('6');
                case DIGIT_7 -> builder.append('7');
                case DIGIT_8 -> builder.append('8');
                case DIGIT_9 -> builder.append('9');
                case UNARY_NEGATION -> builder.append("(-)");
                case RADIX -> builder.append('.');
                case E_PLUS -> builder.append("E+");
                case E_MINUS -> builder.append("E-");
            }
        }

        final String str = builder.toString();
        this.node.setText(str);
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

        this.node.setCaretPosition(position);
    }
}
