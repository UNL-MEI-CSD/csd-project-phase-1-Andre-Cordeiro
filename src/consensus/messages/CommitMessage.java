package consensus.messages;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedMessageSerializer;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedProtoMessage;
import utils.SeqN;

public class CommitMessage extends SignedProtoMessage{

    public final static short MESSAGE_ID = 103;	

	public final int vN, hashOpVal, iN;

	public final SeqN sN;

	public final String cryptoName;

    public CommitMessage(int vN, SeqN sN, int hashOpVal, int iN , String cryptoName){
        super(CommitMessage.MESSAGE_ID);
		this.vN = vN;
		this.sN = sN;
		this.hashOpVal = hashOpVal;
		this.iN = iN;
		this.cryptoName = cryptoName;
    }

    public static final SignedMessageSerializer<CommitMessage> serializer = new SignedMessageSerializer<CommitMessage>() {

		@Override
		public void serializeBody(CommitMessage signedProtoMessage, ByteBuf out) throws IOException {
			out.writeInt(signedProtoMessage.vN);
			signedProtoMessage.sN.serialize(out);
			out.writeInt(signedProtoMessage.hashOpVal);
			out.writeInt(signedProtoMessage.iN);
			out.writeCharSequence(signedProtoMessage.cryptoName, StandardCharsets.UTF_8);
		}

		@Override
		public CommitMessage deserializeBody(ByteBuf in) throws IOException {
			int vN = in.readInt();
			SeqN sN = SeqN.deserialize(in);
			int hashOpVal = in.readInt();
			int iN = in.readInt();
			String cryptoName = in.readCharSequence(in.readableBytes(), StandardCharsets.UTF_8).toString();
			return new CommitMessage(vN, sN, hashOpVal, iN,cryptoName);
		}
		
	};
	
	@Override
	public SignedMessageSerializer<? extends SignedProtoMessage> getSerializer() {
		return CommitMessage.serializer;
	}

	public int getvN() {
		return vN;
	}

	public SeqN getsN() {
		return sN;
	}

	public int getHashOpVal() {
		return hashOpVal;
	}

	public int getiN() {
		return iN;
	}

	public int getOp(){
		return hashOpVal ;
	}


	public int getViewNumber() {
		return vN;
	}

	public SeqN getSequenceNumber() {
		return sN;
	}

	public String getCryptoName() {
		return cryptoName;
	}
}
