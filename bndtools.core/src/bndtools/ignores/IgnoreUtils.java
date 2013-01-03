package bndtools.ignores;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import org.eclipse.core.resources.IProject;

import bndtools.Logger;
import bndtools.api.ILogger;

public class IgnoreUtils {
    private static final ILogger logger = Logger.getLogger();

    /**
     * Convert an ignore entry from Git format to the format required by the target version control system.
     * 
     * @param ignoreEntry
     *            The ignore entry
     * @return The ignore entry in the format required by the target version control system
     */
    private static String ignoreEntryConvert(String ignoreEntry) {
        // FIXME
        return ignoreEntry;
    }

    /**
     * @return the filename to use for an ignore file (similar to the result of a File.getName() invocation)
     */
    private static String getIgnoreFilename() {
        // FIXME use the version control system preference, or detect it
        return ".gitignore";
    }

    /**
     * Read an ignore file and put all lines into a (sorted) set.
     * 
     * @param ignoreFile
     *            The ignore file
     * @return A (non-null) list of lines from the ignore file. Empty when the file does not exists or is not a regular
     *         file. Empty or partially filled when the file could not be fully read (for example due to the file not
     *         being an regular file or due to an IOException).
     */
    private static List<String> readIgnoreFile(File ignoreFile) {
        List<String> result = new LinkedList<String>();

        if (ignoreFile.exists() && ignoreFile.isFile()) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(ignoreFile), "UTF-8"));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.add(line);
                }
            } catch (Exception e) {
                /* swallow FileNotFoundException, UnsupportedEncodingException and IOException*/
                logger.logError("Error reading ignore file " + ignoreFile.getPath(), e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        /* swallow */
                    }
                }
            }
        }

        return result;
    }

    /**
     * Create an ignore file in a project. If the ignore file already exists then the new ignore entries will be merged
     * into existing file.
     * 
     * @param project
     *            The project in which to create the ignore file.
     * @param path
     *            The (directory) path within the project on which to create the ignore file. Can be null, which is the
     *            same as "" and "/".
     * @param newIgnores
     *            A list of ignore entries to write to the ignore file, in Git ignore format. Can be null, which is the
     *            same as an empty set.
     */
    public static void createIgnoreFile(IProject project, String path, List<String> newIgnores) {
        if (project == null) {
            logger.logError("Can't create an ignore file for a null project", new IllegalArgumentException());
            return;
        }

        String projectPath = (path == null) ? "/" : path.replaceAll("/+\\s*$", "") + "/";
        File ignoreFile = new File(project.getFile(projectPath + getIgnoreFilename()).getLocationURI());

        if (newIgnores == null) {
            newIgnores = new LinkedList<String>();
        }
        Collections.sort(newIgnores);

        if (ignoreFile.exists()) {
            List<String> currentIgnores = readIgnoreFile(ignoreFile);
            if (currentIgnores.containsAll(newIgnores)) {
                return;
            }

            newIgnores.addAll(currentIgnores);
        }

        BufferedWriter writer = null;
        try {
            /* will create or truncated ignoreFile */
            writer = new BufferedWriter(new PrintWriter(ignoreFile.getPath(), "UTF-8"));
            for (String newIgnore : newIgnores) {
                writer.write(ignoreEntryConvert(newIgnore));
                writer.newLine();
            }
        } catch (Exception e) {
            logger.logError("Failed to write " + ignoreFile.getPath(), e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    /* swallow */
                }
            }
        }
    }

    /**
     * Create an ignore file in a project.
     * 
     * @param project
     *            The project in which to create the ignore file.
     * @param path
     *            The (directory) path within the project on which to create the ignore file. Can be null, which is the
     *            same as "" and "/".
     * @param newIgnores
     *            A comma-separated list of ignore entries to write to the ignore file, in Git ignore format. Can be
     *            null, which is the same as ""
     */
    public static void createIgnoreFile(IProject project, String path, String newIgnores) {
        if (project == null) {
            logger.logError("Can't create an ignore file for a null project", new IllegalArgumentException());
            return;
        }

        List<String> ignoredEntries = null;
        if ((newIgnores != null) && !newIgnores.isEmpty()) {
            ignoredEntries = new LinkedList<String>();
            StringTokenizer tokenizer = new StringTokenizer(newIgnores, ",");
            while (tokenizer.hasMoreTokens()) {
                ignoredEntries.add(tokenizer.nextToken());
            }
        }

        createIgnoreFile(project, path, ignoredEntries);
    }
}
