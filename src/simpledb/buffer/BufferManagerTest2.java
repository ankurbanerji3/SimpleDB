package simpledb.buffer;
import simpledb.server.SimpleDB;
import simpledb.file.*;
import simpledb.log.LogMgr;

import java.util.*;

public class BufferManagerTest2 {
   private static BufferMgr bm;
   private static LogMgr lm;
   
   public static void main(String args[]) throws Exception {
      SimpleDB db = new SimpleDB("buffermgrtest", 400, 5);
      bm = db.bufferMgr();
      lm = db.logMgr();

      Buffer buff1 = pinBuffer(1);
      Buffer buff2 = pinBuffer(2);
      Buffer buff3 = pinBuffer(3);
      Buffer buff4 = pinBuffer(4);
      Buffer buff5 = pinBuffer(5);

      int lsn1 = createRecords(1);
      modifyPageInBuff(buff5, 80, lsn1);

      int lsn2 = createRecords(2);
      modifyPageInBuff(buff2, 60, lsn2);

      int lsn3 = createRecords(3);
      modifyPageInBuff(buff3, 60, lsn3);

      unpinBuffer(buff3.block());
      unpinBuffer(buff5.block());

      bm.printStatus();

      Buffer buff = pinBuffer(6);

      bm.printStatus();    
      
      int lsn4 = createRecords(4);
      modifyPageInBuff(buff1, 20, lsn4);

      int lsn5 = createRecords(5);
      modifyPageInBuff(buff5, 30, lsn5);

      pinBuffer(3);
      int lsn6 = createRecords(6);
      modifyPageInBuff(buff3, 10, lsn6);

      unpinBuffer(buff3.block());
      unpinBuffer(buff5.block());

      Buffer buff01 = pinBuffer(7);

      bm.printStatus();       
      
      unpinBuffer(buff4.block());

      Buffer buff02 = pinBuffer(5);

      bm.printStatus();      
      
      printTest();
   }
   
   private static Buffer pinBuffer(int i) {
      BlockId blk = new BlockId("test", i);
      Buffer buff = bm.pin(blk);
      System.out.println("Pin block " + i);
      bm.printStatus();
      return buff;
   }
   
   private static void unpinBuffer(BlockId blockId) {
	   
      Buffer buff =  bm.allocated.get(blockId);
      bm.unpin(buff);
      System.out.println("Unpin block " + blockId);
      bm.printStatus();
   }

   private static void modifyPageInBuff(Buffer buff, int byteNum, int lsn) {
      Page p = buff.contents();
      int n = p.getInt(byteNum);
      p.setInt(byteNum, n+1);
      buff.setModified(1, lsn);
      System.out.println("Modify block " + buff.block());
      bm.printStatus();
   }

   private static void printTest() {
      try {
         for(Map.Entry<BlockId, Buffer> set: bm.allocated.entrySet()) {
            System.out.println("For key "+set.getKey()+", the values are "
                    +"Buffer " +set.getValue().getId());
         }
      } catch(Exception e) {
         System.out.println("Could not print the buffer pool contents because: "+e);
      }
   }

   private static int createRecords(int i) {
      byte[] rec = createLogRecord("record"+i, i+100);
      int lsn = lm.append(rec);
      return lsn;
   }

   private static byte[] createLogRecord(String s, int n) {
      int spos = 0;
      int npos = spos + Page.maxLength(s.length());
      byte[] b = new byte[npos + Integer.BYTES];
      Page p = new Page(b);
      p.setString(spos, s);
      p.setInt(npos, n);
      return b;
   }
}

