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
    private final Map<Long, Map<String, Integer>> hourlyShas;

    public JsonProcessingApp(AmpJsonReader ampJsonReader) {
        jsonReader = ampJsonReader;
        extensionToFilenames = new HashMap<>();
        hourlyShas = new HashMap<>();
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

    public void getUniqueSha(String filePath) {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (jsonReader.isValidFileMetaData(line)) {
                    FileMetaData metaData = jsonReader.fromJson(line);
                    Date date = new Date(metaData.getTimestamp() * 1000); // epoch seconds
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);

                    String sha = metaData.getSha();
                    long hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                    //LOG.info(hourOfDay);
                    Map<String, Integer> hourToSha = hourlyShas.getOrDefault(hourOfDay, new HashMap<>());
                    hourToSha.put(sha, hourToSha.getOrDefault(sha, 0) + 1);

                    hourlyShas.put(hourOfDay, hourToSha);
                }
            }

            for (Map.Entry<Long, Map<String, Integer>> entry : hourlyShas.entrySet()) {
                LOG.info(entry.getKey() + " o'clock:");
                for (Map.Entry<String, Integer> e1 : entry.getValue().entrySet()) {
                    LOG.info("sha: " + e1.getKey() + ", count: " + e1.getValue());
                }
            }
        } catch (IOException e) {
            LOG.error("failed to read file: " + filePath);
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

        app.processLogFile(resourceDir + "/test.example");

        app.getUniqueSha(resourceDir + "/inputData_std.jsonl");
    }
}
