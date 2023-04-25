package utils.MessageBatch;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MessageBatch {

    private Map<UUID, MessageCounter[]> messageBatch;

    public MessageBatch() {
        this.messageBatch = new HashMap<>();
    }

    

    public MessageCounter[] getPrePrepareMessage(UUID id) {
        return messageBatch.get(id);
    }

    public boolean containsPrePrepareMessage(UUID id) {
        return messageBatch.containsKey(id);
    }

    public void addPrePrepareMessage(UUID id) {
        if (!messageBatch.containsKey(id)) {
            messageBatch.put(id, new MessageCounter[3]);
        }
        else {
            MessageCounter[] messageCounters = messageBatch.get(id);
            messageCounters[0].incrementCounter();
        }
    }

    public void addPrepareMessage(UUID id) {
        if (messageBatch.containsKey(id)) {
            MessageCounter[] messageCounters = messageBatch.get(id);
            messageCounters[1].incrementCounter();
        }
        else {
            throw new RuntimeException("The id should be in the map");
        }
    }

    public void addCommitMessage(UUID id) {
        if (messageBatch.containsKey(id)) {
            MessageCounter[] messageCounters = messageBatch.get(id);
            messageCounters[2].incrementCounter();
        }
        else {
            throw new RuntimeException("The id should be in the map");
        }
    }

    public void removePrePrepareMessage(UUID id) {
        messageBatch.remove(id);
    }

    public void clear() {
        messageBatch.clear();
    }


}