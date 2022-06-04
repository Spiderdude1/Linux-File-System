
public class SuperBlock {

    public final int defaultInodesBlocks = 64;
    public int totalBlocks;
    public int totalInodes;
    public int freeList;


    public SuperBlock(int diskSize) {
        byte[] SuperBlock = new byte[disk.blockSize];
        SysLib.rawread(0, SuperBlock);

        totalBlocks = SysLib.bytes2int(SuperBlock, 0);
        totalInodes = SysLib.bytes2int(SuperBlock, 4); 
        freeList = SysLib.bytes2int(SuperBlock, 8);
        if(totalBlocks == diskSize && totalInodes > 0 && freeList >= 2) {
            return;
        } 
        else
        {
            totalBlocks = diskSize;
            format(defaultInodesBlocks);
        }
    }

    public void sync() {
        // store the variables to the array
        byte[] SuperBlock = new byte[disk.blockSize];
        SysLib.int2bytes(totalBlocks, SuperBlock, 0);
        SysLib.int2bytes(totalInodes, SuperBlock, 4);
        SysLib.int2bytes(freeList, SuperBlock, 8);

        // write back in-memory superblock to disk
        SysLib.rawwrite(0, SuperBlock);
    }

    public void format( int files){

    }

    public int getFreeBlock() {
        int fBlock = freeList;
        // Checks to make sure that there are any Free blocks to return
        //  
        if(freeList > 0 && freeList < totalBlocks) {
            byte[] block = new byte[disk.blockSize];
            // load block with the contents of the freelist
            SysLib.rawread(freeList, block);
            // dequeues the block and freelist points to the next block
            // offset 0 holds the next block location
            // Block 2 's 0 holds block 3
            freeList = SysLib.bytes2int(block, 0);

            // Reset the the next block location with 0
            SysLib.int2bytes(0,block,0);
            // write the array back to the fBlock holding the original freelist
            SysLib.rawwrite(fBlock, block); 
        }

        return fBlock;
    }

    public boolean returnBlock(int oldBlockNumber) {
        if(oldBlockNumber < 0 ) {
            return false;
        }

        byte[] block = new byte[disk.blockSize];
        SysLib.int2bytes(freeList, block, 0);
        SysLib.rawwrite(oldBlockNumber, block);
        freeList = oldBlockNumber;
        return true;b 
    }

}