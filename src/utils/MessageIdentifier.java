package utils;

import java.util.Objects;

public class MessageIdentifier {

    private final int viewNumber, seqNumber;

    public MessageIdentifier(int viewNumber, int seqNumber) {
        this.viewNumber = viewNumber;
        this.seqNumber = seqNumber;
    }

    public int getViewNumber() {
        return viewNumber;
    }

    public int getSeqNumber() {
        return seqNumber;
    }

    @Override
    public String toString() {
        return "MessageIdentifier{" +
                "viewNumber=" + viewNumber +
                ", seqNumber=" + seqNumber +
                '}';
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        
        MessageIdentifier mID = (MessageIdentifier) o;

        return viewNumber == mID.viewNumber && seqNumber == mID.seqNumber;
    }

    @Override
    public int hashCode(){
        return Objects.hash(viewNumber, seqNumber);
    }
    
}
