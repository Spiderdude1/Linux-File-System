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
        int dirSize = fsize ( dir Ent );
        if ( dirSize > 0 ) {
            // the directory has some data.
            byte[] dirData = new byte[dirSize];
            read( dirEnt, dirDate );
            directory.bytes2directory (dirdata );
        } 
        close (dirEnt);

    }


    // sync the filesystem to the disk with superblock and directory
    void sync() {

    }

    // format the disk
    boolean format (int fCount ) {

        return true;
        
    }


    FileTableEnyty open ( String filename, String mode ) {
        FiletableEnytu ftEnt = fileTable.falloc( filename, mode );
        if ( mode.equals( "w" ) ) {
            if ( deallocAllBlocks ( ftEnt ) == false ) {
                ftEnt = null;
            }
        }
        return ftEnt;
    }
    
    // close file update entry count and free entry
    boolean close ( FileTableEntry ftEnt ) {
        synchronized(ftEnt) {
            ftEnt.count--;
            if (ftEnt.count == 0){
                return FileTable.ffree(ftEnt);
            }
            return true;
        }

    }

    // read block and set buffer size accroding to data size.
    // return the number of byes to read or false;

    int read ( FileTableEntry ftEnt, byte[] buffer ) {
        if ( ftEnt.mode.eqauls( "w" ) || ftEnt.mode.eqauls( "a" ) ){
            return -1;
        }

        // size of data to read
        int bsize = buffer.length;
        // track data to read
        int result = 0;
        synchronized (ftEnt){
            while ( (bsize > 0) && (ftEnt.seekPtr < fsize(ftEnt)) ){
                // block to read
                int block = ftEnt.inode.findTargetBlock(ftEnt);
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
                    result += sizeToRead;
                    bsize -= sizeToRead;
                }
            }
            return result;
        }

    }

    // update seek pointer for the table entry
    int seek ( FileTableEntry ftEnt, int offset, int whence ) {
        synchronized (ftEnt) {
            if ( whence == 0 ) {
                ftEnt.seekPtr = offset;
            }
            if ( whence == 1 ) {
                ftEnt.seekPtr += offset;
            }
            if ( whence == 2 ) {
                ftEnt.seekPtr = offset + fsize(ftEnt);
            }
        }
        if (ftEnt.seekPtr < 0){
            ftEnt.seekPtr = 0;
        }
        if ( fsize(ftEnt) < ftEnt.seekPtr ) {
            ftEnt.seekPtr = fsize(ftEnt);
        }
        return ftEnt.seekPtr;
    }

    int write ( FileTable Entry ftEnt, byte[] buffer ) {


    }

    // delete file, 
    boolean delete ( String filename ) {
        return true;
    }

    // free blocks that was poninted by innode
    boolean deallocAllBlocks( FileTableEntry ftEnt ) {
        if ( ftEnt.inode.count == 1 ) {
            // free direct blocks
            for ( int i = 0; i < ftEnt.inode.directSize; i++ ){
                if ( ftEnt.inode.direct[i] != -1 ) {
                    superblock.returnBlock(i);
                    ftEnt.inode.direct[blocId] = -1;                    
                }
            }
            // free indirect blocks
            byte[] data = ftEnt.inode.freeIndirectBlock();
            if ( data != null ) {
                int block = SysLib.bytes2short(indirectData, 0);
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

