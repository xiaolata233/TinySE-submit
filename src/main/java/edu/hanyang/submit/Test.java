package edu.hanyang.submit;

import java.util.LinkedList;
import java.util.List;

public class Test{
    int val;
    List<List<Integer>> L;

    public Test(){
        L = new LinkedList<>();
    }

    public void add(){
        List<Integer> temp = new LinkedList<>();
        L.add(temp);
    }

    public static void main(String[] args){
//        Test test = new Test();
//        System.out.println(test.L.size());
//        test.add();
//        System.out.println(test.L.size());
        List<Integer> temp[] = new List[3];
        System.out.println(temp[1]);
    }
}
