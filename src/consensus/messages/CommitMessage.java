package consensus.messages;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedMessageSerializer;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedProtoMessage;

public class CommitMessage extends SignedProtoMessage{

    public final static short MESSAGE_ID = 103;	

    public CommitMessage(){
        super(CommitMessage.MESSAGE_ID);
    }

    public static final SignedMessageSerializer<CommitMessage> serializer = new SignedMessageSerializer<CommitMessage>() {

		@Override
		public void serializeBody(CommitMessage signedProtoMessage, ByteBuf out) throws IOException {
			// TODO Auto-generated method stub, you should implement this method.
			
		}

		@Override
		public CommitMessage deserializeBody(ByteBuf in) throws IOException {
			// TODO Auto-generated method stub, you should implement this method.
			return null;
		}
		
	};
	
	@Override
	public SignedMessageSerializer<? extends SignedProtoMessage> getSerializer() {
		return CommitMessage.serializer;
	}
}
