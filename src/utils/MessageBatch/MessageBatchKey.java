package utils.MessageBatch;

public class MessageBatchKey {
    
    private final int opsMapHash;
    private int seqN;
    private int viewNumber;

    public MessageBatchKey(int opsHash, int seqN, int viewNumber) {
        this.opsMapHash = opsHash;
        this.seqN = seqN;
        this.viewNumber = viewNumber;
    }

    public int getOpsMapHash() {
        return opsMapHash;
    }

    public int getSeqN() {
        return seqN;
    }

    public int getViewNumber() {
        return viewNumber;
    }

    public int hashCode() {

        int hash = 1;
        hash = hash * 17 + opsMapHash;
        hash = hash * 31 + seqN;
        hash = hash * 13 + viewNumber;
        return hash;
        
    }

    @Override
    public String toString() {
        return "MessageBatchKey{" +
                "opsHash=" + opsMapHash +
                ", seqN=" + seqN +
                ", viewNumber=" + viewNumber +
                '}';
    }
}
