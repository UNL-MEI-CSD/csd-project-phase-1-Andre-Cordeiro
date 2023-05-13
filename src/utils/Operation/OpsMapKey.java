package utils.Operation;

import java.sql.Timestamp;

public class OpsMapKey {
    
    private final Timestamp timestamp;
    private final int opHash;

    public OpsMapKey(Timestamp timestamp, int opHash) {
        this.timestamp = timestamp;
        this.opHash = opHash;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public int getOpHash() {
        return opHash;
    }

    @Override
    public int hashCode() {
        return timestamp.hashCode() + opHash;
    }
}
