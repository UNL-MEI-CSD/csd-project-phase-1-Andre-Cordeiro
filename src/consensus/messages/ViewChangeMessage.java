package consensus.messages;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedMessageSerializer;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedProtoMessage;

public class ViewChangeMessage extends SignedProtoMessage{

    public final static short MESSAGE_ID = 109;

    private final int viewNumber;

    private final int lastSequenceNumber;

    private final int replicaID;

    public final String cryptoName;

    //Add checkpoint Message and Prepared Message Batch.

    public ViewChangeMessage(int viewNumber, int lastSequenceNumber , int replicaID, String cryptoName) {
        super(ViewChangeMessage.MESSAGE_ID);
        this.viewNumber = viewNumber;
        this.replicaID = replicaID;
        this.lastSequenceNumber = lastSequenceNumber;
        this.cryptoName = cryptoName;
    }

    public static final SignedMessageSerializer<ViewChangeMessage> serializer = new SignedMessageSerializer<ViewChangeMessage>() {

		@Override
		public void serializeBody(ViewChangeMessage signedProtoMessage, ByteBuf out) throws IOException {
			out.writeInt(signedProtoMessage.viewNumber);
            out.writeInt(signedProtoMessage.lastSequenceNumber);
            out.writeInt(signedProtoMessage.replicaID);
            out.writeCharSequence(signedProtoMessage.cryptoName, StandardCharsets.UTF_8);
		}

		@Override
		public ViewChangeMessage deserializeBody(ByteBuf in) throws IOException {
			int viewNumber = in.readInt();
            int lastSequenceNumber = in.readInt();
            int replicaID = in.readInt();
            String cryptoName = in.readCharSequence(in.readableBytes(), StandardCharsets.UTF_8).toString();
            return new ViewChangeMessage(viewNumber, lastSequenceNumber, replicaID, cryptoName);
		}
		
	};
	
	@Override
	public SignedMessageSerializer<? extends SignedProtoMessage> getSerializer() {
		return ViewChangeMessage.serializer;
	}


    public int getViewNumber() {
        return viewNumber;
    }

    public int getReplicaID() {
        return replicaID;
    }

    public int getLastSequenceNumber() {
        return lastSequenceNumber;
    }

    public String getCryptoName() {
        return cryptoName;
    }



    @Override
    public String toString() {
        return "viewChangeMessage []";
    }

}
