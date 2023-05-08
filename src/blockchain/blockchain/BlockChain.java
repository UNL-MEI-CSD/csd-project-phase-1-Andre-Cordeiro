package blockchain.blockchain;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;

import pt.unl.fct.di.novasys.network.data.Host;

public class BlockChain {
    
    LinkedList<Block> chain;

    public BlockChain() {
        chain = new LinkedList<Block>();
        // Genesis block
        try {
			chain.add(new Block(new Host(InetAddress.getLocalHost(), 0), 431, 0, new byte[0], new byte[0]));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
    }

    public void addBlock(Block block) {
        chain.add(block);
    }

    public Block getLastBlock() {
        return chain.getLast();
    }

    public Block getBlock(int index) {
        return chain.get(index);
    }
}
