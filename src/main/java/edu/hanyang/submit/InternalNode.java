package edu.hanyang.submit;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import edu.hanyang.submit.TinySEBPlusTree.*;

public class InternalNode extends Node{
    List<Integer> children;
    List<Node> inMemoryChildren;

    InternalNode() throws IOException {
        this.keys = new ArrayList<Integer>();
        this.children = new ArrayList<Integer>();
        this.inMemoryChildren = new ArrayList<Node>();
        position = TinySEBPlusTree.num_nodes * TinySEBPlusTree.blocksize;
        //save(position);
    }

    InternalNode(Integer offset) throws IOException {
        this.keys = new ArrayList<Integer>();
        this.children = new ArrayList<Integer>();
        position = offset;
        TinySEBPlusTree.file.seek(offset);
        TinySEBPlusTree.file.readInt();
        int num_keys = TinySEBPlusTree.file.readInt();
        for(int i=0; i<num_keys; i++){
            children.add(TinySEBPlusTree.file.readInt());
            keys.add(TinySEBPlusTree.file.readInt());
        }
        if(children.size() > 0){
            children.add(TinySEBPlusTree.file.readInt());
        }
    }

    @Override
    void save() throws IOException {
        TinySEBPlusTree.file.seek(position);
        TinySEBPlusTree.file.writeInt(-2);   // Node type: non-leaf node.
        TinySEBPlusTree.file.writeInt(keyNumber());   // Number of keys.
        int i;
        for(i=0; i<keyNumber(); i++){
            TinySEBPlusTree.file.writeInt(children.get(i));
            TinySEBPlusTree.file.writeInt(keys.get(i));
        }
        if(children.size() > 0){
            TinySEBPlusTree.file.writeInt(children.get(i));
        }
//        return offset + TinySEBPlusTree.blocksize;
    }

//    Node getChild(Integer key) throws IOException {
//        int loc = Collections.binarySearch(keys, key);
//        int childIndex = loc >= 0 ? loc + 1 : -loc - 1;
//        Long offset = children.get(childIndex);
//        if(offset > TinySEBPlusTree.nblocks){
//            return TinySEBPlusTree.loadNode(offset);
//        }else{
//            return TinySEBPlusTree.inMemoryNodes.get(offset.intValue());
//        }
//    }

    Node getChild(Integer key) throws IOException {
        int loc = Collections.binarySearch(keys, key);
        int childIndex = loc >= 0 ? loc + 1 : -loc - 1;
        Integer offset = children.get(childIndex);
        TinySEBPlusTree.file.seek(offset);
        int type = TinySEBPlusTree.file.readInt();
        int temp;

        while(type == -2){
            int num_keys = TinySEBPlusTree.file.readInt();
            offset = TinySEBPlusTree.file.readInt();
            temp = TinySEBPlusTree.file.readInt();
            num_keys -= 1;
            while(temp < key && num_keys != 0){
                offset = TinySEBPlusTree.file.readInt();
                temp = TinySEBPlusTree.file.readInt();
            }
            if(temp == key || (num_keys == 0 && temp < key)){
                offset = TinySEBPlusTree.file.readInt();
            }
            TinySEBPlusTree.file.seek(offset);
            type = TinySEBPlusTree.file.readInt();
        }

        if(type == -1){
            return TinySEBPlusTree.loadNode(offset);
        }else{
            return TinySEBPlusTree.inMemoryNodes.get(type);
        }
    }

    void insertChild(Integer key, Node child) {
        int loc = Collections.binarySearch(keys, key);
        int childIndex = loc >= 0 ? loc + 1 : -loc - 1;
        if (loc >= 0) {
            children.set(childIndex, child.position);
        } else {
            keys.add(childIndex, key);
            children.add(childIndex + 1, child.position);
        }
    }

    @Override
    Integer getValue(Integer key) throws IOException {
        return getChild(key).getValue(key);
    }

    Integer getValueForSearch(Integer key) throws IOException {
        return getChildForSearch(key).getValue(key);
    }

    @Override
    void insertValue(Integer key, Integer value) throws IOException {
        Node child = getChild(key);
        child.insertValue(key, value);
        if (child.isOverflow()) {
            Node sibling = child.split();
            sibling.save();
            TinySEBPlusTree.num_nodes += 1;
            insertChild(sibling.getFirstLeafKey(), sibling);
            save();
        }
        if (TinySEBPlusTree.root.isOverflow()) {
            Node sibling = split();
            sibling.save();
            TinySEBPlusTree.num_nodes += 1;
            InternalNode newRoot = new InternalNode();
            TinySEBPlusTree.num_nodes += 1;
            newRoot.keys.add(sibling.getFirstLeafKey());
            newRoot.children.add(this.position);
            newRoot.children.add(sibling.position);
            newRoot.save();
            TinySEBPlusTree.root = newRoot;
        }
//        else{
//            child.save(child.position);
//        }
    }

    @Override
    Node split() throws IOException {
        int from = keyNumber() / 2 + 1, to = keyNumber();
        InternalNode sibling = new InternalNode();
        sibling.keys.addAll(keys.subList(from, to));
        sibling.children.addAll(children.subList(from, to + 1));
        keys.subList(from - 1, to).clear();
        children.subList(from, to + 1).clear();
        save();
        return sibling;
    }

    void writeToFileEnd() throws IOException {
        save(((Integer)(TinySEBPlusTree.num_nodes * TinySEBPlusTree.blocksize)).longValue());
    }

    @Override
    Integer getFirstLeafKey() throws IOException {
        return getChild(0).getFirstLeafKey();
    }

    @Override
    boolean isOverflow() {
        return children.size() > TinySEBPlusTree.maxKeys;
    }

    @Override
    boolean isUnderflow() {
        return children.size() < (TinySEBPlusTree.maxKeys + 1) / 2;
    }

    @Override
    Long save(Long offset) throws IOException {
        return null;
    }

    void print(){
        int i;
        for(i=0; i<keyNumber(); i++){
            System.out.print(children.get(i) + "| " + keys.get(i) + " |");
        }
        System.out.print(children.get(i));
        System.out.println();
    }

    Node getChildForSearch(Integer key) throws IOException {
        int loc = Collections.binarySearch(keys, key);
        int childIndex = loc >= 0 ? loc + 1 : -loc - 1;
        Integer offset = children.get(childIndex);
        TinySEBPlusTree.file.seek(offset);
        int type = TinySEBPlusTree.file.readInt();
        int temp;

        while(type == -2){
            int num_keys = TinySEBPlusTree.file.readInt();
            offset = TinySEBPlusTree.file.readInt();
            temp = TinySEBPlusTree.file.readInt();
            num_keys -= 1;
            while(temp < key && num_keys != 0){
                offset = TinySEBPlusTree.file.readInt();
                temp = TinySEBPlusTree.file.readInt();
            }
            if(temp == key || (num_keys == 0 && temp < key)){
                offset = TinySEBPlusTree.file.readInt();
            }
            TinySEBPlusTree.file.seek(offset);
            type = TinySEBPlusTree.file.readInt();
        }

        if(type == -2 || type == -1){
            return TinySEBPlusTree.loadNode(offset);
        }else{
            if(TinySEBPlusTree.forSearchNodes[type] == null){
                TinySEBPlusTree.forSearchNodes[type] = TinySEBPlusTree.loadNode(offset);
            }
            return TinySEBPlusTree.forSearchNodes[type];
        }
    }
}
