package utils.MessageBatch;

import java.util.LinkedList;
import java.util.List;

import pt.unl.fct.di.novasys.network.data.Host;

public class MessageCounter {
    
    private List<Host> hosts;

    public MessageCounter() {
        this.hosts = new LinkedList<>();
    }


    public int getCounter() {
        return this.hosts.size();
    }

    public void incrementCounter(Host host) {
        if (!this.hosts.contains(host)) {
            this.hosts.add(host);
        }
    }

    @Override
    public String toString() {
        return "MessageCounter{" +
                ", hosts=" + hosts +
                '}';
    }
}
