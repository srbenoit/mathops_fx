package dev.mathops.fx.coursebuilder;

import javafx.geometry.Insets;
import javafx.scene.layout.BorderWidths;
import javafx.scene.paint.Color;

/**
 * A central location for constants used across the application.
 */
public enum AppConstants {
    ;

    /** The width for aligned labels. */
    static final double LABEL_WIDTH = 90.0;

    /** A padding width. */
    static final double V_PAD = 4.0;

    /** A padding width. */
    static final double H_PAD = 4.0;

    /** A horizontal gap width. */
    static final double V_GAP = V_PAD + V_PAD;

    /** A horizontal gap width. */
    static final double H_GAP = H_PAD + H_PAD;

    /** A commonly-used insets object. */
    public static final Insets PADDING = new Insets(V_PAD, H_PAD, V_PAD, H_PAD);

    /** A commonly-used insets object. */
    public static final Insets BUTTON_ROW_PADDING = new Insets(V_GAP, H_PAD, V_PAD, H_PAD);

    /** A fixed border width specification. */
    public static final BorderWidths ONE_PIX_BORDER = new BorderWidths(1.0, 1.0, 1.0, 1.0);

    /** A fixed border width specification. */
    static final BorderWidths ONE_PIX_TOP_BORDER = new BorderWidths(1.0, 0.0, 0.0, 0.0);

    /** A fixed border width specification. */
    static final BorderWidths ONE_PIX_RIGHT_BORDER = new BorderWidths(0.0, 1.0, 0.0, 0.0);

    /** An CSS style class. */
    static final String FLOATING_CLASS = "floating";

    /** An icon filename. */
    static final String INFO_ICON = "info.png";

    /** An icon filename. */
    static final String LESSONS_ICON = "lessons.png";

    /** An icon filename. */
    static final String SKILLS_REVIEW_ICON = "skills_review.png";

    /** An icon filename. */
    static final String STANDARDS_ICON = "standards.png";

    /** An icon filename. */
    static final String OBJECTIVES_ICON = "objectives.png";

    /** An icon filename. */
    static final String EXAMPLES_ICON = "examples.png";

    /** An icon filename. */
    static final String HANDOUTS_ICON = "handout.png";

    /** An icon filename. */
    static final String EXPLORATIONS_ICON = "exploration.png";

    /** An icon filename. */
    static final String APPLICATIONS_ICON = "application.png";

    /** An icon filename. */
    static final String ITEMS_ICON = "items.png";

    /** An icon filename. */
    static final String ASSESSMENTS_ICON = "assessments.png";

    /** A common directory name. */
    static final String SKILLS_REVIEW_DIR = "10_skills_review";

    /** A common directory name. */
    static final String EXAMPLES_DIR = "30_examples";

    /** A common directory name. */
    static final String EXPLORATIONS_DIR = "40_explorations";

    /** A common directory name. */
    static final String APPLICATIONS_DIR = "41_applications";

    /** A common directory name. */
    static final String HANDOUTS_DIR = "60_handouts";

    /** A common directory name. */
    static final String ITEMS_DIR = "80_items";

    /** A common directory name. */
    static final String ASSESSMENTS_DIR = "81_assessments";

    /** A common filename. */
    static final String METADATA_FILE = "metadata.json";

    /** The filename of the Word icon. */
    static final String WORD_ICON = "docx24.png";

    /** The filename of the PowerPoint icon. */
    static final String POWERPOINT_ICON = "pptx24.png";

    /** The filename of the Excel icon. */
    static final String EXCEL_ICON = "xlsx24.png";

    /** The filename of the PDF icon. */
    static final String PDF_ICON = "pdf24.png";

    /** The filename of the MP4 file icon. */
    static final String MP4_ICON = "mp424.png";

    /** The filename of the WAV file icon. */
    static final String WAV_ICON = "wav24.png";

    /** The filename of the MP4 file icon. */
    static final String VTT_ICON = "vtt24.png";

    /** The filename of the MP4 file icon. */
    static final String TXT_ICON = "txt24.png";

    /** The filename of the Premiere file icon. */
    static final String PREMIERE_ICON = "prproj24.png";

    /** The filename of a PNG image icon. */
    static final String PNG_ICON = "png24.png";

    /** The filename of a JPEG image icon. */
    static final String JPG_ICON = "jpg24.png";

    /** The filename of a webp image icon. */
    static final String WEBP_ICON = "webp24.png";

    /** The filename of the Premiere file icon. */
    static final String SVG_ICON = "svg24.png";

    /** The filename of the Premiere file icon. */
    static final String XML_ICON = "xml24.png";

    /** The filename of the XCF (Gimp) file icon. */
    static final String XCF_ICON = "xcf24.png";

    /** The filename of the unexpected file icon. */
    static final String UNEXPECTED_ICON = "unexpected24.png";

    /** A file extension. */
    static final String DOCX_EXT = ".docx";

    /** A file extension. */
    static final String PPTX_EXT = ".pptx";

    /** A file extension. */
    static final String PDF_EXT = ".pdf";

    /** A file extension. */
    static final String MP4_EXT = ".mp4";

    /** A file extension. */
    static final String WAV_EXT = ".wav";

    /** A file extension. */
    static final String VTT_EXT = ".vtt";

    /** A file extension. */
    static final String TXT_EXT = ".txt";

    /** A file extension. */
    static final String PRPROJ_EXT = ".prproj";

    /** A file extension. */
    static final String PNG_EXT = ".png";

    /** A file extension. */
    static final String JPG_EXT = ".jpg";

    /** A file extension. */
    static final String JPEG_EXT = ".jpeg";

    /** A file extension. */
    static final String WEBP_EXT = ".webp";

    /** A file extension. */
    static final String SVG_EXT = ".svg";

    /** A metadata property. */
    static final String XML_EXT = ".xml";

    /** A file extension. */
    static final String XCF_EXT = ".xcf";

    /** A metadata property. */
    static final String TITLE_PROPERTY = "title";

    /** A metadata property. */
    static final String DESCRIPTION_PROPERTY = "description";

    /** A metadata property. */
    static final String AUTHORS_PROPERTY = "authors";

    /** A metadata property. */
    static final String IMAGE_PREFIX = "image_";

    /** A metadata property. */
    static final String DRAWING_PREFIX = "drawing_";

    /** A metadata property. */
    static final String SLIDES_PREFIX = "slides.";

    /** A metadata property. */
    static final String CAMERA_PREFIX = "camera.";

    /** A metadata property. */
    static final String SCREEN_PREFIX = "screen.";

    /** A metadata property. */
    static final String VIDEO_PREFIX = "video.";

    /** A metadata property. */
    static final String FINAL_PREFIX = "final.";

    /** A metadata property. */
    static final String NOTES_PREFIX = "notes.";

    /** A color for labels. */
    static final Color RED = Color.color(1.0, 0.0, 0.0);

    /** A color for warnings. */
    static final Color ORANGE = Color.color(0.7, 0.2, 0.0);

    /** A color for labels. */
    static final Color BLACK = Color.color(0.0, 0.0, 0.0);

    /** A color for labels. */
    static final Color WHITE = Color.color(1.0, 1.0, 1.0);

    /** An ASCII digit. */
    static final int DIGIT_0 = '0';

    /** An ASCII digit. */
    static final int DIGIT_1 = '1';

    /** An ASCII digit. */
    static final int DIGIT_2 = '2';

    /** An ASCII digit. */
    static final int DIGIT_9 = '9';

    /** An ASCII letter. */
    static final int LETTER_A = 'A';

    /** An ASCII letter. */
    static final int LETTER_Z = 'Z';
}
