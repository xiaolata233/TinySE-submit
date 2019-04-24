package edu.hanyang.submit;

public class Test implements Comparable<Test>{
    int val;

    public Test(int x){
        this.val = x;
    }
    @Override
    public int compareTo(Test m2){
        return this.val - m2.val;
    }
}
