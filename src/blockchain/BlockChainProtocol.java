package blockchain;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import blockchain.messages.ClientRequestUnhandledMessage;
import blockchain.messages.RedirectClientRequestMessage;
import blockchain.requests.ClientRequest;
import blockchain.timers.CheckUnhandledRequestsPeriodicTimer;
import blockchain.timers.LeaderSuspectTimer;
import consensus.PBFTProtocol;
import consensus.notifications.CommittedNotification;
import consensus.notifications.InitialNotification;
import consensus.notifications.ViewChange;
import consensus.requests.ProposeRequest;
import pt.unl.fct.di.novasys.babel.core.GenericProtocol;
import pt.unl.fct.di.novasys.babel.exceptions.HandlerRegistrationException;
import pt.unl.fct.di.novasys.babel.generic.ProtoMessage;
import pt.unl.fct.di.novasys.network.data.Host;
import utils.Crypto;
import utils.SignaturesHelper;
import utils.View;

public class BlockChainProtocol extends GenericProtocol {

	private static final String PROTO_NAME = "blockchain";
	public static final short PROTO_ID = 200;
	
	public static final String ADDRESS_KEY = "address";
	public static final String PORT_KEY = "base_port";
	public static final String INITIAL_MEMBERSHIP_KEY = "initial_membership";
	
	public static final String PERIOD_CHECK_REQUESTS = "check_requests_timeout";
	public static final String SUSPECT_LEADER_TIMEOUT = "leader_timeout";
	
	private static final Logger logger = LogManager.getLogger(BlockChainProtocol.class);
	
	//Crypto
	private String cryptoName;
	private KeyStore truststore;
	private PrivateKey key;

	//Timers
	private final long checkRequestsPeriod;
	private final long leaderTimeout;
 	
	//State
	private Host self;
	private int viewNumber;
	private View view;
	private boolean leader;
	private int f;
	// a HashMap to store the timers for the pending requests
	private Map<UUID, Long> pendingRequestsTimers;
	// a HashMap to count the host that have send a unhandled request
	private Map<UUID, List<Host>> unhandledRequestsMessages;
	

	
	public BlockChainProtocol(Properties props) throws NumberFormatException, UnknownHostException {
		super(BlockChainProtocol.PROTO_NAME, BlockChainProtocol.PROTO_ID);

		//Probably the following informations could be provided by a notification
		//emitted by the PBFTProtocol
		//(this should not be interpreted as the unique or canonical solution)
		// self = new Host(InetAddress.getByName(props.getProperty(ADDRESS_KEY)),
		// 		Integer.parseInt(props.getProperty(PORT_KEY)));
		
		viewNumber = 0;
		view = new View(viewNumber);
		
		//Read timers and timeouts configurations
		checkRequestsPeriod = Long.parseLong(props.getProperty(PERIOD_CHECK_REQUESTS));
		leaderTimeout = Long.parseLong(props.getProperty(SUSPECT_LEADER_TIMEOUT));
	}

	@Override
	public void init(Properties props) throws HandlerRegistrationException, IOException {
		try {
			cryptoName = props.getProperty(Crypto.CRYPTO_NAME_KEY);
			truststore = Crypto.getTruststore(props);
			key = Crypto.getPrivateKey(cryptoName, props);
		} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException | CertificateException
				| IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//init state
		this.leader = false;
		this.unhandledRequestsMessages = new HashMap<>();
		this.pendingRequestsTimers = new HashMap<>();

		
		// Request Handlers
		registerRequestHandler(ClientRequest.REQUEST_ID, this::handleClientRequest);
		
		// Timer Handlers
		registerTimerHandler(CheckUnhandledRequestsPeriodicTimer.TIMER_ID, this::handleCheckUnhandledRequestsPeriodicTimer);
		registerTimerHandler(LeaderSuspectTimer.TIMER_ID, this::handleLeaderSuspectTimer);
		
		// Notification Handlers
		subscribeNotification(ViewChange.NOTIFICATION_ID, this::handleViewChangeNotification);
		subscribeNotification(CommittedNotification.NOTIFICATION_ID, this::handleCommittedNotification);
		subscribeNotification(InitialNotification.NOTIFICATION_ID, this::handleInitialNotification);

		// setupPeriodicTimer(new CheckUnhandledRequestsPeriodicTimer(), checkRequestsPeriod, checkRequestsPeriod);
	}
	
	
	/* ----------------------------------------------- ------------- ------------------------------------------ */
    /* ---------------------------------------------- REQUEST HANDLER ----------------------------------------- */
    /* ----------------------------------------------- ------------- ------------------------------------------ */
    
