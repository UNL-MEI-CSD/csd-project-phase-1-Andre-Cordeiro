package blockchain.blockchain.Operation;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;

import app.messages.client.requests.IssueWant;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class WantOp {
    
    private UUID rid;
	private PublicKey cID;
	private String resourceType;
	private int quantity;
	private float pricePerUnit;

	public WantOp(IssueWant want) {
		this.setRid(want.getRid());
		this.setcID(want.getcID());
		this.setResourceType(want.getResourceType());
		this.setQuantity(want.getQuantity());
		this.setPricePerUnit(want.getPricePerUnit());
		
	}

    public WantOp(UUID rid, PublicKey cID, String resourceType, int quantity, float pricePerUnit) {
        this.rid = rid;
        this.cID = cID;
        this.resourceType = resourceType;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
    }

    public UUID getRid() {
        return rid;
    }

    public PublicKey getcID() {
        return cID;
    }

    public String getResourceType() {
        return resourceType;
    }

    public int getQuantity() {
        return quantity;
    }

    public float getPricePerUnit() {
        return pricePerUnit;
    }

    public void setRid(UUID rid) {
        this.rid = rid;
    }

    public void setcID(PublicKey cID) {
        this.cID = cID;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setPricePerUnit(float pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    // convert to byte array
    public byte[] toByteArray() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeLong(rid.getMostSignificantBits());
        buf.writeLong(rid.getLeastSignificantBits());
        buf.writeBytes(cID.getEncoded());
        buf.writeBytes(resourceType.getBytes());
        buf.writeInt(quantity);
        buf.writeFloat(pricePerUnit);
        return buf.array();
    }

    // convert from byte array
    public static WantOp parseFrom(byte[] operation){
        ByteBuf buf = Unpooled.wrappedBuffer(operation);
        UUID rid = new UUID(buf.readLong(), buf.readLong());
        PublicKey cID = null;
        try {
            byte[] cIDBytes = new byte[buf.readableBytes()];
            buf.readBytes(cIDBytes);
            cID = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(cIDBytes));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
        String resourceType = new String(buf.readBytes(buf.readInt()).array());
        int quantity = buf.readInt();
        float pricePerUnit = buf.readFloat();
        return new WantOp(rid, cID, resourceType, quantity, pricePerUnit);
    }

    @Override
    public String toString() {
        return "Want{" + "rid=" + rid + ", cID=" + cID + ", resourceType=" + resourceType + ", quantity=" + quantity + ", pricePerUnit=" + pricePerUnit + '}';
    }


}
