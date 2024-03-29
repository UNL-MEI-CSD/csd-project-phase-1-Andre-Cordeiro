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
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Properties;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import consensus.messages.CommitMessage;
import consensus.messages.PrePrepareMessage;
import consensus.messages.PrepareMessage;
import consensus.notifications.CommittedNotification;
import consensus.notifications.InitialNotification;
import consensus.notifications.ViewChange;
import consensus.requests.ProposeRequest;
import consensus.requests.SuspectLeader;
import consensus.timers.ReconnectTimer;
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
import utils.SignaturesHelper;
import utils.View;
import utils.MessageBatch.MessageBatch;
import utils.MessageBatch.MessageBatchKey;
import utils.Operation.OpsMap;
import utils.Operation.OpsMapKey;


public class PBFTProtocol extends GenericProtocol {

	public static final String PROTO_NAME = "pbft";
	public static final short PROTO_ID = 100;
	
	public static final String ADDRESS_KEY = "address";
	public static final String PORT_KEY = "base_port";
	public static final String INITIAL_MEMBERSHIP_KEY = "initial_membership";

	public static final String RECONNECT_TIME_KEY = "reconnect_time";
	public static final String LEADER_TIMEOUT_KEY = "leader_timeout";

	private static final Logger logger = LogManager.getLogger(PBFTProtocol.class);

	// Timers
	private final int RECONNECT_TIME;

	// Crypto
	private String cryptoName;
	private KeyStore truststore;
	private PrivateKey privKey;
	public PublicKey pubKey;

	//Leadership
	private int currentSeqN;
	private int highestSeqN;
	
	//View
	private Host self;
	private View view;
	private int failureNumber;

	//State
	private OpsMap opsMap;
	private MessageBatch mb;


	
	public PBFTProtocol(Properties props) throws NumberFormatException, UnknownHostException {
		super(PBFTProtocol.PROTO_NAME, PBFTProtocol.PROTO_ID);

		self = new Host(InetAddress.getByName(props.getProperty(ADDRESS_KEY)),
				Integer.parseInt(props.getProperty(PORT_KEY)));

		this.RECONNECT_TIME = Integer.parseInt(props.getProperty(RECONNECT_TIME_KEY));
		
		
		opsMap = new OpsMap();
		this.view = new View(0);
		String[] membership = props.getProperty(INITIAL_MEMBERSHIP_KEY).split(",");
		for (String s : membership) {
			String[] tokens = s.split(":");
			this.view.addMember(new Host(InetAddress.getByName(tokens[0]), Integer.parseInt(tokens[1])));
		}

		failureNumber = (view.size() - 1) / 3;

		currentSeqN = 0;
		highestSeqN = currentSeqN;

		mb = new MessageBatch();
		opsMap = new OpsMap();
		
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
			e.printStackTrace();
		}

		Properties peerProps = new Properties();
		peerProps.put(MultithreadedTCPChannel.ADDRESS_KEY, props.getProperty(ADDRESS_KEY));
		peerProps.setProperty(TCPChannel.PORT_KEY, props.getProperty(PORT_KEY));
		int peerChannel = createChannel(TCPChannel.NAME, peerProps);
		
		
		// Connection Events Handlers
		registerChannelEventHandler(peerChannel, InConnectionDown.EVENT_ID, this::uponInConnectionDown);
        registerChannelEventHandler(peerChannel, InConnectionUp.EVENT_ID, this::uponInConnectionUp);
        registerChannelEventHandler(peerChannel, OutConnectionDown.EVENT_ID, this::uponOutConnectionDown);
        registerChannelEventHandler(peerChannel, OutConnectionUp.EVENT_ID, this::uponOutConnectionUp);
        registerChannelEventHandler(peerChannel, OutConnectionFailed.EVENT_ID, this::uponOutConnectionFailed);

		// Request Handlers
		registerRequestHandler( ProposeRequest.REQUEST_ID, this::uponProposeRequest);
		registerRequestHandler( SuspectLeader.REQUEST_ID, this::uponSuspectLeader);


		// Message Handlers
		registerMessageHandler(peerChannel, PrePrepareMessage.MESSAGE_ID, this::uponPrePrepareMessage, this::uponMessageFailed);
        registerMessageHandler(peerChannel, PrepareMessage.MESSAGE_ID, this::uponPrepareMessage, this::uponMessageFailed);
		registerMessageHandler(peerChannel, CommitMessage.MESSAGE_ID, this::uponCommitMessage, this::uponMessageFailed);
		
