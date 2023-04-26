package blockchain.messages;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import blockchain.requests.ClientRequest;
import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedMessageSerializer;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedProtoMessage;

public class RedirectClientRequestMessage extends SignedProtoMessage {

	public final static short MSG_ID = 201;

	private final ClientRequest clientRequest;
	
	public RedirectClientRequestMessage(ClientRequest clientRequest) {
		super(RedirectClientRequestMessage.MSG_ID);
		this.clientRequest = clientRequest;
	}

	public static SignedMessageSerializer<RedirectClientRequestMessage> serializer = new SignedMessageSerializer<RedirectClientRequestMessage>() {
		
		@Override
		public void serializeBody(RedirectClientRequestMessage signedProtoMessage, ByteBuf out) throws IOException {
			
			byte[] bytes = signedProtoMessage.clientRequest.generateByteRepresentation();
			out.writeCharSequence(new String(bytes), StandardCharsets.UTF_8);
			
		}
		
		@Override
		public RedirectClientRequestMessage deserializeBody(ByteBuf in) throws IOException {
			
			ClientRequest req = ClientRequest.fromBytes(in.readCharSequence(in.readableBytes(), StandardCharsets.UTF_8).toString().getBytes());

			return new RedirectClientRequestMessage(req);
		}
	};
	
	@Override
	public SignedMessageSerializer<? extends SignedProtoMessage> getSerializer() {
		return RedirectClientRequestMessage.serializer;
	}

	@Override
	public String toString() {
		return "RedirectClientRequestMessage{" +
				"clientRequest=" + clientRequest +
				'}';
	}

	public ClientRequest getClientRequest() {
		return clientRequest;
	}

}
