package simpledb.buffer;

import java.util.Comparator;

public class BufferComparator implements Comparator<Buffer>{
    @Override
    public int compare(Buffer b1, Buffer b2){
    	if(b1.getId() == b2.getId())
    		return 0;
        boolean p1 = b1.isPinned(), p2 = b2.isPinned();
        if(!p1 && !p2){
            int l1 = b1.getLsn(), l2 = b2.getLsn();
            if(l1 != l2) {
            	if(l1 == -1) return 1;
            	else if(l2 == -1) return -1;
            	else return l1-l2;
            }
        }else{
            if(!p1){
                return -1;
            }else if(!p2){
                return 1;
            }
        }
        return b1.getId() - b2.getId();
    }
}

