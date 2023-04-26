package utils.MessageBatch;

import java.util.HashMap;
import java.util.Map;

public class MessageBatch {

    private Map<Integer, int[]> messageBatch;

    public MessageBatch() {
        this.messageBatch = new HashMap<>();
    }


    public boolean containsMessage(int opsHash) {
        return this.messageBatch.containsKey(opsHash);
    }

    public void addMessage(int opsHash) {
        if (!this.messageBatch.containsKey(opsHash)) {
            this.messageBatch.put(opsHash, new int[3]);
        }
        else {
            throw new RuntimeException("The opsHash should not be in the map");
        }
    }

    public int addPrepareMessage(int opsHash) {
        int[] messageCounters = this.messageBatch.get(opsHash);
        messageCounters[1]++;
        return messageCounters[1];
    }

    public int addCommitMessage(int opsHash) {
        int[] messageCounters = this.messageBatch.get(opsHash);
        messageCounters[2]++;
        return messageCounters[2];
    }

    public void removePrePrepareMessage(int opsHash) {
        this.messageBatch.remove(opsHash);
    }

    public void clear() {
        this.messageBatch.clear();
    }

    public int size() {
        return this.messageBatch.size();
    }

    public int[] getKeys() {
        int[] keys = new int[this.messageBatch.size()];
        int i = 0;
        for (int key : this.messageBatch.keySet()) {
            keys[i] = key;
            i++;
        }
        return keys;
    }

    public  int[] getValues(int opsHash) {
        return this.messageBatch.get(opsHash);
    }

    public Map<Integer, int[]> getMessageBatch() {
        return messageBatch;
    }

    @Override
    public String toString() {
        return "MessageBatch{" +
                "messageBatch=" + messageBatch +
                '}';
    }


}