package dev.mathops.fx.expr;

/**
 * Symbols that can appear in expressions.
 */
enum ExprSymbol {

    DIGIT_0("0", false, true),

    DIGIT_1("1", false, true),

    DIGIT_2("2", false, true),

    DIGIT_3("3", false, true),

    DIGIT_4("4", false, true),

    DIGIT_5("5", false, true),

    DIGIT_6("6", false, true),

    DIGIT_7("7", false, true),

    DIGIT_8("8", false, true),

    DIGIT_9("9", false, true),

    UNARY_NEGATION("negative", true, false),

    PI("Pi", true, false),

    E("e", true, false),

    RADIX(".", false, false),

    SLASH("over", true, false),

    E_PLUS("times ten to power", true, false),

    E_MINUS("times ten to power negative", true, false),

    RADICAL("root", true, false);

    /** The string that represents this symbol in alt-text. */
    private final String altText;

    /** Flag indicating alt-text string should be surrounded by spaces in alt-text. */
    private final boolean spaceAroundAltText;

    /** Flag indicating the symbol is a digit. */
    private final boolean digit;

    /**
     * Constructs a new {@code ExprSymbol}.
     *
     * @param theAltText           the string that represents the symbol in alt-text
     * @param isSpaceAroundAltText true if the alt-text string should be surrounded by spaces; false if it should be
     *                             emitted without spaces between its string and adjacent symbol strings that do not
     *                             require surrounding space
     * @param isDigit              true if the symbol is a digit; false if not
     */
    ExprSymbol(final String theAltText, final boolean isSpaceAroundAltText, final boolean isDigit) {

        this.altText = theAltText;
        this.spaceAroundAltText = isSpaceAroundAltText;
        this.digit = isDigit;
    }

    /**
     * Gets the string that represents this symbol in alt-text.
     *
     * @return the alt-text string
     */
    public String getAltText() {

        return this.altText;
    }

    /**
     * Tests whether the alt-text should be presented with surrounding space.
     *
     * @return true if the alt-text should be surrounded by spaces; false if not
     */
    boolean isSpaceAroundAltText() {

        return this.spaceAroundAltText;
    }

    /**
     * Tests whether the symbol is a digit.
     *
     * @return true if the symbol is a digit; false if not
     */
    boolean isDigit() {

        return this.digit;
    }
}
