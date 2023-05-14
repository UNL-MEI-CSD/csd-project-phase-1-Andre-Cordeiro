package app.messages.client.replies;

import pt.unl.fct.di.novasys.babel.generic.ProtoMessage;

public class Balance extends ProtoMessage {

    private static final short MESSAGE_ID = 311;
    
    private float balance;

    public Balance(float balance) {
        super(MESSAGE_ID);
        this.balance = balance;
    }

    public float getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        return "Balance{" +
                "balance=" + balance +
                '}';
    }
}
