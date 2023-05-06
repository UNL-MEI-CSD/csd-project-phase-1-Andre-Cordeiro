package blockchain.messages;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedMessageSerializer;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedProtoMessage;

public class ClientRequestUnhandledMessage extends SignedProtoMessage {

	public final static short MESSAGE_ID = 202;
	
	private final UUID pendingRequestID;

	public ClientRequestUnhandledMessage(UUID pendingRequestID) {
		super(MESSAGE_ID);
		this.pendingRequestID = pendingRequestID;
	}

	public UUID getPendingRequestID() {
		return pendingRequestID;
	}

	public static final SignedMessageSerializer<ClientRequestUnhandledMessage> serializer = new SignedMessageSerializer<ClientRequestUnhandledMessage>() {

		@Override
		public void serializeBody(ClientRequestUnhandledMessage signedProtoMessage, ByteBuf out) throws IOException {
			out.writeInt(signedProtoMessage.pendingRequestID.toString().length());
			out.writeCharSequence(signedProtoMessage.pendingRequestID.toString(), StandardCharsets.UTF_8);
		}

		@Override
		public ClientRequestUnhandledMessage deserializeBody(ByteBuf in) throws IOException {
			int pendingRequestIDint = in.readInt();
			UUID pendingRequestID = UUID.fromString(in.readCharSequence(pendingRequestIDint, StandardCharsets.UTF_8).toString());
			return new ClientRequestUnhandledMessage(pendingRequestID);
		}
		
	};


	@Override
	public SignedMessageSerializer<? extends SignedProtoMessage> getSerializer() {
		return ClientRequestUnhandledMessage.serializer;
	}
}
