package consensus.messages;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedMessageSerializer;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedProtoMessage;
import utils.SeqN;

public class PrePrepareMessage extends SignedProtoMessage {

	public final static short MESSAGE_ID = 101;

	public final int vN; // View Number
	public final SeqN sN; // Sequence Number
	public final String op; // Operation
	
	public PrePrepareMessage(int vN, SeqN sN, String operation) {  
		super(PrePrepareMessage.MESSAGE_ID);
		this.vN = vN;
		this.sN = sN;
		this.op = operation;
	}

	public static SignedMessageSerializer<PrePrepareMessage> serializer = new SignedMessageSerializer<PrePrepareMessage>() {

		@Override
		public void serializeBody(PrePrepareMessage signedProtoMessage, ByteBuf out) throws IOException {
			out.writeInt(signedProtoMessage.vN);
			signedProtoMessage.sN.serialize(out);
			out.writeCharSequence(signedProtoMessage.op, StandardCharsets.UTF_8);
		}

		@Override
		public PrePrepareMessage deserializeBody(ByteBuf in) throws IOException {
			int vN = in.readInt();
			SeqN sN = SeqN.deserialize(in);
			String op = in.readCharSequence(in.readableBytes(), StandardCharsets.UTF_8).toString();

			return new PrePrepareMessage(vN, sN, op);
		}
		
	};
	
	@Override
	public SignedMessageSerializer<? extends SignedProtoMessage> getSerializer() {
		return PrePrepareMessage.serializer;
	}

	@Override
	public String toString() {
		return "PrePrepareMessage{" +
				"vN=" + vN +
				", sN=" + sN +
				'}';
	}

	public int getViewNumber(){
		return vN;
	}

	public SeqN getSeqNumber(){
		return sN;
	}

	public String getOp(){
		return op;
	}

}
