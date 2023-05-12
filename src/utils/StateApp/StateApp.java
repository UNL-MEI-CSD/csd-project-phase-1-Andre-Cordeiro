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

	private HashMap<WantOfferKeys, List<IssueWant>> wantHashMap = new HashMap<>();
    
	private HashMap<WantOfferKeys, List<IssueOffer>>  offerHashMap = new HashMap<>();

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

    public HashMap<WantOfferKeys, List<IssueWant>> getWantHashMap() {
        return wantHashMap;
    }

    public void setWantHashMap(HashMap<WantOfferKeys, List<IssueWant>> wantHashMap) {
        this.wantHashMap = wantHashMap;
    }

    public HashMap<WantOfferKeys, List<IssueOffer>> getOfferHashMap() {
        return offerHashMap;
    }

    public void setOfferHashMap(HashMap<WantOfferKeys, List<IssueOffer>> offerHashMap) {
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

    public void executeOperation(byte[] op){
		ByteBuf buf = Unpooled.copiedBuffer(op);
		try {
			Deposit deposit = new Deposit();
			deposit = deposit.getSerializer().deserializeBody(buf);
			executeDeposit(deposit);
		} catch (Exception e) {/*do nothing*/}
		try {
			Withdrawal withdrawal = new Withdrawal();
			withdrawal = withdrawal.getSerializer().deserializeBody(buf);
			executeWithdrawal(withdrawal);
		} catch (Exception e) {/*do nothing*/}
		try {
			IssueOffer offer = new IssueOffer();
			offer = offer.getSerializer().deserializeBody(buf);
			executeIssueOffer(offer);
		} catch (Exception e) {/*do nothing*/}
		try {
			IssueWant want = new IssueWant();
			want = want.getSerializer().deserializeBody(buf);
			executeIssueWant(want);
		} catch (Exception e) {/*do nothing*/}
		try {
			Cancel cancel = new Cancel();
			cancel = cancel.getSerializer().deserializeBody(buf);
			executeCancel(cancel);
		} catch (Exception e) {/*do nothing*/}
	}

	private void executeIssueOffer(IssueOffer msg){
		WantOfferKeys tempkeys = new WantOfferKeys(msg.getResourceType(),msg.getQuantity(), msg.getPricePerUnit());
		if (wantHashMap.containsKey(tempkeys)){
			//remove the money from the buyer
			clientAccountBalance.put(wantHashMap.get(tempkeys).get(0).getcID(), clientAccountBalance.get(wantHashMap.get(tempkeys).get(0).getcID()) - msg.getQuantity() * msg.getPricePerUnit());
			//add the money to the seller
			clientAccountBalance.put(msg.getcID(), clientAccountBalance.get(msg.getcID()) + msg.getQuantity() * msg.getPricePerUnit());
			wantHashMap.remove(tempkeys);
		} else {
			offerHashMap.put(tempkeys, new LinkedList<>());
			offerHashMap.get(tempkeys).add(msg);
		}
	}

	private void executeIssueWant(IssueWant msg){
		WantOfferKeys tempkeys = new WantOfferKeys(msg.getResourceType(),msg.getQuantity(), msg.getPricePerUnit());
		if (offerHashMap.containsKey(tempkeys)){
			//remove the money from the buyer 
			clientAccountBalance.put(msg.getcID(), clientAccountBalance.get(msg.getcID()) - msg.getQuantity() * msg.getPricePerUnit());
			//add the money to the seller
			clientAccountBalance.put(offerHashMap.get(tempkeys).get(0).getcID(), clientAccountBalance.get(offerHashMap.get(tempkeys).get(0).getcID()) + msg.getQuantity() * msg.getPricePerUnit());
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
				((IssueWant) tempmsg).getResourceType(),
				((IssueWant) tempmsg).getQuantity(), 
				((IssueWant) tempmsg).getPricePerUnit()
			);
			wantHashMap.remove(tempkeys);
		} 
		else if (tempmsg instanceof IssueOffer){
			WantOfferKeys tempkeys = new WantOfferKeys(
				((IssueOffer) tempmsg).getResourceType(),
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
		if (clientAccountBalance.containsKey(msg.getClientID())){
			clientAccountBalance.put(msg.getClientID(), clientAccountBalance.get(msg.getClientID()) + msg.getAmount());
		} else {
			clientAccountBalance.put(msg.getClientID(), msg.getAmount());
		}
	}

	private void executeWithdrawal(Withdrawal msg){
		if (clientAccountBalance.containsKey(msg.getClientID())){
			clientAccountBalance.put(msg.getClientID(), clientAccountBalance.get(msg.getClientID()) - msg.getAmount());
		} 
	}

    /* ----------------------------------------- Operation Validation ----------------------------------- */

    public boolean isOperationValid(byte[] operation){
        // try to deserialize the operation to the different types
		ByteBuf buf = Unpooled.buffer();
		buf.writeBytes(operation);
		try {
			Deposit deposit = new Deposit();
			deposit = deposit.getSerializer().deserializeBody(buf);
			return isDepositValid(deposit);
		} catch (Exception e) {/*do nothing*/}
		try {
			Withdrawal withdrawal = new Withdrawal();
			withdrawal = withdrawal.getSerializer().deserializeBody(buf);
			return isWithdrawalValid(withdrawal);
		} catch (Exception e) {/*do nothing*/}
		try {
			IssueOffer offer = new IssueOffer();
			offer = offer.getSerializer().deserializeBody(buf);
			return isOfferValid(offer);
		} catch (Exception e) {/*do nothing*/}
		try {
			IssueWant want = new IssueWant();
			want = want.getSerializer().deserializeBody(buf);
			return isWantValid(want);
		} catch (Exception e) {/*do nothing*/}
		try {
			Cancel cancel = new Cancel();
			cancel = cancel.getSerializer().deserializeBody(buf);
			return isCancelValid(cancel);
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
		if (clientAccountBalance.containsKey(want.getcID())){
			return clientAccountBalance.get(want.getcID()) >= want.getQuantity() * want.getPricePerUnit();
		} else {
			return false;
		}
	}

	private boolean isOfferValid(IssueOffer offer) {
		return true;
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
