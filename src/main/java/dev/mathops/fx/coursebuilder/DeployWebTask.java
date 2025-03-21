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
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * A task that recursively copies files needed for the course delivery website to a destination directory.  This
 * includes metadata files, VTT closed captions, and sentinel files for all media files that will exist on the media
 * server.
 */
class DeployWebTask extends Task<Integer> {

    /** The host services. */
    private final HostServices hostServices;

    /** The source directory. */
    private final File sourceDir;

    /** The target directory. */
    private final File targetDir;

    /**
     * Constructs a new {@code DeployWebTask}
     *
     * @param progressBar     the progress bar to update with status
     * @param statusLabel     a label to update with status information
     * @param theSourceDir    the source directory
     * @param theTargetDir    the target directory
     * @param theHostServices the host services
     */
    DeployWebTask(final ProgressBar progressBar, final Label statusLabel, final File theSourceDir,
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
        final long[] toCopy = new long[3];
        final List<File> dirsToCopy = new ArrayList<>(100);
        if (this.sourceDir.isDirectory()) {
            scanSource(this.sourceDir, toCopy, dirsToCopy);
        }

        Log.info("There are ", toCopy[0], " files spanning ", dirsToCopy.size(), " directories to copy and ", toCopy[1],
                " sentinel files to create with a total of ", toCopy[2], " bytes");

        updateMessage("Copying files...");
        updateProgress(2.0, 100.0);

        // Step 2: Do the copy
        final long[] finished = new long[2];
        if (this.sourceDir.isDirectory()) {
            copyFiles(this.sourceDir, this.targetDir, toCopy, finished, dirsToCopy);
            finished[0] = 0L;
            finished[1] = toCopy[1];
            createSentinels(this.sourceDir, this.targetDir, finished);
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
     * @param statistics a three-long array whose first entry will be set to the number of files to copy, whose second
     *                   entry will be set to the number of sentinel files to be created, and whose third entry will be
     *                   set to the total file size
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
                } else if (isWebContent(file)) {
                    ++statistics[0];
                    statistics[2] += file.length();
                    found = true;
                } else if (DeployMediaTask.isStudentFacing(file)) {
                    ++statistics[1];
                }
            }
            if (found) {
                dirsToCopy.add(dir);
            }
        }

        return found;
    }

    /**
     * Tests whether a filename represents a "web content" file.
     *
     * @param file the file
     * @return true if the file is "web content"
     */
    private static boolean isWebContent(final File file) {

        final String name = file.getName();

        return "final.vtt".equals(name) || "metadata.json".equals(name) || "thumb.png".equals(name)
               || "thumb.jpg".equals(name) || "thumb.jpeg".equals(name);
    }

    /**
     * Recursively copies all student-facing
     *
     * @param sourceDir  the source directory (a directory in {@code dirsToCopy})
     * @param targetDir  the target directory
     * @param toCopy     a three-long array whose first entry contains the number of files to copy, and whose third
     *                   entry contains the total file size to copy
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
                    } else if (isWebContent(sourceFile)) {
                        if (targetDir.exists() || targetDir.mkdirs()) {
                            final File targetFile = new File(targetDir, sourceFile.getName());
                            AppUtils.copyFile(sourceFile, targetFile);

                            ++finished[0];
                            finished[1] += sourceFile.length();

                            final double percentage = 2.0 + (double) finished[1] * 52.0 / toCopy[2];
                            updateProgress(Math.min(100.0, percentage), 100.0);
                        } else {
                            // TODO: Show error: failed to create directory
                        }
                    }
                }
            }
        }
    }

    /**
     * Recursively creates sentinel files in the target directory for all zero-byte student-facing media files in the
     * source directory.
     *
     * @param sourceDir the source directory
     * @param targetDir the target directory
     * @param finished  a two-long array whose first entry with the number of sentinel files created so far and whose
     *                  second entry is the total number of sentinel files that need to be created
     */
    private void createSentinels(final File sourceDir, final File targetDir, final long[] finished) {

        if (isCancelled()) {
            Log.info("Cancelled");
        } else {
            final File[] files = sourceDir.listFiles();

            if (files != null) {
                for (final File sourceFile : files) {
                    if (sourceFile.isDirectory()) {
                        final File targetFile = new File(targetDir, sourceFile.getName());
                        createSentinels(sourceFile, targetFile, finished);
                    } else if (DeployMediaTask.isStudentFacing(sourceFile)) {
                        if (targetDir.exists() || targetDir.mkdirs()) {
                            final File targetFile = new File(targetDir, sourceFile.getName());
                            try {
                                targetFile.createNewFile();
                                ++finished[0];

                                final double percentage = 52.0 + (double) finished[0] * 48.0 / finished[1];
                                updateProgress(Math.min(100.0, percentage), 100.0);
                            } catch (final IOException ex) {
                                Log.warning("Failed to create sentinel file: ", targetFile.getAbsolutePath());
                                // TODO: Show error: failed to create sentinel file
                                Log.warning(ex);
                            }
                        } else {
                            Log.warning("Failed to create directory for sentinel file: ", targetDir.getAbsolutePath());
                            // TODO: Show error: failed to create sentinel file
                        }
                    }
                }
            }
        }
    }
}
