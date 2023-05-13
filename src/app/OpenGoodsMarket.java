package app;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import app.messages.client.replies.GenericClientReply;
import app.messages.client.replies.OperationStatusReply;
import app.messages.client.replies.OperationStatusReply.Status;
import app.messages.client.requests.Cancel;
import app.messages.client.requests.CheckOperationStatus;
import app.messages.client.requests.IssueOffer;
import app.messages.client.requests.IssueWant;
import app.messages.exchange.requests.Deposit;
import app.messages.exchange.requests.Withdrawal;
import blockchain.BlockChainProtocol;
import blockchain.requests.ClientRequest;
import consensus.PBFTProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import pt.unl.fct.di.novasys.babel.core.Babel;
import pt.unl.fct.di.novasys.babel.core.GenericProtocol;
import pt.unl.fct.di.novasys.babel.exceptions.HandlerRegistrationException;
import pt.unl.fct.di.novasys.babel.exceptions.InvalidParameterException;
import pt.unl.fct.di.novasys.babel.exceptions.ProtocolAlreadyExistsException;
import pt.unl.fct.di.novasys.babel.generic.signed.InvalidFormatException;
import pt.unl.fct.di.novasys.babel.generic.signed.NoSignaturePresentException;
import pt.unl.fct.di.novasys.channel.simpleclientserver.SimpleServerChannel;
import pt.unl.fct.di.novasys.channel.simpleclientserver.events.ClientDownEvent;
import pt.unl.fct.di.novasys.channel.simpleclientserver.events.ClientUpEvent;
import pt.unl.fct.di.novasys.network.data.Host;
import utils.StateApp.StateApp;

public class OpenGoodsMarket extends GenericProtocol {

    private static final Logger logger = LogManager.getLogger(OpenGoodsMarket.class);
     
	public static final String ADDRESS_KEY = "address";
	public static final String PORT_KEY = "client_port";
	public static final String SERVER_PORT_KEY = "server_port";
    
    public final static String PROTO_NAME = "OpenGoodsMarketProto";
    public final static short PROTO_ID = 500;

	private static final String EXCHANGE_KEY_STORE_PASSWORD = "ex_key_store_password";

	private static final String EXCHANGE_KEY_STORE_FILE = "ex_key_store";
    
    private int clientChannel;
	private PublicKey exchangeIdentity;
	private StateApp state;
    
    public static void main(String[] args) throws InvalidParameterException, IOException,
            HandlerRegistrationException, ProtocolAlreadyExistsException, GeneralSecurityException {
        Properties props =
                Babel.loadConfig(Arrays.copyOfRange(args, 0, args.length), "config.properties");
        logger.debug(props);
        if (props.containsKey("interface")) {
            String address = getAddress(props.getProperty("interface"));
            if (address == null) return;
            props.put(ADDRESS_KEY, address);
         }
        
        Babel babel = Babel.getInstance();

        OpenGoodsMarket opm = new OpenGoodsMarket(props);
        BlockChainProtocol bc = new BlockChainProtocol(props);
        PBFTProtocol pbft = new PBFTProtocol(props);

        babel.registerProtocol(opm);
        babel.registerProtocol(bc);
        babel.registerProtocol(pbft);
       
        opm.init(props);
        bc.init(props);
        pbft.init(props);
        
        babel.start();
        logger.info("Babel has started...");
        
        logger.info("Waiting 10s to start issuing requests.");
        
        while(true) {
        	logger.info("System is running...");
        	try {
				Thread.sleep(1000 * 60);
			} catch (InterruptedException e) {
				//Nothing to be done here...
			}
        }
        
    }

    public OpenGoodsMarket(Properties props) throws IOException, ProtocolAlreadyExistsException,
            HandlerRegistrationException, GeneralSecurityException {

    	super(OpenGoodsMarket.PROTO_NAME, OpenGoodsMarket.PROTO_ID);
    	    	
    }

    private static String getAddress(String inter) throws SocketException {
        NetworkInterface byName = NetworkInterface.getByName(inter);
        if (byName == null) {
            logger.error("No interface named " + inter);
            return null;
        }
        Enumeration<InetAddress> addresses = byName.getInetAddresses();
        InetAddress currentAddress;
        while (addresses.hasMoreElements()) {
            currentAddress = addresses.nextElement();
            if (currentAddress instanceof Inet4Address)
                return currentAddress.getHostAddress();
        }
        logger.error("No ipv4 found for interface " + inter);
        return null;
    }

