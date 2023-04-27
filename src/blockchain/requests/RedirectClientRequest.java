package blockchain.requests;

import pt.unl.fct.di.novasys.babel.generic.ProtoRequest;

public class RedirectClientRequest extends ProtoRequest {

	public final static short REQUEST_ID = 207;

	private final ClientRequest clientRequest;
	
	public RedirectClientRequest(ClientRequest clientRequest) {
		super(RedirectClientRequest.REQUEST_ID);
		this.clientRequest = clientRequest;
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
