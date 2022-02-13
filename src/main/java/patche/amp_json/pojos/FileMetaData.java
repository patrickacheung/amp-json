package patche.amp_json.pojos;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

/**
 * Represents a file seen by the user and its metadata
 */
public class FileMetaData implements IFileMetaData {

    @SerializedName("ts")
    private final long timestamp;
    @SerializedName("pt")
    private final long processingTime;
    @SerializedName("si")
    private final UUID sessionID;
    @SerializedName("uu")
    private final UUID userID;
    @SerializedName("bg")
    private final UUID businessID;
    @SerializedName("sha")
    private final String sha;
    @SerializedName("nm")
    private final String fileName;
    @SerializedName("ph")
    private final String filePath;
    @SerializedName("dp")
    private final Disposition disposition;

    public FileMetaData(
            long timestamp,
            long processingTime,
            UUID sessionID,
            UUID userID,
            UUID businessID,
            String sha,
            String fileName,
            String filePath,
            Disposition disposition) {
        this.timestamp = timestamp;
        this.processingTime = processingTime;
        this.sessionID = sessionID;
        this.userID = userID;
        this.businessID = businessID;
        this.sha = sha;
        this.fileName = fileName;
        this.filePath = filePath;
        this.disposition = disposition;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getProcessingTime() {
        return processingTime;
    }

    public UUID getSessionID() {
        return sessionID;
    }

    public UUID getUserID() {
        return userID;
    }

    public UUID getBusinessID() {
        return businessID;
    }

    public String getSha() {
        return sha;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public Disposition getDisposition() {
        return disposition;
    }

    @Override
    public String toString() {
        return "FileMetaData{" +
                "timestamp=" + timestamp +
                ", processingTime=" + processingTime +
                ", sessionID=" + sessionID +
                ", userID=" + userID +
                ", businessID=" + businessID +
                ", sha='" + sha + '\'' +
                ", fileName='" + fileName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", disposition=" + disposition +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileMetaData that = (FileMetaData) o;

        if (timestamp != that.timestamp) return false;
        if (processingTime != that.processingTime) return false;
        if (!sessionID.equals(that.sessionID)) return false;
        if (!userID.equals(that.userID)) return false;
        if (!businessID.equals(that.businessID)) return false;
        if (!sha.equals(that.sha)) return false;
        if (!fileName.equals(that.fileName)) return false;
        if (!filePath.equals(that.filePath)) return false;
        return disposition.equals(that.disposition);
    }

    @Override
    public int hashCode() {
        int result = (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (int) (processingTime ^ (processingTime >>> 32));
        result = 31 * result + sessionID.hashCode();
        result = 31 * result + userID.hashCode();
        result = 31 * result + businessID.hashCode();
        result = 31 * result + sha.hashCode();
        result = 31 * result + fileName.hashCode();
        result = 31 * result + filePath.hashCode();
        result = 31 * result + disposition.hashCode();
        return result;
    }
}