	@Override
	public void init(Properties props) throws HandlerRegistrationException, IOException {
		Properties serverProps = new Properties();
		serverProps.put(SimpleServerChannel.ADDRESS_KEY, props.getProperty(ADDRESS_KEY));
		serverProps.setProperty(SimpleServerChannel.PORT_KEY, props.getProperty(SERVER_PORT_KEY));
		KeyStore exchange;
		try (FileInputStream fis = new FileInputStream(props.getProperty(EXCHANGE_KEY_STORE_FILE))) {
			try {
				exchange = KeyStore.getInstance(KeyStore.getDefaultType());
				exchange.load(fis, props.getProperty(EXCHANGE_KEY_STORE_PASSWORD).toCharArray());
				this.exchangeIdentity = exchange.getCertificate("exchange").getPublicKey();
			} catch (NoSuchAlgorithmException | CertificateException e) {
				e.printStackTrace();
			} catch (KeyStoreException e) {
				e.printStackTrace();
			}
		}
		this.state = StateApp.getInstance();

		
    	clientChannel = createChannel(SimpleServerChannel.NAME, serverProps);
    	
    	registerMessageSerializer(clientChannel, IssueOffer.MESSAGE_ID, IssueOffer.serializer);
    	registerMessageSerializer(clientChannel, IssueWant.MESSAGE_ID, IssueWant.serializer);
    	registerMessageSerializer(clientChannel, Cancel.MESSAGE_ID, Cancel.serializer);
    	registerMessageSerializer(clientChannel, CheckOperationStatus.MESSAGE_ID, CheckOperationStatus.serializer);
    	
    	registerMessageSerializer(clientChannel, Deposit.MESSAGE_ID, Deposit.serializer);
    	registerMessageSerializer(clientChannel, Withdrawal.MESSAGE_ID, Withdrawal.serializer);
    	
    	registerMessageSerializer(clientChannel, OperationStatusReply.MESSAGE_ID, OperationStatusReply.serializer);
    	registerMessageSerializer(clientChannel, GenericClientReply.MESSAGE_ID, GenericClientReply.serializer);
    	
    	registerMessageHandler(clientChannel, IssueOffer.MESSAGE_ID, this::handleIssueOfferMessage);
    	registerMessageHandler(clientChannel, IssueWant.MESSAGE_ID, this::handleIssueWantMessage);
    	registerMessageHandler(clientChannel, Cancel.MESSAGE_ID, this::handleCancelMessage);
    	registerMessageHandler(clientChannel, CheckOperationStatus.MESSAGE_ID, this::handleCheckOperationStatusMessage);
    	
    	registerMessageHandler(clientChannel, Deposit.MESSAGE_ID, this::handleDepositMessage);
    	registerMessageHandler(clientChannel, Withdrawal.MESSAGE_ID, this::handleWithdrawalMessage);
	
    	registerChannelEventHandler(clientChannel, ClientUpEvent.EVENT_ID, this::uponClientConnectionUp);
        registerChannelEventHandler(clientChannel, ClientDownEvent.EVENT_ID, this::uponClientConnectionDown);
   	}
	
