package challenge2;


import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

// reference https://algs4.cs.princeton.edu/home/
// author : Yuxue Piao

public class SuffixArrayBinary {
    private Suffix[] suffixes;

    public SuffixArrayBinary(byte[] fileBytes) {
        int n = fileBytes.length;
        this.suffixes = new Suffix[n];
        for (int i = 0; i < n; i++)
            suffixes[i] = new Suffix(fileBytes, i);
        Arrays.sort(suffixes);
    }

    private static class Suffix implements Comparable<Suffix> {
        private final byte[] fileBytes;
        private final int index;

        private Suffix(byte[] fileBytes, int index) {
            this.fileBytes = fileBytes;
            this.index = index;
        }
        private int length() {
            return fileBytes.length - index;
        }
        private byte charAt(int i) {
            return fileBytes[index + i];
        }

        public int compareTo(Suffix that) {
            if (this == that) return 0;  // optimization
            int n = Math.min(this.length(), that.length());
            for (int i = 0; i < n; i++) {
                if (this.charAt(i) < that.charAt(i)) return -1;
                if (this.charAt(i) > that.charAt(i)) return +1;
            }
            return this.length() - that.length();
        }

        public String toString() {

            return Hex.encodeHexString(Arrays.copyOfRange(fileBytes, index, fileBytes.length));
        }
    }


    public int length() {
        return suffixes.length;
    }



    public int index(int i) {
        if (i < 0 || i >= suffixes.length) throw new IllegalArgumentException();
        return suffixes[i].index;
    }



    public int lcp(int i) {
        if (i < 1 || i >= suffixes.length) throw new IllegalArgumentException();
        return lcpSuffix(suffixes[i], suffixes[i-1]);
    }

    private static byte[] lcp(byte[] s, int p, byte[] t, int q) {
        int n = Math.min(s.length - p, t.length - q);
        for (int i = 0; i < n; i++) {
            if (s[p + i] != t[q + i]) {

                return Arrays.copyOfRange(s, p, p + i);
            }

        }


        return Arrays.copyOfRange(s, p, p + n);
    }


    public static ArrayList<FileInfo> lcs2(ArrayList<byte[]> inputs) {


        SuffixArrayBinary[] suffixArrays = new SuffixArrayBinary[inputs.size()];

        for (int i = 0 ; i < inputs.size(); i++) {
            suffixArrays[i] = new SuffixArrayBinary(inputs.get(i));
        }


        ArrayList<FileInfo> lcsList = new ArrayList<>();
        for (int k = 0; k < inputs.size(); k++) {
            for (int t = k + 1; t < inputs.size(); t++) {
                byte[] lcs = new byte[0];
                int i = 0, j = 0;
                int p = 0, q = 0;
                int fp = 0, fq = 0;


                while (i < inputs.get(k).length && j < inputs.get(t).length) {
                    p = suffixArrays[k].index(i);
                    q = suffixArrays[t].index(j);
                    byte[] x = lcp(inputs.get(k), p, inputs.get(t), q);
                    if (x.length > lcs.length) {
                        lcs = x;
                        fp = p;
                        fq = q;
                    }
                    if (compare(inputs.get(k), p, inputs.get(t), q) < 0) i++;
                    else                         j++;
                }

                lcsList.add(new FileInfo(k, t, fp, fq, lcs));
            }
        }


        return lcsList;
    }

    static class FileInfo implements Comparable<FileInfo>{
        int firstFileIndex;
        int secondFileIndex;
        int offset1;
        int offset2;

        byte[] longestCommonSubBytes;

        public FileInfo() {

        }


        public FileInfo(int firstFileIndex, int secondFileIndex, int offset1, int offset2, byte[] longestCommonSubBytes) {
            this.firstFileIndex = firstFileIndex;
            this.offset1 = offset1;
            this.secondFileIndex = secondFileIndex;
            this.offset2 = offset2;
            this.longestCommonSubBytes = longestCommonSubBytes;
        }

        public int compareTo(FileInfo that) {
            if (this == that) return 0;
            return this.longestCommonSubBytes.length - that.longestCommonSubBytes.length;
        }
    }


    private static int lcpSuffix(Suffix s, Suffix t) {
        int n = Math.min(s.length(), t.length());
        for (int i = 0; i < n; i++) {
            if (s.charAt(i) != t.charAt(i)) return i;
        }
        return n;
    }


    public String select(int i) {
        if (i < 0 || i >= suffixes.length) throw new IllegalArgumentException();
        return suffixes[i].toString();
    }




    // compare query string to suffix
    private static int compare(byte[] s, int p, byte[] t, int q) {
        int n = Math.min(s.length - p, t.length - q);
        for (int i = 0; i < n; i++) {
            if (s[p + i] != t[q + i])
                return s[p+i] - t[q+i];
        }
        if      (s.length - p < t.length - q) return -1;
        else if (s.length - p > t.length - q) return +1;
        else                                  return  0;
    }


    public static void main(String[] args) throws IOException {

        File dir = new File(args[0]);

        ArrayList<byte[]> bytesList = new ArrayList<>();
        File[] readFiles = dir.listFiles();
        for (File eachFile : readFiles) {

            byte[] eachFilebytes = FileUtils.readFileToByteArray(eachFile);
            bytesList.add(eachFilebytes);
        }

        ArrayList<FileInfo> result2 = lcs2(bytesList);


        FileInfo finalResult = Collections.max(result2);

        System.out.println("The most longest strand bytes exists in the following files.");

        System.out.println(readFiles[finalResult.firstFileIndex].getName() + " : The offset is "
                + finalResult.offset1 + " : "
                + readFiles[finalResult.secondFileIndex].getName() + " : The offset is "
                + finalResult.offset2 + " : "
                + finalResult.longestCommonSubBytes.length
        );

    }

}