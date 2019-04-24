package edu.hanyang.submit;

import edu.hanyang.utils.DiskIO;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.junit.rules.DisableOnDebug;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class Tree {
    private int[] tree = null;// 以顺序存储方式保存所有非叶子结点
    private int size = 0;
    private ArrayList<DataManager> leaves = null;// 叶子节点

    public Tree(ArrayList<DataManager> initResults) {
        this.leaves = initResults;
        this.size = initResults.size();
        this.tree = new int[size];
        for (int i = 0; i < size; ++i) {
            tree[i] = -1;
        }
        for (int i = size - 1; i >= 0; --i) {
            adjust(i);
        }
    }

    public int getSize(){
        return size;
    }

    public void del(int s) {
        leaves.remove(s);
        size--;
        tree = new int[size];
        for (int i = 0; i < size; ++i) {
            tree[i] = -1;
        }
        for (int i = size - 1; i >= 0; --i) {
            adjust(i);
        }
    }

    public MutableTriple<Integer, Integer, Integer> pop() throws IOException {
        DataManager temp = getLeaf(getWinner());
        MutableTriple<Integer, Integer, Integer> result = temp.getTupleCopy();
        temp.reload();
        if(temp.isEmpty()){
            this.del(tree[0]);
        }else{
            adjust(tree[0]);
        }
        return result;
    }

    public void add(DataManager leaf, int s) {
        leaves.set(s, leaf);// 调整叶子结点
        adjust(s);// 调整非叶子结点
    }

    public DataManager getLeaf(int i) {
        return leaves.get(i);
    }

    public int getWinner() {
        return tree[0];
    }

    private void adjust(int s) {
        int t = (s + size) / 2;// t是s的双亲

        while (t > 0) {
            if (s >= 0 && (tree[t] == -1 || leaves.get(s).getTuple().compareTo(
                    leaves.get(tree[t]).getTuple()) > 0)) {
                int tmp = s;
                s = tree[t];
                tree[t] = tmp;
            }
            t /= 2;
        }
        tree[0] = s;// 树根指向胜者
    }

    public void printMT(MutableTriple<Integer, Integer, Integer> mt){
        System.out.println("" + mt.getLeft() + ", " + mt.getMiddle() + ", " + mt.getRight());
    }

    public static void main(String[] args) throws IOException {
//        DataOutputStream os = DiskIO.open_output_run("1.data", 1024);
//        DiskIO.append_tuple(os, new MutableTriple<Integer, Integer, Integer>(1,2,3));
//        DiskIO.append_tuple(os, new MutableTriple<Integer, Integer, Integer>(1,2,4));
//        DiskIO.append_tuple(os, new MutableTriple<Integer, Integer, Integer>(2,2,4));
//        os.close();
//        os = DiskIO.open_output_run("2.data", 1024);
//        DiskIO.append_tuple(os, new MutableTriple<Integer, Integer, Integer>(2,3,3));
//        DiskIO.append_tuple(os, new MutableTriple<Integer, Integer, Integer>(2,3,4));
//        DiskIO.append_tuple(os, new MutableTriple<Integer, Integer, Integer>(3,4,5));
//        os.close();

        ArrayList<DataManager> DMs = new ArrayList<>();
        DMs.add(new DataManager(DiskIO.open_input_run("1.data", 1024)));
        DMs.add(new DataManager(DiskIO.open_input_run("2.data", 1024)));
        Tree t = new Tree(DMs);
//        DataManager dm = t.getLeaf(t.getWinner());
//        t.printMT(dm.getTuple());
        while(t.getSize() > 0){
            t.printMT(t.pop());
        }
    }
}
