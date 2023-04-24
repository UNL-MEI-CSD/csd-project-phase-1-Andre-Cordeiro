package consensus.messages;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedMessageSerializer;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedProtoMessage;
import utils.SeqN;

public class PrepareMessage extends SignedProtoMessage {

	public final static short MESSAGE_ID = 102;	
	
	public final int viewNumber;
	public final SeqN sequenceNumber;
	public final int hashOpVal;
	public final int instanceNumber;
	
	public PrepareMessage(int viewNumber, SeqN sequenceNumber, int hashOpVal, int instanceNumber) {
		super(PrepareMessage.MESSAGE_ID);
		this.viewNumber = viewNumber;
		this.sequenceNumber = sequenceNumber;
		this.hashOpVal = hashOpVal;
		this.instanceNumber = instanceNumber;
	}

	public static final SignedMessageSerializer<PrepareMessage> serializer = new SignedMessageSerializer<PrepareMessage>() {

		@Override
		public void serializeBody(PrepareMessage signedProtoMessage, ByteBuf out) throws IOException {
			out.writeInt(signedProtoMessage.viewNumber);
			signedProtoMessage.sequenceNumber.serialize(out);
			out.writeInt(signedProtoMessage.hashOpVal);
			out.writeInt(signedProtoMessage.instanceNumber);
		}

		@Override
		public PrepareMessage deserializeBody(ByteBuf in) throws IOException {
			int vN = in.readInt();
			SeqN sN = SeqN.deserialize(in);
			int hashOpVal = in.readInt();
			int instanceNumber = in.readInt();
			return new PrepareMessage(vN, sN, hashOpVal, instanceNumber);
		}
		
	};
	
	@Override
	public SignedMessageSerializer<? extends SignedProtoMessage> getSerializer() {
		return PrepareMessage.serializer;
	}

	@Override
	public String toString() {
		return "PrepareMessage{" +
				"viewNumber=" + viewNumber +
				", sequenceNumber=" + sequenceNumber +
				", hashOpVal=" + hashOpVal +
				", instanceNumber=" + instanceNumber +
				'}';
	}

	public int getViewNumber(){
		return viewNumber;
	}

	public SeqN getSequenceNumber(){
		return sequenceNumber;
	}

	public int getHashOpVal(){
		return hashOpVal;
	}

	public int getInstanceNumber(){
		return instanceNumber;
	}

	public int getOp() {
		return hashOpVal;
	}
}
