package edu.hanyang.submit;

import edu.hanyang.indexer.BPlusTree;

import java.io.*;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.Queue;

import static org.junit.Assert.assertEquals;

public class TinySEBPlusTree implements BPlusTree{
	static Node root;
	static Long root_position;
	static Integer maxKeys;
	static Integer blocksize;
	static RandomAccessFile file;
	static Integer num_nodes;
	String savepath;
	String metapath;

	public TinySEBPlusTree(String savepath, String metapath, Node root, Integer maxKeys) throws FileNotFoundException {
		this.root = root;
		this.maxKeys = maxKeys;
		this.file = new RandomAccessFile(savepath, "rw");
		this.savepath = savepath;
		this.metapath = metapath;
	}

	public TinySEBPlusTree(){
	}

	@Override
	public void close() throws IOException {
		root_position = root.position;
		File meta = new File(metapath);
		if(!meta.exists()){
			meta.createNewFile();
		}else {
			meta.delete();
			meta.createNewFile();
		}
		DataOutputStream is = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(metapath, true), 1024));
		is.writeLong(root_position);
		is.writeInt(maxKeys);
		is.writeInt(blocksize);
		is.close();
		root.save(root_position);
	}

	@Override
	public void insert(int arg0, int arg1) throws IOException {
		root.insertValue(arg0, arg1);
	}

	@Override
	public void open(String metapath, String savepath, int blocksize, int nblocks) throws IOException {
		this.metapath = metapath;
		this.savepath = savepath;
		this.blocksize = blocksize;
		maxKeys = (blocksize - 12) / 8;
		root_position = new Long(0);

		File meta = new File(metapath);
		if(meta.exists()){
			DataInputStream is = new DataInputStream(new BufferedInputStream(new FileInputStream(metapath), 1024));
			root_position = is.readLong();
			maxKeys = is.readInt();
			this.blocksize = is.readInt();
		}else{
			meta.createNewFile();
		}

		File save = new File(savepath);
		if(!save.exists()){
			save.createNewFile();
		}
		file = new RandomAccessFile(savepath, "rw");
		TinySEBPlusTree.num_nodes = 0;
		if(file.length() > 0){
			TinySEBPlusTree.root = loadNode(root_position);
		}else{
			TinySEBPlusTree.root = new LeafNode();
		}
		TinySEBPlusTree.num_nodes += 1;
	}

	@Override
	public int search(int arg0) throws IOException {
		return root.getValue(arg0);
	}

	public static Node loadNode(Long offset) throws IOException {
		file.seek(offset);
		int type = file.readInt();
		if(type == 1){
			return new LeafNode(offset);
		}else{
			return new InternalNode((offset));
		}
	}

	public void print(String path) throws IOException {
		DataInputStream is = new DataInputStream(new BufferedInputStream(new FileInputStream(path), 1024));
		while(is.available() > 0){
			System.out.print(is.readInt() + "-");
		}
		System.out.println();
	}

	public static void main(String[] args) throws IOException {
		String savepath = "btree.txt";
		String metapath = "meta.txt";
		File save = new File(savepath);
		if(save.exists()){
			save.delete();
		}
		save = new File(metapath);
		if(save.exists()){
			save.delete();
		}
		int blocksize = 44;
		int nblocks = 100;
//		TinySEBPlusTree.file = new RandomAccessFile(savepath, "rw");
//		TinySEBPlusTree.blocksize = 44;
//		TinySEBPlusTree.num_nodes = 0;
//		Node root = new LeafNode();
//		TinySEBPlusTree.num_nodes = 1;
//		TinySEBPlusTree tree = new TinySEBPlusTree(savepath, metapath, root,4);
//		tree.open(metapath, savepath, 44, 10);

		TinySEBPlusTree tree = new TinySEBPlusTree();
		tree.open(metapath, savepath, blocksize, nblocks);
		tree.insert(5, 10);
		tree.insert(6, 15);
		tree.insert(4, 20);
		tree.insert(7, 1);
		tree.insert(8, 5);
		tree.insert(17, 7);
		tree.insert(30, 8);
		tree.insert(1, 8);
		tree.insert(58, 1);
		tree.insert(25, 8);
		tree.insert(96, 32);
		tree.insert(21, 8);
		tree.insert(9, 98);
		tree.insert(57, 54);
		tree.insert(157, 54);
		tree.insert(247, 54);
		tree.insert(357, 254);
		tree.insert(557, 54);


		tree.close();
		tree.open(metapath, savepath, blocksize, nblocks);
		assertEquals(tree.search(5), 10);
		assertEquals(tree.search(6), 15);
		assertEquals(tree.search(4), 20);
		assertEquals(tree.search(7), 1);
		assertEquals(tree.search(8), 5);
		assertEquals(tree.search(17), 7);
		assertEquals(tree.search(30), 8);
		assertEquals(tree.search(1), 8);
		assertEquals(tree.search(58), 1);
		assertEquals(tree.search(25), 8);
		assertEquals(tree.search(96), 32);
		assertEquals(tree.search(21), 8);
		assertEquals(tree.search(9), 98);
		assertEquals(tree.search(57), 54);
		assertEquals(tree.search(157), 54);
		assertEquals(tree.search(247), 54);
		assertEquals(tree.search(357), 254);
		assertEquals(tree.search(557), 54);


//		System.out.println(TinySEBPlusTree.root.position);
//		((InternalNode)TinySEBPlusTree.root).print();
//		((LeafNode)loadNode(new Long(0))).print();
//		((LeafNode)loadNode(new Long(44))).print();
//		((LeafNode)loadNode(new Long(132))).print();

//		System.out.println(tree.search(5));


//		tree.insert(5, 10);
//		tree.insert(6, 15);
//		tree.insert(4, 20);
//		tree.insert(7, 1);
//		tree.insert(8, 5);
//		tree.insert(17, 7);
//		tree.insert(30, 8);
//		tree.insert(1, 8);
//		tree.insert(58, 1);
//		tree.insert(25, 8);
//		tree.insert(96, 32);
//		tree.insert(21, 8);
//		tree.insert(9, 98);
//		tree.insert(57, 54);
//		tree.insert(157, 54);
//		tree.insert(247, 54);
//		tree.insert(357, 254);
//		tree.insert(557, 54);

//		System.out.println(TinySEBPlusTree.root.keyNumber());
//		System.out.println(((InternalNode)TinySEBPlusTree.root).children.size());
//		System.out.println(((InternalNode)TinySEBPlusTree.root).children.get(3).keyNumber());

//		tree.insert(1, 10);
//		tree.insert(2, 10);
//		tree.insert(3, 10);
//		tree.insert(4, 10);
//		tree.insert(5, 10);
//		tree.insert(6, 10);
//		tree.insert(7, 10);
//		System.out.println(TinySEBPlusTree.root.position);
//		((InternalNode)TinySEBPlusTree.root).print();
//
//		((LeafNode)loadNode(new Long(0))).print();
//		((LeafNode)loadNode(new Long(44))).print();
//		((LeafNode)loadNode(new Long(132))).print();

//		file.seek(new Long(44));
//		System.out.println(file.readInt());

//		assertEquals(tree.search(1), 10);
//		assertEquals(tree.search(3), 10);
//		assertEquals(tree.search(6), 10);

//		tree.close();
//		((InternalNode)TinySEBPlusTree.root).print();
//		LeafNode node = (LeafNode)((InternalNode)TinySEBPlusTree.root).children.get(2);
//		tree.print(savepath);

	}
}
