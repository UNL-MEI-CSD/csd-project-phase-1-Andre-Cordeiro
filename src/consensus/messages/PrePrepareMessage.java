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
	public final int operationHash; // Operation
	public byte[] signature; // Signature
	public final String cryptoName; //cryptoName
	
	public PrePrepareMessage(int vN, SeqN sN, int operationHash, String cryptoName) {  
		super(PrePrepareMessage.MESSAGE_ID);
		this.vN = vN;
		this.sN = sN;
		this.operationHash = operationHash;
		this.cryptoName = cryptoName;
		this.signature = null;
	}

	public static SignedMessageSerializer<PrePrepareMessage> serializer = new SignedMessageSerializer<PrePrepareMessage>() {

		@Override
		public void serializeBody(PrePrepareMessage signedProtoMessage, ByteBuf out) throws IOException {
			out.writeInt(signedProtoMessage.vN);
			signedProtoMessage.sN.serialize(out);
			out.writeInt(signedProtoMessage.operationHash);
			out.writeCharSequence(signedProtoMessage.cryptoName, StandardCharsets.UTF_8);
		}



		@Override
		public PrePrepareMessage deserializeBody(ByteBuf in) throws IOException {
			int vN = in.readInt();
			SeqN sN = SeqN.deserialize(in);
			int operationHash = in.readInt();
			String cryptoName = in.readCharSequence(in.readableBytes(), StandardCharsets.UTF_8).toString();

			return new PrePrepareMessage(vN, sN, operationHash,cryptoName);
		}
		
	};
	
	@Override
	public SignedMessageSerializer<PrePrepareMessage> getSerializer() {
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
