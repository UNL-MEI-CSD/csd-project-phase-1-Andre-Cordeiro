package blockchain.blockchain;

import pt.unl.fct.di.novasys.network.data.Host;

//The block : basic unit of the blockchain
public class Block {

    //The block's data
    private Host validater;

    private int hashPreviousBlock;

    private int blockNumber;

    private byte[] signature;

    private byte[] operations;
    
    public Block(Host validater, int hashPreviousBlock, int blockNumber, byte[] signature, byte[] operations) {
        this.validater = validater;
        this.hashPreviousBlock = hashPreviousBlock;
        this.blockNumber = blockNumber;
        this.signature = signature;
        this.operations = operations;
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

    public byte[] getOperations() {
        return operations;
    }

    @Override
    public int hashCode() {
        return validater.hashCode() + hashPreviousBlock + blockNumber + signature.hashCode();
    }

    @Override
    public String toString() {
        return "Block{" +
                "validater=" + validater +
                ", hashPreviousBlock=" + hashPreviousBlock +
                ", blockNumber=" + blockNumber +
                ", signature=" + signature +
                ", operations=" + operations +
                '}';
    }
    

}
