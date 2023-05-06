package blockchain.timers;

import java.util.UUID;

import pt.unl.fct.di.novasys.babel.generic.ProtoTimer;

public class CheckUnhandledRequestsPeriodicTimer extends ProtoTimer {
	
	public final static short TIMER_ID = 201;

	private final UUID pendingRequestID;


	public CheckUnhandledRequestsPeriodicTimer(UUID pendingRequestID) {
		super(CheckUnhandledRequestsPeriodicTimer.TIMER_ID);
		this.pendingRequestID = pendingRequestID;
	}

	@Override
	public ProtoTimer clone() {
		return this;
	}

	public UUID getPendingRequestID() {
		return pendingRequestID;
	}
}
