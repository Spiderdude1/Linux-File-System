
public class SuperBlock {

    public final int defaultInodesBlocks = 64;
    public int totalBlocks;
    public int totalInodes;
    public int freeList;


    public SuperBlock(int diskSize) {
        byte[] SuperBlock = new byte[Disk.blockSize];
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
        byte[] SuperBlock = new byte[Disk.blockSize];
        SysLib.int2bytes(totalBlocks, SuperBlock, 0);
        SysLib.int2bytes(totalInodes, SuperBlock, 4);
        SysLib.int2bytes(freeList, SuperBlock, 8);

        // write back in-memory superblock to disk
        SysLib.rawwrite(0, SuperBlock);
    }

    public void format( int files){
        totalInodes = files;

        // Instantiate and set up the inodes
        for(int i = 0; i < totalInodes; i++) {
            Inode inode = new Inode();
            inode.flag = 0;  // set it to unused
            inode.disk((short) i);
        }

        // byte array for storing the data about the next pointer
        byte[] data = null;
        
        // set up the free blocks
        freeList = (totalInodes / 16) + 2;
        for(int i = freeList; i < totalBlocks; i++) {
            //data array for storing the information about the block
            data = new byte[Disk.blockSize];
            // need to store the next block number into the array
            // each block holds the location of the next block at offset 0
            SysLib.int2bytes(i + 1, data, 0);
            // need to store the next blocks location to this block
            SysLib.rawwrite(i, data);

        }

        // Update the old superblock with the new superblock
        sync();

    }

    public int getFreeBlock() {
        int fBlock = freeList;
        // Checks to make sure that there are any Free blocks to return
        //  
        if(freeList > 0 && freeList < totalBlocks) {
            byte[] block = new byte[Disk.blockSize];
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
        // Validity check: block number has to be positive
        if(oldBlockNumber < 0 ) {
            return false;
        }

        byte[] block = new byte[Disk.blockSize];
        // need to store the current blocks number
        SysLib.int2bytes(freeList, block, 0);
        // store the current head to the offset 0 of the oldblock number
        SysLib.rawwrite(oldBlockNumber, block);
        // set the oldblock number as the new head
        freeList = oldBlockNumber;
        return true;
    }

}