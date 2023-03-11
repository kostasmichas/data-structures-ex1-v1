import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;

public class FileHandler {
	public byte[][] dataPageArray;
	public byte[][] indexArray;
	public byte[][] sortedIndexArray;
	public String[] fNames;
	PageHandler ph;
	RandomAccessFile[] file;
	DataClass[] dClass_array;
	
	public FileHandler(byte[][] bArray, DataClass[] dClass_array) throws FileNotFoundException {
		this.dClass_array = new DataClass[dClass_array[0].n];
		System.arraycopy(dClass_array, 0, this.dClass_array, 0, dClass_array.length);
		indexArray =new byte[dClass_array[0].n/32 + 1][256];
		sortedIndexArray =new byte[dClass_array[0].n/32 + 1][256];
		ph = new PageHandler(dClass_array);
		ph.makeIndexArray();
		ph.makeSortedArray();
		System.arraycopy(ph.indexArray, 0, indexArray, 0, ph.indexArray.length);
		System.arraycopy(ph.sortedIndexArray, 0, sortedIndexArray, 0, ph.sortedIndexArray.length);
		dataPageArray = new byte[ph.totalPages][ph.instancesPerPage*(dClass_array[0].dataLength+4)];
		System.arraycopy(bArray, 0, dataPageArray, 0, bArray.length);
		fNames=new String[3];
		fNames[0] = new String("outa.bin");
		fNames[1] = new String("outb.bin");
		fNames[2] = new String("outc.bin");
		this.file = new RandomAccessFile[3];
		file[0] = new RandomAccessFile(fNames[0], "rw");
		file[1] = new RandomAccessFile(fNames[1], "rw");
		file[2] = new RandomAccessFile(fNames[2], "rw");
	}
	//-------this is the main output file-----------
	public void makeA() {
		try {
			
			for(int i=0; i<ph.totalPages; i++) {
				file[0].seek(i*PageHandler.PAGE_SIZE);
				file[0].write(dataPageArray[i]);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//------this is the index file for question B------
	
	public void makeB() {
		for(int i =0; i<dClass_array[0].n/32 +1;i++) {
			try {
				file[1].seek(i*PageHandler.PAGE_SIZE);
				file[1].write(indexArray[i]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	//-----this is the sorted index file for question C----
	
	public void makeC() {
		for(int i =0; i<dClass_array[0].n/32 +1;i++) {
			try {
				file[2].seek(i*PageHandler.PAGE_SIZE);
				file[2].write(sortedIndexArray[i]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	/*--For question A, the page variable will be equal to 0
	 *--For question B and C the page declaration will be equal to the return value of the corresponding method
	 */
	public DataClass lookForKeyInPage(int page, int key) throws IOException {
		
		byte[] buffer = new byte[256];
		byte[] instanceBuffer = new byte[dClass_array[0].dataLength+4];
		DataClass[] dcBuffer = new DataClass[ph.instancesPerPage];
		for(int i =page; i<ph.totalPages+1; i++) {
			file[0].read(buffer);
			file[0].seek(i*256);
			for(int k=0; k<ph.instancesPerPage; k++) {
				System.arraycopy(buffer, k*instanceBuffer.length, instanceBuffer, 0, instanceBuffer.length);
				dcBuffer[k] = new DataClass(dClass_array[0].byteArrayToDataClass(instanceBuffer));
			}
			for(int k=0; k<ph.instancesPerPage; k++) {
				if(dcBuffer[k].key==key) {
					//System.out.println("Key found with data :" + dcBuffer[k].data);
					return dcBuffer[k];
				}
					
			}
		}
		//System.out.println("Key not found");
		return null;	
	}
	
	public DataClass lookForKeyInIndex(int key) throws IOException {
		int[][] buffer =new int[32][2];
		byte[] byteBuffer = new byte[256];
		file[1].seek(0);
		for(int i=0; i<dClass_array[0].n/32 +1 ; i++) {
			file[1].read(byteBuffer);
			file[1].seek(i*256);
			buffer = ph.byteArrayToIntArray(byteBuffer);
			//PageHandler.printIntArray(buffer);
			for(int k=0; k<32;k++) {
				if(buffer[k][0]==key) {
					DataClass dcBuffer = new DataClass(lookForKeyInPage(buffer[k][1], key));
					//System.out.println("Key found with data: " + dcBuffer.data + "at page : " + (buffer[k][1]+1)); 
					return dcBuffer;
				}
			}
			
		}
		//System.out.println("Key not found");
		return null;
	}
	
	public DataClass lookForKeyInSortedIndex(int key) throws IOException {
		int[][] buffer =new int[32][2];
		byte[] byteBuffer = new byte[256];
		file[2].seek(0);
		int minPage =0;
		int maxPage=dClass_array[0].n/32 +1;
		int middle = (maxPage+minPage)/2;
		int c=0;
		while(maxPage!=minPage) {
			int j=31;
			while(buffer[j][0]==0 && j!=0)
				j--;
			middle = (maxPage+minPage)/2;
			file[2].seek(middle*256);
			file[2].read(byteBuffer);
			buffer = ph.byteArrayToIntArray(byteBuffer);
			if (c!=0 ) {
				if(key > buffer[j][0]) {
					
					minPage = middle+1;
				}
				else if (key<buffer[0][0]) {
					maxPage=middle-1;
				}
			}
			c=1;
			for(int k=0; k<32;k++) {
				if(buffer[k][0]==key) {
					DataClass dcBuffer = new DataClass(lookForKeyInPage(buffer[k][1], key));
					//System.out.println("Key found with data: " + dcBuffer.data + "at page : " + (buffer[k][1]+1)); 
					return dcBuffer;
				}
			}
			if(key < buffer[j][0] && key > buffer[0][0])
				break;
			
		}
		//System.out.println("Key not found");
		return null;
	}
	
	public void searchA() throws IOException {
		int randomInts = 10;
		int searches[] = new int[randomInts];
		Random rng = new Random();
		if(dClass_array[0].n<=randomInts) {
			searches = rng.ints(1 ,dClass_array[0].n*2 + 1).limit(randomInts).toArray();
		}
		else {
			searches = rng.ints(1 ,dClass_array[0].n*2 + 1).distinct().limit(randomInts).toArray();
		}
		for(int i=0; i<randomInts; i++) {
			lookForKeyInPage(0, searches[i]);
		}
	}
	
	public void searchB() throws IOException {
		int randomInts = 10;
		int searches[] = new int[randomInts];
		Random rng = new Random();
		if(dClass_array[0].n<=randomInts) {
			searches = rng.ints(1 ,dClass_array[0].n*2 + 1).limit(randomInts).toArray();
		}
		else {
			searches = rng.ints(1 ,dClass_array[0].n*2 + 1).distinct().limit(randomInts).toArray();
		}
		for(int i=0; i<randomInts; i++) {
			lookForKeyInIndex(searches[i]);
		}
		
	}
	
	public void searchC() throws IOException {
		int randomInts = 10;
		int searches[] = new int[randomInts];
		Random rng = new Random();
		if(dClass_array[0].n<=randomInts) {
			searches = rng.ints(1 ,dClass_array[0].n*2 + 1).limit(randomInts).toArray();
		}
		else {
			searches = rng.ints(1 ,dClass_array[0].n*2 + 1).distinct().limit(randomInts).toArray();
		}
		for(int i=0; i<randomInts; i++) {
			lookForKeyInSortedIndex(searches[i]);
		}
		
	}
	
}
