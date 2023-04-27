package utils.Operation;

import java.util.HashMap;
import java.util.Map;

public class OpsMap {
        
        private Map<Integer, byte[]> opsMap;
        
        public OpsMap() {
                opsMap = new HashMap<>();
        }
        
        public void addOp(int opsMapKeyHash, byte[] opsBlock) {
                opsMap.put(opsMapKeyHash, opsBlock);
        }

        public byte[] getOp(int opsMapKeyHash) {
                return opsMap.get(opsMapKeyHash);
        }

        public boolean containsOp(int opsMapKeyHash) {
                return opsMap.containsKey(opsMapKeyHash);
        }

        public void removeOp(int opsMapKeyHash) {
                opsMap.remove(opsMapKeyHash);
        }

        public void clear() {
                opsMap.clear();
        }

        public int size() {
                return opsMap.size();
        }

        public Map<Integer, byte[]> getOpsMap() {
                return opsMap;
        }


}