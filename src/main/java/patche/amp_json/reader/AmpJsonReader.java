package patche.amp_json.reader;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import patche.amp_json.pojos.FileMetaData;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.google.gson.stream.JsonToken.END_DOCUMENT;
import static patche.amp_json.pojos.IFileMetaData.*;
import static patche.amp_json.pojos.IFileMetaData.VALID_KEYS;

public class AmpJsonReader {

    private static final Logger LOG = LogManager.getLogger(AmpJsonReader.class);
    private final Gson gson;

    public AmpJsonReader() {
        this.gson = new Gson();
    }

    /**
     * Validates if the log line contains a valid json entry
     * @param logLine - the possible valid json entry
     * @return true if the log line is a valid json entry, false otherwise.
     */
    public boolean isValidFileMetaData(String logLine) {
        Deque<Character> bracketStack = new ArrayDeque<>();
        Map<String, Integer> keyCounts = new HashMap<>();

        try (InputStream is = new ByteArrayInputStream(logLine.getBytes(StandardCharsets.UTF_8));
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader bufferedIsr = new BufferedReader(isr);
             JsonReader jsonReader = new JsonReader(bufferedIsr)) {

            String prevKey = "";
            JsonToken jsonToken;
            while ((jsonToken = jsonReader.peek()) != END_DOCUMENT) {
                switch (jsonToken) {
                    case BEGIN_OBJECT:
                        bracketStack.push('{');
                        jsonReader.beginObject();
                        break;
                    case END_OBJECT:
                        if (bracketStack.isEmpty() || bracketStack.peek() != '{') {
                            return false;
                        } else {
                            bracketStack.pop();
                            jsonReader.endObject();
                        }
                        break;
                    case NAME:
                        String key = jsonReader.nextName();
                        int count = keyCounts.getOrDefault(key, 0) + 1;
                        if (count > 1) {
                            return false;
                        }
                        keyCounts.put(key, count);
                        prevKey = key;
                        break;
                    case NUMBER:
                        String numString = jsonReader.nextString();
                        if (!NUMBER_KEYS.contains(prevKey)  ||
                                (NUMBER_KEYS.contains(prevKey) && !isValidWholeNumber(numString)) ||
                                (DISPOSITION_KEY.contains(prevKey) && !isValidDisposition(numString))) {
                            return false;
                        }
                        break;
                    case STRING:
                        String val = jsonReader.nextString();
                        if ((UUID_KEYS.contains(prevKey) &&!isValidUUID(val)) ||
                                (FILENAME_KEY.equals(prevKey) && !isValidFileName(val)) ||
                                (val.isEmpty() || val.isBlank()) || NUMBER_KEYS.contains(prevKey)) {
                            return false;
                        }
                        break;
                    case NULL:
                    case BOOLEAN:
                    case BEGIN_ARRAY:
                    case END_ARRAY:
                    default:
                        return false;
                }
            }
        } catch (IOException e) {
            LOG.warn(e);
            return false;
        }

        return bracketStack.isEmpty() && keyCounts.size() == VALID_KEYS.size();
    }

    /**
     * <pre>
     *     Returns the filename without the extension
     *     Assumes a valid filename is name.ext (name dot extension).
     *     All other forms are invalid.
     * </pre>
     * @param filename the filename
     * @return the filename without the extension
     */
    public String getFilenameWithoutExt(String filename) {
        if (!isValidFileName(filename)) {
            throw new IllegalArgumentException("invalid filename");
        }
        int dotIdx = filename.lastIndexOf(".");
        return filename.substring(0, dotIdx);
    }

    /**
     * <pre>
     *     Returns the file extension
     *     Assumes a valid filename is name.ext (name dot extension).
     *     All other forms are invalid.
     * </pre>
     * @param filename the filename
     * @return the file extension
     */
    public String getFileExt(String filename) {
        if (!isValidFileName(filename)) {
            throw new IllegalArgumentException("invalid filename");
        }
        int dotIdx = filename.lastIndexOf(".");
        return filename.substring(dotIdx + 1);
    }

    /**
     * <pre>
     *     Deserializes the json line into a {@link FileMetaData}
     * </pre>
     * @param line the json log line
     * @return a {@link FileMetaData}
     */
    public FileMetaData fromJson(String line) {
        return gson.fromJson(line, FileMetaData.class);
    }

    @VisibleForTesting
    protected boolean isValidWholeNumber(String num) {
        for (char c : num.toCharArray()) {
            if ('.' == c) {
                return false;
            }
        }
        return true;
    }

    @VisibleForTesting
    protected boolean isValidUUID(String uuid) {
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @VisibleForTesting
    protected boolean isValidDisposition(String disLabel) {
        return FileMetaData.Disposition.fromSerializedLabel(Integer.parseInt(disLabel)) != null;
    }

    /**
     * <pre>
     *     Determines if the filename provided is valid.
     *     Assumes a valid filename is name.ext (name dot extension).
     *     All other forms are invalid.
     * </pre>
     * @param filename the filename
     * @return true if the filename is valid, or false if invalid (as per description).
     */
    @VisibleForTesting
    protected boolean isValidFileName(String filename) {
        if (filename.isBlank() || filename.isEmpty()) {
            return false;
        }

        int idx = filename.lastIndexOf('.');
        if (idx == -1) {
            return false;
        }

        String[] parts = filename.split("\\.");
        return parts.length >= 2 && validFileNameParts(parts[0], true) && validFileNameParts(parts[1], false);
    }

    private boolean validFileNameParts(String part, boolean head) {
        if (part.isBlank() || part.isEmpty()) {
            return false;
        }

        char c;
        if (head) {
            c = part.charAt(part.length() - 1);
        } else {
            c = part.charAt(0);
        }

        return c != ' ';
    }
}
