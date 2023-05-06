package useless;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import consensus.messages.PrePrepareMessage;
import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedMessageSerializer;
import pt.unl.fct.di.novasys.babel.generic.signed.SignedProtoMessage;

public class NewViewMessage extends SignedProtoMessage {

    public final static short MESSAGE_ID = 107;

    private final int viewNumber;

    private final List<ViewChangeMessage> viewChangeMessages = new LinkedList<>();

    private final List<PrePrepareMessage> prePrepareMessagesBatch = new LinkedList<>();

    private final String cryptoName;

    public NewViewMessage(int viewNumber, List<ViewChangeMessage> viewChangeMessages, List<PrePrepareMessage> prePrepareMessagesBatch, String cryptoName) {
        super(NewViewMessage.MESSAGE_ID);
        this.viewNumber = viewNumber;
        // this.viewChangeMessages = viewChangeMessages;
        // this.prePrepareMessagesBatch = prePrepareMessagesBatch;
        this.cryptoName = cryptoName;
    }

    public int getViewNumber() {
        return viewNumber;
    }

    public List<ViewChangeMessage> getViewChangeMessages() {
        return viewChangeMessages;
    }

    public List<PrePrepareMessage> getPrePrepareMessagesBatch() {
        return prePrepareMessagesBatch;
    }

    public String getCryptoName() {
        return cryptoName;
    }

    //serialization

    public static final SignedMessageSerializer<NewViewMessage> serializer = new SignedMessageSerializer<NewViewMessage>() {

        @Override
        public void serializeBody(NewViewMessage signedProtoMessage, ByteBuf out) throws IOException {
            out.writeInt(signedProtoMessage.viewNumber);
            // out.writeInt(signedProtoMessage.viewChangeMessages.size());
            // for (ViewChangeMessage viewChangeMessage : signedProtoMessage.viewChangeMessages) {
            //     ViewChangeMessage.serializer.serializeBody(viewChangeMessage,out);
            // }
            // out.writeInt(signedProtoMessage.prePrepareMessagesBatch.size());
            // for (PrePrepareMessage prePrepareMessage : signedProtoMessage.prePrepareMessagesBatch) {
            //     PrePrepareMessage.serializer.serializeBody(prePrepareMessage,out);
            // }
            out.writeCharSequence(signedProtoMessage.cryptoName, StandardCharsets.UTF_8);
        }

        @Override
        public NewViewMessage deserializeBody(ByteBuf in) throws IOException {
            int viewNumber = in.readInt();
            // int viewChangeMessagesSize = in.readInt();
            List<ViewChangeMessage> viewChangeMessages = new LinkedList<>();
            // for (int i = 0; i < viewChangeMessagesSize; i++) {
            //     byte[] viewChangeMessageBytes = new byte[in.readInt()];
            //     in.readBytes(viewChangeMessageBytes);
            //     ByteBuf viewChangeMessageBuffer = in.alloc().buffer(viewChangeMessageBytes.length);
            //     viewChangeMessageBuffer.writeBytes(viewChangeMessageBytes);
            //     viewChangeMessages.add(ViewChangeMessage.serializer.deserializeBody(viewChangeMessageBuffer));
            // }
            // int prePrepareMessagesBatchSize = in.readInt();
            List<PrePrepareMessage> prePrepareMessagesBatch = new LinkedList<>();
            // for (int i = 0; i < prePrepareMessagesBatchSize; i++) {
            //     byte[] prePrepareMessageBytes = new byte[in.readInt()];
            //     in.readBytes(prePrepareMessageBytes);
            //     ByteBuf prePrepareMessageBuffer = in.alloc().buffer(prePrepareMessageBytes.length);
            //     prePrepareMessageBuffer.writeBytes(prePrepareMessageBytes);
            //     prePrepareMessagesBatch.add(PrePrepareMessage.serializer.deserializeBody(prePrepareMessageBuffer));
            // }
            String cryptoName = in.readCharSequence(in.readableBytes(), StandardCharsets.UTF_8).toString();
            return new NewViewMessage(viewNumber, viewChangeMessages, prePrepareMessagesBatch, cryptoName);
        }

    };


    @Override
    public SignedMessageSerializer<? extends SignedProtoMessage> getSerializer() {
        return NewViewMessage.serializer;
    }

    @Override
    public String toString() {
        return "NewViewMessage{" +
                "viewNumber=" + viewNumber +
                ", viewChangeMessages=" + viewChangeMessages +
                ", prePrepareMessagesBatch=" + prePrepareMessagesBatch +
                ", cryptoName='" + cryptoName + '\'' +
                '}';
    }
}

