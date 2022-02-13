package patche.amp_json;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import com.google.common.annotations.VisibleForTesting;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import patche.amp_json.pojos.FileMetaData;
import patche.amp_json.reader.AmpJsonReader;

/**
 * Log line json processing app as per the documentation.
 */
public class JsonProcessingApp {

    private static final Logger LOG = LogManager.getLogger(JsonProcessingApp.class);

    private final AmpJsonReader jsonReader;
    private final Map<String, Set<String>> extensionToFilenames;

    public JsonProcessingApp(AmpJsonReader ampJsonReader) {
        jsonReader = ampJsonReader;
        extensionToFilenames = new HashMap<>();
    }

    /**
     * Processes the log file and prints as per the documentation.
     * @param filePath - the log file path
     */
    public void processLogFile(String filePath) {
        Path path = Paths.get(filePath);

        try (BufferedReader bufferedReader = Files.newBufferedReader(path)) {
            processLogFile(bufferedReader, extensionToFilenames);
            printExtensionsAndUniqueCount(extensionToFilenames);
        } catch (IOException e) {
            LOG.error("Unable to read file: " + filePath);
        }
    }

    @VisibleForTesting
    protected void processLogFile(BufferedReader reader, Map<String, Set<String>> extensionToFilenames) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (jsonReader.isValidFileMetaData(line)) {
                FileMetaData fileMetaData = jsonReader.fromJson(line);

                String ext = jsonReader.getFileExt(fileMetaData.getFileName());
                String filename = jsonReader.getFilenameWithoutExt(fileMetaData.getFileName());
                Set<String> uniqueFilenames = extensionToFilenames.getOrDefault(ext, new HashSet<>());
                uniqueFilenames.add(filename);

                extensionToFilenames.put(ext, uniqueFilenames);
            } else {
                LOG.error("skipping invalid log line (ideally send to a failure bin or text file or splunk etc)");
            }
        }
    }

    private void printExtensionsAndUniqueCount(Map<String, Set<String>> extAndFilenameCounts) {
        for (Map.Entry<String, Set<String>> entry : extAndFilenameCounts.entrySet()) {
            LOG.info("{}: {}", entry.getKey(), entry.getValue().size());
        }
    }

    public static void main( String[] args ) {
        File resourceDir = new File("src/test/resources");
        AmpJsonReader jsonReader = new AmpJsonReader();
        JsonProcessingApp app = new JsonProcessingApp(jsonReader);

        app.processLogFile(resourceDir + "/test.log");
    }
}