		// Message Serializers
		registerMessageSerializer(peerChannel, PrePrepareMessage.MESSAGE_ID, PrePrepareMessage.serializer);
		registerMessageSerializer(peerChannel, PrepareMessage.MESSAGE_ID, PrepareMessage.serializer);
		registerMessageSerializer(peerChannel, CommitMessage.MESSAGE_ID, CommitMessage.serializer);
		
		// Timer Handlers
        registerTimerHandler(ReconnectTimer.TIMER_ID, this::onReconnectTimer);


		logger.info("Standing by to extablish connections (10s)");
		
		try { Thread.sleep(10 * 1000); } catch (InterruptedException e) { }
		
		view.getView().forEach(this::openConnection);

		triggerNotification(new InitialNotification(self, peerChannel));
		
		//Installing first view
		triggerNotification(new ViewChange(view, currentSeqN));
	}

	/* --------------------------------------- Timer Handlers ----------------------------------- */

	private void onReconnectTimer(ReconnectTimer timer, long timerId) {
		openConnection(timer.getHost());
	}

	// --------------------------------------- Connection Manager Handlers -----------------------------
	
	private void uponOutConnectionUp(OutConnectionUp event, int channel) {
        logger.info(event);
    }

    private void uponOutConnectionDown(OutConnectionDown event, int channel) {
        logger.warn(event);
		setupTimer(new ReconnectTimer(event.getNode()), RECONNECT_TIME);
    }

    private void uponOutConnectionFailed(OutConnectionFailed<ProtoMessage> ev, int ch) {
    	logger.warn(ev); 
    	setupTimer(new ReconnectTimer(ev.getNode()), RECONNECT_TIME);
    }

    private void uponInConnectionUp(InConnectionUp event, int channel) {
        logger.info(event);
    }

    private void uponInConnectionDown(InConnectionDown event, int channel) {
        logger.warn(event);
    }


	/*
	 * ------------------------------------------------------------------------------------------------------
	 * 											Request Handlers
	 * ------------------------------------------------------------------------------------------------------
	 */
	
	/* --------------------------------------- Propose Request Handler ----------------------------------- */
	
    private void uponProposeRequest(ProposeRequest req, int channel) {

		logger.info("Received propose request: " + req);

		//check if the signature is valid
		try {
			if (!SignaturesHelper.checkSignature(req.getBlock(), req.getSignature(), pubKey)){
				logger.warn("Request received :" + req + "has an invalid signature");
				return;
			}
		} catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		//check if the node is the leader
		if (view.isLeader(self)){

			logger.info("Received propose request: " + req.hashCode());
			OpsMapKey opsMapKey = new OpsMapKey(req.getTimestamp(), req.hashCode());
			if (opsMap.containsOp(opsMapKey.hashCode())){
				logger.warn("Request received :" + req + "is a duplicate");
				return;
			}

			this.currentSeqN++;
			int operationHash = opsMapKey.hashCode();
			MessageBatchKey mbKey = new MessageBatchKey(operationHash, currentSeqN, view.getViewNumber());
			logger.info("Proposing request: " + currentSeqN);
			PrePrepareMessage prePrepareMsg = new PrePrepareMessage(mbKey,req.getBlock(),cryptoName);

			try {
				prePrepareMsg.signMessage(privKey);
			} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException
					| InvalidSerializerException e) {
				e.printStackTrace();
			}
			

			view.getView().forEach(node -> {	
				if (!node.equals(self)){
					sendMessage(prePrepareMsg, node);
				} else {
					mb.addMessage(mbKey.hashCode());
					opsMap.addOp(mbKey.hashCode(), req.getBlock());
				}
			});
		}
		else {
			logger.warn("Request received :" + req + "without being leader"); 
		}


	}


	/* --------------------------------------- Suspect Leader Request Handler ----------------------------------- */

	private void uponSuspectLeader(SuspectLeader req, int channel) {

		//check if the signature is valid
		try {
			if (!SignaturesHelper.checkSignature(req.getPendingRequestID().toString().getBytes(), req.getSignature(), pubKey)){
				logger.warn("Request received :" + req + "has an invalid signature");
				return;
			}
		} catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		// make a standard agreement but with the operation of suspect leader
		currentSeqN++;	
		//Create a normal agreement for the suspect leader with skiping the pre-prepare phase
		if (opsMap.containsOp( req.hashCode())){
			logger.warn("Request received :" + req + "is a duplicate");
			return;
		} else {
			opsMap.addOp( req.hashCode(), req.getPendingRequestID().toString().getBytes());
			logger.info("Suspect leader request added to the opsMap " + req.getPendingRequestID().toString());
		}
		int operationHash =  req.hashCode();
		MessageBatchKey mbKey = new MessageBatchKey(operationHash, currentSeqN, view.getViewNumber());
		PrepareMessage prepareMsg = new PrepareMessage(mbKey,cryptoName);

		//sign the message
		try {
			prepareMsg.signMessage(privKey);
		} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException
				| InvalidSerializerException e) {
			e.printStackTrace();
		}

		//send the message to all the nodes
		view.getView().forEach(node -> {	
			if (!node.equals(self)){
				sendMessage(prepareMsg, node);
			} else {
				mb.addPrepareMessage(mbKey.hashCode(), self);
			}
		});
		

		
	}

	/*
	 * ------------------------------------------------------------------------------------------------------
	 * 											Message Handlers
	 * ------------------------------------------------------------------------------------------------------
	 */

	// ---------------------- PrePrepare Message Handlers -----------------------------

	private void uponPrePrepareMessage(PrePrepareMessage msg, Host from, short sourceProto, int channel){
		
		if (msg.getBatchKey().getViewNumber() != view.getViewNumber() || msg.getBatchKey().getSeqN() < highestSeqN){
			if (msg.getBatchKey().getViewNumber() != view.getViewNumber())
				logger.warn("Received a pre-prepare message with an invalid view number: " + msg + " from " + from);
			else {
				logger.warn("Received a pre-prepare message with an invalid sequence number: " + msg + " from " + from);
			}
			return;
		}

		if(checkValidMessage(msg,from)){
			try {
				opsMap.addOp(msg.getBatchKey().hashCode(), msg.getBlock());
				logger.info("Pre-prepare message added to the opsMap " + msg.getBatchKey().hashCode());
				mb.addMessage(msg.getBatchKey().hashCode());
			}
			catch (RuntimeException e){
				logger.warn("Received a duplicate pre-prepare message: " + msg +" "+ mb.size()  + " " + e.getMessage() + " from " + from);
				
				// TODO: handle exception 

				// debug code
				// logger.warn("Keys in the mb: " + msg.getBatchKey().hashCode());
				// for (int key : mb.getKeys()){
				// 	logger.warn("Key: " + key);	
				// }
				// return;
			}
			
			//send a prepare message to all nodes in the view
			PrepareMessage prepareMsg = new PrepareMessage( msg.getBatchKey(), cryptoName );
			try {
				prepareMsg.signMessage(privKey);
			} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException
					| InvalidSerializerException e) {
				e.printStackTrace();
			}

			view.getView().forEach(node -> {
				if (!node.equals(self)){
					sendMessage(prepareMsg, node);
				} else {
					mb.addPrepareMessage(msg.getBatchKey().hashCode(), self);
				}
			});
		}
	}

	// --------------------------- Prepare Message ---------------------------

	private void uponPrepareMessage(PrepareMessage msg, Host from, short sourceProto, int channel){

		logger.info("Received a prepare message: " + msg + " from " + from);

		if (msg.getBatchKey().getViewNumber() != view.getViewNumber() || msg.getBatchKey().getSeqN() < highestSeqN){
			if (msg.getBatchKey().getViewNumber() != view.getViewNumber())
				logger.warn("Received a prepare message with an invalid view number: " + msg + " from " + from);
			else {
				logger.warn("Received a prepare message with an invalid sequence number: " + msg + " from " + from);
			}
			return;
		}
		if (checkValidMessage(msg,from)){

			int prepareMessagesReceived = 0;
			try {
				int hash = msg.getBatchKey().hashCode();
				prepareMessagesReceived = mb.addPrepareMessage(hash, from);
			} catch (RuntimeException e){
				logger.warn("Received a unknown prepare message: " + msg + mb.getValues(msg.getBatchKey().hashCode()));
				return;
			}
			if (prepareMessagesReceived >= 2 * failureNumber + 1) {
				CommitMessage commitMsg = new CommitMessage(msg.getBatchKey(), cryptoName);
				try {
					commitMsg.signMessage(privKey);
				} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException
						| InvalidSerializerException e) {
					e.printStackTrace();
				}
				view.getView().forEach(node -> {	
					if (!node.equals(self)){
						sendMessage(commitMsg, node);
					} else {
						mb.addCommitMessage(msg.getBatchKey().hashCode(), self);
					}
				});
			}
		}
	}

	// -----------------------Commit Message -----------------------------

	private void uponCommitMessage(CommitMessage msg, Host from, short sourceProto, int channel){

		logger.info("Received a commit message: " + msg + " from " + from);

		if (msg.getBatchKey().getViewNumber() != view.getViewNumber() || msg.getBatchKey().getSeqN() < highestSeqN){
			if (msg.getBatchKey().getViewNumber() != view.getViewNumber()) {
				logger.warn("Received a commit message with an invalid view number: " + msg + " from " + from);
			}
			return;
		}	

		if (checkValidMessage(msg, from)) {

			if (msg.getBatchKey().getSeqN() < highestSeqN) {
				logger.warn("Received a commit message for a lower sequence number: " + msg);
				return;
			} 
			
			int commitMessagesReceived = 0;
			int hash = msg.getBatchKey().hashCode();
			if (mb.containsMessage(hash)) {
				commitMessagesReceived = mb.addCommitMessage(hash, from);
			}
			if (commitMessagesReceived == failureNumber + 1) {

				newHighestSeqN(msg.getBatchKey().getSeqN());
				byte[] block = opsMap.getOp(msg.getBatchKey().hashCode());

				try {
					UUID.fromString(new String(block));
					// send a view change notification to the blockchain
					view = new View(view.getView(), view.getViewNumber()+1);
					ViewChange viewChange = new ViewChange(this.view, currentSeqN);
					triggerNotification(viewChange);
				}
				catch (IllegalArgumentException e){

					try {
						CommittedNotification commitNotificationMsg = new CommittedNotification(block, SignaturesHelper.generateSignature(block, privKey));
						triggerNotification(commitNotificationMsg);
					} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException f) {
						logger.error("Error signing committed notification message: " + e.getMessage());
						e.printStackTrace();
					}

				}
				// discard the message and the op
				mb.removeMessage(hash);
				opsMap.removeOp(msg.getBatchKey().hashCode());
			}
		}
	}

	// ------------------------- Failure handling ------------------------- //

	private void uponMessageFailed(ProtoMessage msg, Host from, short sourceProto, int channel){
		logger.warn("Failed to deliver message " + msg + " from " + from);
	}

	// ------------------------- Validation functions ------------------------- //

	private boolean checkValidMessage(Object msgObj,Host from){
		boolean check;
		if(msgObj instanceof PrePrepareMessage){
			PrePrepareMessage msg = (PrePrepareMessage) msgObj;
			try{
				check = msg.checkSignature(truststore.getCertificate(msg.getCryptoName()).getPublicKey());
			}
			catch(InvalidFormatException | NoSignaturePresentException | NoSuchAlgorithmException | InvalidKeyException |
			SignatureException | KeyStoreException e){
				logger.error("Error checking signature in " + msg.getClass() + " from " + from + ": " + e.getMessage());
				return false;
			}
		}
		else if(msgObj instanceof PrepareMessage){
			PrepareMessage msg = (PrepareMessage) msgObj;
			try{
				check = msg.checkSignature(truststore.getCertificate(msg.getCryptoName()).getPublicKey());
			}
			catch(InvalidFormatException | NoSignaturePresentException | NoSuchAlgorithmException | InvalidKeyException |
			SignatureException | KeyStoreException e){
				logger.error("Error checking signature in " + msg.getClass() + " from " + from + ": " + e.getMessage());
				return false;
			}
		} else if (msgObj instanceof CommitMessage){
			CommitMessage msg = (CommitMessage) msgObj;
			try{
				check = msg.checkSignature(truststore.getCertificate(msg.getCryptoName()).getPublicKey());
			}
			catch(InvalidFormatException | NoSignaturePresentException | NoSuchAlgorithmException | InvalidKeyException |
			SignatureException | KeyStoreException e){
				logger.error("Error checking signature in " + msg.getClass() + " from " + from + ": " + e.getMessage());
				return false;
			}
		} 
		else {
			logger.error("Unknown message type: " + msgObj.getClass());
			throw new IllegalArgumentException("Message is not valid");
		}
		return check;
	}


	// ------------------------- Auxiliary functions ------------------------- //

	private void newHighestSeqN(int seqN){
		highestSeqN = seqN;
	}
}
