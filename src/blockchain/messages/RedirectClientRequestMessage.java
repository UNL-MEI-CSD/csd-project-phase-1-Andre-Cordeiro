package blockchain.messages;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import blockchain.requests.ClientRequest;
import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedMessageSerializer;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedProtoMessage;

public class RedirectClientRequestMessage extends SignedProtoMessage{

	public final static short MESSAGE_ID = 207;

	private final ClientRequest clientRequest;

	private final String cryptoName;
	
	public RedirectClientRequestMessage(ClientRequest clientRequest, String cryptoName) {
		super(RedirectClientRequestMessage.MESSAGE_ID);
		this.clientRequest = clientRequest;
		this.cryptoName = cryptoName;
	}

	public static final SignedMessageSerializer<RedirectClientRequestMessage> serializer = new SignedMessageSerializer<RedirectClientRequestMessage>() {

		@Override
		public void serializeBody(RedirectClientRequestMessage signedProtoMessage, ByteBuf out) throws IOException {
			out.writeInt(signedProtoMessage.clientRequest.generateByteRepresentation().length);
			out.writeBytes(signedProtoMessage.clientRequest.generateByteRepresentation());
			out.writeInt(signedProtoMessage.cryptoName.length());
			out.writeCharSequence(signedProtoMessage.cryptoName, StandardCharsets.UTF_8);
		}

		@Override
		public RedirectClientRequestMessage deserializeBody(ByteBuf in) throws IOException {
			int clientRequestLength = in.readInt();
			byte[] clientRequestBytes = new byte[clientRequestLength];
			in.readBytes(clientRequestBytes);
			ClientRequest clientRequest = ClientRequest.fromBytes(clientRequestBytes);
			int cryptoNameint = in.readInt();
			String cryptoName = in.readCharSequence(cryptoNameint, StandardCharsets.UTF_8).toString();
			return new RedirectClientRequestMessage(clientRequest, cryptoName);
		}
		
	};


	@Override
	public SignedMessageSerializer<? extends SignedProtoMessage> getSerializer() {
		return RedirectClientRequestMessage.serializer;
	}


	

	public ClientRequest getClientRequest() {
		return clientRequest;
	}

	public String getCryptoName() {
		return cryptoName;
	}

	@Override
	public String toString() {
		return "RedirectClientRequestMessage{" +
				"clientRequest=" + clientRequest +
				", cryptoName='" + cryptoName + '\'' +
				'}';
	}

}
