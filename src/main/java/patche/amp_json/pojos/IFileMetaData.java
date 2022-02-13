package patche.amp_json.pojos;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public interface IFileMetaData {

    Set<String> VALID_KEYS = new HashSet<>(Arrays.asList(
            "ts", "pt", "si", "uu", "bg", "sha", "nm", "ph", "dp"
    ));
    Set<String> UUID_KEYS = new HashSet<>(Arrays.asList(
            "si", "uu", "bg"
    ));
    Set<String> NUMBER_KEYS = new HashSet<>(Arrays.asList(
            "ts", "pt", "dp"
    ));
    String FILENAME_KEY = "nm";
    String DISPOSITION_KEY = "dp";

    enum Disposition {
        @SerializedName("1")
        MALICIOUS(1),
        @SerializedName("2")
        CLEAN(2),
        @SerializedName("3")
        UKNOWN(3);

        private final int serializedLabel;

        Disposition(int label) {
            this.serializedLabel = label;
        }

        public int getSerializedLabel() {
            return serializedLabel;
        }

        public static Disposition fromSerializedLabel(int label) {
            for (Disposition dis : Disposition.values()) {
                if (dis.getSerializedLabel() == label) {
                    return dis;
                }
            }
            return null;
        }
    }
}
