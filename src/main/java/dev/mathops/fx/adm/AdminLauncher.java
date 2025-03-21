package dev.mathops.fx.adm;

/**
 * Launcher because JavaFX can't run an application that extends "Application".
 *
 * <p>
 * Also, be sure to add these VM options:
 * <pre>
 *   --module-path "C:\opt\javafx-sdk-23.0.1\lib" --add-modules javafx.controls
 * </pre>
 */
public final class AdminLauncher {

    /**
     * Private constructor to prevent instantiation.
     */
    private AdminLauncher() {

        // No action
    }

    /**
     * Runs the main application.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        Admin.main(args);
    }
}
