package simpledb.buffer;

import java.util.HashMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import simpledb.file.*;
import simpledb.log.LogMgr;

/**
 * Manages the pinning and unpinning of buffers to blocks.
 * @author Edward Sciore
 *
 */

public class BufferMgr {
	//LRM cache for unallocated buffers
   private NavigableSet<Buffer> unallocated;
   private int numAvailable;
   private static final long MAX_TIME = 10000; // 10 seconds
   //map of allocated buffers
   public HashMap<BlockId, Buffer> allocated;
   
   /**
    * Creates a buffer manager having the specified number 
    * of buffer slots.
    * This constructor depends on a {@link FileMgr} and
    * {@link simpledb.log.LogMgr LogMgr} object.
    * @param numbuffs the number of buffer slots to allocate
    */
   public BufferMgr(FileMgr fm, LogMgr lm, int numbuffs) {
      numAvailable = numbuffs;
      allocated = new HashMap<BlockId, Buffer>();
      unallocated = new TreeSet<Buffer>(new BufferComparator());
      int i;
      for (i=0; i<numbuffs; i++){
         unallocated.add(new Buffer(fm, lm, i));
      }
      System.out.println("Initializing Buffer Manager With "+ unallocated.size() +" Buffers.");
      printStatus();
   }
   
   public void addBuffer(Buffer buff){
	   unallocated.add(buff);
   }
   public void removeBuffer(Buffer buff){
	   unallocated.remove(buff);
   }
   
   /**
    * Returns the number of available (i.e. unpinned) buffers.
    * @return the number of available buffers
    */
   public synchronized int available() {
      return numAvailable;
   }
   
   /**
    * Flushes the dirty buffers modified by the specified transaction.
    * @param txnum the transaction's id number
    */
   public synchronized void flushAll(int txnum) {
      for (Buffer buff : unallocated)
         if (buff.modifyingTx() == txnum)
         buff.flush();
   }
   
   
   /**
    * Unpins the specified data buffer. If its pin count
    * goes to zero, then notify any waiting threads.
    * @param buff the buffer to be unpinned
    */
   public synchronized void unpin(Buffer buff) {
	   buff.unpin();
      if (!buff.isPinned()) {
    	 unallocated.add(buff);
         numAvailable++;
//         BlockId blk = buff.block();
//         allocated.remove(blk);
         notifyAll();
      }
   }
   
   /**
    * Pins a buffer to the specified block, potentially
    * waiting until a buffer becomes available.
    * If no buffer becomes available within a fixed 
    * time period, then a {@link BufferAbortException} is thrown.
    * @param blk a reference to a disk block
    * @return the buffer pinned to that block
    */
   public synchronized Buffer pin(BlockId blk) {
      try {
         long timestamp = System.currentTimeMillis();
         Buffer buff = tryToPin(blk);
         while (buff == null && !waitingTooLong(timestamp)) {
            wait(MAX_TIME);
            buff = tryToPin(blk);
         }
         if (buff == null)
            throw new BufferAbortException();
         return buff;
      }
      catch(InterruptedException e) {
         throw new BufferAbortException();
      }
   }  

   public void printStatus(){
	  System.out.println("---------------------------------------------------------------------------------------------------------");
	  System.out.println("Allocated Buffers:");
	  boolean seen = false;
      for(BlockId blk : allocated.keySet()){
    	 seen = true;
         Buffer buff = allocated.get(blk);
         String pinned = buff.isPinned() ? "  pinned" : "unpinned";
//         System.out.println("Buffer "+ buff.getId() + ": "+blk.toString() + " " + pinned +" LSN: " + buff.getLsn() + " Pins = " + buff.getPins() + 
//        		 " Transaction = " + buff.modifyingTx());
         System.out.println("Buffer "+ buff.getId() + ": "+blk.toString() + " " + pinned +" lsn = " + buff.getLsn());
         
      }
      if(!seen)System.out.println("There are no pinned buffers at the moment.");
      seen = false;
      int skipped = 0;
      System.out.print("\nUnpinned Buffers in LRM order:");
      for(Buffer buff : unallocated){
    	 seen = true;
   
    	 BlockId blk = buff.block();
    	 String pinned = buff.isPinned() ? "pinned" : "unpinned";
    	 String blkString = blk == null ? "[EMPTY]" : blk.toString();
    	 if(buff.getLsn() != -1) {
//    		 System.out.println("Buffer "+ buff.getId() + ": "+blkString+ " " + pinned +" LSN: " + buff.getLsn() + " Pins = " + buff.getPins() + 
//        		 " Transaction = " + buff.modifyingTx());
    		 System.out.print(" "+buff.getId()+"(lsn = "+buff.getLsn()+")"+" ");
    	 }else {
    		 skipped += 1;
    	 }
      }
      if(skipped > 0)System.out.println("\nThere are "+ skipped +" unmodified buffers.");
      if(!seen)System.out.println("\nThere are no unpinned buffers at the moment.");
      System.out.println("\n---------------------------------------------------------------------------------------------------------");
   }
   
   private boolean waitingTooLong(long starttime) {
      return System.currentTimeMillis() - starttime > MAX_TIME;
   }
   
   /**
    * Tries to pin a buffer to the specified block. 
    * If there is already a buffer assigned to that block
    * then that buffer is used;  
    * otherwise, an unpinned buffer from the pool is chosen.
    * Returns a null value if there are no available buffers.
    * @param blk a reference to a disk block
    * @return the pinned buffer
    */
   private Buffer tryToPin(BlockId blk) {
      Buffer buff = findExistingBuffer(blk);
      if (buff == null) {
         buff = chooseUnpinnedBuffer();
         if (buff == null)
            return null;
         if(buff.getUsedFlag() && buff.getLsn()==-1) return null;
         BlockId blk1 = buff.block();
         if(blk1 != null && allocated.containsKey(blk1)) allocated.remove(blk1);
         buff.assignToBlock(blk);
         allocated.put(blk, buff);
         buff.setLsn(-1);
      }
      if (!buff.isPinned())
         numAvailable--;
      
      unallocated.remove(buff);
      buff.pin();
      
      return buff;
   }
   
   private Buffer findExistingBuffer(BlockId blk) {
      if(allocated.containsKey(blk)) return allocated.get(blk);
      return null;
   }
   
   private Buffer chooseUnpinnedBuffer() {
      Buffer buff = unallocated.pollFirst();
         if (!buff.isPinned())
            return buff;
      return null;
   }

}
