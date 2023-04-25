package utils.MessageBatch;

import java.util.HashMap;
import java.util.Map;

public class MessageBatch {

    private Map<Integer, MessageCounter[]> messageBatch;

    public MessageBatch() {
        this.messageBatch = new HashMap<>();
    }

    

    public MessageCounter[] getPrePrepareMessage(int opsHash) {
        return messageBatch.get(opsHash);
    }

    public boolean containsPrePrepareMessage(int opsHash) {
        return messageBatch.containsKey(opsHash);
    }

    public void addPrePrepareMessage(int opsHash) {
        if (!messageBatch.containsKey(opsHash)) {
            messageBatch.put(opsHash, new MessageCounter[3]);
        }
        else {
            throw new RuntimeException("The opsHash should not be in the map");
        }
    }

    public int addPrepareMessage(int opsHash) {
        if (messageBatch.containsKey(opsHash)) {
            MessageCounter[] messageCounters = messageBatch.get(opsHash);
            messageCounters[1].incrementCounter();
            return messageCounters[1].getCounter();
        }
        else {
            throw new RuntimeException("The opsHash should be in the map");
        }
    }

    public int addCommitMessage(int opsHash) {
        if (messageBatch.containsKey(opsHash)) {
            MessageCounter[] messageCounters = messageBatch.get(opsHash);
            messageCounters[2].incrementCounter();
            return messageCounters[2].getCounter();
        }
        else {
            throw new RuntimeException("The opsHash should be in the map");
        }
    }

    public void removePrePrepareMessage(int opsHash) {
        messageBatch.remove(opsHash);
    }

    public void clear() {
        messageBatch.clear();
    }


}