	public void handleIssueOfferMessage(IssueOffer io, Host from, short sourceProto, int channelID ) {
		// logger.info("Received IssueOffer (" + io.getRid() + " from " + from + "(" + io.getcID().toString() + ")");

		// check if the signature is valid
		try {
			if (io.checkSignature(io.getcID())){
				state.putOpers(io.getRid(), OperationStatusReply.Status.PENDING);
			} else {
				state.putOpers(io.getRid(), OperationStatusReply.Status.REJECTED);
				return;
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException | InvalidFormatException
				| NoSignaturePresentException e) {
			e.printStackTrace();
		}

		this.state.getOpers_body().put(io.getRid(), io);
		
		GenericClientReply ack = new GenericClientReply(io.getRid());
		
		sendMessage(clientChannel, ack, sourceProto, from, 0);

		ByteBuf buf = Unpooled.buffer();
		try {
			io.getSerializer().serializeBody(io, buf);
			ClientRequest cr = new ClientRequest(io.getRid(), buf.array());
			sendRequest(cr, BlockChainProtocol.PROTO_ID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void handleIssueWantMessage(IssueWant iw, Host from, short sourceProto, int channelID ) {
		// logger.info("Received IssueWant (" + iw.getRid() + " from " + from + "(" + iw.getcID().toString() + ")");
		
		// Verify the signature of the message
		try {
			if (iw.checkSignature(iw.getcID())){
				state.putOpers(iw.getRid(), OperationStatusReply.Status.PENDING);
			} else {
				state.putOpers(iw.getRid(), OperationStatusReply.Status.REJECTED);
				return;
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException | InvalidFormatException
				| NoSignaturePresentException e) {
			e.printStackTrace();
		}
		
		this.state.getOpers_body().put(iw.getRid(), iw);
		
		GenericClientReply ack = new GenericClientReply(iw.getRid());
		sendMessage(clientChannel, ack, sourceProto, from, 0);

		ByteBuf buf = Unpooled.buffer();
		try {
			iw.getSerializer().serializeBody(iw, buf);
			ClientRequest cr = new ClientRequest(iw.getRid(), buf.array());
			sendRequest(cr, BlockChainProtocol.PROTO_ID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void handleCancelMessage(Cancel c, Host from, short sourceProto, int channelID ) {
		// logger.info("Received Cancel for operation " + c.getrID() + " from " + from);
		
		try {
			if (!c.checkSignature(c.getcID())){
				logger.warn ("Received a cancel with an invalid signature");
				return;
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException | InvalidFormatException
				| NoSignaturePresentException e) {
			e.printStackTrace();
		}

		// check if the operation exists
		if(state.getOpers().containsKey(c.getrID())) {
			this.state.getOpers_body().remove(c.getrID());
		} else {
			logger.error("Received a cancel for an operation that does not exist");
			state.changeOpers(c.getrID(), OperationStatusReply.Status.REJECTED);
		}

		GenericClientReply ack = new GenericClientReply(c.getrID());
		sendMessage(clientChannel, ack, sourceProto, from, 0);

		ByteBuf buf = Unpooled.buffer();
		try {
			c.getSerializer().serializeBody(c, buf);
			ClientRequest cr = new ClientRequest(c.getrID(), buf.array());
			sendRequest(cr, BlockChainProtocol.PROTO_ID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void handleCheckOperationStatusMessage(CheckOperationStatus cos, Host from, short sourceProto, int channelID) {
		// logger.info("Received CheckOperation for operation " + cos.getrID() + " from " + from);
		
		OperationStatusReply osr = null;
		
		Status s = state.getOpers().get(cos.getrID());
		
		if(s != null) {
			switch (s) {
			case CANCELLED:
				osr = new OperationStatusReply(cos.getrID(), Status.CANCELLED);
				break;
			case EXECUTED:
				osr = new OperationStatusReply(cos.getrID(), Status.EXECUTED);
				break;
			case FAILED:
				osr = new OperationStatusReply(cos.getrID(), Status.FAILED);
				break;
			case PENDING:
				osr = new OperationStatusReply(cos.getrID(), Status.PENDING);
				break;
			case REJECTED:
				osr = new OperationStatusReply(cos.getrID(), Status.REJECTED);
				break;
			default:
				osr = new OperationStatusReply(cos.getrID(), Status.UNKOWN);
				break;
			
			}
		} else {
			osr = new OperationStatusReply(cos.getrID(), Status.UNKOWN);
		}
		
		if(osr != null) {
			sendMessage(clientChannel, osr, sourceProto, from, 0);
		}
	}
	
	public void handleDepositMessage(Deposit d, Host from, short sourceProto, int channelID) {
		// logger.info("Received deposit of " + d.getAmount() + " to " + d.getClientID().toString() + " from the Exchange (" + from + ")");
		try {
			if (d.checkSignature(exchangeIdentity)) {
				state.putOpers(d.getRid(), OperationStatusReply.Status.PENDING);
				this.state.getOpers_body().put(d.getRid(), d);
				// logger.info("Received deposit of " + d.getAmount() + " to " + d.getClientID().toString() + " from the Exchange (" + from + ")");
			} else {
				// The deposit is not valid
				state.putOpers(d.getRid(), OperationStatusReply.Status.REJECTED);
				return;
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException | InvalidFormatException
				| NoSignaturePresentException e) {
			e.printStackTrace();
		}
		
		GenericClientReply ack = new GenericClientReply(d.getRid());
		sendMessage(clientChannel, ack, sourceProto, from, 0);

		ByteBuf buf = Unpooled.buffer();
		try {
			d.getSerializer().serializeBody(d, buf);
			ClientRequest cr = new ClientRequest(d.getRid(), buf.array());
			sendRequest(cr, BlockChainProtocol.PROTO_ID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void handleWithdrawalMessage(Withdrawal w, Host from, short sourceProto, int channelID) {
		// logger.info("Received withdrawal of " + w.getAmount() + " to " + w.getClientID().toString() + " from the Exchange (" + from + ")");
		try {
			if (w.checkSignature(exchangeIdentity)) {
				state.putOpers(w.getRid(), OperationStatusReply.Status.PENDING);
				logger.info("Received deposit of " + w.getAmount() + " to " + w.getClientID().toString() + " from the Exchange (" + from + ")");
			} else {
				//The widthdrawal is not valid
				state.putOpers(w.getRid(), OperationStatusReply.Status.REJECTED);
				return;
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException | InvalidFormatException
				| NoSignaturePresentException e) {
			e.printStackTrace();
		}
		this.state.getOpers_body().put(w.getRid(), w);
		
		GenericClientReply ack = new GenericClientReply(w.getRid());
		sendMessage(clientChannel, ack, sourceProto, from, 0);

		ByteBuf buf = Unpooled.buffer();
		try {
			w.getSerializer().serializeBody(w, buf);
			ClientRequest cr = new ClientRequest(w.getRid(), buf.array());
			sendRequest(cr, BlockChainProtocol.PROTO_ID);
		} catch (IOException e) {
			e.printStackTrace();
		};
	}
	
	private void uponClientConnectionUp(ClientUpEvent event, int channel) {
        logger.debug(event);
    }

    private void uponClientConnectionDown(ClientDownEvent event, int channel) {
        logger.warn(event);
    }

}
