
import edu.ucsc.leeps.fire.cong.server.ScriptedPayoffFunction.PayoffScriptInterface;
import edu.ucsc.leeps.fire.cong.config.Config;
import java.util.*;

public class Hotelling implements PayoffScriptInterface {

    public float getPayoff(
            int id,
            float percent,
            Map<Integer, float[]> popStrategies,
            Map<Integer, float[]> matchPopStrategies,
            Config config) {
        if (popStrategies.size() < 2) {
            return 0;
        }
        List<Float> sorted = new ArrayList<Float>();
        for (float[] s : popStrategies.values()) {
            sorted.add(s[0]);
        }
        Collections.sort(sorted);
        int i = sorted.indexOf(popStrategies.get(id)[0]);
        float s = sorted.get(i);
        float u;
        if (i == 0) {
            u = s + 0.5f * (sorted.get(i + 1) - s);
        } else if (i == sorted.size() - 1) {
            u = 0.5f * (s - sorted.get(i - 1)) + (1 - s);
        } else {
            u = 0.5f * (s - sorted.get(i - 1)) + 0.5f * (sorted.get(i + 1) - s);
        }
        return config.get("Alpha") * 100 * u;
    }
}