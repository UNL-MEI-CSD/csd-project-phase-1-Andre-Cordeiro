package blockchain;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
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

import app.messages.client.requests.Cancel;
import app.messages.client.requests.IssueOffer;
import app.messages.client.requests.IssueWant;
import app.messages.exchange.requests.Deposit;
import app.messages.exchange.requests.Withdrawal;
import blockchain.blockchain.Block;
import blockchain.blockchain.BlockChain;
import blockchain.messages.ClientRequestUnhandledMessage;
import blockchain.messages.RedirectClientRequestMessage;
import blockchain.messages.StartClientRequestSuspectMessage;
import blockchain.requests.ClientRequest;
import blockchain.timers.CheckUnhandledRequestsPeriodicTimer;
import blockchain.timers.LeaderSuspectTimer;
import consensus.PBFTProtocol;
import consensus.notifications.CommittedNotification;
import consensus.notifications.InitialNotification;
import consensus.notifications.ViewChange;
import consensus.requests.ProposeRequest;
import consensus.requests.SuspectLeader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import pt.unl.fct.di.novasys.babel.core.GenericProtocol;
import pt.unl.fct.di.novasys.babel.exceptions.HandlerRegistrationException;
import pt.unl.fct.di.novasys.babel.generic.ProtoMessage;
import pt.unl.fct.di.novasys.babel.generic.signed.InvalidFormatException;
import pt.unl.fct.di.novasys.babel.generic.signed.InvalidSerializerException;
import pt.unl.fct.di.novasys.babel.generic.signed.NoSignaturePresentException;
import pt.unl.fct.di.novasys.network.data.Host;
import utils.Crypto;
import utils.SignaturesHelper;
import utils.View;
import utils.StateApp.StateApp;

public class BlockChainProtocol extends GenericProtocol {

	private static final String PROTO_NAME = "blockchain";
	public static final short PROTO_ID = 200;

	public static final String ADDRESS_KEY = "address";
	public static final String PORT_KEY = "base_port";
	public static final String INITIAL_MEMBERSHIP_KEY = "initial_membership";

	public static final String PERIOD_CHECK_REQUESTS = "check_requests_timeout";
	public static final String SUSPECT_LEADER_TIMEOUT = "leader_timeout";

	private static final Logger logger = LogManager.getLogger(BlockChainProtocol.class);

	// Crypto
	private String cryptoName;
	private KeyStore truststore;
	private PrivateKey key;

	// Timers
	private final long checkRequestsPeriod;
	private final long leaderTimeout;

	// State
	private boolean waitingForViewChange;
	private Host self;
	private int viewNumber;
	private View view;
	private boolean leader;
	private int f;
	// a HashMap to store the timers for the pending requests
	private Map<UUID, Long> pendingRequestsTimers;
	// a HashMap to count the host that have send a unhandled request
	private Map<UUID, List<Host>> unhandledRequestsMessages;

	// State of the app
	private StateApp stateApp;

	// BlockChain
	private BlockChain blockChain;
	private int lastBlockNumber; // sequence number equivalent
	private List<byte[]> pendingOperations;
	
	public BlockChainProtocol(Properties props) throws NumberFormatException, UnknownHostException {
		super(BlockChainProtocol.PROTO_NAME, BlockChainProtocol.PROTO_ID);

		// Probably the following informations could be provided by a notification
		// emitted by the PBFTProtocol
		// (this should not be interpreted as the unique or canonical solution)
		// self = new Host(InetAddress.getByName(props.getProperty(ADDRESS_KEY)),
		// Integer.parseInt(props.getProperty(PORT_KEY)));

		viewNumber = 0;
		view = new View(viewNumber);
		waitingForViewChange = false;
		initializeBlockChain();
		this.stateApp = StateApp.getInstance();

		// Read timers and timeouts configurations
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

		// init state
		this.leader = false;
		this.unhandledRequestsMessages = new HashMap<>();
		this.pendingRequestsTimers = new HashMap<>();

		// Request Handlers
		registerRequestHandler(ClientRequest.REQUEST_ID, this::handleClientRequest);

		// Timer Handlers
		registerTimerHandler(CheckUnhandledRequestsPeriodicTimer.TIMER_ID,
				this::handleCheckUnhandledRequestsPeriodicTimer);
		registerTimerHandler(LeaderSuspectTimer.TIMER_ID, this::handleLeaderSuspectTimer);

		// Notification Handlers
		subscribeNotification(ViewChange.NOTIFICATION_ID, this::handleViewChangeNotification);
		subscribeNotification(CommittedNotification.NOTIFICATION_ID, this::handleCommittedNotification);
		subscribeNotification(InitialNotification.NOTIFICATION_ID, this::handleInitialNotification);

		// setupPeriodicTimer(new CheckUnhandledRequestsPeriodicTimer(),
		// checkRequestsPeriod, checkRequestsPeriod);
	}


