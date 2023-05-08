package blockchain.blockchain.Operation;

import java.security.PublicKey;
import java.util.UUID;

import app.messages.client.requests.Cancel;

public class CancelOp {
    
    private UUID rID;
	private PublicKey cID;
	
	public CancelOp(Cancel cancel) {
		this.rID = cancel.getrID();
		this.cID = cancel.getcID();
	}

    public UUID getRID() {
        return rID;
    }

    public PublicKey getCID() {
        return cID;
    }

    public byte[] toByteArray() {
        return (rID.toString() + cID.toString()).getBytes();
    }

    @Override
    public String toString() {
        return "Cancel{" + "rID=" + rID + ", cID=" + cID + '}';
    }
}
