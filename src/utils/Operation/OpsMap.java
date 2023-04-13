package utils.Operation;


import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class OpsMap {
        
        private Map<Timestamp, Integer> opsMap;
        
        public OpsMap() {
                opsMap = new HashMap<>();
        }
        
        public void addOp(Timestamp timestamp, int op) {
                opsMap.put(timestamp, op);
        }

        public int getOp(Timestamp timestamp) {
                return opsMap.get(timestamp);
        }

        public boolean containsOp(Timestamp timestamp) {
                return opsMap.containsKey(timestamp);
        }

        public void removeOp(Timestamp timestamp) {
                opsMap.remove(timestamp);
        }

        public void clear() {
                opsMap.clear();
        }

        public int size() {
                return opsMap.size();
        }

        public Map<Timestamp, Integer> getOpsMap() {
                return opsMap;
        }


}