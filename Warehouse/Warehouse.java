package WorkStealingThreadPool.Warehouse;

import WorkStealingThreadPool.Deferred;
import WorkStealingThreadPool.Warehouse.conf.ManufactoringPlan;
import WorkStealingThreadPool.Warehouse.tools.GcdScrewDriver;
import WorkStealingThreadPool.Warehouse.tools.NextPrimeHammer;
import WorkStealingThreadPool.Warehouse.tools.RandomSumPliers;
import WorkStealingThreadPool.Warehouse.tools.Tool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class representing the warehouse in the simulation
 */
public class Warehouse {
    private Tool gSD = new GcdScrewDriver();
    private Tool nPH = new NextPrimeHammer();
    private Tool rSP = new RandomSumPliers();
    private AtomicInteger gSDC, nPHC, rSPC;
    private ConcurrentLinkedDeque<Deferred<Tool>> gSDQ;
    private ConcurrentLinkedDeque<Deferred<Tool>> nPHQ;
    private ConcurrentLinkedDeque<Deferred<Tool>> rSPQ;

    private List<ManufactoringPlan> plans;

    /**
     * Constructor
     */
    public Warehouse() {
        gSDQ = new ConcurrentLinkedDeque<>();
        nPHQ = new ConcurrentLinkedDeque<>();
        rSPQ = new ConcurrentLinkedDeque<>();
        gSDC = new AtomicInteger(0);
        nPHC = new AtomicInteger(0);
        rSPC = new AtomicInteger(0);
        plans = new ArrayList<ManufactoringPlan>();

    }
    public void print(){
        System.out.println(gSDC.get() + " " +  nPHC.get() + " " + rSPC.get());
    }


    /**
     * Tool acquisition procedure
     * Note that this procedure is non-blocking and should return immediatly
     *
     * @param type - string describing the required tool
     * @return a deferred promise for the  requested tool
     */
    public synchronized Deferred<Tool> acquireTool(String type) {
        Deferred<Tool> newDef = new Deferred<Tool>();
        //switching for the tool needed
        //if it exists, resolve the deferred,
        //otherwise, put it in a list waiting to be resolved
        switch (type) {
            case "gs-driver":
                if (gSDC.get() > 0) {
                    gSDC.decrementAndGet();
                    newDef.resolve(gSD);
                } else {
                    gSDQ.addLast(newDef);
                }
                return newDef;
            case "np-hammer":
                if (nPHC.get() > 0) {
                    nPHC.decrementAndGet();
                    newDef.resolve(nPH);
                } else {
                    nPHQ.addLast(newDef);
                }
                return newDef;
            case "rs-pliers":
                if (rSPC.get() > 0) {
                    rSPC.decrementAndGet();
                    newDef.resolve(rSP);
                } else {
                    rSPQ.addLast(newDef);
                }
        }
        return newDef;
    }


    /**
     * Tool return procedure - releases a tool which becomes available in the warehouse upon completion.
     *
     * @param tool - The tool to be returned
     */

    public synchronized void releaseTool(Tool tool) {
        if(tool.getType()=="gs-driver"){

            Deferred def=gSDQ.pollFirst();
            if(def!=null){
                def.resolve(tool);
            //    gSDC.decrementAndGet();
            }else{
                this.gSDC.incrementAndGet();
            }
        }else if(tool.getType()=="np-hammer"){
            Deferred def=nPHQ.pollFirst();
            if(def!=null){
                def.resolve(tool);
           //     nPHC.decrementAndGet();
            }else{
                this.nPHC.incrementAndGet();
            }
        }else{
            Deferred def=rSPQ.pollFirst();
            if(def!=null){
                def.resolve(tool);
          //      rSPC.decrementAndGet();
            }else{
                this.rSPC.incrementAndGet();
            }
        }

    }


    /**
     * Getter for ManufactoringPlans
     *
     * @param product - a string with the product name for which a ManufactoringPlan is desired
     * @return A ManufactoringPlan for product
     */
    public ManufactoringPlan getPlan(String product) {
        for (ManufactoringPlan p : plans) {
            if (p.getProductName().equals(product)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Store a ManufactoringPlan in the warehouse for later retrieval
     *
     * @param plan - a ManufactoringPlan to be stored
     */
    public void addPlan(ManufactoringPlan plan) {
        plans.add(plan);
    }

    /**
     * Store a qty Amount of tools of type tool in the warehouse for later retrieval
     *
     * @param tool - type of tool to be stored
     * @param qty  - amount of tools of type tool to be stored
     */
    public void addTool(Tool tool, int qty) {
        switch (tool.getType()) {
            case "gs-driver":
                gSDC.addAndGet(qty);
                break;
            case "np-hammer":
                nPHC.addAndGet(qty);
                break;
            case "rs-pliers":
                rSPC.addAndGet(qty);
                break;
        }
    }

}
