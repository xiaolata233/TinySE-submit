package edu.hanyang.submit;
import static org.junit.Assert.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;


import edu.hanyang.indexer.ExternalSort;
import edu.hanyang.utils.DiskIO;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.MutableTriple;

public class TinySEExternalSort implements ExternalSort {
	int memo_size;
	int n_elements;
	int buffer_size;
	int block_elements;
	int n_way_merge;
	int memo_elements;
	String tmpdir;
	public int getSize(String infile){
		File f = new File(infile);
		int len = (int)f.length();
		return len;
	}

	public static int read_array(DataInputStream in, int nelements, ArrayList<MutableTriple<Integer, Integer, Integer>> arr) throws IOException {
		arr.clear();
		MutableTriple<Integer, Integer, Integer> mt;
		for(int cnt = 0; cnt < nelements; ++cnt) {
			mt = new MutableTriple<>();
			try {
				mt.setLeft(in.readInt());
			} catch (EOFException var5) {
				return cnt;
			}
			mt.setMiddle(in.readInt());
			mt.setRight(in.readInt());
			arr.add(mt);
		}
		return nelements;
	}

	public void merge_use_tree(ArrayList<DataInputStream> files, Integer run, Integer part) throws IOException {
		ArrayList<DataManager> DMs = new ArrayList<>();
		for(DataInputStream file: files){
			DMs.add(new DataManager(file));
		}
		Tree queue = new Tree(DMs);
		ArrayList<MutableTriple<Integer, Integer, Integer>> result = new ArrayList<>();
		String output_path = tmpdir + "/run_" + Integer.toString(run) + "/" + Integer.toString(part) + ".data";
		DataOutputStream os = open_output_run(output_path, buffer_size);
		while(queue.getSize() != 0){
			MutableTriple<Integer, Integer, Integer> temp = queue.pop();
			result.add(temp);
			if(result.size() == memo_elements){
				DiskIO.append_arr(os, result, result.size());
				os.flush();
				result.clear();
			}
		}
		if(result.size() > 0){
			DiskIO.append_arr(os, result, result.size());
			os.flush();
		}
		os.close();
	}

	public void merge_use_tree(ArrayList<DataInputStream> files, String path) throws IOException {
		ArrayList<DataManager> DMs = new ArrayList<>();
		for(DataInputStream file: files){
			DMs.add(new DataManager(file));
		}
		Tree queue = new Tree(DMs);
		ArrayList<MutableTriple<Integer, Integer, Integer>> result = new ArrayList<>();
		DataOutputStream os = open_output_run(path, buffer_size);
		while(queue.getSize() != 0){
			MutableTriple<Integer, Integer, Integer> temp = queue.pop();
			result.add(temp);
			if(result.size() == memo_elements){
				DiskIO.append_arr(os, result, result.size());
				os.flush();
				result.clear();
			}
		}
		if(result.size() > 0){
			DiskIO.append_arr(os, result, result.size());
			os.flush();
		}
		os.close();
	}

	public void merge(ArrayList<DataInputStream> files, Integer run, Integer part) throws IOException {
		PriorityQueue<DataManager> queue = new PriorityQueue<>();
		ArrayList<MutableTriple<Integer, Integer, Integer>> result = new ArrayList<>();
		DataManager dm;
		String output_path = tmpdir + "/run_" + Integer.toString(run) + "/" + Integer.toString(part) + ".data";
		DataOutputStream os = open_output_run(output_path, buffer_size);
		for(DataInputStream file: files){
			queue.add(new DataManager(file));
		}
		while(queue.size() != 0){
			dm = queue.poll();
			result.add(dm.pop());
			if(result.size() == memo_elements){
				DiskIO.append_arr(os, result, result.size());
				os.flush();
				result.clear();
			}
			if(!dm.isEmpty()){
				queue.add(dm);
			}
		}
		if(result.size() > 0){
			DiskIO.append_arr(os, result, result.size());
			os.flush();
		}
		os.close();
	}
	public void merge(ArrayList<DataInputStream> files, String path) throws IOException {
		PriorityQueue<DataManager> queue = new PriorityQueue<>();
		ArrayList<MutableTriple<Integer, Integer, Integer>> result = new ArrayList<>();
		DataManager dm;
		DataOutputStream os = open_output_run(path, buffer_size);
		for(DataInputStream file: files){
			queue.add(new DataManager(file));
		}
		while(queue.size() != 0){
			dm = queue.poll();
			result.add(dm.pop());
			if(result.size() == memo_elements){
				DiskIO.append_arr(os, result, result.size());
				os.flush();
				result.clear();
			}
			if(!dm.isEmpty()){
				queue.add(dm);
			}
		}
		if(result.size() > 0){
			DiskIO.append_arr(os, result, result.size());
			os.flush();
		}
		os.close();
	}

