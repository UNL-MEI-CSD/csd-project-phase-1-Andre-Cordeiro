package utils.Operation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OpsMap {
        
        private Map<OpsMapKey, UUID> opsMap;
        
        public OpsMap() {
                opsMap = new HashMap<>();
        }
        
        public void addOp(OpsMapKey opsMapKey, UUID id) {
                opsMap.put(opsMapKey, id);
        }

        public UUID getOp(OpsMapKey opsMapKey) {
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

        public Map<OpsMapKey, UUID> getOpsMap() {
                return opsMap;
        }


}