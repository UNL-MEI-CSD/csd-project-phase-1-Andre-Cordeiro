package blockchain.blockchain.Operation;

import java.security.PublicKey;
import java.util.UUID;

import app.messages.client.requests.IssueWant;

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

    public byte[] toByteArray() {
        return (rid.toString() + cID.toString() + resourceType + quantity + pricePerUnit).getBytes();
    }

    @Override
    public String toString() {
        return "Want{" + "rid=" + rid + ", cID=" + cID + ", resourceType=" + resourceType + ", quantity=" + quantity + ", pricePerUnit=" + pricePerUnit + '}';
    }


}
