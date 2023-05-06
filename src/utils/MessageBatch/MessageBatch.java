package utils.MessageBatch;

import java.util.HashMap;
import java.util.Map;

import pt.unl.fct.di.novasys.network.data.Host;

public class MessageBatch {

    private Map<Integer, MessageCounter[]> messageBatch;

    public MessageBatch() {
        this.messageBatch = new HashMap<>();
    }


    public boolean containsMessage(int opsHash) {
        return this.messageBatch.containsKey(opsHash);
    }

    public void addMessage(int opsHash) {
        if (!this.messageBatch.containsKey(opsHash)) {
            this.messageBatch.put(opsHash, new MessageCounter[2]);
        }
        else {
            throw new RuntimeException("The opsHash should not be in the map");
        }
    }

    public int addPrepareMessage(int opsHash, Host host) {
        MessageCounter[] messageCounters = this.messageBatch.get(opsHash);
        if (messageCounters[0] == null) {
            messageCounters[0] = new MessageCounter();
        }
        messageCounters[0].incrementCounter(host);
        return messageCounters[0].getCounter();
    }

    public int addCommitMessage(int opsHash, Host host) {
        MessageCounter[] messageCounters = this.messageBatch.get(opsHash);
        if (messageCounters[1] == null) {
            messageCounters[1] = new MessageCounter();
        }
        messageCounters[1].incrementCounter(host);
        return messageCounters[1].getCounter();
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

    public void clearMessage(int opsHash) {
        this.messageBatch.remove(opsHash);
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

    public  MessageCounter[] getValues(int opsHash) {
        return this.messageBatch.get(opsHash);
    }

    public Map<Integer, MessageCounter[]> getMessageBatch() {
        return messageBatch;
    }

    @Override
    public String toString() {
        return "MessageBatch{" +
                "messageBatch=" + messageBatch +
                '}';
    }


}