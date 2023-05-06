package blockchain.messages;

import java.io.IOException;

import blockchain.requests.ClientRequest;
import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedMessageSerializer;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedProtoMessage;

public class RedirectClientRequestMessage extends SignedProtoMessage{

	public final static short MESSAGE_ID = 207;

	private final ClientRequest clientRequest;
	
	public RedirectClientRequestMessage(ClientRequest clientRequest) {
		super(RedirectClientRequestMessage.MESSAGE_ID);
		this.clientRequest = clientRequest;
	}

	public static final SignedMessageSerializer<RedirectClientRequestMessage> serializer = new SignedMessageSerializer<RedirectClientRequestMessage>() {

		@Override
		public void serializeBody(RedirectClientRequestMessage signedProtoMessage, ByteBuf out) throws IOException {
			out.writeInt(signedProtoMessage.clientRequest.generateByteRepresentation().length);
			out.writeBytes(signedProtoMessage.clientRequest.generateByteRepresentation());
		}

		@Override
		public RedirectClientRequestMessage deserializeBody(ByteBuf in) throws IOException {
			int clientRequestLength = in.readInt();
			byte[] clientRequestBytes = new byte[clientRequestLength];
			in.readBytes(clientRequestBytes);
			ClientRequest clientRequest = ClientRequest.fromBytes(clientRequestBytes);
			return new RedirectClientRequestMessage(clientRequest);
		}
		
	};


	@Override
	public SignedMessageSerializer<? extends SignedProtoMessage> getSerializer() {
		return RedirectClientRequestMessage.serializer;
	}


	

	public ClientRequest getClientRequest() {
		return clientRequest;
	}

	@Override
	public String toString() {
		return "RedirectClientRequestMessage{" +
				"clientRequest=" + clientRequest +
				'}';
	}

}
