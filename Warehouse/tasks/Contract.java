package WorkStealingThreadPool.Warehouse.tasks;

import WorkStealingThreadPool.*;
import WorkStealingThreadPool.Warehouse.*;
import WorkStealingThreadPool.Warehouse.conf.*;
import WorkStealingThreadPool.Warehouse.tools.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Class that represents a Manufactoring task
 */
public class Contract extends Task<Product> {
    private Product product;

    @Override
    protected void start() {
        List<Task<Product>> tasks = new ArrayList<>();
        ManufactoringPlan plan = Simulator.getWarehouse().getPlan(product.getName());
        AtomicLong startId = new AtomicLong((product.getStartId()));
        //Product has no sub-products
        if (plan.getParts().length == 0) {
            complete(product);
        }
        //manufactoring sub products
        for (String p : plan.getParts()) {
            Product son = new Product(startId.get() + 1, p);
            product.addPart(son);
            Contract newContract = new Contract(son);
            spawn(newContract);
            tasks.add(newContract);
        }
        whenResolved(tasks, () -> {
            //using the tools on the sub-products once they are done
            for (String t : plan.getTools()) {
                Deferred<Tool> def = Simulator.getWarehouse().acquireTool(t);
                def.whenResolved(() -> {
                    long ans = def.get().useOn(product);
                    //calculating the final Id
                    product.addToFID(ans);
                    Simulator.getWarehouse().releaseTool(def.get());
                });
            }
            complete(product);
        });
    }

    public Contract(Product p) {
        this.product = p;
    }
}
