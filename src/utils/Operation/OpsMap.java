package utils.Operation;

import java.util.HashMap;
import java.util.Map;

public class OpsMap {
        
        private Map<OpsMapKey, byte[]> opsMap;
        
        public OpsMap() {
                opsMap = new HashMap<>();
        }
        
        public void addOp(OpsMapKey opsMapKey, byte[] op) {
                opsMap.put(opsMapKey, op);
        }

        public byte[] getOp(OpsMapKey opsMapKey) {
                return opsMap.get(opsMapKey);
        }

        public boolean containsOp(OpsMapKey opsMapKey) {
                return opsMap.containsKey(opsMapKey);
        }

        public void removeOp(OpsMapKey opsMapKey) {
                opsMap.remove(opsMapKey);
        }

        public void clear() {
                opsMap.clear();
        }

        public int size() {
                return opsMap.size();
        }

        public Map<OpsMapKey, byte[]> getOpsMap() {
                return opsMap;
        }


}