package blockchain.timers;

import java.util.UUID;

import pt.unl.fct.di.novasys.babel.generic.ProtoTimer;

public class LeaderSuspectTimer extends ProtoTimer {
	
	public final static short TIMER_ID = 202;

	private final UUID requestID;
	private final int viewNumber;
	
	public LeaderSuspectTimer(UUID requestID, int viewNumber) {

		super(LeaderSuspectTimer.TIMER_ID);
		this.requestID = requestID;
		this.viewNumber = viewNumber;

	}
	
	@Override
	public ProtoTimer clone() {
		return this;
	}

	public UUID getRequestID() {
		return requestID;
	}

	public int getViewNumber() {
		return viewNumber;
	}

	@Override
	public String toString() {
		return "LeaderSuspectTimer{" +
				"requestID=" + requestID +
				", viewNumber=" + viewNumber +
				'}';
	}

}
