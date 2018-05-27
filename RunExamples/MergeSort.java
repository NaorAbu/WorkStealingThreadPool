
package WorkStealingThreadPool.RunExamples;

import WorkStealingThreadPool.Task;
import WorkStealingThreadPool.WorkStealingThreadPool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class MergeSort extends Task<int[]> {

    private final int[] array;

    public MergeSort(int[] array) {
        this.array = array;
    }

    @Override
    protected void start() {
        if (array.length == 1) {
            complete(array);
        } else if (array.length > 1) {
            int mid = array.length / 2;
            int[] left = new int[mid];
            int[] right = new int[array.length - mid];
            for (int i = 0; i < left.length; i++) {
                left[i] = array[i];
            }
            for (int i = 0; i < right.length; i++) {
                right[i] = array[i + mid];
            }
            List<Task<int[]>> tasks = new ArrayList<>();
            MergeSort rightPart = new MergeSort(right);
            MergeSort leftPart = new MergeSort(left);
            tasks.add(rightPart);
            tasks.add(leftPart);
            spawn(rightPart);
            spawn(leftPart);
            whenResolved(tasks, () -> {
                int[] arrOne = tasks.get(0).getResult().get();
                int[] arrTwo = tasks.get(1).getResult().get();
                int[] arrAns = new int[array.length];
                int i = 0;
                int j = 0;
                int k = 0;
                while (i < arrOne.length && j < arrTwo.length) {
                    if (arrOne[i] < arrTwo[j])
                        arrAns[k++] = arrOne[i++];

                    else
                        arrAns[k++] = arrTwo[j++];
                }

                while (i < arrOne.length)
                    arrAns[k++] = arrOne[i++];


                while (j < arrTwo.length)
                    arrAns[k++] = arrTwo[j++];
                complete(arrAns);
            });

        }
    }

    public static void main(String[] args) throws InterruptedException {
           WorkStealingThreadPool pool = new WorkStealingThreadPool(20);
           int n = 1000; //you may check on different number of elements if you like
           int[] array = new Random().ints(n).toArray();

           MergeSort task = new MergeSort(array);

           CountDownLatch l = new CountDownLatch(1);
           pool.start();
           pool.submit(task);
           task.getResult().whenResolved(() -> {
               //warning - a large print!! - you can remove this line if you wish
               System.out.println(Arrays.toString(task.getResult().get()));
               int[] ans = task.getResult().get();
               boolean isSorted = true;
               for (int i = 0; i < ans.length - 1; i++) {
                   isSorted = isSorted && (i < i + 1);
               }
               System.out.println(isSorted);
               l.countDown();
           });
           l.await();
           pool.shutdown();
    }
}