	public static DataInputStream open_input_run(String filepath, int buffersize) throws FileNotFoundException {
		return new DataInputStream(new BufferedInputStream(new FileInputStream(filepath), buffersize));
	}

	public static DataOutputStream open_output_run(String filepath, int buffersize) throws IOException {
		File file = new File(filepath);
		File parentFile = file.getParentFile();
		if(!parentFile.exists()){
			parentFile.mkdir();
		}
		if(!file.exists()){
			file.createNewFile();
		}
		return new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filepath, true), buffersize));
	}
	public void sort(String infile, String outfile, String tmpdir, int blocksize, int nblocks) throws IOException {
		memo_size = (int) (blocksize * nblocks);
		n_elements = (int) memo_size / 12;
		buffer_size = 4096;
		block_elements = (int) blocksize / 12;
		memo_elements = (int) 1024 * 64 / 12;
		n_way_merge = nblocks > 64 ? 64 : nblocks;
		this.tmpdir = tmpdir;
		File file = new File(tmpdir);
		if (!file.exists()) {
			file.mkdir();
		}
		DataInputStream is = DiskIO.open_input_run(infile, blocksize);
		DataOutputStream os;
		ArrayList<MutableTriple<Integer, Integer, Integer>> arr = new ArrayList<>();

		long timestamp = System.currentTimeMillis();
		// ---------------- initial run -------------------
		int run = 1;
		int part = 1;
		String output_path;
		while (is.available() > 0) {
			if (is.available() < memo_elements) {
				read_array(is, (int) is.available() / 12, arr);
			} else {
				read_array(is, memo_elements, arr);
			}
			DiskIO.sort_arr(arr, arr.size());
			part += 1;
			output_path = tmpdir + "/run_" + Integer.toString(run) + "/" + Integer.toString(part) + ".data";
			os = open_output_run(output_path, buffer_size);
			DiskIO.append_arr(os, arr, arr.size());
			os.flush();
		}

		System.out.println("time duration: " + (System.currentTimeMillis() - timestamp) + " msecs");
		timestamp = System.currentTimeMillis();
		// ----------------- merge ------------------
		String prev_run = tmpdir + "/run_" + Integer.toString(run);
		File[] prev_files = new File(prev_run).listFiles();
		ArrayList<DataInputStream> file_to_merge = new ArrayList<>();
		while (prev_files.length > 1) {
			part = 1;
			run += 1;
			for (int i = 0; i < prev_files.length; i++) {
				file_to_merge.add(open_input_run(prev_files[i].getAbsolutePath(), buffer_size));
				if (file_to_merge.size() == n_way_merge) {
					merge_use_tree(file_to_merge, run, part);
					file_to_merge.clear();
					part += 1;0.
				}
			}
			if(prev_files.length < n_way_merge){
				merge_use_tree(file_to_merge, outfile);
				file_to_merge.clear();
				break;
			}
			if (file_to_merge.size() > 0) {
				merge_use_tree(file_to_merge, run, part);
				file_to_merge.clear();
			}
			prev_run = tmpdir + "/run_" + Integer.toString(run);
			prev_files = new File(prev_run).listFiles();
		}
		System.out.println("time duration: " + (System.currentTimeMillis() - timestamp) + " msecs");
	}

	public static void main(String[] args) throws IOException {
		int blocksize = 1024;
		int nblocks = 160;
		String infile = "./src/test/resources/test.data";
		String outfile = "./sorted.data";
		String tmpdir = "./tmp";

		File resultFile = new File(outfile);
		if(!resultFile.exists()) {
			try {
				resultFile.createNewFile();
			} catch (IOException e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}

		TinySEExternalSort sort = new TinySEExternalSort();
		long timestamp = System.currentTimeMillis();
		//sort.sort(infile, outfile, tmpdir, blocksize, nblocks);
		//sort.test("sorted.data", "answer.data");
		//System.out.println(sort.countElement(open_input_run(infile, 1024)));

//		DataInputStream is = open_input_run("./tmp/run_1/24.data", 1024);
//		int i = 0;
//		while(i < 5){
//			System.out.println("" + is.readInt() + ", " + is.readInt() + ", " + is.readInt());
//			i += 1;
//		}

//		File[] prev_files = new File("./tmp/run_2").listFiles();
//		System.out.println(prev_files.length);
	}
}

// 102个MT = 11264 Bytes。 1个MT = 11
// total triple = 1,000,000