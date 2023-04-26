package consensus.requests;

import pt.unl.fct.di.novasys.babel.generic.ProtoReply;

public class Reply extends ProtoReply {

    public final static short REPLY_ID = 121;

    private final int vN;
    
    private final byte[] opBlock;

    private final String cryptoName;

    public Reply(int vN, byte[] opBlock, String cryptoName) {
        super(Reply.REPLY_ID);
        this.vN = vN;
        this.opBlock = opBlock;
        this.cryptoName = cryptoName;
    }

    public int getvN() {
        return vN;
    }

    public byte[] getOpBlock() {
        return opBlock;
    }

    public String getCryptoName() {
        return cryptoName;
    }
    
}