	/* ----------------------------------------------- ------------- ------------------------------------------ */
	/* ------------------------------------------ Block Creation function ------------------------------------- */
	/* ----------------------------------------------- ------------- ------------------------------------------ */
	
	private void forgeBlock() {

		ByteBuf buffer = Unpooled.buffer();
		for (byte[] operation : pendingOperations) {
			buffer.writeShort(operation.length);
			buffer.writeBytes(operation);
		}
		byte[] operationsBytes = buffer.array();
		pendingOperations.clear();
		// Create a new block
		byte[] signature;
		try {
			signature = SignaturesHelper.generateSignature(operationsBytes, key);
			Block block = new Block(self, blockChain.getLastBlock().hashCode(), lastBlockNumber + 1, signature,
					operationsBytes);
			// generate the propose request
			byte[] blockBytes = serializeBlock(block);
			byte[] blockSignature = SignaturesHelper.generateSignature(blockBytes, key);
			ProposeRequest proposeRequest = new ProposeRequest(blockBytes, blockSignature);
			// send the propose request to the PBFTProtocol
			sendRequest(proposeRequest, PBFTProtocol.PROTO_ID);
		} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
			e.printStackTrace();
		}
	}

	private void addOperationToBlock(byte[] operation) {

		// First check if the operation is valid
		if (stateApp.isOperationValid(operation)) {
			// then add it to the list of next operations
			// logger.info("Adding operation to the block : " + operationToString(operation));
			pendingOperations.add(operation);
			if (pendingOperations.size() == 10) {
				forgeBlock();
			}
		} else {
			logger.info("Operation is not valid");
		}

	}

	/* ----------------------------------------------- ------------- ------------------------------------------ */
	/* ------------------------------------------- Blockchain Function ---------------------------------------- */
	/* ----------------------------------------------- ------------- ------------------------------------------ */

	private void initializeBlockChain() {

		// Create the blockChain
		blockChain = new BlockChain();

		pendingOperations = new LinkedList<>();
		lastBlockNumber = 0;

	}
	/* ----------------------------------------------- ------------- ------------------------------------------ */
    /* ---------------------------------------------- REQUEST HANDLER ----------------------------------------- */
    /* ----------------------------------------------- ------------- ------------------------------------------ */
    
	public void handleClientRequest(ClientRequest req, short protoID) {

		if (waitingForViewChange) {
			if (this.leader)
				logger.info("Received a ClientRequeest with id: " + req.getRequestId());
			return;
		}

		if (this.leader) {

			addOperationToBlock(req.getOperation());
		} else {
			// Redirect the request to the leader
			RedirectClientRequestMessage msg = new RedirectClientRequestMessage(req, cryptoName);
			// sign the message
			try {
				msg.signMessage(key);
			} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException
					| InvalidSerializerException e) {
				e.printStackTrace();
				System.exit(1); // Catastrophic failure!!!
			}
			sendMessage(msg, this.view.getLeader());
		}
		// Start a timer for this request
		CheckUnhandledRequestsPeriodicTimer timer = new CheckUnhandledRequestsPeriodicTimer(req.getRequestId());
		pendingRequestsTimers.put(req.getRequestId(), setupTimer(timer, checkRequestsPeriod));
	}

	/* ----------------------------------------------- ------------- ------------------------------------------ */
    /* ------------------------------------------- NOTIFICATION HANDLER --------------------------------------- */
    /* ----------------------------------------------- ------------- ------------------------------------------ */
    
	public void handleViewChangeNotification(ViewChange vc, short from) {
		logger.info("New view received (" + vc.getViewNumber() + ")");

		// TODO: Should maybe validate this ViewChange :)

		this.viewNumber = vc.getViewNumber();
		this.view = new View(vc.getView(), vc.getViewNumber());
		this.f = (this.view.getView().size() - 1) / 3;

		this.leader = this.view.getLeader().equals(this.self);
		if (this.leader)
			logger.info("I am the leader: " + this.leader);
		// handle all unhandled blocks
		UUID[] reqIds = unhandledRequestsMessages.keySet().toArray(new UUID[0]);
		for (UUID reqId : reqIds) {
			handleUnhandledRequest(reqId);
		}
		// reset unhandledMessages
		unhandledRequestsMessages.clear();
		logger.info("View change completed");
		waitingForViewChange = false;
	}

	public void handleCommittedNotification(CommittedNotification cn, short from) {

		if (waitingForViewChange) {
			return;
		}
		logger.info("Received a commit notification with id: " + cn + " from: " + from);

		// Deserialize the block to cut timers
		Block block = deserializeBlock(cn.getBlock());

		ByteBuf buffer = Unpooled.copiedBuffer(block.getOperations());
		// cancel the timer for each request and execute the request
		for (int i = 0; i < 10; i++) {
			Short operationByteSize = buffer.readShort();
			byte[] operation = new byte[operationByteSize];
			buffer.readBytes(operation);
			stateApp.executeOperation(operation);
			cancelAllTimersForRequest(operation);
		}

		// add the block to the blockchain
		blockChain.addBlock(block);
		// update the last block number
		lastBlockNumber++;

	}

	public void handleInitialNotification(InitialNotification in, short from) {

		if (waitingForViewChange) {
			return;
		}

		this.self = in.getSelf();

		int peerChannel = in.getPeerChannel();
		registerSharedChannel(peerChannel);

		// Message Handlers
		try {
			registerMessageHandler(peerChannel, ClientRequestUnhandledMessage.MESSAGE_ID,
					this::handleClientRequestUnhandledMessage, this::uponMessageFailed);
			registerMessageHandler(peerChannel, RedirectClientRequestMessage.MESSAGE_ID,
					this::handleRedirectClientRequestMessage, this::uponMessageFailed);
			registerMessageHandler(peerChannel, StartClientRequestSuspectMessage.MESSAGE_ID,
					this::handleStartClientRequestSuspectMessage, this::uponMessageFailed);
		} catch (HandlerRegistrationException e) {
			e.printStackTrace();
		}

		// Message Serializers
		registerMessageSerializer(peerChannel, ClientRequestUnhandledMessage.MESSAGE_ID,
				ClientRequestUnhandledMessage.serializer);
		registerMessageSerializer(peerChannel, RedirectClientRequestMessage.MESSAGE_ID,
				RedirectClientRequestMessage.serializer);
		registerMessageSerializer(peerChannel, StartClientRequestSuspectMessage.MESSAGE_ID,
				StartClientRequestSuspectMessage.serializer);

	}

	/* ----------------------------------------------- ------------- ------------------------------------------ */
    /* ---------------------------------------------- MESSAGE HANDLER ----------------------------------------- */
    /* ----------------------------------------------- ------------- ------------------------------------------ */

	// ------------------------------------------ RedirectClientRequestMessage ------------------------------------------*/

	private void handleRedirectClientRequestMessage(RedirectClientRequestMessage msg, Host from, short sourceProto,
			int channel) {
		if (!checkValidMessage(msg, from)) {
			return;
		}
		if (this.leader) {

			try {
				addOperationToBlock(msg.getClientRequest().getOperation());
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1); // Catastrophic failure!!!
			}
		} else {
			logger.warn("Received a RedirectClientRequestMessage without being the leader");
		}

	}

	// ------------------------------------------ ClientRequestUnhandledMessage ------------------------------------------*/

	private void handleClientRequestUnhandledMessage(ClientRequestUnhandledMessage msg, Host from, short sourceProto,
			int channel) {

		if (!checkValidMessage(msg, from)) {
			return;
		}

		this.unhandledRequestsMessages.put(msg.getPendingRequestID(), new LinkedList<Host>());
		this.unhandledRequestsMessages.get(msg.getPendingRequestID()).add(self);
		// remove the timer for this request
		if (pendingRequestsTimers.containsKey(msg.getPendingRequestID())) {
			cancelTimer(pendingRequestsTimers.get(msg.getPendingRequestID()));
			pendingRequestsTimers.remove(msg.getPendingRequestID());
			LeaderSuspectTimer timer = new LeaderSuspectTimer(msg.getPendingRequestID(), this.viewNumber);
			this.pendingRequestsTimers.put(msg.getPendingRequestID(), setupTimer(timer, leaderTimeout));
		}
		// build the message
		StartClientRequestSuspectMessage startMsg = new StartClientRequestSuspectMessage(msg.getPendingRequestID(),
				cryptoName);
		// sign the message
		try {
			startMsg.signMessage(key);
		} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException | InvalidSerializerException e) {
			e.printStackTrace();
			System.exit(1); // Catastrophic failure!!!
		}
		// send it to everyone
		view.getView().forEach(host -> {
			if (!host.equals(this.self)) {
				sendMessage(startMsg, host);
			}
		});
	}

	// ----------------------------------------- StartClientRequestSuspectMessage ------------------------------------------*/

	private void handleStartClientRequestSuspectMessage(StartClientRequestSuspectMessage msg, Host from,
			short sourceProto, int channel) {

		if (!checkValidMessage(msg, from)) {
			return;
		}

		if (this.unhandledRequestsMessages.containsKey(msg.getPendingRequestID())) {
			this.unhandledRequestsMessages.get(msg.getPendingRequestID()).add(from);
			// logger.info("Received " +
			// this.unhandledRequestsMessages.get(msg.getPendingRequestID()).size() + "
			// StartClientRequestSuspectMessage with id: " + msg.getPendingRequestID());
			if (this.unhandledRequestsMessages.get(msg.getPendingRequestID()).size() >= 2 * this.f + 1) {
				// start a leader suspect timer
				LeaderSuspectTimer timer = new LeaderSuspectTimer(msg.getPendingRequestID(), this.viewNumber);
				// add the timer to the list
				this.pendingRequestsTimers.put(msg.getPendingRequestID(), setupTimer(timer, leaderTimeout));
			}
		} else {
			this.unhandledRequestsMessages.put(msg.getPendingRequestID(), new LinkedList<Host>());
			this.unhandledRequestsMessages.get(msg.getPendingRequestID()).add(from);
		}
	}

	// ----------------------------------------------- Fail message handler -----------------------------------------*/

	
	private void uponMessageFailed(ProtoMessage msg, Host from, short sourceProto, int channel) {
		logger.warn("Failed to deliver message " + msg + " from " + from);
	}


	/* ----------------------------------------------- ------------- ------------------------------------------ */
    /* ----------------------------------------------- TIMER HANDLER ------------------------------------------ */
    /* ----------------------------------------------- ------------- ------------------------------------------ */
    
	public void handleCheckUnhandledRequestsPeriodicTimer(CheckUnhandledRequestsPeriodicTimer t, long timerId) {

		cancelTimer(timerId);
		pendingRequestsTimers.remove(t.getPendingRequestID());

		if (waitingForViewChange) {
			return;
		}

		ClientRequestUnhandledMessage msg = new ClientRequestUnhandledMessage(t.getPendingRequestID(), cryptoName);

		// sign the message
		try {
			msg.signMessage(key);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1); // Catastrophic failure!!!
		}

		if (!this.unhandledRequestsMessages.containsKey(t.getPendingRequestID())) {
			// send it to everyone
			view.getView().forEach(host -> {
				if (!host.equals(self)) {
					sendMessage(msg, host);
				} else {
					handleClientRequestUnhandledMessage(msg, self, (short) 0, 0);
				}
			});
		} else {
			// cancel the timer
			cancelTimer(timerId);
		}
	}

	public void handleLeaderSuspectTimer(LeaderSuspectTimer t, long timerId) {

		cancelTimer(timerId);
		pendingRequestsTimers.remove(t.getRequestID());

		if (waitingForViewChange) {
			return;
		}

		if (!stateApp.isOperationPending(t.getRequestID())) {
			logger.info("Fin de timer avec" + stateApp.getOperationStatus(t.getRequestID()));
			return;
		}
		// Send a StartViewChange message to his PBFT protocol
		logger.info("Leader suspect timer expired for request " + t.getRequestID());
		this.unhandledRequestsMessages.remove(t.getRequestID());

		if (this.viewNumber > t.getViewNumber()) {
			return;
		}

		byte[] signature;
		try {
			signature = SignaturesHelper.generateSignature(t.getRequestID().toString().getBytes(), this.key);
			SuspectLeader suspectLeader = new SuspectLeader(t.getRequestID(), signature);
			sendRequest(suspectLeader, PBFTProtocol.PROTO_ID);
			waitingForViewChange = true;
		} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
			e.printStackTrace();
		}

	}

	/* ----------------------------------------------- ------------- ------------------------------------------ */
	/* ---------------------------------------------- VERIFY FUNCTION ----------------------------------------- */
	/* ----------------------------------------------- ------------- ------------------------------------------ */

	private boolean checkValidMessage(Object msgObj, Host from) {
		boolean check = false;
		if (msgObj instanceof ClientRequestUnhandledMessage) {
			ClientRequestUnhandledMessage msg = (ClientRequestUnhandledMessage) msgObj;
			try {
				check = msg.checkSignature(truststore.getCertificate(msg.getCryptoName()).getPublicKey());
			} catch (InvalidFormatException | NoSignaturePresentException | NoSuchAlgorithmException
					| InvalidKeyException | SignatureException | KeyStoreException e) {
				logger.error("Error checking signature in " + msg.getClass() + " from " + from + ": " + e.getMessage());
				return false;
			}
		} else if (msgObj instanceof RedirectClientRequestMessage) {
			RedirectClientRequestMessage msg = (RedirectClientRequestMessage) msgObj;
			try {
				check = msg.checkSignature(truststore.getCertificate(msg.getCryptoName()).getPublicKey());
			} catch (InvalidFormatException | NoSignaturePresentException | NoSuchAlgorithmException
					| InvalidKeyException | SignatureException | KeyStoreException e) {
				logger.error("Error checking signature in " + msg.getClass() + " from " + from + ": " + e.getMessage());
				return false;
			}
		} else if (msgObj instanceof StartClientRequestSuspectMessage) {
			StartClientRequestSuspectMessage msg = (StartClientRequestSuspectMessage) msgObj;
			try {
				check = msg.checkSignature(truststore.getCertificate(msg.getCryptoName()).getPublicKey());
			} catch (InvalidFormatException | NoSignaturePresentException | NoSuchAlgorithmException
					| InvalidKeyException | SignatureException | KeyStoreException e) {
				logger.error("Error checking signature in " + msg.getClass() + " from " + from + ": " + e.getMessage());
				return false;
			}
		} else {
			logger.error("Unknown message type: " + msgObj.getClass());
			throw new IllegalArgumentException("Message is not valid");
		}
		return check;
	}

	/* ----------------------------------------------- ------------- ------------------------------------------ */
    /* ----------------------------------------------- APP INTERFACE ------------------------------------------ */
    /* ----------------------------------------------- ------------- ------------------------------------------ */
    public void submitClientOperation(byte[] b) {
    	sendRequest(new ClientRequest(b), BlockChainProtocol.PROTO_ID);
    }

	/* ----------------------------------------------- ------------- ------------------------------------------ */
	/* ----------------------------------------------- UTIL FUNCTION ------------------------------------------ */
	/* ----------------------------------------------- ------------- ------------------------------------------ */

	private void handleUnhandledRequest(UUID reqId) {
		if (this.pendingRequestsTimers.get(reqId) != null)
			cancelTimer(this.pendingRequestsTimers.get(reqId));
		this.pendingRequestsTimers.remove(reqId);
		CheckUnhandledRequestsPeriodicTimer timer = new CheckUnhandledRequestsPeriodicTimer(reqId);
		this.pendingRequestsTimers.put(reqId, setupTimer(timer, checkRequestsPeriod));
	}

	public byte[] serializeBlock(Block block) {
		ByteBuf bufValidator = Unpooled.buffer();
		try {
			Host.serializer.serialize(block.getValidater(),bufValidator);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ByteBuf buf = Unpooled.buffer();
		buf.writeShort(bufValidator.readableBytes());
		buf.writeBytes(bufValidator);
		buf.writeInt(block.getHashPreviousBlock());
		buf.writeInt(block.getBlockNumber());
		buf.writeInt(block.getSignature().length);
		buf.writeBytes(block.getSignature());
		buf.writeInt(block.getOperations().length);
		buf.writeBytes(block.getOperations());
		return buf.array();
	}

	private Block deserializeBlock(byte[] block) {
		// convert to bytebuf
		ByteBuf buf = Unpooled.copiedBuffer(block);
		// read the block
		int validaterLength = buf.readShort();
		ByteBuf validater = buf.readBytes(validaterLength);
		Host validaterHost;
		try {
			validaterHost = Host.serializer.deserialize(validater);
			int hashPreviousBlock = buf.readInt();
			int blockNumber = buf.readInt();
			int signatureLength = buf.readInt();
			byte[] signature = new byte[signatureLength];
			buf.readBytes(signature);
			int operationsLength = buf.readInt();
			byte[] operations = new byte[operationsLength];
			buf.readBytes(operations);
			return new Block(validaterHost, hashPreviousBlock, blockNumber, signature, operations);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Error deserializing block");
		}
		return null;
	}

	private void cancelAllTimersForRequest(byte[] operation) {
		ByteBuf buf = Unpooled.copiedBuffer(operation);
		try {
			Deposit deposit = new Deposit();
			deposit = deposit.getSerializer().deserializeBody(buf);
			if (this.pendingRequestsTimers.get(deposit.getRid()) != null)
				cancelTimer(this.pendingRequestsTimers.get(deposit.getRid()));
			if (this.unhandledRequestsMessages.get(deposit.getRid()) != null)
				this.unhandledRequestsMessages.remove(deposit.getRid());
		} catch (Exception e) {/*do nothing*/}
		try {
			Withdrawal withdrawal = new Withdrawal();
			withdrawal = withdrawal.getSerializer().deserializeBody(buf);
			if (this.pendingRequestsTimers.get(withdrawal.getRid()) != null)
				cancelTimer(this.pendingRequestsTimers.get(withdrawal.getRid()));
			if (this.unhandledRequestsMessages.get(withdrawal.getRid()) != null)
				this.unhandledRequestsMessages.remove(withdrawal.getRid());
		} catch (Exception e) {/*do nothing*/}
		try {
			IssueOffer offer = new IssueOffer();
			offer = offer.getSerializer().deserializeBody(buf);
			if (this.pendingRequestsTimers.get(offer.getRid()) != null)
				cancelTimer(this.pendingRequestsTimers.get(offer.getRid()));
			if (this.unhandledRequestsMessages.get(offer.getRid()) != null)
				this.unhandledRequestsMessages.remove(offer.getRid());
		} catch (Exception e) {/*do nothing*/}
		try {
			IssueWant want = new IssueWant();
			want = want.getSerializer().deserializeBody(buf);
			if (this.pendingRequestsTimers.get(want.getRid()) != null)
				cancelTimer(this.pendingRequestsTimers.get(want.getRid()));
			if (this.unhandledRequestsMessages.get(want.getRid()) != null)
				this.unhandledRequestsMessages.remove(want.getRid());
		} catch (Exception e) {/*do nothing*/}
		try {
			Cancel cancel = new Cancel();
			cancel = cancel.getSerializer().deserializeBody(buf);
			if (this.pendingRequestsTimers.get(cancel.getrID()) != null)
				cancelTimer(this.pendingRequestsTimers.get(cancel.getrID()));
			if (this.unhandledRequestsMessages.get(cancel.getrID()) != null)
				this.unhandledRequestsMessages.remove(cancel.getrID());
		} catch (Exception e) {/*do nothing*/}
	}

	/* ----------------------------------------------- ------------- ------------------------------------------ */
	/* ----------------------------------------------- DEBUG FUNCTION ----------------------------------------- */
	/* ----------------------------------------------- ------------- ------------------------------------------ */


	private String operationToString(byte[] op){
		ByteBuf buf = Unpooled.copiedBuffer(op);
		try {
			Deposit deposit = new Deposit();
			deposit = deposit.getSerializer().deserializeBody(buf);
			return deposit.toString();
		} catch (Exception e) {/*do nothing*/}
		try {
			Withdrawal withdrawal = new Withdrawal();
			withdrawal = withdrawal.getSerializer().deserializeBody(buf);
			return withdrawal.toString();
		} catch (Exception e) {/*do nothing*/}
		try {
			IssueOffer offer = new IssueOffer();
			offer = offer.getSerializer().deserializeBody(buf);
			return offer.toString();
		} catch (Exception e) {/*do nothing*/}
		try {
			IssueWant want = new IssueWant();
			want = want.getSerializer().deserializeBody(buf);
			return want.toString();
		} catch (Exception e) {/*do nothing*/}
		try {
			Cancel cancel = new Cancel();
			cancel = cancel.getSerializer().deserializeBody(buf);
			return cancel.toString();
		} catch (Exception e) {/*do nothing*/}
		return "Unknown operation";
	}
}
