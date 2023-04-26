package utils.MessageBatch;

import utils.SeqN;

public class MessageBatchKey {
    
    private final int opsHash;
    private SeqN seqN;
    private int viewNumber;

    public MessageBatchKey(int opsHash, SeqN seqN, int viewNumber) {
        this.opsHash = opsHash;
        this.seqN = seqN;
        this.viewNumber = viewNumber;
    }

    public int getOpsHash() {
        return opsHash;
    }

    public SeqN getSeqN() {
        return seqN;
    }

    public int getViewNumber() {
        return viewNumber;
    }

    public int hashCode() {
        return opsHash + seqN.hashCode() + viewNumber;
    }
}
