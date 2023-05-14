package consensus.requests;

import java.sql.Timestamp;
import java.util.Date;

import pt.unl.fct.di.novasys.babel.generic.ProtoRequest;

public class ProposeRequest extends ProtoRequest {

	public static final short REQUEST_ID = 121;
	
	private final byte[] block;
	private final byte[] signature;
	private final Timestamp timestamp;
	
	public ProposeRequest(byte[] block, byte[] signature) {

		super(ProposeRequest.REQUEST_ID);
		this.block = block;
		this.signature = signature;
		this.timestamp = new Timestamp(new Date().getTime());

	}

	public byte[] getBlock() {
		return block;
	}

	public byte[] getSignature() {
		return signature;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public int hashCode() {
		return block.hashCode();
	}
	
	@Override
	public String toString() {
		return "ProposeRequest{" +
				"block=" + block +
				", signature=" + signature +
				", timestamp=" + timestamp +
				'}';
	}

}
