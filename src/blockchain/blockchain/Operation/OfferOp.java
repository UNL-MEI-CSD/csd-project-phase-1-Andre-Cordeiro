package blockchain.blockchain.Operation;

import java.security.PublicKey;
import java.util.UUID;

import app.messages.client.requests.IssueOffer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class OfferOp {
    
    private UUID rid;
	private PublicKey cID;
	private String resourceType;
	private int quantity;
	private float pricePerUnit;
	

	public OfferOp(IssueOffer offer) {
		this.setRid(offer.getRid());
		this.setcID(offer.getcID());
		this.setResourceType(offer.getResourceType());
		this.setQuantity(offer.getQuantity());
		this.setPricePerUnit(offer.getPricePerUnit());
		
	}

    public OfferOp(UUID rid, PublicKey cID, String resourceType, int quantity, float pricePerUnit) {
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
    public static OfferOp parseFrom(byte[] operation){
        ByteBuf buf = Unpooled.wrappedBuffer(operation);
        long mostSigBits = buf.readLong();
        long leastSigBits = buf.readLong();
        byte[] encoded = new byte[buf.readableBytes()];
        buf.readBytes(encoded);
        byte[] resourceType = new byte[buf.readableBytes()];
        buf.readBytes(resourceType);
        int quantity = buf.readInt();
        float pricePerUnit = buf.readFloat();
        try {
            UUID rid = new UUID(mostSigBits, leastSigBits);
            PublicKey cID = java.security.KeyFactory.getInstance("RSA").generatePublic(new java.security.spec.X509EncodedKeySpec(encoded));
            return new OfferOp(rid, cID, new String(resourceType), quantity, pricePerUnit);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "Offer{" + 
        "rid=" + rid + 
        ", cID=" + cID + 
        ", resourceType=" + resourceType + 
        ", quantity=" + quantity + 
        ", pricePerUnit=" + pricePerUnit + 
        '}';
    }

    
}
