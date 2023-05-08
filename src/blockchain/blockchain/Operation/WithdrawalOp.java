package blockchain.blockchain.Operation;

import java.security.PublicKey;
import java.util.UUID;

import app.messages.exchange.requests.Withdrawal;

public class WithdrawalOp {
    
    private UUID rid;
	private PublicKey clientID;
	private float amount;

	public WithdrawalOp(Withdrawal withdrawal) {
		this.rid = withdrawal.getRid();
		this.clientID = withdrawal.getClientID();
		this.amount = withdrawal.getAmount();
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
        return "Withdrawal{" + "rid=" + rid + ", clientID=" + clientID + ", amount=" + amount + '}';
    }

}
