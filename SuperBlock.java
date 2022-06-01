
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

        byte[] SuperBlock = new byte[disk.blockSize];
        SysLib.int2bytes(totalBlocks, SuperBlock, 0);
        SysLib.int2bytes(totalInodes, SuperBlock, 4);
        SysLib.int2bytes(freeList, SuperBlock, 8);

        SysLib.rawwrite(0, SuperBlock);
    }

    public void format( int files){

    }

    public int getFreeBlock() {

    }

    public boolean returnBlock(int oldBlockNumber) {

    }

}