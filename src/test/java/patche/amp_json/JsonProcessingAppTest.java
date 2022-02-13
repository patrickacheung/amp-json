package patche.amp_json;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import patche.amp_json.pojos.FileMetaData;
import patche.amp_json.reader.AmpJsonReader;

import java.io.BufferedReader;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JsonProcessingAppTest {

    private JsonProcessingApp fixture;
    private Map<String, Set<String>> extToFilenameCount;
    @Mock
    private AmpJsonReader jsonReader;
    @Mock
    private BufferedReader bufferedReader;
    @Mock
    private FileMetaData fileMetaData;

    @Captor
    ArgumentCaptor<Map<String, Set<String>>> mapCaptor;

    @BeforeEach
    void setup() {
        fixture = new JsonProcessingApp(jsonReader);
        extToFilenameCount = new HashMap<>();
    }

    @Test
    void uniqueExtensionsAndCountsTest() throws Exception {
        String[] exts = new String[] {"ext1", "ext4", "ext4", "ext6", "ext1", "ext1", "ext3"};
        String[] fns = new String[] {"f1", "f", "f2", "f2", "f2", "f3", "f1"};
        String[] files = new String[] {"f1.ext1", "f.ext4", "f2.ext4", "f2.ext6", "f2.ext1", "f3.ext1", "f1.ext3"};

        when(bufferedReader.readLine()).thenReturn("", "", "", "", "", "", "").thenReturn(null);
        when(jsonReader.isValidFileMetaData(anyString())).thenReturn(true);
        when(jsonReader.fromJson(anyString())).thenReturn(fileMetaData);
        when(fileMetaData.getFileName()).thenReturn(
                files[0], files[0],
                files[1], files[1],
                files[2], files[2],
                files[3], files[3],
                files[4], files[4],
                files[5], files[5],
                files[6], files[6]);

        when(jsonReader.getFileExt(files[0])).thenReturn(exts[0]);
        when(jsonReader.getFilenameWithoutExt(files[0])).thenReturn(fns[0]);

        when(jsonReader.getFileExt(files[1])).thenReturn(exts[1]);
        when(jsonReader.getFilenameWithoutExt(files[1])).thenReturn(fns[1]);

        when(jsonReader.getFileExt(files[2])).thenReturn(exts[2]);
        when(jsonReader.getFilenameWithoutExt(files[2])).thenReturn(fns[2]);

        when(jsonReader.getFileExt(files[3])).thenReturn(exts[3]);
        when(jsonReader.getFilenameWithoutExt(files[3])).thenReturn(fns[3]);

        when(jsonReader.getFileExt(files[4])).thenReturn(exts[4]);
        when(jsonReader.getFilenameWithoutExt(files[4])).thenReturn(fns[4]);

        when(jsonReader.getFileExt(files[5])).thenReturn(exts[5]);
        when(jsonReader.getFilenameWithoutExt(files[5])).thenReturn(fns[5]);

        when(jsonReader.getFileExt(files[6])).thenReturn(exts[6]);
        when(jsonReader.getFilenameWithoutExt(files[6])).thenReturn(fns[6]);

        fixture.processLogFile(bufferedReader, extToFilenameCount);

        verify(fileMetaData, times(14)).getFileName();
        verify(jsonReader, times(7)).isValidFileMetaData(anyString());
        assertEquals(4, extToFilenameCount.size());
        assertEquals(2, extToFilenameCount.get("ext4").size());
        assertEquals(3, extToFilenameCount.get("ext1").size());
        assertEquals(1, extToFilenameCount.get("ext3").size());
        assertEquals(1, extToFilenameCount.get("ext6").size());
    }

    @Test
    void allUniqueExtensionsAndCountsTest() throws Exception {
        String[] exts = new String[] {"ext1", "ext2", "ext3"};
        String[] fns = new String[] {"f1", "f2", "f3"};
        String[] files = new String[] {"f1.ext1", "f2.ext2", "f3.ext3"};

        when(bufferedReader.readLine()).thenReturn("", "", "").thenReturn(null);
        when(jsonReader.isValidFileMetaData(anyString())).thenReturn(true);
        when(jsonReader.fromJson(anyString())).thenReturn(fileMetaData);
        when(fileMetaData.getFileName()).thenReturn(files[0], files[0], files[1], files[1], files[2], files[2]);

        when(jsonReader.getFileExt(files[0])).thenReturn(exts[0]);
        when(jsonReader.getFilenameWithoutExt(files[0])).thenReturn(fns[0]);

        when(jsonReader.getFileExt(files[1])).thenReturn(exts[1]);
        when(jsonReader.getFilenameWithoutExt(files[1])).thenReturn(fns[1]);

        when(jsonReader.getFileExt(files[2])).thenReturn(exts[2]);
        when(jsonReader.getFilenameWithoutExt(files[2])).thenReturn(fns[2]);

        fixture.processLogFile(bufferedReader, extToFilenameCount);

        verify(fileMetaData, times(6)).getFileName();
        verify(jsonReader, times(3)).isValidFileMetaData(anyString());
        assertEquals(3, extToFilenameCount.size());
        for (int i = 0; i < 3; ++i) {
            assertEquals(1, extToFilenameCount.get(exts[i]).size());
        }
    }

    @Test
    void uniqueExtensionsAndCountsWithSameEntriesTest() throws Exception {
        String ext = "pdf";
        String filename = "file";
        String file = "file.pdf";

        when(bufferedReader.readLine()).thenReturn("", "", "", "").thenReturn(null);
        when(jsonReader.isValidFileMetaData(anyString())).thenReturn(true);
        when(jsonReader.fromJson(anyString())).thenReturn(fileMetaData);
        when(fileMetaData.getFileName()).thenReturn(file);
        when(jsonReader.getFileExt(file)).thenReturn(ext);
        when(jsonReader.getFilenameWithoutExt(file)).thenReturn(filename);

        fixture.processLogFile(bufferedReader, extToFilenameCount);

        verify(fileMetaData, times(8)).getFileName();
        verify(jsonReader, times(4)).isValidFileMetaData(anyString());
        assertEquals(1, extToFilenameCount.size());
        assertEquals(1, extToFilenameCount.get(ext).size());
    }

    @Test
    void uniqueExtensionsAndCountWithSameExtensionDiffFilenames() throws Exception {
        String[] exts = new String[] {"ext", "ext", "ext"};
        String[] fns = new String[] {"f1", "f2", "f3"};
        String[] files = new String[] {"f1.ext", "f2.ext", "f3.ext"};

        when(bufferedReader.readLine()).thenReturn("", "", "").thenReturn(null);
        when(jsonReader.isValidFileMetaData(anyString())).thenReturn(true);
        when(jsonReader.fromJson(anyString())).thenReturn(fileMetaData);
        when(fileMetaData.getFileName()).thenReturn(files[0], files[0], files[1], files[1], files[2], files[2]);

        when(jsonReader.getFileExt(files[0])).thenReturn(exts[0]);
        when(jsonReader.getFilenameWithoutExt(files[0])).thenReturn(fns[0]);

        when(jsonReader.getFileExt(files[1])).thenReturn(exts[1]);
        when(jsonReader.getFilenameWithoutExt(files[1])).thenReturn(fns[1]);

        when(jsonReader.getFileExt(files[2])).thenReturn(exts[2]);
        when(jsonReader.getFilenameWithoutExt(files[2])).thenReturn(fns[2]);

        fixture.processLogFile(bufferedReader, extToFilenameCount);

        verify(fileMetaData, times(6)).getFileName();
        verify(jsonReader, times(3)).isValidFileMetaData(anyString());
        assertEquals(1, extToFilenameCount.size());
        assertEquals(3, extToFilenameCount.get("ext").size());
    }

    @Test
    void uniqueExtensionsAndCountsWithDiffExtensionsSameFilenames() throws Exception {
        String[] exts = new String[] {"ext1", "ext2", "ext3"};
        String[] fns = new String[] {"f", "f", "f"};
        String[] files = new String[] {"f.ext1", "f.ext2", "f.ext3"};

        when(bufferedReader.readLine()).thenReturn("", "", "").thenReturn(null);
        when(jsonReader.isValidFileMetaData(anyString())).thenReturn(true);
        when(jsonReader.fromJson(anyString())).thenReturn(fileMetaData);
        when(fileMetaData.getFileName()).thenReturn(files[0], files[0], files[1], files[1], files[2], files[2]);

        when(jsonReader.getFileExt(files[0])).thenReturn(exts[0]);
        when(jsonReader.getFilenameWithoutExt(files[0])).thenReturn(fns[0]);

        when(jsonReader.getFileExt(files[1])).thenReturn(exts[1]);
        when(jsonReader.getFilenameWithoutExt(files[1])).thenReturn(fns[1]);

        when(jsonReader.getFileExt(files[2])).thenReturn(exts[2]);
        when(jsonReader.getFilenameWithoutExt(files[2])).thenReturn(fns[2]);

        fixture.processLogFile(bufferedReader, extToFilenameCount);

        verify(fileMetaData, times(6)).getFileName();
        verify(jsonReader, times(3)).isValidFileMetaData(anyString());
        assertEquals(3, extToFilenameCount.size());

        for (int i = 0; i < 3; ++i) {
            assertEquals(1, extToFilenameCount.get(exts[i]).size());
        }
    }

    @Test
    void endToEndTest() throws Exception {
        fixture = new JsonProcessingApp(new AmpJsonReader());
        fixture = spy(fixture);

        File resourceDirectory = new File("src/test/resources");
        fixture.processLogFile(resourceDirectory + "/test.log");

        verify(fixture).processLogFile(any(BufferedReader.class) , mapCaptor.capture());

        Map<String, Set<String>> actualMap = mapCaptor.getValue();
        assertEquals(2, actualMap.size());
        assertEquals(1, actualMap.get("ext").size());
    }
}
