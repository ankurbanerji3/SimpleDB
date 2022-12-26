package simpledb.buffer;

import simpledb.server.SimpleDB;
import simpledb.file.*;
import simpledb.log.LogMgr;

import java.util.*;

public class BufferManagerTest1 {
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
      
      bm.printStatus();//1
      
      int lsn1 = createRecords(1);
      modifyPageInBuff(buff5, 80, lsn1);

      int lsn2 = createRecords(2);
      modifyPageInBuff(buff2, 60, lsn2);

      int lsn3 = createRecords(3);
      modifyPageInBuff(buff3, 60, lsn3);
      
      bm.printStatus();//2
      
      System.out.println("Allocted map contents: "+ bm.allocated.keySet().toString());
      unpinBuffer(3);
      System.out.println("Allocted map contents: "+ bm.allocated.keySet().toString());
      unpinBuffer(5);
      System.out.println("Allocted map contents: "+ bm.allocated.keySet().toString());

      bm.printStatus();//3

      Buffer buff = pinBuffer(6);

      bm.printStatus();//4    
      
      int lsn4 = createRecords(4);
      modifyPageInBuff(buff1, 20, lsn4);

      int lsn5 = createRecords(5);
      modifyPageInBuff(buff5, 30, lsn5);
      
      bm.printStatus();//5
      
      System.out.println("Allocted map contents: "+ bm.allocated.keySet().toString());
      pinBuffer(3);
      bm.printStatus();//6
      int lsn6 = createRecords(6);
      modifyPageInBuff(buff3, 10, lsn6);
      bm.printStatus();//7

      unpinBuffer(3);
      bm.printStatus();//8
      
      bm.printStatus();//9
      
      unpinBuffer(6);

      Buffer buff01 = pinBuffer(7);

      bm.printStatus();//10       
      
      unpinBuffer(4);

      Buffer buff02 = pinBuffer(5);

      bm.printStatus(); //11     
      
      printTest();
   }
   
   private static Buffer pinBuffer(int i) {
      BlockId blk = new BlockId("test", i);
      Buffer buff = bm.pin(blk);
      bm.allocated.put(blk, buff);
      System.out.println("Pin block " + i);
      return buff;
   }
   
   private static void unpinBuffer(int i) {
      BlockId blk = new BlockId("test", i);
      Buffer buff = bm.allocated.remove(blk);
      bm.unpin(buff);
      System.out.println("Unpin block " + i);
   }

   private static void modifyPageInBuff(Buffer buff, int byteNum, int lsn) {
      Page p = buff.contents();
      int n = p.getInt(byteNum);
      p.setInt(byteNum, n+1);
      buff.setModified(1, lsn);
      System.out.println("Modify block " + buff.block());
   }

   private static void printTest() {
      try {
         for(Map.Entry<BlockId, Buffer> set: bm.allocated.entrySet()) {
            System.out.println("For key "+set.getKey().toString()+", the values are "
                    + set.getValue().getId());
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

