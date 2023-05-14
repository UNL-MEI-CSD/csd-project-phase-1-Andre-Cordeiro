package blockchain.blockchain.Operation;

import java.security.PublicKey;
import java.util.UUID;

import app.messages.exchange.requests.Deposit;

public class DepositOp {
    
    private UUID rid;
	private PublicKey clientID;
	
    private float amount;

	public DepositOp(Deposit deposit) {
		this.rid = deposit.getRid();
		this.clientID = deposit.getClientID();
		this.amount = deposit.getAmount();
	}

    public UUID getRid() {
        return rid;
    }

    public PublicKey getClientID() {
        return clientID;
    }

    public float getAmount() {
        return amount;
    }

    public byte[] toByteArray() {
        return (rid.toString() + clientID.toString() + amount).getBytes();
    }

    @Override
    public String toString() {
        return "Deposit{" + 
        "rid=" + rid + 
        ", clientID=" + clientID + 
        ", amount=" + amount + 
        '}';
    }
}
