package simpledb.buffer;

import java.io.*;
import simpledb.file.BlockId;
import simpledb.file.Page;
import simpledb.server.SimpleDB;

public class BufferFileTest {
   public static void main(String[] args) throws IOException {
      SimpleDB db = new SimpleDB("bufferfiletest", 400, 8);
      BufferMgr bm = db.bufferMgr();
      
      
      System.out.println("Before pinning [file testfile, block 2]");
      bm.printStatus();
      BlockId blk = new BlockId("testfile", 2);
      int pos1 = 88;
      Buffer b1 = bm.pin(blk);
      System.out.println("After pinning [file testfile, block 2]");
      bm.printStatus();
      
      
      System.out.println("Before Modifying [file testfile, block 2]");
      bm.printStatus();
      Page p1 = b1.contents();
      p1.setString(pos1, "abcdefghijklm");
      int size = Page.maxLength("abcdefghijklm".length());
      int pos2 = pos1 + size;
      p1.setInt(pos2, 345);
      b1.setModified(1, 0);
      System.out.println("After Modifying [file testfile, block 2]");
      bm.printStatus();
      
      
      System.out.println("Before un-pinning [file testfile, block 2]");
      bm.printStatus();
      bm.unpin(b1);
      System.out.println("After un-pinning [file testfile, block 2]");
      bm.printStatus();
      
      
      System.out.println("Before pinning [file testfile, block 2]");
      bm.printStatus();
      Buffer b2 = bm.pin(blk);
      System.out.println("After pinning [file testfile, block 2]");
      bm.printStatus();

      
      System.out.println("Before un-pinning [file testfile, block 2]");
      bm.printStatus();
      Page p2 = b2.contents();
      System.out.println("offset " + pos2 + " contains " + p2.getInt(pos2));
      System.out.println("offset " + pos1 + " contains " + p2.getString(pos1));
      bm.unpin(b2);
      System.out.println("After un-pinning [file testfile, block 2]");
      bm.printStatus();
   }
}