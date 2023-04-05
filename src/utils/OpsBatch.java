package utils;

import java.util.Arrays;
import java.util.Objects;

import pt.unl.fct.di.novasys.network.data.Host;

public class OpsBatch {
    
    private final long idBatch;
    private final byte[] ops;
    private final Host node;

    public OpsBatch(long idBatch, byte[] ops, Host node){
        this.idBatch = idBatch;
        this.ops = ops;
        this.node = node;
    }

    public long getIdBatch() {
        return idBatch;
    }

    public byte[] getOps() {
        return ops;
    }

    public Host getNode() {
        return node;
    }

    @Override
    public String toString() {
        return "OpsBatch [idBatch=" + idBatch + ", ops=" + Arrays.toString(ops) + ", node=" + node + "]";
    }

    @Override
    public int hashCode() {
       
        return Objects.hash(idBatch, node);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        OpsBatch opBatch = (OpsBatch) obj;
        return idBatch == opBatch.idBatch && Objects.equals(node, opBatch.node);
    }
    

    
}
