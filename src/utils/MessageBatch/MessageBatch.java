package utils.MessageBatch;

import java.util.HashMap;
import java.util.Map;

public class MessageBatch {

    private Map<Integer, MessageCounter[]> messageBatch;

    public MessageBatch() {
        this.messageBatch = new HashMap<>();
    }

    

    public MessageCounter[] getPrePrepareMessage(int opsHash) {
        return this.messageBatch.get(opsHash);
    }

    public boolean containsMessage(int opsHash) {
        return this.messageBatch.containsKey(opsHash);
    }

    public void addPrePrepareMessage(int opsHash) {
        if (!this.messageBatch.containsKey(opsHash)) {
            this.messageBatch.put(opsHash, new MessageCounter[3]);
        }
        else {
            throw new RuntimeException("The opsHash should not be in the map");
        }
    }

    public int addPrepareMessage(int opsHash) {
        if (this.messageBatch.containsKey(opsHash)) {
            MessageCounter[] messageCounters = this.messageBatch.get(opsHash);
            messageCounters[1].incrementCounter();
            return messageCounters[1].getCounter();
        }
        else {
            throw new RuntimeException("The opsHash should be in the map");
        }
    }

    public int addCommitMessage(int opsHash) {
        if (this.messageBatch.containsKey(opsHash)) {
            MessageCounter[] messageCounters = this.messageBatch.get(opsHash);
            messageCounters[2].incrementCounter();
            return messageCounters[2].getCounter();
        }
        else {
            throw new RuntimeException("The opsHash should be in the map");
        }
    }

    public void removePrePrepareMessage(int opsHash) {
        this.messageBatch.remove(opsHash);
    }

    public void clear() {
        this.messageBatch.clear();
    }

    @Override
    public String toString() {
        return "MessageBatch{" +
                "messageBatch=" + this.messageBatch +
                '}';
    }


}