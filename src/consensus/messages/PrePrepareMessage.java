package consensus.messages;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedMessageSerializer;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedProtoMessage;
import utils.SeqN;

public class PrePrepareMessage extends SignedProtoMessage {

	public final static short MESSAGE_ID = 101;

	public final int vN; // View Number
	public final SeqN sN; // Sequence Number
	public final int operationHash; // Operation
	public byte[] signature; // Signature
	
	public PrePrepareMessage(int vN, SeqN sN, int operationHash) {  
		super(PrePrepareMessage.MESSAGE_ID);
		this.vN = vN;
		this.sN = sN;
		this.operationHash = operationHash;
		this.signature = null;
	}

	public static SignedMessageSerializer<PrePrepareMessage> serializer = new SignedMessageSerializer<PrePrepareMessage>() {

		@Override
		public void serializeBody(PrePrepareMessage signedProtoMessage, ByteBuf out) throws IOException {
			out.writeInt(signedProtoMessage.vN);
			signedProtoMessage.sN.serialize(out);
			out.writeInt(signedProtoMessage.operationHash);
		}

		

		@Override
		public PrePrepareMessage deserializeBody(ByteBuf in) throws IOException {
			int vN = in.readInt();
			SeqN sN = SeqN.deserialize(in);
			int operationHash = in.readInt();

			return new PrePrepareMessage(vN, sN, operationHash);
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
				", operationHash=" + operationHash +
				'}';
	}

	public int getViewNumber(){
		return vN;
	}

	public SeqN getSeqNumber(){
		return sN;
	}

	public int getOp(){
		return operationHash ;
	}

	public byte[] getSignature() {
		return signature;
	}

	public void setSignature(byte[] signature) {
		this.signature = signature;
	}

}
