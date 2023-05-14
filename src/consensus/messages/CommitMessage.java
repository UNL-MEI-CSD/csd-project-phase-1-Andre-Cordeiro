package consensus.messages;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedMessageSerializer;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedProtoMessage;
import utils.MessageBatch.MessageBatchKey;

public class CommitMessage extends SignedProtoMessage{

    public final static short MESSAGE_ID = 103;	

	public final MessageBatchKey batchKey;

	public final String cryptoName;

    public CommitMessage(MessageBatchKey batchKey, String cryptoName){
        super(CommitMessage.MESSAGE_ID);
		this.batchKey = batchKey;
		this.cryptoName = cryptoName;
    }

    public static final SignedMessageSerializer<CommitMessage> serializer = new SignedMessageSerializer<CommitMessage>() {

		@Override
		public void serializeBody(CommitMessage signedProtoMessage, ByteBuf out) throws IOException {
			out.writeInt(signedProtoMessage.batchKey.getOpsMapHash());
			out.writeInt(signedProtoMessage.batchKey.getSeqN());
			out.writeInt(signedProtoMessage.batchKey.getViewNumber());
			out.writeCharSequence(signedProtoMessage.cryptoName, StandardCharsets.UTF_8);
		}

		@Override
		public CommitMessage deserializeBody(ByteBuf in) throws IOException {
			MessageBatchKey batchKey = new MessageBatchKey(in.readInt(), in.readInt(), in.readInt());
			String cryptoName = in.readCharSequence(in.readableBytes(), StandardCharsets.UTF_8).toString();
			return new CommitMessage(batchKey,cryptoName);
		}
		
	};
	
	@Override
	public SignedMessageSerializer<? extends SignedProtoMessage> getSerializer() {
		return CommitMessage.serializer;
	}

	public MessageBatchKey getBatchKey() {
		return batchKey;
	}

	public String getCryptoName() {
		return cryptoName;
	}

	@Override
	public String toString() {
		return "CommitMessage{" +
				"batchKey=" + batchKey +
				", cryptoName='" + cryptoName + '\'' +
				'}';
	}
}
