package WorkStealingThreadPool; /**
 * 
 */
/* This is optional JSON deserializer using for the WareHouse run by the WorkStealingThreadPool */
import WorkStealingThreadPool.sim.Product;

import java.io.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class SerReader {


	public static void main(String[] args) {
		AtomicBoolean flag = new AtomicBoolean(false);
		
		System.out.println("deserializer for WorkStealingThreadPool\n\n");

		SerReader obj = new SerReader();

		ConcurrentLinkedQueue<Product> res;
		try {
			res = obj.deserialzeObject(args[0]);
		} catch (FileNotFoundException e) {
			System.err.println("can't find file, check args");
			throw new RuntimeException();
		} catch (ClassNotFoundException e) {
			System.err.println("class not found");
			throw new RuntimeException();
		}

		if (res == null) {
			System.out.println("error -> null object");
		} else {
			System.out.println("file read successful\n\n\nWriting into file...");
			
			writeToFile(res);
			System.out.println("done");
		}
	}

	public ConcurrentLinkedQueue<Product> deserialzeObject(String filename) throws FileNotFoundException, ClassNotFoundException{

		ConcurrentLinkedQueue<Product> res = null;

		try (FileInputStream fin = new FileInputStream(filename);
				ObjectInputStream ois = new ObjectInputStream(fin);) {
			ConcurrentLinkedQueue<Product> concurrentLinkedQueue = (ConcurrentLinkedQueue<Product>) (ois.readObject());
			res = concurrentLinkedQueue;

		} catch (IOException e){
			e.printStackTrace();
		}

		return res;

	}
	
	private static void writeToFile(ConcurrentLinkedQueue<Product> res){
		File txtfout = new File("readableOutput.txt");
		
		try (FileOutputStream fos = new FileOutputStream(txtfout);
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));) {
			for (Product p : res)
				addProductToBW(bw, p);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void addProductToBW(BufferedWriter bw, Product product) throws IOException{
		bw.write("ProductName: " + product.getName() + "  Product Id = " + product.getFinalId());
		bw.newLine();

		bw.write("PartsList {");
		bw.newLine();
		if (product.getParts().size() > 0) {
			for (Product p : product.getParts()) {
				addProductToBW(bw, p);
			}
		}
		bw.write("}");
		bw.newLine();
	}
;
}
