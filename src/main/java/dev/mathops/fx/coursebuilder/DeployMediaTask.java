package dev.mathops.fx.coursebuilder;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import javafx.application.HostServices;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * A task that recursively copies student-facing media (video and PDF) files from a course media directory to a
 * destination directory that can then be copied to the video server.
 */
class DeployMediaTask extends Task<Integer> {

    /** The host services. */
    private final HostServices hostServices;

    /** The source directory. */
    private final File sourceDir;

    /** The target directory. */
    private final File targetDir;

    /**
     * Constructs a new {@code DeployMediaTask}
     *
     * @param progressBar     the progress bar to update with status
     * @param statusLabel     a label to update with status information
     * @param theSourceDir    the source directory
     * @param theTargetDir    the target directory
     * @param theHostServices the host services
     */
    DeployMediaTask(final ProgressBar progressBar, final Label statusLabel, final File theSourceDir,
                    final File theTargetDir, final HostServices theHostServices) {

        super();

        final String sourcePath = theSourceDir.getAbsolutePath();
        final String targetPath = theTargetDir.getAbsolutePath();
        if (targetPath.startsWith(sourcePath)) {
            throw new IllegalArgumentException("Target path may not be a subdirectory of source path");
        }

        final DoubleProperty barProgressProperty = progressBar.progressProperty();
        final ReadOnlyDoubleProperty myProgressProperty = this.progressProperty();
        barProgressProperty.bind(myProgressProperty);

        final StringProperty labelTextProperty = statusLabel.textProperty();
        final ReadOnlyStringProperty myMessageProperty = this.messageProperty();
        labelTextProperty.bind(myMessageProperty);

        this.sourceDir = theSourceDir;
        this.targetDir = theTargetDir;

        this.hostServices = theHostServices;
    }

    /**
     * Invokes the task.
     *
     * @return the tick count
     */
    @Override
    protected Integer call() {

        updateMessage("Scanning source directory...");
        updateProgress(0.0, 100.0);

        // Step 1: Count the number of student-facing files we will copy and collect their total size
        final long[] toCopy = new long[2];
        final List<File> dirsToCopy = new ArrayList<>(100);
        if (this.sourceDir.isDirectory()) {
            scanSource(this.sourceDir, toCopy, dirsToCopy);
        }

        Log.info("There are ", toCopy[0], " files spanning ", dirsToCopy.size(),
                " directories to copy, with a total of ", toCopy[1], " bytes");

        updateMessage("Copying files...");
        updateProgress(2.0, 100.0);

        // Step 2: Do the copy
        final long[] finished = new long[2];
        if (this.sourceDir.isDirectory()) {
            copyFiles(this.sourceDir, this.targetDir, toCopy, finished, dirsToCopy);
        }

        Log.info("Deploy is finished.");

        updateMessage(CoreConstants.EMPTY);
        updateProgress(0.0, 100.0);

        final URI uri = this.targetDir.toURI();
        final String uriString = uri.toString();
        this.hostServices.showDocument(uriString);

        return null;
    }

    /**
     * Recursively scans a directory to count the number of files that need to be copied and their total size.
     *
     * @param dir        the directory
     * @param statistics a two-long array whose first entry will be set to the number of files to copy, and whose second
     *                   entry will be set to the total file size
     * @param dirsToCopy a list to which to add all directories to be copied
     * @return true if any files were found to copy; false if not
     */
    private static boolean scanSource(final File dir, final long[] statistics, final List<? super File> dirsToCopy) {

        boolean found = false;

        final File[] files = dir.listFiles();
        if (files != null) {
            for (final File file : files) {
                if (file.isDirectory()) {
                    if (scanSource(file, statistics, dirsToCopy)) {
                        found = true;
                    }
                } else if (isStudentFacing(file)) {
                    ++statistics[0];
                    statistics[1] += file.length();
                    found = true;
                }
            }
            if (found) {
                dirsToCopy.add(dir);
            }
        }

        return found;
    }

