package simpledb.buffer;

import simpledb.server.SimpleDB;
import simpledb.file.*;

public class BufferTest {
   public static void main(String[] args) {
      SimpleDB db = new SimpleDB("buffertest", 400, 3); // only 3 buffers
      BufferMgr bm = db.bufferMgr();

      System.out.println("Before pinning [file testfile, block 1]");
      bm.printStatus();
      Buffer buff1 = bm.pin(new BlockId("testfile", 1));
      System.out.println("After pinning [file testfile, block 1]");
      bm.printStatus();
      
      
      System.out.println("Before modifying [file testfile, block 1]");
      bm.printStatus();
      Page p = buff1.contents();
      int n = p.getInt(80);
      p.setInt(80, n+1); 
//      bm.removeBuffer(buff1);
      buff1.setModified(1, 0); //placeholder values
//      bm.addBuffer(buff1);
      System.out.println("The new value is " + (n+1));
      System.out.println("After modifying [file testfile, block 1]");
      bm.printStatus();
      
      
      System.out.println("Before un-pinning [file testfile, block 1]");
      bm.printStatus();
      bm.unpin(buff1);
      System.out.println("After un-pinning [file testfile, block 1]");
      bm.printStatus();
      
      
      System.out.println("Before pinning [file testfile, block 2], [file testfile, block 3] and [file testfile, block 4]");
      bm.printStatus();
      // One of these pins will flush buff1 to disk:
      Buffer buff2 = bm.pin(new BlockId("testfile", 2));
      Buffer buff3 = bm.pin(new BlockId("testfile", 3));
      Buffer buff4 = bm.pin(new BlockId("testfile", 4));
      System.out.println("After pinning [file testfile, block 2], [file testfile, block 3] and [file testfile, block 4]");
      bm.printStatus();
      
      
      System.out.println("Before un-pinning [file testfile, block 2]");
      bm.printStatus();
      bm.unpin(buff2);
      System.out.println("After un-pinning [file testfile, block 2]");
      bm.printStatus();
     
      
      System.out.println("Before pinning [file testfile, block 1]");
      bm.printStatus();
      buff2 = bm.pin(new BlockId("testfile", 1));
      System.out.println("After pinning [file testfile, block 1]");
      bm.printStatus();
      
      
      System.out.println("Before modifying [file testfile, block 1]");
      bm.printStatus();
      Page p2 = buff2.contents();
      p2.setInt(80, 9999);     // This modification
//      bm.removeBuffer(buff2);
      buff2.setModified(1, 0); // won't get written to disk.
//      bm.addBuffer(buff2);
      System.out.println("After modifying [file testfile, block 1]");
      bm.printStatus();
   }
}
