package consensus.notifications;

import pt.unl.fct.di.novasys.babel.generic.ProtoNotification;
import pt.unl.fct.di.novasys.network.data.Host;

public class InitialNotification extends ProtoNotification{
    
    public final static short NOTIFICATION_ID = 103;
	
	private final Host self;
    private final int peerChannel;
	
	public InitialNotification(Host self, int peerChannel ) {
		super(InitialNotification.NOTIFICATION_ID);
		this.self = self;
        this.peerChannel = peerChannel;
	}

    public Host getSelf() {
        return self;
    }

    public int getPeerChannel() {
        return peerChannel;
    }

	
}
