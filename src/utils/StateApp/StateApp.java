package utils.StateApp;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import app.WantOfferKeys;
import app.messages.client.replies.OperationStatusReply;
import app.messages.client.requests.Cancel;
import app.messages.client.requests.IssueOffer;
import app.messages.client.requests.IssueWant;
import app.messages.exchange.requests.Deposit;
import app.messages.exchange.requests.Withdrawal;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedProtoMessage;

// a singleton pattern to provide the state of the application to the different layers
public class StateApp {

    private HashMap<PublicKey, Float> clientAccountBalance = new HashMap<>();

	private HashMap<WantOfferKeys, List<SignedProtoMessage>> wantHashMap = new HashMap<>();
    
	private HashMap<WantOfferKeys, List<SignedProtoMessage>>  offerHashMap = new HashMap<>();

	private HashMap<UUID, SignedProtoMessage> opers_body = new HashMap<>();

	private HashMap<UUID, OperationStatusReply.Status> opers = new HashMap<>();

    private static StateApp instance = null;

    private StateApp() {
    }

    public static StateApp getInstance() {
        if (instance == null) {
            instance = new StateApp();
        }
        return instance;
    }

    public HashMap<PublicKey, Float> getClientAccountBalance() {
        return clientAccountBalance;
    }

    public void setClientAccountBalance(HashMap<PublicKey, Float> clientAccountBalance) {
        this.clientAccountBalance = clientAccountBalance;
    }

    public HashMap<WantOfferKeys, List<SignedProtoMessage>> getWantHashMap() {
        return wantHashMap;
    }

    public void setWantHashMap(HashMap<WantOfferKeys, List<SignedProtoMessage>> wantHashMap) {
        this.wantHashMap = wantHashMap;
    }

    public HashMap<WantOfferKeys, List<SignedProtoMessage>> getOfferHashMap() {
        return offerHashMap;
    }

    public void setOfferHashMap(HashMap<WantOfferKeys, List<SignedProtoMessage>> offerHashMap) {
        this.offerHashMap = offerHashMap;
    }

    public HashMap<UUID, SignedProtoMessage> getOpers_body() {
        return opers_body;
    }

    public void setOpers_body(HashMap<UUID, SignedProtoMessage> opers_body) {
        this.opers_body = opers_body;
    }

	public HashMap<UUID, OperationStatusReply.Status> getOpers() {
		return opers;
	}

	public void setOpers(HashMap<UUID, OperationStatusReply.Status> opers) {
		this.opers = opers;
	}


    // --------------------------------------------- Executions ---------------------------------------------

    private void executeOperation(SignedProtoMessage msg){
		if (msg instanceof IssueWant){
			executeIssueWant((IssueWant) msg);
		} 
		else if (msg instanceof IssueOffer){
			executeIssueOffer((IssueOffer) msg);
		}
		else if (msg instanceof Cancel){
			executeCancel((Cancel) msg);
		}
		else if (msg instanceof Deposit){
			executeDeposit((Deposit) msg);
		}
		else if (msg instanceof Withdrawal){
			executeWithdrawal((Withdrawal) msg);
		}
		else {
			throw new RuntimeException("Unknown message type: " + msg.getClass());
		}
	}

	private void executeIssueOffer(IssueOffer msg){
		WantOfferKeys tempkeys = new WantOfferKeys(msg.getQuantity(), msg.getPricePerUnit());
		if (wantHashMap.containsKey(tempkeys)){
			wantHashMap.remove(tempkeys);
		} else {
			offerHashMap.put(tempkeys, new LinkedList<>());
			offerHashMap.get(tempkeys).add(msg);
		}
	}

	private void executeIssueWant(IssueWant msg){
		WantOfferKeys tempkeys = new WantOfferKeys(msg.getQuantity(), msg.getPricePerUnit());
		if (offerHashMap.containsKey(tempkeys)){
			offerHashMap.remove(tempkeys);
		} else {
			wantHashMap.put(tempkeys, new LinkedList<>());
			wantHashMap.get(tempkeys).add(msg);
		}
	}

	private void executeCancel(Cancel msg){
		SignedProtoMessage tempmsg = opers_body.get(msg.getrID());
		if (tempmsg instanceof IssueWant){
			WantOfferKeys tempkeys = new WantOfferKeys(
				((IssueWant) tempmsg).getQuantity(), 
				((IssueWant) tempmsg).getPricePerUnit()
			);
			wantHashMap.remove(tempkeys);
		} 
		else if (tempmsg instanceof IssueOffer){
			WantOfferKeys tempkeys = new WantOfferKeys(
				((IssueOffer) tempmsg).getQuantity(), 
				((IssueOffer) tempmsg).getPricePerUnit()
			);
			offerHashMap.remove(tempkeys);
		}
		else {
            throw new RuntimeException("Unknown message type: " + tempmsg.getClass());
        }
	}

	private void executeDeposit(Deposit msg){

	}

	private void executeWithdrawal(Withdrawal msg){

	}

    /* ----------------------------------------- Operation Validation ----------------------------------- */

    public boolean isOperationValid(byte[] operation){
        // try to deserialize the operation to the different types
		ByteBuf buf = Unpooled.buffer();
		buf.writeBytes(operation);
		try {
			IssueOffer offer = new IssueOffer();
			offer = offer.getSerializer().deserialize(buf);
			return isOfferValid(offer);
		} catch (Exception e) {/*do nothing*/}
		try {
			IssueWant want = new IssueWant();
			want = want.getSerializer().deserialize(buf);
			return isWantValid(want);
		} catch (Exception e) {/*do nothing*/}
		try {
			Cancel cancel = new Cancel();
			cancel = cancel.getSerializer().deserialize(buf);
			return isCancelValid(cancel);
		} catch (Exception e) {/*do nothing*/}
		try {
			Deposit deposit = new Deposit();
			deposit = deposit.getSerializer().deserialize(buf);
			return isDepositValid(deposit);
		} catch (Exception e) {/*do nothing*/}
		try {
			Withdrawal withdrawal = new Withdrawal();
			withdrawal = withdrawal.getSerializer().deserialize(buf);
			return isWithdrawalValid(withdrawal);
		} catch (Exception e) {/*do nothing*/}
		return false;
    }

	private boolean isWithdrawalValid(Withdrawal withdrawal) {
		if (clientAccountBalance.containsKey(withdrawal.getClientID())){
			return clientAccountBalance.get(withdrawal.getClientID()) >= withdrawal.getAmount();
		} else {
			return false;
		}
	}

	private boolean isDepositValid(Deposit deposit) {
		return true;
	}

	private boolean isWantValid(IssueWant want) {
		return true;
	}

	private boolean isOfferValid(IssueOffer offer) {
		if (clientAccountBalance.containsKey(offer.getcID())){
			return clientAccountBalance.get(offer.getcID()) >= offer.getQuantity() * offer.getPricePerUnit();
		} else {
			return false;
		}
	}

	public boolean isCancelValid(Cancel cancel){
		if (opers.containsKey(cancel.getrID())){
			return opers.get(cancel.getrID()) == OperationStatusReply.Status.PENDING ||
				opers.get(cancel.getrID()) == OperationStatusReply.Status.UNKOWN;
		} else {
			return false;
		}
	}

}
