package app.messages.client.replies;
import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.novasys.babel.generic.ProtoMessage;
import pt.unl.fct.di.novasys.network.ISerializer;

import java.io.IOException;
import java.util.UUID;

public class CheckBalanceReply extends ProtoMessage {

    private final float balance;
    private UUID rID;
    private String data;

    public final static short MESSAGE_ID = 311;

        public CheckBalanceReply(UUID rID, float balance) {
            super(CheckBalanceReply.MESSAGE_ID);
            this.rID = rID;
            this.balance = balance;
        }

        public CheckBalanceReply(UUID rID, float balance, String data) {
            super(CheckBalanceReply.MESSAGE_ID);
            this.rID = rID;
            this.balance = balance;
        }

        public UUID getrID() {
            return rID;
        }

        public void setrID(UUID rID) {
            this.rID = rID;
        }

        public float getBalance() {
            return balance;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public static ISerializer<CheckBalanceReply> serializer = new ISerializer<CheckBalanceReply>() {

            @Override
            public void serialize(CheckBalanceReply t, ByteBuf out) throws IOException {
                out.writeLong(t.rID.getMostSignificantBits());
                out.writeLong(t.rID.getLeastSignificantBits());
                if(t.data == null) {
                    out.writeShort(0);
                } else {
                    byte[] s = t.data.getBytes();
                    out.writeShort(s.length);
                    out.writeBytes(s);
                }
            }

            @Override
            public CheckBalanceReply deserialize(ByteBuf in) throws IOException {
                long msb = in.readLong();
                long lsb = in.readLong();
                UUID rID = new UUID(msb,lsb);
                int size = in.readShort();
                byte[] s = new byte[size];
                in.readBytes(s);
                String data = new String(s);
                return new CheckBalanceReply(rID, 0, data);
            }
        };

}
