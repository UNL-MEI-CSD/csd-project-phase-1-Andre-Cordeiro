package consensus.messages;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedMessageSerializer;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedProtoMessage;
import useless.SeqN;
import utils.MessageBatch.MessageBatchKey;

public class PrepareMessage extends SignedProtoMessage {

	public final static short MESSAGE_ID = 102;	
	
	public final MessageBatchKey batchKey;
	public final int instanceNumber;
	public final String cryptoName;
	
	public PrepareMessage(MessageBatchKey batchKey, int instanceNumber, String cryptoName) {
		super(PrepareMessage.MESSAGE_ID);
		this.batchKey = batchKey;
		this.instanceNumber = instanceNumber;
		this.cryptoName = cryptoName;
	}

	public static final SignedMessageSerializer<PrepareMessage> serializer = new SignedMessageSerializer<PrepareMessage>() {

		@Override
		public void serializeBody(PrepareMessage signedProtoMessage, ByteBuf out) throws IOException {
			out.writeInt(signedProtoMessage.batchKey.getOpsHash());
			signedProtoMessage.batchKey.getSeqN().serialize(out);
			out.writeInt(signedProtoMessage.batchKey.getViewNumber());
			out.writeInt(signedProtoMessage.instanceNumber);
			out.writeCharSequence(signedProtoMessage.cryptoName, StandardCharsets.UTF_8);
		}

		@Override
		public PrepareMessage deserializeBody(ByteBuf in) throws IOException {
			MessageBatchKey batchKey = new MessageBatchKey(in.readInt(), SeqN.deserialize(in), in.readInt());
			int instanceNumber = in.readInt();
			String cryptoName = in.readCharSequence(in.readableBytes(), StandardCharsets.UTF_8).toString();
			return new PrepareMessage(batchKey, instanceNumber, cryptoName);
		}
		
	};
	
	@Override
	public SignedMessageSerializer<? extends SignedProtoMessage> getSerializer() {
		return PrepareMessage.serializer;
	}

	@Override
	public String toString() {
		return "PrepareMessage{" +
				"batchKey=" + batchKey +
				", instanceNumber=" + instanceNumber +
				'}';
	}

	public MessageBatchKey getBatchKey(){
		return batchKey;
	}
	

	public int getInstanceNumber(){
		return instanceNumber;
	}

	public String getCryptoName() {
		return cryptoName;
	}
}