    /**
     * Tests whether a filename represents a "student-facing" file.
     *
     * @param file the file
     * @return true if the file is "student-facing"
     */
    static boolean isStudentFacing(final File file) {

        final String name = file.getName();

        return "final.mp4".equals(name) || "final.txt".equals(name) || "example.pdf".equals(name)
               || "notes.pdf".equals(name)
               || (name.endsWith(".pdf") && (isContext(name) || isWorksheet(name)
                                             || isPractice(name) || isAnswers(name) || isSolutions(name)));
    }

    /**
     * Tests whether a name (known to end with ".pdf") is a "context_##.pdf" file.
     *
     * @param name the name
     * @return true if this is a context file
     */
    private static boolean isContext(final String name) {

        return name.length() == 14 && name.startsWith("context_")
               && AppUtils.isAsciiDigit(name.charAt(8)) && AppUtils.isAsciiDigit(name.charAt(9));
    }

    /**
     * Tests whether a name (known to end with ".pdf") is a "worksheet_##.pdf" file.
     *
     * @param name the name
     * @return true if this is a worksheet file
     */
    private static boolean isWorksheet(final String name) {

        return name.length() == 16 && name.startsWith("worksheet_")
               && AppUtils.isAsciiDigit(name.charAt(10)) && AppUtils.isAsciiDigit(name.charAt(11));
    }

    /**
     * Tests whether a name (known to end with ".pdf") is a "practice_##.pdf" file.
     *
     * @param name the name
     * @return true if this is a practice file
     */
    private static boolean isPractice(final String name) {

        return name.length() == 15 && name.startsWith("practice_")
               && AppUtils.isAsciiDigit(name.charAt(9)) && AppUtils.isAsciiDigit(name.charAt(10));
    }

    /**
     * Tests whether a name (known to end with ".pdf") is a "answers_##.pdf" file.
     *
     * @param name the name
     * @return true if this is a answers file
     */
    private static boolean isAnswers(final String name) {

        return name.length() == 14 && name.startsWith("answers_")
               && AppUtils.isAsciiDigit(name.charAt(8)) && AppUtils.isAsciiDigit(name.charAt(9));
    }

    /**
     * Tests whether a name (known to end with ".pdf") is a "solution_##.pdf" file.
     *
     * @param name the name
     * @return true if this is a solutions file
     */
    private static boolean isSolutions(final String name) {

        return name.length() == 15 && name.startsWith("solution_")
               && AppUtils.isAsciiDigit(name.charAt(9)) && AppUtils.isAsciiDigit(name.charAt(10));
    }

    /**
     * Recursively copies all student-facing
     *
     * @param sourceDir  the source directory (a directory in {@code dirsToCopy})
     * @param targetDir  the target directory
     * @param toCopy     a two-long array whose first entry contains the number of files to copy, and whose second entry
     *                   contains the total file size to copy
     * @param finished   a two-long array whose first entry with the number of files copied so far, and whose second
     *                   entry has total bytes copied so far, to be updated by this method as files are copied
     * @param dirsToCopy a list of directories to copy
     */
    private void copyFiles(final File sourceDir, final File targetDir, final long[] toCopy,
                           final long[] finished, final List<File> dirsToCopy) {

        if (isCancelled()) {
            Log.info("Cancelled");
        } else {
            final File[] files = sourceDir.listFiles();
            if (files != null) {
                for (final File sourceFile : files) {

                    if (sourceFile.isDirectory()) {
                        if (dirsToCopy.contains(sourceFile)) {
                            final File targetFile = new File(targetDir, sourceFile.getName());
                            copyFiles(sourceFile, targetFile, toCopy, finished, dirsToCopy);
                        }
                    } else if (isStudentFacing(sourceFile)) {
                        if (targetDir.exists() || targetDir.mkdirs()) {
                            final File targetFile = new File(targetDir, sourceFile.getName());
                            AppUtils.copyFile(sourceFile, targetFile);

                            ++finished[0];
                            finished[1] += sourceFile.length();

                            final double percentage = 2.0 + (double) finished[1] * 98.0 / toCopy[1];
                            updateProgress(Math.min(100.0, percentage), 100.0);
                        } else {
                            // TODO: Show error: failed to create directory
                        }
                    }
                }
            }
        }
    }
}
