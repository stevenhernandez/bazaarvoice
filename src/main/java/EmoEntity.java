import org.joda.time.DateTime;
import org.vvcephei.occ_map.Versioned;

import java.util.Map;

/**
 * Created by Steven on 1/16/2017.
 */
public abstract class EmoEntity implements Versioned {

    private String id;
    private String table;
    private long version;
    private String signature;
    private boolean deleted;
    private DateTime firstUpdateAt;
    private DateTime lastUpdateAt;
    private DateTime lastMutateAt;

    public DateTime getLastMutateAt() {
        return lastMutateAt;
    }

    public void setLastMutateAt(DateTime lastMutateAt) {
        this.lastMutateAt = lastMutateAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public DateTime getFirstUpdateAt() {
        return firstUpdateAt;
    }

    public void setFirstUpdateAt(DateTime firstUpdateAt) {
        this.firstUpdateAt = firstUpdateAt;
    }

    public DateTime getLastUpdateAt() {
        return lastUpdateAt;
    }

    public void setLastUpdateAt(DateTime lastUpdateAt) {
        this.lastUpdateAt = lastUpdateAt;
    }

    @Override
    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public EmoEntity mapEmoEntity(Map<String, Object> from, EmoEntity to) {
        to.setDeleted(Boolean.parseBoolean(from.get("~deleted").toString()));
        to.setId(from.get("~id").toString());
        to.setTable(from.get("~table").toString());
        to.setVersion(Long.parseLong(from.get("~version").toString()));
        to.setSignature(from.get("~signature").toString());
        to.setFirstUpdateAt(DateTime.parse(from.get("~firstUpdateAt").toString()));
        to.setLastUpdateAt(DateTime.parse(from.get("~lastUpdateAt").toString()));
        to.setLastMutateAt(DateTime.parse(from.get("~lastMutateAt").toString()));

        return to;
    }
}
