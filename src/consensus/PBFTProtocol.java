package consensus;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.Timestamp;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import consensus.messages.CommitMessage;
import consensus.messages.PrePrepareMessage;
import consensus.messages.PrepareMessage;
import consensus.notifications.ViewChange;
import consensus.requests.ProposeRequest;
import io.netty.buffer.ByteBuf;
import static io.netty.buffer.Unpooled.*;
import pt.unl.fct.di.novasys.babel.core.GenericProtocol;
import pt.unl.fct.di.novasys.babel.exceptions.HandlerRegistrationException;
import pt.unl.fct.di.novasys.babel.generic.ProtoMessage;
import pt.unl.fct.di.novasys.babel.generic.signed.InvalidFormatException;
import pt.unl.fct.di.novasys.babel.generic.signed.InvalidSerializerException;
import pt.unl.fct.di.novasys.babel.generic.signed.NoSignaturePresentException;
import pt.unl.fct.di.novasys.channel.tcp.MultithreadedTCPChannel;
import pt.unl.fct.di.novasys.channel.tcp.TCPChannel;
import pt.unl.fct.di.novasys.channel.tcp.events.InConnectionDown;
import pt.unl.fct.di.novasys.channel.tcp.events.InConnectionUp;
import pt.unl.fct.di.novasys.channel.tcp.events.OutConnectionDown;
import pt.unl.fct.di.novasys.channel.tcp.events.OutConnectionFailed;
import pt.unl.fct.di.novasys.channel.tcp.events.OutConnectionUp;
import pt.unl.fct.di.novasys.network.data.Host;
import utils.Crypto;
import utils.SeqN;
import utils.SignaturesHelper;
import utils.Operation.OpsMap;
import utils.MessageIdentifier;


public class PBFTProtocol extends GenericProtocol {

	public static final String PROTO_NAME = "pbft";
	public static final short PROTO_ID = 100;
	
	public static final String ADDRESS_KEY = "address";
	public static final String PORT_KEY = "base_port";
	public static final String INITIAL_MEMBERSHIP_KEY = "initial_membership";

	private static final Logger logger = LogManager.getLogger(PBFTProtocol.class);
	
	private String cryptoName;
	private KeyStore truststore;
	private PrivateKey privKey;
	public PublicKey pubKey;

	//Leadership
	private SeqN currentSeqN;
	private boolean iAmCurrentLeader;
	
	//TODO: add protocol state (related with the internal operation of the view)
	private Host self;
	private int viewNumber;
	private final List<Host> view;
	private final int seq;
	
	private OpsMap opsMap;
	private int prepareMessagesReceived;
	private int f;

	
	public PBFTProtocol(Properties props) throws NumberFormatException, UnknownHostException {
		super(PBFTProtocol.PROTO_NAME, PBFTProtocol.PROTO_ID);
	
		this.seq = 0;

		self = new Host(InetAddress.getByName(props.getProperty(ADDRESS_KEY)),
				Integer.parseInt(props.getProperty(PORT_KEY)));
		
		viewNumber = 1;
		prepareMessagesReceived = 0;
		
		opsMap = new OpsMap();
		
		view = new LinkedList<>();
		String[] membership = props.getProperty(INITIAL_MEMBERSHIP_KEY).split(",");
		for (String s : membership) {
			String[] tokens = s.split(":");
			view.add(new Host(InetAddress.getByName(tokens[0]), Integer.parseInt(tokens[1])));
		}
		f = (view.size() - 1) / 3;
		currentSeqN = new SeqN(0, view.get(0));
		iAmCurrentLeader = self.equals(view.get(0));
		
	}

	@Override
	public void init(Properties props) throws HandlerRegistrationException, IOException {
		try {
			cryptoName = props.getProperty(Crypto.CRYPTO_NAME_KEY);
			truststore = Crypto.getTruststore(props);
			privKey = Crypto.getPrivateKey(cryptoName, props);
			pubKey = Crypto.getPublicKey(cryptoName, props);
		} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException | CertificateException
				| IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Properties peerProps = new Properties();
		peerProps.put(MultithreadedTCPChannel.ADDRESS_KEY, props.getProperty(ADDRESS_KEY));
		peerProps.setProperty(TCPChannel.PORT_KEY, props.getProperty(PORT_KEY));
		int peerChannel = createChannel(TCPChannel.NAME, peerProps);
		
		// TODO: Must add handlers for requests and messages and register message serializers
		
		registerChannelEventHandler(peerChannel, InConnectionDown.EVENT_ID, this::uponInConnectionDown);
        registerChannelEventHandler(peerChannel, InConnectionUp.EVENT_ID, this::uponInConnectionUp);
        registerChannelEventHandler(peerChannel, OutConnectionDown.EVENT_ID, this::uponOutConnectionDown);
        registerChannelEventHandler(peerChannel, OutConnectionUp.EVENT_ID, this::uponOutConnectionUp);
        registerChannelEventHandler(peerChannel, OutConnectionFailed.EVENT_ID, this::uponOutConnectionFailed);

		// Request Handlers
		registerRequestHandler( ProposeRequest.REQUEST_ID, this::uponProposeRequest);


		// Message Handlers
		registerMessageHandler(peerChannel, PrePrepareMessage.MESSAGE_ID, this::uponPrePrepareMessage, this::uponMessageFailed);
        registerMessageHandler(peerChannel, PrepareMessage.MESSAGE_ID, this::uponPrepareMessage, this::uponMessageFailed);
		registerMessageHandler(peerChannel, CommitMessage.MESSAGE_ID, this::uponCommitMessage, this::uponMessageFailed);

		// Message Serializers
		registerMessageSerializer(peerChannel, PrePrepareMessage.MESSAGE_ID, PrePrepareMessage.serializer);
		registerMessageSerializer(peerChannel, PrepareMessage.MESSAGE_ID, PrepareMessage.serializer);
		registerMessageSerializer(peerChannel, CommitMessage.MESSAGE_ID, CommitMessage.serializer);


		logger.info("Standing by to extablish connections (10s)");
		
		try { Thread.sleep(10 * 1000); } catch (InterruptedException e) { }
		
		// TODO: Open connections to all nodes in the (initial) view
		view.forEach(this::openConnection);
		
		//Installing first view
		triggerNotification(new ViewChange(view, viewNumber));
	}
	
