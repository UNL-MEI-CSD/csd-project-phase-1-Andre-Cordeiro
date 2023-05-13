package app.messages.client.requests;

import java.io.IOException;
import java.security.PublicKey;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedMessageSerializer;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedProtoMessage;

public class CheckBalance extends SignedProtoMessage{

    public final static short MESSAGE_ID = 309;

    private PublicKey cID;
    private UUID rID;


    public CheckBalance(PublicKey cID) {
        super(MESSAGE_ID);
        this.setRid(UUID.randomUUID());
		this.setcID(cID);
    }


    public static final SignedMessageSerializer<IssueOffer> serializer = new SignedMessageSerializer<IssueOffer>() {
        @Override
		public void serializeBody(IssueOffer io, ByteBuf out) throws IOException {
			
		}

		@Override
		public IssueOffer deserializeBody(ByteBuf in) throws IOException {
			return null;
            
		}
   };

   @Override
	public SignedMessageSerializer<IssueOffer> getSerializer() {
		return IssueOffer.serializer;
	}

    public UUID getRid() {
        return rID;
    }

    public void setRid(UUID rID) {
        this.rID = rID;
    }

    public PublicKey getcID() {
        return cID;
    }

    public void setcID(PublicKey cID) {
        this.cID = cID;
    }

    @Override
    public String toString() {
        return "IssueOffer ["+
            "rid=" + rID + 
            ", cID=" + cID +
            "]";
    }


    
    
}
