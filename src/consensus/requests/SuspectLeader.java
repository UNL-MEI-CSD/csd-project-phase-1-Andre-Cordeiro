package consensus.requests;

import java.util.Date;
import java.sql.Timestamp;
import java.util.UUID;

import pt.unl.fct.di.novasys.babel.generic.ProtoRequest;

public class SuspectLeader extends ProtoRequest {

	public final static short REQUEST_ID = 122;
	
	//Represents the client transaction ID that was not ordered by the leader
	//and justifies this suspicion (while will lead to a view change)
	//Note: Could potentially be a Set of pending requests identifiers)
	private final UUID pendingRequestID;
	
	private final byte[] signature;
	private final Timestamp timestamp;
	
	public SuspectLeader(UUID pendingRequestID, byte[] signature) {
		super(SuspectLeader.REQUEST_ID);
		this.pendingRequestID = pendingRequestID;
		this.signature = signature;
		this.timestamp = new Timestamp(new Date().getTime());
	}

	public UUID getPendingRequestID() {
		return pendingRequestID;
	}

	public byte[] getSignature() {
		return signature;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public int hashCode() {
		return pendingRequestID.hashCode();
	}

}
