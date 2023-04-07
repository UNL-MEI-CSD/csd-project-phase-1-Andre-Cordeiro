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
	public final int digest; // Digest of the block
	
	public PrePrepareMessage(int vN, SeqN sN, int digest) {  
		super(PrePrepareMessage.MESSAGE_ID);
		this.vN = vN;
		this.sN = sN;
		this.digest = digest;
	}

	public static SignedMessageSerializer<PrePrepareMessage> serializer = new SignedMessageSerializer<PrePrepareMessage>() {

		@Override
		public void serializeBody(PrePrepareMessage signedProtoMessage, ByteBuf out) throws IOException {
			out.writeInt(signedProtoMessage.vN);
			signedProtoMessage.sN.serialize(out);
			out.writeInt(signedProtoMessage.digest);
		}

		@Override
		public PrePrepareMessage deserializeBody(ByteBuf in) throws IOException {
			int vN = in.readInt();
			SeqN sN = SeqN.deserialize(in);
			int digest = in.readInt();

			return new PrePrepareMessage(vN, sN, digest);
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

	public int getDigest(){
		return digest;
	}

}
