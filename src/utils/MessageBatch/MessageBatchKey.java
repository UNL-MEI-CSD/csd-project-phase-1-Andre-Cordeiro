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
        return opsHash + seqN + viewNumber;
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
