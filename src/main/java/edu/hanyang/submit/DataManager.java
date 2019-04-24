package edu.hanyang.submit;

import org.apache.commons.lang3.tuple.MutableTriple;

import java.io.DataInputStream;
import java.io.IOException;

public class DataManager implements Comparable<DataManager>{
    DataInputStream is;
    MutableTriple<Integer, Integer, Integer> mt;

    public DataManager(DataInputStream is) throws IOException {
        this.is = is;
        this.mt = new MutableTriple<>();
        if(this.is.available() > 0){
            this.mt.setLeft(this.is.readInt());
            this.mt.setMiddle(this.is.readInt());
            this.mt.setRight(this.is.readInt());
        }else {
            this.mt = null;
        }
    }

    public MutableTriple<Integer, Integer, Integer> getTuple() {
        return mt;
    }

    public DataInputStream getIs() {
        return is;
    }

    public MutableTriple<Integer, Integer, Integer> getTupleCopy(){
        MutableTriple<Integer, Integer, Integer> copy = new MutableTriple<>(mt.getLeft(), mt.getMiddle(), mt.getRight());
        return copy;
    }

    public MutableTriple<Integer, Integer, Integer> peek(){
        return mt;
    }

    public MutableTriple<Integer, Integer, Integer> pop() throws IOException {
        MutableTriple<Integer, Integer, Integer> temp = new MutableTriple<>(mt.getLeft(), mt.getMiddle(), mt.getRight());
        reload();
        return temp;
    }

    public void reload() throws IOException {
        if(this.is.available() > 0){
            this.mt.setLeft(this.is.readInt());
            this.mt.setMiddle(this.is.readInt());
            this.mt.setRight(this.is.readInt());
        }else {
            this.mt = null;
        }
    }

    public boolean isEmpty(){
        return this.mt == null;
    }
    @Override
    public int compareTo(DataManager m2){
        return this.mt.compareTo(m2.mt);
    }
}
