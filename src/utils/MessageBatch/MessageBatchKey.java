package utils.MessageBatch;

public class MessageBatchKey {
    
    private final int opsHash;
    private int seqN;
    private int viewNumber;

    public MessageBatchKey(int opsHash, int seqN, int viewNumber) {
        this.opsHash = opsHash;
        this.seqN = seqN;
        this.viewNumber = viewNumber;
    }

    public int getOpsHash() {
        return opsHash;
    }

    public int getSeqN() {
        return seqN;
    }

    public int getViewNumber() {
        return viewNumber;
    }

    public int hashCode() {
        //create a unique hashcode for each batch
        int hash = 1;
        hash = hash * 17 + opsHash;
        hash = hash * 31 + seqN;
        hash = hash * 13 + viewNumber;
        return hash;
    }

    @Override
    public String toString() {
        return "MessageBatchKey{" +
                "opsHash=" + opsHash +
                ", seqN=" + seqN +
                ", viewNumber=" + viewNumber +
                '}';
    }
}
