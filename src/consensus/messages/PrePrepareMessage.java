package consensus.messages;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedMessageSerializer;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedProtoMessage;
import utils.SeqN;
import utils.MessageBatch.MessageBatchKey;

public class PrePrepareMessage extends SignedProtoMessage {

	public final static short MESSAGE_ID = 101;

	public final MessageBatchKey batchKey;
	public final String cryptoName; //cryptoName
	
	public PrePrepareMessage(MessageBatchKey batchKey, String cryptoName) {  
		super(PrePrepareMessage.MESSAGE_ID);
		this.batchKey = batchKey;
		this.cryptoName = cryptoName;
	}

	public static SignedMessageSerializer<PrePrepareMessage> serializer = new SignedMessageSerializer<PrePrepareMessage>() {

		@Override
		public void serializeBody(PrePrepareMessage signedProtoMessage, ByteBuf out) throws IOException {
			out.writeInt(signedProtoMessage.batchKey.getOpsHash());
			signedProtoMessage.batchKey.getSeqN().serialize(out);
			out.writeInt(signedProtoMessage.batchKey.getViewNumber());
			out.writeCharSequence(signedProtoMessage.cryptoName, StandardCharsets.UTF_8);
		}



		@Override
		public PrePrepareMessage deserializeBody(ByteBuf in) throws IOException {
			MessageBatchKey batchKey = new MessageBatchKey(in.readInt(), SeqN.deserialize(in), in.readInt());
			String cryptoName = in.readCharSequence(in.readableBytes(), StandardCharsets.UTF_8).toString();

			return new PrePrepareMessage(batchKey,cryptoName);
		}
		
	};
	
	@Override
	public SignedMessageSerializer<PrePrepareMessage> getSerializer() {
		return PrePrepareMessage.serializer;
	}

	@Override
	public String toString() {
		return "PrePrepareMessage{" +
				"batchKey=" + batchKey +
				'}';
	}

	public MessageBatchKey getBatchKey() {
		return batchKey;
	}

	public String getCryptoName(){
		return cryptoName;
	}

}
