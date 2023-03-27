package consensus.messages;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedMessageSerializer;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedProtoMessage;

public class CommitMessage extends SignedProtoMessage{

    public final static short MESSAGE_ID = 103;	

	public final int vN, sN, hashOpVal, iN;

    public CommitMessage(int vN, int sN, int hashOpVal, int iN){
        super(CommitMessage.MESSAGE_ID);
		this.vN = vN;
		this.sN = sN;
		this.hashOpVal = hashOpVal;
		this.iN = iN;
    }

    public static final SignedMessageSerializer<CommitMessage> serializer = new SignedMessageSerializer<CommitMessage>() {

		@Override
		public void serializeBody(CommitMessage signedProtoMessage, ByteBuf out) throws IOException {
			out.writeInt(signedProtoMessage.vN);
			out.writeInt(signedProtoMessage.sN);
			out.writeInt(signedProtoMessage.hashOpVal);
			out.writeInt(signedProtoMessage.iN);
		}

		@Override
		public CommitMessage deserializeBody(ByteBuf in) throws IOException {
			int vN = in.readInt();
			int sN = in.readInt();
			int hashOpVal = in.readInt();
			int iN = in.readInt();
			return new CommitMessage(vN, sN, hashOpVal, iN);
		}
		
	};
	
	@Override
	public SignedMessageSerializer<? extends SignedProtoMessage> getSerializer() {
		return CommitMessage.serializer;
	}
}