	public void handleClientRequest(ClientRequest req, short protoID) {
		
		if(this.leader) {
			
			try {
			logger.info("Received a ClientRequeest with id: " + req.getRequestId());	
				//TODO: This is a super over simplification we will handle latter
				//Only one block should be submitted for agreement at a time
				//Also this assumes that a block only contains a single client request
				byte[] request = req.generateByteRepresentation();
				byte[] signature = SignaturesHelper.generateSignature(request, this.key);
				
				sendRequest(new ProposeRequest(request, signature), PBFTProtocol.PROTO_ID);
				
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1); //Catastrophic failure!!!
			}
		} else {
			//Redirect the request to the leader
			RedirectClientRequestMessage msg = new RedirectClientRequestMessage(req);
			sendMessage(msg, this.view.getLeader());
		}
		//Start a timer for this request
		CheckUnhandledRequestsPeriodicTimer timer = new CheckUnhandledRequestsPeriodicTimer(req.getRequestId());
		pendingRequestsTimers.put(req.getRequestId(), setupTimer(timer, checkRequestsPeriod));
	}
	
	/* ----------------------------------------------- ------------- ------------------------------------------ */
    /* ------------------------------------------- NOTIFICATION HANDLER --------------------------------------- */
    /* ----------------------------------------------- ------------- ------------------------------------------ */
    
	public void handleViewChangeNotification(ViewChange vc, short from) {
		logger.info("New view received (" + vc.getViewNumber() + ")");
		
		//TODO: Should maybe validate this ViewChange :)

		this.viewNumber = vc.getViewNumber();
		this.view = new View(vc.getView(), vc.getViewNumber());
		this.f = (this.view.getView().size() - 1)/3;
		
		this.leader = this.view.getLeader().equals(this.self);
		
	}
	
	public void handleCommittedNotification(CommittedNotification cn, short from) {
		//TODO: write this handler
		logger.info("Received a commit notification with id: " + cn + " from: " + from);

		// get the client request from the block
		ClientRequest req = ClientRequest.fromBytes(cn.getBlock());
		// cancel the timer for this request
		if (pendingRequestsTimers.containsKey(req.getRequestId())) {
			cancelTimer(pendingRequestsTimers.get(req.getRequestId()));
			pendingRequestsTimers.remove(req.getRequestId());
		}
		// remove the list of unhandled requests
		if (unhandledRequestsMessages.containsKey(req.getRequestId())) {
			unhandledRequestsMessages.remove(req.getRequestId());
		}
		
	}

	public void handleInitialNotification(InitialNotification in, short from) {

		this.self = in.getSelf();
		
		int peerChannel = in.getPeerChannel();
		registerSharedChannel(peerChannel);

		// Message Handlers
		try {
			registerMessageHandler(peerChannel, ClientRequestUnhandledMessage.MESSAGE_ID, this::handleClientRequestUnhandledMessage, this::uponMessageFailed);
			registerMessageHandler(peerChannel, RedirectClientRequestMessage.MESSAGE_ID, this::handleRedirectClientRequestMessage, this::uponMessageFailed);
		} catch (HandlerRegistrationException e) {
			e.printStackTrace();
		}

		// Message Serializers
		registerMessageSerializer(peerChannel, ClientRequestUnhandledMessage.MESSAGE_ID, ClientRequestUnhandledMessage.serializer);
		registerMessageSerializer(peerChannel, RedirectClientRequestMessage.MESSAGE_ID, RedirectClientRequestMessage.serializer);
		
	}
		
	/* ----------------------------------------------- ------------- ------------------------------------------ */
    /* ---------------------------------------------- MESSAGE HANDLER ----------------------------------------- */
    /* ----------------------------------------------- ------------- ------------------------------------------ */

	// ------------------------------------------ RedirectClientRequestMessage ------------------------------------------*/

	private void handleRedirectClientRequestMessage(RedirectClientRequestMessage msg, Host from, short sourceProto, int channel) {
		if(this.leader) {
			
			try {
				logger.info("Received a RedirectClientRequestMessage with id: " + msg.getClientRequest().getRequestId() + " from: " + from);
				
				byte[] request = msg.getClientRequest().generateByteRepresentation();
				byte[] signature = SignaturesHelper.generateSignature(request, this.key);
				
				sendRequest(new ProposeRequest(request, signature), PBFTProtocol.PROTO_ID);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1); //Catastrophic failure!!!
			}
		} else {
			logger.warn("Received a RedirectClientRequestMessage without being the leader");
		}
	}
    
	// ------------------------------------------ ClientRequestUnhandledMessage ------------------------------------------*/

	private void handleClientRequestUnhandledMessage(ClientRequestUnhandledMessage msg, Host from, short sourceProto, int channel) {
		
		if (this.pendingRequestsTimers.containsKey(msg.getPendingRequestID())) {
			// Stop the timer for this request
			cancelTimer(this.pendingRequestsTimers.get(msg.getPendingRequestID()));
			this.pendingRequestsTimers.remove(msg.getPendingRequestID());
		} else {
			logger.warn("Received a ClientRequestUnhandledMessage for a request that is not pending from: " + from);
		}

		// Put the host in the list of hosts that have sent a ClientRequestUnhandledMessage
		if (!this.unhandledRequestsMessages.containsKey(msg.getPendingRequestID())) {
			this.unhandledRequestsMessages.put(msg.getPendingRequestID(), new LinkedList<Host>());
			this.unhandledRequestsMessages.get(msg.getPendingRequestID()).add(from);
		} else {
			if (!this.unhandledRequestsMessages.get(msg.getPendingRequestID()).contains(from)) {
				this.unhandledRequestsMessages.get(msg.getPendingRequestID()).add(from);
				if (this.unhandledRequestsMessages.get(msg.getPendingRequestID()).size() == 2 * this.f + 1) {

					//Clear the list of hosts that have sent a ClientRequestUnhandledMessage
					this.unhandledRequestsMessages.remove(msg.getPendingRequestID());

					// We have received enough ClientRequestUnhandledMessage to assume that the Leader had it
					// We can now start a last chance timer
					LeaderSuspectTimer timer = new LeaderSuspectTimer(msg.getPendingRequestID());
					//Put the timer in the hashmap of timers
					this.pendingRequestsTimers.put(msg.getPendingRequestID(), setupTimer(timer, leaderTimeout));
					
				}
			} else {
				logger.warn("Received a ClientRequestUnhandledMessage from a host that has already sent one");
			}
		}
	}

	// ----------------------------------------------- Fail message handler -----------------------------------------*/

	
	private void uponMessageFailed(ProtoMessage msg, Host from, short sourceProto, int channel){
		logger.warn("Failed to deliver message " + msg + " from " + from);
	}


	/* ----------------------------------------------- ------------- ------------------------------------------ */
    /* ----------------------------------------------- TIMER HANDLER ------------------------------------------ */
    /* ----------------------------------------------- ------------- ------------------------------------------ */
    
	public void handleCheckUnhandledRequestsPeriodicTimer(CheckUnhandledRequestsPeriodicTimer t, long timerId) {


		ClientRequestUnhandledMessage msg = new ClientRequestUnhandledMessage(t.getPendingRequestID());	

		//sign the message
		try {
			msg.signMessage(key);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1); //Catastrophic failure!!!
		}

		//Send the message to all replicas
		view.getView().forEach(node -> {
			if (!node.equals(self)){
				sendMessage(msg, node);
			} else {
				handleClientRequestUnhandledMessage(msg, self, (short) 0, 0);
			}
		});
	}
	
	public void handleLeaderSuspectTimer(LeaderSuspectTimer t, long timerId) {

		//Send a StartViewChange message to his PBFT protocol
		logger.info("Leader suspect timer expired for request " + t.getRequestID());

	}
	
	/* ----------------------------------------------- ------------- ------------------------------------------ */
    /* ----------------------------------------------- APP INTERFACE ------------------------------------------ */
    /* ----------------------------------------------- ------------- ------------------------------------------ */
    public void submitClientOperation(byte[] b) {
    	sendRequest(new ClientRequest(b), BlockChainProtocol.PROTO_ID);
    }

}
