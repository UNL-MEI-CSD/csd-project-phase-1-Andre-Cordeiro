package utils.MessageBatch;

import pt.unl.fct.di.novasys.babel.generic.ProtoMessage;

public class MessageCounter {
    
    private ProtoMessage message;
    private int counter;

    public MessageCounter(ProtoMessage message) {
        this.message = message;
        this.counter = 0;
    }

    public ProtoMessage getMessage() {
        return message;
    }

    public int getCounter() {
        return counter;
    }

    public void incrementCounter() {
        this.counter++;
    }

}
