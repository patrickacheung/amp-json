package patche.amp_json.reader;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import patche.amp_json.pojos.FileMetaData;

import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AmpJsonReaderTest {

    private static AmpJsonReader fixture;
    private static Random random;

    private FileMetaData validMetaData;
    private JsonObject metaDataJsonObject;

    @BeforeAll
    static void setup() {
        fixture = new AmpJsonReader();
        random = new Random();
    }

    @BeforeEach
    void setupValidFileMetaData() {
        final long expectedTimestamp = generateRandomPositiveLong();
        final long expectedProcessingTime = generateRandomPositiveLong();
        final UUID expectedSessionID = UUID.randomUUID();
        final UUID expectedUserID = UUID.randomUUID();
        final UUID expectedBusinessID = UUID.randomUUID();
        final String expectedSha = "random-sha";
        final String expectedFilename = "filename.ext";
        final String expectedPath = "a/nice/path/to/the/file";
        final int expectedDisposition = getRandomNumberInDispositionRange();
        validMetaData = new FileMetaData(
                expectedTimestamp,
                expectedProcessingTime,
                expectedSessionID,
                expectedUserID,
                expectedBusinessID,
                expectedSha,
                expectedFilename,
                expectedPath,
                FileMetaData.Disposition.fromSerializedLabel(expectedDisposition));

        metaDataJsonObject = fileMetaDataJsonObjectBuilder(
                expectedTimestamp,
                expectedProcessingTime,
                expectedSessionID,
                expectedUserID,
                expectedBusinessID,
                expectedSha,
                expectedFilename,
                expectedPath,
                expectedDisposition);
    }

    @Test
    void validLogLineTest() {
        assertTrue(fixture.isValidFileMetaData(metaDataJsonObject.toString()));
    }

    @Test
    void invalidLogLineTest() {
        JsonObject jsonObj = new JsonObject();
        jsonObj.addProperty("some", "prop");

        assertFalse(fixture.isValidFileMetaData(jsonObj.toString()));
    }

    @Test
    void invalidLogLineWithMissingKey() {
        metaDataJsonObject.remove("sha");
        assertFalse(fixture.isValidFileMetaData(metaDataJsonObject.toString()));
    }

    @Test
    void invalidLogLineWithExtraKey() {
        metaDataJsonObject.addProperty("extra", "prop");
        assertFalse(fixture.isValidFileMetaData(metaDataJsonObject.toString()));
    }

    @Test
    void invalidLogLineWithMissingValueOrEmptyValue() {
        metaDataJsonObject.remove("sha");
        metaDataJsonObject.addProperty("sha", "");
        assertFalse(fixture.isValidFileMetaData(metaDataJsonObject.toString()));

        metaDataJsonObject.remove("sha");
        metaDataJsonObject.addProperty("sha", (String) null);
        assertFalse(fixture.isValidFileMetaData(metaDataJsonObject.toString()));
    }

    @Test
    void invalidLogLineWithInvalidTimeStampValue() {
        metaDataJsonObject.remove("ts");
        metaDataJsonObject.addProperty("ts", "100000");
        assertFalse(fixture.isValidFileMetaData(metaDataJsonObject.toString()));
    }

    @Test
    void invalidLogLineWithInvalidSessionIDValue() {
        metaDataJsonObject.remove("si");
        metaDataJsonObject.addProperty("si", "invalid-uuid");
        assertFalse(fixture.isValidFileMetaData(metaDataJsonObject.toString()));

        metaDataJsonObject.remove("si");
        metaDataJsonObject.addProperty("si", 1000);
        assertFalse(fixture.isValidFileMetaData(metaDataJsonObject.toString()));
    }

    @Test
    void invalidLogLineWithInvalidFileNameValue() {
        metaDataJsonObject.remove("nm");
        metaDataJsonObject.addProperty("nm", "..");
        assertFalse(fixture.isValidFileMetaData(metaDataJsonObject.toString()));

        metaDataJsonObject.remove("nm");
        metaDataJsonObject.addProperty("nm", "");
        assertFalse(fixture.isValidFileMetaData(metaDataJsonObject.toString()));

        metaDataJsonObject.remove("nm");
        metaDataJsonObject.addProperty("nm", ".");
        assertFalse(fixture.isValidFileMetaData(metaDataJsonObject.toString()));
    }

    @Test
    void invalidLogLineWithInvalidDispositionValue() {
        metaDataJsonObject.remove("dp");
        metaDataJsonObject.addProperty("dp", "1");
        assertFalse(fixture.isValidFileMetaData(metaDataJsonObject.toString()));

        metaDataJsonObject.remove("dp");
        metaDataJsonObject.addProperty("dp", 0);
        assertFalse(fixture.isValidFileMetaData(metaDataJsonObject.toString()));
    }

    @Test
    void invalidLogLineMalformedJson() {
        String malformed = "{invalid}";
        assertFalse(fixture.isValidFileMetaData(malformed));

        String metaDataJsonString = metaDataJsonObject.toString();
        malformed = metaDataJsonString.substring(0, metaDataJsonString.length() - 1); // without closing brace
        assertFalse(fixture.isValidFileMetaData(malformed));

        malformed = "{invalid: invalid}";
        assertFalse(fixture.isValidFileMetaData(malformed));

        malformed = "{invalid:}";
        assertFalse(fixture.isValidFileMetaData(malformed));
    }

    @Test
    void validUUIDTest() {
        assertTrue(fixture.isValidUUID(UUID.randomUUID().toString()));
    }

    @Test
    void invalidUUIDTest() {
        assertFalse(fixture.isValidUUID("invalid-uuid"));
    }

    @Test
    void validValidPositiveSignedLong() {
        assertTrue(fixture.isValidPositiveLong("100"));
        assertTrue(fixture.isValidPositiveLong("0"));
        assertTrue(fixture.isValidPositiveLong("999999999"));
        assertTrue(fixture.isValidPositiveLong(String.valueOf(Long.MAX_VALUE)));
    }

    @Test
    void isInvalidPositiveSignedLong() {
        assertFalse(fixture.isValidPositiveLong("0.0"));
        assertFalse(fixture.isValidPositiveLong("-100.0"));
        assertFalse(fixture.isValidPositiveLong("100.0"));
        assertFalse(fixture.isValidPositiveLong("100.00001"));
        assertFalse(fixture.isValidPositiveLong("1e5"));
        assertFalse(fixture.isValidPositiveLong("-100"));
    }

    @Test
    void validFileNameTest() {
        assertTrue(fixture.isValidFileName("valid.filename"));
        assertEquals("valid", fixture.getFilenameWithoutExt("valid.filename"));
        assertEquals("filename", fixture.getFileExt("valid.filename"));

        assertTrue(fixture.isValidFileName("i.am.a.valid.file.pdf"));
        assertEquals("i.am.a.valid.file", fixture.getFilenameWithoutExt("i.am.a.valid.file.pdf"));
        assertEquals("pdf", fixture.getFileExt("i.am.a.valid.file.pdf"));

        assertTrue(fixture.isValidFileName("asdf"));
        assertEquals("asdf", fixture.getFilenameWithoutExt("asdf"));
        assertEquals("", fixture.getFileExt("asdf"));

        assertTrue(fixture.isValidFileName("asdf "));
        assertEquals("asdf ", fixture.getFilenameWithoutExt("asdf "));
        assertEquals("", fixture.getFileExt("asdf "));

        assertTrue(fixture.isValidFileName("asdf."));
        assertEquals("asdf", fixture.getFilenameWithoutExt("asdf"));
        assertEquals("", fixture.getFileExt("asdf."));

        assertTrue(fixture.isValidFileName("asdf. "));
        assertEquals("asdf", fixture.getFilenameWithoutExt("asdf. "));
        assertEquals(" ", fixture.getFileExt("asdf. "));

        assertTrue(fixture.isValidFileName("asdf. asdf"));
        assertEquals("asdf", fixture.getFilenameWithoutExt("asdf. asdf"));
        assertEquals(" asdf", fixture.getFileExt("asdf. asdf"));

        assertTrue(fixture.isValidFileName(".asdf"));
        assertEquals("", fixture.getFilenameWithoutExt(".asdf"));
        assertEquals("asdf", fixture.getFileExt(".asdf"));

        assertTrue(fixture.isValidFileName(" .ext"));
        assertEquals(" ", fixture.getFilenameWithoutExt(" .ext"));
        assertEquals("ext", fixture.getFileExt(" .ext"));

        assertTrue(fixture.isValidFileName(". "));
        assertEquals("", fixture.getFilenameWithoutExt(". "));
        assertEquals(" ", fixture.getFileExt(". "));

        assertTrue(fixture.isValidFileName(" . "));
        assertEquals(" ", fixture.getFilenameWithoutExt(" . "));
        assertEquals(" ", fixture.getFileExt(" . "));
    }

    @Test
    void invalidFileNameTest() {
        assertFalse(fixture.isValidFileName(""));
        assertFalse(fixture.isValidFileName("."));
        assertFalse(fixture.isValidFileName(".."));
    }

    @Test
    void validDispositionTest() {
        assertTrue(fixture.isValidDisposition("1"));
        assertTrue(fixture.isValidDisposition("2"));
        assertTrue(fixture.isValidDisposition("3"));
    }

    @Test
    void invalidDispositionTest() {
        assertFalse(fixture.isValidDisposition("0"));
    }

    @Test
    void fileMetaDataDeserializationTest() {
        Gson gson = new Gson();
        FileMetaData actualMetaData = gson.fromJson(metaDataJsonObject.toString(), FileMetaData.class);
        assertEquals(validMetaData, actualMetaData);
    }

    private JsonObject fileMetaDataJsonObjectBuilder(
            long expectedTimestamp,
            long expectedProcessingTime,
            UUID expectedSessionID,
            UUID expectedUserID,
            UUID expectedBusinessID,
            String expectedSha,
            String expectedFilename,
            String expectedPath,
            int expectedDisposition) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("ts", expectedTimestamp);
        jsonObject.addProperty("pt", expectedProcessingTime);
        jsonObject.addProperty("si", expectedSessionID.toString());
        jsonObject.addProperty("uu", expectedUserID.toString());
        jsonObject.addProperty("bg", expectedBusinessID.toString());
        jsonObject.addProperty("sha", expectedSha);
        jsonObject.addProperty("nm", expectedFilename);
        jsonObject.addProperty("ph", expectedPath);
        jsonObject.addProperty("dp", expectedDisposition);

        return jsonObject;
    }

    private static int getRandomNumberInDispositionRange() {
        return random.ints(1, (FileMetaData.Disposition.values().length + 1)).findFirst().getAsInt();
    }

    private static long generateRandomPositiveLong() {
        return random.longs(0, Long.MAX_VALUE).findFirst().getAsLong();
    }
}
