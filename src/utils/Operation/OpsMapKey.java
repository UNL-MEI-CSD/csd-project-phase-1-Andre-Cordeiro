package utils.Operation;

import java.sql.Timestamp;

public class OpsMapKey {
    
    private final Timestamp timestamp;
    private final int blockHash;

    public OpsMapKey(Timestamp timestamp, int opHash) {
        this.timestamp = timestamp;
        this.blockHash = opHash;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public int getBlockHash() {
        return blockHash;
    }

    @Override
    public int hashCode() {
        return timestamp.hashCode() + blockHash;
    }
}
