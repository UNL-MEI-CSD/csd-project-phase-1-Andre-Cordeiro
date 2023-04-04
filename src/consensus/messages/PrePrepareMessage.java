package consensus.messages;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedMessageSerializer;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedProtoMessage;

public class PrePrepareMessage extends SignedProtoMessage {

	public final static short MESSAGE_ID = 101;

	public final int vN; // View Number
	public final int sN; // Sequence Number
	public final int digest; //Message Digest
	
	public PrePrepareMessage(int vN, int sN, int digest) {  //TODO SHOULD THE HASH VALUE BE AN INT?
		super(PrePrepareMessage.MESSAGE_ID);
		this.vN = vN;
		this.sN = sN;
		this.digest = digest;
	}

	public static SignedMessageSerializer<PrePrepareMessage> serializer = new SignedMessageSerializer<PrePrepareMessage>() {

		@Override
		public void serializeBody(PrePrepareMessage signedProtoMessage, ByteBuf out) throws IOException {
			out.writeInt(signedProtoMessage.vN);
			out.writeInt(signedProtoMessage.sN);
			out.writeInt(signedProtoMessage.digest); 
		}

		@Override
		public PrePrepareMessage deserializeBody(ByteBuf in) throws IOException {
			int vN = in.readInt();
			int sN = in.readInt();
			int digest = in.readInt();
			return new PrePrepareMessage(vN, sN, digest);
		}
		
	};
	
	@Override
	public SignedMessageSerializer<? extends SignedProtoMessage> getSerializer() {
		return PrePrepareMessage.serializer;
	}

}