	//TODO: Add event (messages, requests, timers, notifications) handlers of the protocol
	
	/* --------------------------------------- Connection Manager Functions ----------------------------------- */
	
    private void uponProposeRequest(ProposeRequest req, int channel) {
		logger.info("Received propose request: " + req);
		//check if the node is the leader
		if (currentSeqN.getNode().equals(self)){

			if (opsMap.containsOp(req.getTimestamp())){
				logger.warn("Request received :" + req + "is a duplicate");
				return;
			}

			int operationHash = req.hashCode();
			PrePrepareMessage prePrepareMsg = new PrePrepareMessage(viewNumber, currentSeqN, operationHash,cryptoName);

			try {
				prePrepareMsg.signMessage(privKey);
			} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException
					| InvalidSerializerException e) {
				e.printStackTrace();
			}
			opsMap.addOp(req.getTimestamp(), operationHash);

			view.forEach(node -> {	
				if (!node.equals(self)){
					sendMessage(prePrepareMsg, node);
				}
			});
		}
		else {
			logger.warn("Request received :" + req + "without being leader"); 
		}


	}
	
	private void uponOutConnectionUp(OutConnectionUp event, int channel) {
        logger.info(event);
    }

    private void uponOutConnectionDown(OutConnectionDown event, int channel) {
        logger.warn(event);
    }

    private void uponOutConnectionFailed(OutConnectionFailed<ProtoMessage> ev, int ch) {
    	logger.warn(ev); 
    	openConnection(ev.getNode());
    }

    private void uponInConnectionUp(InConnectionUp event, int channel) {
        logger.info(event);
    }

    private void uponInConnectionDown(InConnectionDown event, int channel) {
        logger.warn(event);
    }

	private void uponPrePrepareMessage(PrePrepareMessage msg, Host from, short sourceProto, int channel){
		
		logger.info("Received pre-prepare message: " + msg );
		if (msg.getViewNumber() == viewNumber){
			
			if(checkValidMessage(msg,from)){

				logger.info("Signed ");
				//TODO: add the message to the batch

				//send a prepare message to all nodes in the view
				PrepareMessage prepareMsg = new PrepareMessage(viewNumber, currentSeqN, msg.getOp(), 0);

				view.forEach(node -> {
					if (!node.equals(self)){
						sendMessage(prepareMsg, node);
					}
				});

			}

		} else {
			logger.warn("Received pre-prepare message with wrong view number: " + msg);
		}
	}

	private void uponPrepareMessage(PrepareMessage msg, Host from, short sourceProto, int channel){
		// wait for 2f+1 prepare messages with the same view number and sequence number

		if (msg.getViewNumber() == viewNumber){

			if (msg.getSequenceNumber().equals(currentSeqN)){
				
				//TODO: check if the message is valid (not a duplicate and well signed)
				prepareMessagesReceived++;
				if (prepareMessagesReceived == 2 * f + 1){
					//TODO: send a commit message to all nodes in the view
					logger.info("Sending commit message");
				}
			}
		} else {
			logger.warn("Received prepare message with wrong view number: " + msg);
		}
	}

	private void uponCommitMessage(CommitMessage msg, Host from, short sourceProto, int channel){
		//TODO
	}

	private void uponMessageFailed(ProtoMessage msg, Host from, short sourceProto, int channel){
		//TODO
	}


	private boolean checkValidMessage(Object msgObj,Host from){
		if(msgObj instanceof PrePrepareMessage){
			PrePrepareMessage msg = (PrePrepareMessage) msgObj;
			boolean check;
			try{
				check = msg.checkSignature(truststore.getCertificate(msg.cryptoName).getPublicKey());
			}
			catch(InvalidFormatException | NoSignaturePresentException | NoSuchAlgorithmException | InvalidKeyException |
			SignatureException | KeyStoreException e){
				logger.error("Error checking signature in " + msg.getClass() + " from " + from + ": " + e.getMessage());
				return false;
			}
			return check;
		}
		else
			throw new IllegalArgumentException("Message is not valid");
	}
		
}
