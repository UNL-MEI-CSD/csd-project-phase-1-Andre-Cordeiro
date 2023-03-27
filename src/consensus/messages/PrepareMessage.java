package consensus.messages;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedMessageSerializer;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedProtoMessage;

public class PrepareMessage extends SignedProtoMessage {

	public final static short MESSAGE_ID = 102;	
	
	public final int vN, sN, hashOpVal, iN;
	
	public PrepareMessage(int vN, int sN, int hashOpVal, int iN) {
		super(PrepareMessage.MESSAGE_ID);
		this.vN = vN;
		this.sN = sN;
		this.hashOpVal = hashOpVal;
		this.iN = iN;
		
	}

	public static final SignedMessageSerializer<PrepareMessage> serializer = new SignedMessageSerializer<PrepareMessage>() {

		@Override
		public void serializeBody(PrepareMessage signedProtoMessage, ByteBuf out) throws IOException {
			out.writeInt(signedProtoMessage.vN);
			out.writeInt(signedProtoMessage.sN);
			out.writeInt(signedProtoMessage.hashOpVal);
			out.writeInt(signedProtoMessage.iN);
		}

		@Override
		public PrepareMessage deserializeBody(ByteBuf in) throws IOException {
			int vN = in.readInt();
			int sN = in.readInt();
			int hashOpVal = in.readInt();
			int iN = in.readInt();
			return new PrepareMessage(vN, sN, hashOpVal, iN);
		}
		
	};
	
	@Override
	public SignedMessageSerializer<? extends SignedProtoMessage> getSerializer() {
		return PrepareMessage.serializer;
	}

}
