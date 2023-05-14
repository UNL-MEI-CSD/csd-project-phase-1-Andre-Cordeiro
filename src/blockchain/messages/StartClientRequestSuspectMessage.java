package blockchain.messages;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedMessageSerializer;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedProtoMessage;

public class StartClientRequestSuspectMessage extends SignedProtoMessage {

	public final static short MESSAGE_ID = 203;

	private final UUID pendingRequestID;
	private final String cryptoName;

	public StartClientRequestSuspectMessage(UUID pendingRequestID, String cryptoName) {
		super(MESSAGE_ID);
		this.pendingRequestID = pendingRequestID;
		this.cryptoName = cryptoName;
	}

	public UUID getPendingRequestID() {
		return pendingRequestID;
	}

	public String getCryptoName() {
		return cryptoName;
	}
	public static final SignedMessageSerializer<StartClientRequestSuspectMessage> serializer = new SignedMessageSerializer<StartClientRequestSuspectMessage>() {

		@Override
		public void serializeBody(StartClientRequestSuspectMessage signedProtoMessage, ByteBuf out) throws IOException {
			out.writeInt(signedProtoMessage.pendingRequestID.toString().length());
			out.writeCharSequence(signedProtoMessage.pendingRequestID.toString(), StandardCharsets.UTF_8);
			out.writeInt(signedProtoMessage.cryptoName.length());
			out.writeCharSequence(signedProtoMessage.cryptoName, StandardCharsets.UTF_8);
		}

		@Override
		public StartClientRequestSuspectMessage deserializeBody(ByteBuf in) throws IOException {
			int pendingRequestIDint = in.readInt();
			UUID pendingRequestID = UUID.fromString(in.readCharSequence(pendingRequestIDint, StandardCharsets.UTF_8).toString());
			int cryptoNameint = in.readInt();
			String cryptoName = in.readCharSequence(cryptoNameint, StandardCharsets.UTF_8).toString();
			return new StartClientRequestSuspectMessage(pendingRequestID, cryptoName);
		}
		
	};


	@Override
	public SignedMessageSerializer<? extends SignedProtoMessage> getSerializer() {
		return StartClientRequestSuspectMessage.serializer;
	}

	@Override
	public String toString() {
		return "ClientRequestUnhandledMessage{" +
				"pendingRequestID=" + pendingRequestID +
				", cryptoName='" + cryptoName + '\'' +
				'}';
	}
	
	
}
