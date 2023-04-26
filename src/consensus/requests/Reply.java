package consensus.requests;

import pt.unl.fct.di.novasys.babel.generic.ProtoReply;

public class Reply extends ProtoReply {

    public final static short REQUEST_ID = 200;

    private final int vN, hashOpVal;

    private final String cryptoName;

    public Reply(int vN, int hashOpVal, String cryptoName) {
        super(Reply.REQUEST_ID);
        this.vN = vN;
        this.hashOpVal = hashOpVal;
        this.cryptoName = cryptoName;
    }

    public int getvN() {
        return vN;
    }

    public int getHashOpVal() {
        return hashOpVal;
    }

    public String getCryptoName() {
        return cryptoName;
    }
    
}
