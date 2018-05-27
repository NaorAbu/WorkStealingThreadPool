package WorkStealingThreadPool.RunExamples;

import WorkStealingThreadPool.Task;
import WorkStealingThreadPool.WorkStealingThreadPool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;


public class SumMatrix extends Task<int[]> {
    private int[][] array;

    public SumMatrix(int[][] array) {
        this.array = array;
    }

    protected void start() {
        int sum = 0;
        List<Task<Integer>> tasks = new ArrayList<>();
        int rows = array.length;
        for (int i = 0; i < rows; i++) {
            SumRow newTask = new SumRow(array, i);
            spawn(newTask);
            tasks.add(newTask);
        }
        whenResolved(tasks, () -> {
                    int[] res = new int[rows];
                    for (int j = 0; j < rows; j++) {
                        res[j] = tasks.get(j).getResult().get();
                    }
                    complete(res);
                }
        );
    }

    public static void main(String[] args) throws InterruptedException {
        WorkStealingThreadPool pool = new WorkStealingThreadPool(10);
        int n = 5; //you may check on different number of elements if you like
        int[][] array = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                array[i][j] = (int) (Math.random() * n);
                System.out.print(array[i][j] + " ");
            }
            System.out.println("");
        }
        SumMatrix task = new SumMatrix(array);
        pool.start();
        pool.submit(task);
        pool.shutdown();
    }

}

