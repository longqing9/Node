package cn.longq.array;


/**
 *  1、环形数组 模拟数组
 */

public class Test1 {

    public static void main(String[] args) {
        Test1 test1 = new Test1(10);
        for (int i=0 ;i<10;i++){
            test1.add(i);
        }
        for (int i=0 ;i<10;i++){
            System.out.println(test1.show());
        }

        for (int i=0 ;i<10;i++){
            test1.add(i);
        }
        for (int i=0 ;i<10;i++){
            System.out.println(test1.show());
        }

    }

    private int max;

    private int start;

    private int end;

    private int size;

    private int[] array;

    Test1(int size){
        this.size =size;
        this.start = -1;
        this.end =-1;
        this.max = 0;
        this.array = new int[size];
    }


    public boolean add(int arr){
        if (max == size) throw new RuntimeException();
        end ++;
        max++;
        end = end % (size);
        array[end] = arr;
        return true;
    }

    public int show(){
        if (end == -1 || max == 0 || size == 0 ) throw  new RuntimeException();
        max --;
        start ++;
        start %= size;
        return array[start];
    }
}
