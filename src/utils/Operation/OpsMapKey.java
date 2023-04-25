package utils.Operation;

import java.sql.Timestamp;

public class OpsMapKey {
    
    private final Timestamp timestamp;
    private final Integer opHash;

    public OpsMapKey(Timestamp timestamp, Integer opHash) {
        this.timestamp = timestamp;
        this.opHash = opHash;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public Integer getOpHash() {
        return opHash;
    }

    @Override
    public int hashCode() {
        return timestamp.hashCode() + opHash.hashCode();
    }
}
