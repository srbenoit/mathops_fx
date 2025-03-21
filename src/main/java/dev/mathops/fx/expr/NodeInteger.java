package dev.mathops.fx.expr;

import javafx.scene.Node;
import javafx.scene.text.Text;

/**
 * A node that represents an integer.
 *
 * <p>
 * The tokens in this node can include an optional (-) sign at the start, followed by zero or more ASCII digits (0
 * through 9).
 *
 * <p>
 * If the user is typing into an integer node and types a symbol that would only be allowed in a node that represents a
 * real number (a radix, slash, E+, E-, Pi, e, or Root symbol), the integer node can be replaced by an appropriate real
 * number node with its current list of symbols copied to the new object, and then the typed symbol can be applied to
 * the real value node.
 */
final class NodeInteger extends AbstractExpressionNode {

    /** The text node that represents this object. */
    private final Text node;

    /** The sequence of symbols in the node (length will be at least {@code numSymbols}). */
    private ExprSymbol[] symbols;

    /** The number of symbols currently in the node. */
    private int numSymbols = 0;

    /**
     * Creates an empty {@code NodeInteger}.
     */
    NodeInteger() {

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

        return this.numSymbols > 0 && (this.symbols[0] != ExprSymbol.UNARY_NEGATION || this.numSymbols > 1);
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
            if (position == 0) {
                result = this.symbols[0] == ExprSymbol.UNARY_NEGATION ? EAddSymbolOutcome.REJECTED :
                        EAddSymbolOutcome.CONVERT_TO_REAL_DECIMAL;
            } else {
                result = EAddSymbolOutcome.CONVERT_TO_REAL_DECIMAL;
            }
        } else if (symbol == ExprSymbol.E_PLUS || symbol == ExprSymbol.E_MINUS) {
            if (position == 1) {
                result = this.symbols[0] == ExprSymbol.UNARY_NEGATION ? EAddSymbolOutcome.REJECTED :
                        EAddSymbolOutcome.CONVERT_TO_REAL_DECIMAL;
            } else if (position > 1) {
                result = EAddSymbolOutcome.CONVERT_TO_REAL_DECIMAL;
            }
        } else if (symbol == ExprSymbol.SLASH) {
            if (position == 1) {
                result = this.symbols[0] == ExprSymbol.UNARY_NEGATION ? EAddSymbolOutcome.REJECTED :
                        EAddSymbolOutcome.CONVERT_TO_RATIONAL;
            } else if (position > 1) {
                result = EAddSymbolOutcome.CONVERT_TO_RATIONAL;
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
     * @param codePoint the code point of the key pressed
     * @param position  the cursor position within this node's tokens
     */
    EAddSymbolOutcome handleKey(final int codePoint, final int position) {

        EAddSymbolOutcome result = EAddSymbolOutcome.REJECTED;

        if (codePoint == '0') {
            result = addSymbol(ExprSymbol.DIGIT_0, position);
        } else if (codePoint == '1') {
            result = addSymbol(ExprSymbol.DIGIT_1, position);
        } else if (codePoint == '2') {
            result = addSymbol(ExprSymbol.DIGIT_2, position);
        } else if (codePoint == '3') {
            result = addSymbol(ExprSymbol.DIGIT_3, position);
        } else if (codePoint == '4') {
            result = addSymbol(ExprSymbol.DIGIT_4, position);
        } else if (codePoint == '5') {
            result = addSymbol(ExprSymbol.DIGIT_5, position);
        } else if (codePoint == '6') {
            result = addSymbol(ExprSymbol.DIGIT_6, position);
        } else if (codePoint == '7') {
            result = addSymbol(ExprSymbol.DIGIT_7, position);
        } else if (codePoint == '8') {
            result = addSymbol(ExprSymbol.DIGIT_8, position);
        } else if (codePoint == '9') {
            result = addSymbol(ExprSymbol.DIGIT_9, position);
        } else if (codePoint == '-') {
            result = addSymbol(ExprSymbol.UNARY_NEGATION, position);
        } else if (codePoint == '.') {
            result = addSymbol(ExprSymbol.RADIX, position);
        } else if (codePoint == '/') {
            result = addSymbol(ExprSymbol.SLASH, position);
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

    /**
     * Constructs a {@code NodeRealDecimal} that is initialized with the same sequence of symbols as this object.
     *
     * @return the decimal node
     */
    NodeRealDecimal toRealDecimal() {

        final NodeRealDecimal result = new NodeRealDecimal();

        for (int i = 0; i < this.numSymbols; ++i) {
            result.addSymbol(this.symbols[i], i);
        }

        return result;
    }

    /**
     * Constructs a {@code NodeRational} that is initialized with the same sequence of symbols as this object.
     *
     * @return the decimal node
     */
    NodeRational toRational() {

        final NodeRational result = new NodeRational();

        for (int i = 0; i < this.numSymbols; ++i) {
            result.addSymbol(this.symbols[i], i);
        }

        return result;
    }
}
