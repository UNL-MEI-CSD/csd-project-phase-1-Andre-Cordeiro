package app.messages.client.requests;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedMessageSerializer;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedProtoMessage;
import pt.unl.fct.di.novasys.network.ISerializer;

public class CheckBalance extends SignedProtoMessage{

    public final static short MESSAGE_ID = 309;

    private PublicKey cID;
    public UUID rID;

    public CheckBalance(UUID rID, PublicKey cID) {
        super(CheckBalance.MESSAGE_ID);
        this.rID = rID;
        this.cID = cID;
    }

    public UUID getRid() {
        return rID;
    }

    public void setRid(UUID rID) {
        this.rID = rID;
    }

    public PublicKey getCid() {
        return cID;
    }

    public void setCid(PublicKey cID) {
        this.cID = cID;
    }

    public final static SignedMessageSerializer<CheckBalance> serializer = new SignedMessageSerializer<CheckBalance>() {
        @Override
        public void serializeBody(CheckBalance c, ByteBuf out) throws IOException {
            out.writeLong(c.rID.getMostSignificantBits());
            out.writeLong(c.rID.getLeastSignificantBits());
            byte[] pk = c.cID.getEncoded();
            out.writeShort(pk.length);
            out.writeBytes(pk);
        }

        @Override
        public CheckBalance deserializeBody(ByteBuf in) throws IOException {
            long msb = in.readLong();
            long lsb = in.readLong();
            short l = in.readShort();
            byte[] pk = new byte[l];
            in.readBytes(pk);
            PublicKey cID = null;
            try {
                cID = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pk));
            } catch (InvalidKeySpecException e) {
            } catch (NoSuchAlgorithmException e) {
            }

            return new CheckBalance(new UUID(msb,lsb), cID);
        }

    };

    @Override
    public SignedMessageSerializer<CheckBalance> getSerializer() {
        return CheckBalance.serializer;
    }

    @Override
    public String toString() {
        return "IssueOffer ["+
            "rid=" + rID + 
            ", cID=" + cID +
            "]";
    }


    
    
}
