import java.util.*;
public class FileTable { 

      private Vector table;         // the actual entity of this file table 
      private Directory dir;        // the root directory  
 
      public FileTable( Directory directory ) { // constructor 
         table = new Vector( );     // instantiate a file (structure) table 
         dir = directory;           // receive a reference to the Director 
      }                             // from the file system 
 
      // major public methods 
      public synchronized FileTableEntry falloc( String filename, String mode ) { 
         // allocate a new file (structure) table entry for this file name 
         // allocate/retrieve and register the corresponding inode using dir 
         // increment this inode's count 
         // immediately write back this inode to the disk 
         // return a reference to this file (structure) table entry
         short iNumber = -1;
         Inode inode = null;

         
         while(true) {
            iNumber = (filename.equals("/") ? (short) 0 : dir.namei(filename));
            // If filename exists or root
            if(iNumber >= 0) {
               inode = new Inode(iNumber);
               if(mode.compareTo("r") == 1)
               {
                  // if it is being read then break out of it or if it is not being used or unused
                  if(inode.flag == 2 || inode.flag == 0 || inode.flag == 1) {
                     inode.flag = 2;
                     break;
                     // if written must wait until done
                  } else if (inode.flag == 3)
                  {
                     try {
                        wait();
                     } catch (InterruptedException e ) {}
                     // if it is being deleted
                  } else if(inode.flag == 4)
                  {
                     iNumber = -1;
                     return null;
                  }
               } else {
                  // Need to see if there are being used or unused
                  // will only set to write when true
                  if(inode.flag == 0 || inode.flag == 1 ) {
                     inode.flag = 3;
                     break;
                  } else {
                     // if the flag is not used or unused then it has to wait for the completion
                     try{
                        wait();
                     } catch(InterruptedException e) {}
                  }
               }
               // does not exist so create it
            } else { 
               iNumber = dir.ialloc(filename);
               inode = new Inode();
               break;
            }
         }

         inode.count++;
         inode.toDisk(iNumber);
         FileTableEntry e = new FileTableEntry(inode, iNumber, mode);
         table.addElement(e);
         return e;

      } 
 
      public synchronized boolean ffree( FileTableEntry e ) { 
        // receive a file table entry reference 
         // save the corresponding inode to the disk 
         // free this file table entry. 
         // return true if this file table entry found in my table 
         boolean exists = table.remove(e);
         if(exists) {
            e.inode.count -= 1;
            e.inode.toDisk(e.iNumber);
            e.inode.flag = 0;
            e = null;
            notify();
            return true;

         }

         return false;
      } 
 
      public synchronized boolean fempty( ) { 
         return table.isEmpty( );  // return if table is empty  
      }                            // should be called before starting a format 
   } 