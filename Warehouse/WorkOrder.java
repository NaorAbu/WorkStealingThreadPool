package WorkStealingThreadPool.Warehouse;

import com.google.gson.internal.LinkedTreeMap;

import java.util.List;

/**
 * Class that represents the Json input
 */
public class WorkOrder {
    private int threads;
    public List<LinkedTreeMap> tools;
    private List<LinkedTreeMap> plans;
    private List<List<LinkedTreeMap>> waves;

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public List<LinkedTreeMap> getTools() {
        return tools;
    }

    public void setTools(List<LinkedTreeMap> tools) {
        this.tools = tools;
    }

    public List<LinkedTreeMap> getPlans() {
        return plans;
    }

    public void setPlans(List<LinkedTreeMap> plans) {
        this.plans = plans;
    }

    public List<List<LinkedTreeMap>> getWaves() {
        return waves;
    }

    public void setWaves(List<List<LinkedTreeMap>> waves) {
        this.waves = waves;
    }

}
