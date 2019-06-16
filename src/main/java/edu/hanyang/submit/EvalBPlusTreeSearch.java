package edu.hanyang.submit;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.net.URL;
import java.net.URLClassLoader;
import edu.hanyang.indexer.BPlusTree;

public class EvalBPlusTreeSearch {
    public static void main(String[] args) throws Exception {
//        if (args.length < 6) {
//            System.out.println("<metafile path> <file path> <block size> <# of blocks> <studnet id> <input file> <student's jar file>");
//            System.exit(1);
//        }

        String metafile = "./metafile.meta";
        String filepath = "./tree.tree";
        int blocksize = 8192;
        int nblocks = 1000;
        String studentID = "111";
        studentID = studentID.split("_")[0];
        String inputfile = "./treetest-15000000.data";
        String filename = "9366020182-0.0.1-SNAPSHOT.jar";

        BufferedWriter bw = new BufferedWriter(new FileWriter(studentID + "_search", true));

        FileInputStream fis = new FileInputStream(inputfile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dis = new DataInputStream(bis);

//		TinySEBPlusTree tree = new TinySEBPlusTree();
        // dynamic loading from student's submission
        File file = new File(filename);
        URL url = file.toURI().toURL();
        URL[] urls = new URL[] {url};
        ClassLoader cl = new URLClassLoader(urls);
        Class<BPlusTree> cls = (Class<BPlusTree>) cl.loadClass("edu.hanyang.submit.TinySEBPlusTree");
        BPlusTree tree = (BPlusTree) cls.newInstance();

        try {
            tree.open(metafile, filepath, blocksize, nblocks);
        } catch (Exception e) {
            bw.write(e.getMessage());
            System.exit(1);
        }

        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
//        for (int i = 0; i < 15000000; i++) {
            int key = dis.readInt();
            int val = dis.readInt();
            try {
                int searchVal = tree.search(key);
                if (searchVal != val) {
                    System.out.println("");
                    System.out.println("value not same");
                    System.out.println("");
                    bw.newLine();
                    tree.close();
                    System.exit(1);
                }
            } catch (Exception e) {
                System.out.println("");
                System.out.println("Delete Error!");
                System.out.println("");
                bw.write(e.getMessage());
                tree.close();
                System.exit(1);
            }
        }
        tree.close();
        long end = System.currentTimeMillis();

        System.out.println((end - start) / 1000.0 + " sec");
        bw.write(nblocks + " " + ((end - start) / 1000.0));
        bw.newLine();
        fis.close();
        bis.close();
        dis.close();

        bw.close();
    }

}
