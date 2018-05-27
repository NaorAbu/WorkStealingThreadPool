
package WorkStealingThreadPool.WareHouse;

import WorkStealingThreadPool.WareHouseWorkStealingThreadPool;
import WorkStealingThreadPool.WareHouse.conf.ManufactoringPlan;
import WorkStealingThreadPool.WareHouse.tasks.Contract;
import WorkStealingThreadPool.WareHouse.tools.GcdScrewDriver;
import WorkStealingThreadPool.WareHouse.tools.NextPrimeHammer;
import WorkStealingThreadPool.WareHouse.tools.RandomSumPliers;
import WorkStealingThreadPool.WareHouse.tools.Tool;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * A class describing the simulator for part 2 of the assignment
 */
public class Simulator {
    static WorkStealingThreadPool pool;
    private static Warehouse warehouse;
    private static WorkOrder workObj;
    private static ConcurrentLinkedQueue<Product> SimulationResult;

    /**
     * Begin the simulation
     * Should not be called before attachWorkStealingThreadPool()
     */


    public static Warehouse getWarehouse() {
        return warehouse;
    }
    public static ConcurrentLinkedQueue<Product> start() throws Exception {
        ConcurrentLinkedQueue<Product> ans = new ConcurrentLinkedQueue<>();
        pool.start();
        for (int i = 0; i < workObj.getWaves().size(); i++) {
            AtomicBoolean running = new AtomicBoolean(true);
            AtomicBoolean check = new AtomicBoolean(false);
            while (running.get()) {
                if (!check.get()) {

                    check.set(true);
                    List<LinkedTreeMap> d = (List<LinkedTreeMap>) workObj.getWaves().get(i);
                    AtomicInteger total = new AtomicInteger(0);
                    for (LinkedTreeMap ltm : d){

                        String s = (String) ltm.get("product");
                        long id = ((Double) ltm.get("startId")).longValue();
                        int q = ((Double) ltm.get("qty")).intValue();
                        total.addAndGet(q);
                        for (int z = 0; z < q; z++) {
                            Product p = new Product(id + z, s);
                            ans.add(p);
                            Contract newContract = new Contract(p);
                            newContract.getResult().whenResolved(() -> {
                                if (total.decrementAndGet() == 0) {
                                    running.set(false);
                                }
                            });
                            pool.submit(newContract);
                        }
                    }
                }
            }
        }
        return ans;
    }

    /**
     * attach a WorkStealingThreadPool to the Simulator, this WorkStealingThreadPool will be used to run the simulation
     *
     * @param myWorkStealingThreadPool - the WorkStealingThreadPool which will be used by the simulator
     */
    public static void attachWorkStealingThreadPool(WorkStealingThreadPool myWorkStealingThreadPool) {
        pool = myWorkStealingThreadPool;
    }

    public static void main(String[] args) throws Exception {
        //reding the Json file
        Gson gson = new Gson();
        BufferedReader br = new BufferedReader(new FileReader(args[0]));
        workObj = gson.fromJson(br, WorkOrder.class);
        //attaching the newly created pool
        attachWorkStealingThreadPool(new WorkStealingThreadPool(workObj.getThreads()));
        //building the warehouse
        warehouse = new Warehouse();
        List<LinkedTreeMap> toolslist = workObj.getTools();
        for (LinkedTreeMap ltm : toolslist) {
            Tool tool = null;
            switch ((String) ltm.get("tool")) {
                case "gs-driver":
                    tool = new GcdScrewDriver();
                    break;
                case "np-hammer":
                    tool = new NextPrimeHammer();
                    break;
                case "rs-pliers":
                    tool = new RandomSumPliers();
                    break;

            }
            int qty = ((Double) ltm.get("qty")).intValue();
            warehouse.addTool(tool, qty);
        }
        List<LinkedTreeMap> plansList = workObj.getPlans();
        for (LinkedTreeMap ltm : plansList) {
            //product, string[] parts, string[] tools
            String product = (String) ltm.get("product");
            ArrayList<String> parts = (ArrayList<String>) ltm.get("parts");
            String[] partsAns = new String[parts.size()];
            for (int i = 0; i < parts.size(); i++) {
                partsAns[i] = parts.get(i);
            }
            ArrayList<String> tools = (ArrayList<String>) ltm.get("tools");
            String[] toolsAns = new String[tools.size()];
            for (int i = 0; i < tools.size(); i++) {
                toolsAns[i] = tools.get(i);
            }
            ManufactoringPlan plan = new ManufactoringPlan(product, partsAns, toolsAns);
            warehouse.addPlan(plan);
        }
        //starting the simulation
        SimulationResult = start();
        FileOutputStream fout = new FileOutputStream("result.ser");
        ObjectOutputStream oos = new ObjectOutputStream(fout);
        oos.writeObject(SimulationResult);
        oos.close();
        pool.shutdown();
    }
}

