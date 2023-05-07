package blockchain.blockchain;

import pt.unl.fct.di.novasys.babel.generic.signed.SignedProtoMessage;
import pt.unl.fct.di.novasys.network.data.Host;

//The block : basic unit of the blockchain
public class Block {


    //The block's data
    private Host validater;

    private int hashPreviousBlock;

    private int blockNumber;

    private byte[] signature;

    private SignedProtoMessage[] transactions;
    
    public Block(Host validater, int hashPreviousBlock, int blockNumber, byte[] signature, SignedProtoMessage[] transactions) {
        this.validater = validater;
        this.hashPreviousBlock = hashPreviousBlock;
        this.blockNumber = blockNumber;
        this.signature = signature;
        this.transactions = transactions;
    }

    public Host getValidater() {
        return validater;
    }

    public int getHashPreviousBlock() {
        return hashPreviousBlock;
    }

    public int getBlockNumber() {
        return blockNumber;
    }

    public byte[] getSignature() {
        return signature;
    }

    public SignedProtoMessage[] getTransactions() {
        return transactions;
    }

    @Override
    public int hashCode() {
        return validater.hashCode() + hashPreviousBlock + blockNumber + signature.hashCode();
    }
    

}
