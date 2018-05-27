package WorkStealingThreadPool.Warehouse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A class that represents a product produced during the simulation.
 */
public class Product implements Serializable {
    private final long startId;
    private AtomicLong finalId;
    private final String name;
    private List<Product> partList;

    /**
     * Constructor
     *
     * @param startId - Product start id
     * @param name    - Product name
     */
    public Product(long startId, String name) {
        this.startId = startId;
        this.finalId = new AtomicLong(startId);
        this.name = name;
        partList = new ArrayList<Product>();
    }

    /**
     * @return The product name as a string
     */
    public String getName() {
        return name;
    }

    /**
     * @return The product start ID as a long. start ID should never be changed.
     */
    public long getStartId() {
        return startId;
    }

    /**
     * @return The product final ID as a long.
     * final ID is the ID the product received as the sum of all UseOn();
     */
    public long getFinalId() {
        return finalId.get();
    }

    public synchronized void addToFID(long val) {
        finalId.addAndGet(val);
    }

    /**
     * @return Returns all parts of this product as a List of Products
     */
    public List<Product> getParts() {
        return partList;
    }

    /**
     * Add a new part to the product
     *
     * @param p - part to be added as a Product object
     */
    public void addPart(Product p) {
        if(p != null){
            partList.add(p);
        }else{
            throw new RuntimeException("Unable to add product null");
        }
    }

    /**
     * @return Returns the entire structure of a product
     */
    public String toString() {
        String ans = "ProductName: " + this.getName() + "  Product Id = " + this.getFinalId() + "\n" + "PartsList {\n";
        for (Product parts : this.getParts()) {
            ans = ans + parts.toString();
        }
        ans += "}\n";
        return ans;
    }
}
