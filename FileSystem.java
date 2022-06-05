public class FileSystem {
    private SuperBlock superblock;
    private Directory directory;
    private FileStructureTable FileTable;

    public FileSystem ( int disBlocks ) {
        superblock = new SuperBlock ( diskBlocks );
        directory = new Directory ( superblock.totalInodes );
        filltable = new FileSturctureTable ( directory );
        // read the "/" file from disk
        FiletableEntry dirEnt = open ( "/", "r" );
        int dirSize = fsize ( dirEnt );
        if ( dirSize > 0 ) {
            // the directory has some data.
            byte[] dirData = new byte[dirSize];
            read( dirEnt, dirDate );
            directory.bytes2directory ( dirdata );
        } 
        close (dirEnt);
    }

    // sync the filesystem to the disk with superblock and directory
    void sync() {
        // update directory to the disk
        FileTableEntry dirEntry = open ( "/", "w" );
        byte[] dirData = directory.directory2bytes();
        write( dirEnty, dirData );
        close( dirEntry );
        // update (sycn) superblock to disk
        superBlock.sync();
    }

    // format the disk
    boolean format (int fCount ) {
        // format superblock accroding to fCount
        superblock.format( fCount );
        // create directory. create filetable new directory.
        directory = new Directory( superblock.inodeBlocks );
        FileTable = new FileTable( directory );
        return true;
    }

    FileTableEnyty open ( String filename, String mode ) {
        FiletableEnytu ftEnt = fileTable.falloc( filename, mode );
        if ( mode.equals( "w" ) ) {
            if ( deallocAllBlocks ( ftEnt ) == false ) {
                return null;
            }
        }
        return ftEnt;
    }
    
    // close file update entry count and free entry
    boolean close ( FileTableEntry ftEnt ) {
        synchronized(ftEnt) {
            // decrement ftEnt count
            ftEnt.count--;
            // free entry from table
            if (ftEnt.count == 0){
                return FileTable.ffree(ftEnt);
            }
            return true;
        }
    }

    // read block and set buffer size accroding to data size.
    // return the number of byes to read or false (-1);
    int read ( FileTableEntry ftEnt, byte[] buffer ) {
        if ( ftEnt == null || ftEnt.mode.eqauls( "w" ) || ftEnt.mode.eqauls( "a" ) ){
            return -1;
        }
        synchronized (ftEnt){
            // size of data to read
            int bsize = buffer.length;
            // track data to read
            int result = 0;
            while ( (bsize > 0) && (ftEnt.seekPtr < fsize(ftEnt)) ){
                // block to read
                int block = ftEnt.inode.findTargetBlock(ftEnt.seekPtr);
                if ( block != -1 ) {
                    // read the block
                    byte[] bdata = new byte[Disk.blockSize];
                    SysLib.rawread( block, bdata );
                    int start = ftEnt.seekptr  % Disk.blockSize;
                    // update how much to read according to filesize, block size and updated buffer size
                    int lblock = Disk.blockSize - start;
                    int lfile = fsize(ftEnt) - ftEnt.seekPtr;
                    int size = 0;
                    if ( lblock < lfile ) {
                        size = lblock;
                    }
                    else {
                        size = lfile;
                    }
                    if ( size > bsize ){
                        size = bsize;
                    }
                    // copy block data to buffer
                    System.arraycopy ( bdata, start, buffer, result, size);
                    // update seekptr, total bytes to read (result) and buffer size
                    ftEnt.seekPtr += size;
                    result += size;
                    bsize -= size;
                }
            }
            return result;
        }
    }

    // update seek pointer for the table entry
    int seek ( FileTableEntry ftEnt, int offset, int whence ) {
        if (ftEnt == null ){
            return -1;
        }
        synchronized (ftEnt) {
            switch ( whence ) {
                // set offset from start
                case 0:
                ftEnt.seekPtr = offset;
                break;
                // set offset to current
                case 1:
                ftEnt.seekPtr += offset;
                break;
                // set offset from end
                case 2:
                ftEnt.seekPtr = offset + fsize(ftEnt);
                break;
            }
        }
        // update and return ftEnt.seekPtr with correct size
        if (ftEnt.seekPtr < 0){
            ftEnt.seekPtr = 0;
        }
        if ( fsize(ftEnt) < ftEnt.seekPtr ) {
            ftEnt.seekPtr = fsize(ftEnt);
        }
        return ftEnt.seekPtr;
    }

    // write block and set buffer size accroding to data size.
    // return the number of byes that was written or false (-1)
    int write ( FileTable Entry ftEnt, byte[] buffer ) {
        if ( ftEnt == null || ftEnt.mode.equals( "r" ) ) {
            return -1;
        }
        synchronized ( ftEnt ) {
            // size of data to write
            int bsize = buffer.length;
            // track data to write
            int result = 0;
            while ( bsize > 0) {
                int block = ftEnt.inode.findTargetBlock(ftEnt.seekPtr);
                // find free block and assign to inode
                while (block == -1) {
                    int fblock = superblock.getBlock();
                    if (!ftEnt.inode.addBlock((short)fblock)) {
                        superblock.returnBlock(fblock);
                        return -1;
                    }
                    block = ftEnt.inode.findTargetBlock(ftEnt.seekPtr);
                }
                // read the block
                byte[] bdata = new byte[Disk.blockSize];
                SysLib.rawread( block, bdata );
                int start = ftEnt.seekptr  % Disk.blockSize;
                // update how much to write according to block size and updated buffer size
                int lblock = Disk.blockSize - start;
                int size = 0;
                if ( lblock > bsize ) {
                    size = bsize;
                }
                else {
                    size = lblock;
                }
    
                // copy block data from buffer to block and write on disk
                System.arraycopy ( buffer, result, bdata, start, size );
                SysLib.rawwrite( block, bdata );

                // update seekptr, total bytes to read (result) and buffer size
                ftEnt.seekPtr += size;
                result += size;
                bsize -= size;

                // update ftEnt size
                if ( ftEnt.seekPtr > fsize(ftEnt) ) {
                    ftEnt.inode.lenth = entry.seekPtr;
                }    
            }
            // update inode to disk
            ftEnt.inode.toDisk(ftEnt.iNumber);
            return result;
        }
    }

    // delete file, create filename's FileTableEntry obj and check its iNumber free and close, the file 
    // deleted successfully. Otherwise return false
    boolean delete ( String filename ) {
        FileTableEntry check = open ( filename, "w" );
        // if it free and close fine return ture (file deleted success)
        if ( directory.ifree(check.iNumber) && close(check) ) {
            return true;
        }
        return false;
    }

    // free blocks that was poninted by innode
    boolean deallocAllBlocks( FileTableEntry ftEnt ) {
        if ( ftEnt != null && ftEnt.inode.count == 1 ) {
            // free direct blocks
            for ( int i = 0; i < ftEnt.inode.directSize; i++ ){
                if ( ftEnt.inode.direct[i] != -1 ) {
                    superblock.returnBlock(i);
                    ftEnt.inode.direct[i] = -1;                    
                }
            }
            // free indirect blocks
            byte[] data = ftEnt.inode.freeIndirectBlock();
            if ( data != null ) {
                short block = SysLib.bytes2short(data, 0);
                while ( block != -1 ){
                    superblock.returnBlock(block);
                }
            }
            ftEnt.inode.toDisk(ftEnt.iNumber);
            return true;
        }
        return false;
    }

    // size (bytes) of the file
    int fsize ( FileTableEntry ftEnt ) {
        synchronized (ftEnt) {
            return ftEnt.inode.length;
        }
    }
}
