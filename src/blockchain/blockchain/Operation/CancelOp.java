package blockchain.blockchain.Operation;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;

import app.messages.client.requests.Cancel;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class CancelOp {
    
    private UUID rID;
	private PublicKey cID;
    private static String resourceType;
	
	public CancelOp(Cancel cancel) {
		this.rID = cancel.getrID();
		this.cID = cancel.getcID();
	}

    public CancelOp(UUID rID, PublicKey cID) {
        this.rID = rID;
        this.cID = cID;
    }

    public UUID getRID() {
        return rID;
    }

    public PublicKey getCID() {
        return cID;
    }

    // convert to byte array
    public byte[] toByteArray() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeLong(rID.getMostSignificantBits());
        buf.writeLong(rID.getLeastSignificantBits());
        buf.writeBytes(cID.getEncoded());
        return buf.array();
    }

    // convert from byte array
    public CancelOp parseFrom(byte[] bytes) {
        ByteBuf buf = Unpooled.wrappedBuffer(bytes);
        long mostSigBits = buf.readLong();
        long leastSigBits = buf.readLong();
        byte[] encoded = new byte[buf.readableBytes()];
        buf.readBytes(encoded);
        try {
            UUID rID = new UUID(mostSigBits, leastSigBits);
            PublicKey cID = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encoded));
            return new CancelOp(rID, cID);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "Cancel{" + 
            "rID=" + rID + 
            ", cID=" + cID + 
            ", resourceType=" + resourceType + 
            '}';
    }
}
