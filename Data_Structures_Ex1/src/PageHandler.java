import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Comparator;

public class PageHandler {
	static final int PAGE_SIZE = 256;
	byte[][] pageArray;
	byte[][] indexArray;
	byte[][] sortedIndexArray;
	public int instancesPerPage;
	public int leftoverMemoryPerPage;
	public int totalPages;
	public int leftOverMemoryAtLastPage;
	public DataClass[] dClass_array;

	public PageHandler(DataClass[] arr) {
		dClass_array = new DataClass[arr[0].n];
		System.arraycopy(arr, 0, dClass_array, 0, arr.length);
		instancesPerPage = PAGE_SIZE / (dClass_array[0].dataLength + 4);
		leftoverMemoryPerPage = PAGE_SIZE - instancesPerPage * (dClass_array[0].dataLength + 4);
		totalPages = ((dClass_array[0].dataLength + 4) * dClass_array[0].n) / (PAGE_SIZE - leftoverMemoryPerPage) + 1;
		leftOverMemoryAtLastPage = 256 - (dClass_array[0].n * (dClass_array[0].dataLength + 4)- (256 - leftoverMemoryPerPage) * (totalPages - 1));
		this.indexArray = new byte[dClass_array[0].n / 32 + 1][256];
		this.sortedIndexArray = new byte[dClass_array[0].n / 32 + 1][256];
		if (dClass_array[0].n % instancesPerPage == 0)
			totalPages--;
		pageArray = new byte[totalPages][instancesPerPage * (dClass_array[0].dataLength + 4)];
		dClassArrayToPageArray();
	}

	public void printStats() {
		System.out.println("Instances per page : " + instancesPerPage + " instances.");
		System.out.println("Leftover memory per page : " + leftoverMemoryPerPage + " bytes.");
		System.out.println("Total amount of pages : " + totalPages + " pages.");
		System.out.println("Leftover memory at last page :" + leftOverMemoryAtLastPage + " bytes.");
		System.out.println("---------------------------------------------");
	}

	public byte[][] dClassArrayToPageArray() {
		byte[][] pArr = new byte[totalPages][instancesPerPage * (dClass_array[0].dataLength + 4)];
		int c = 0;
		for (int i = 0; i < totalPages; i++) {
			for (int k = 0; k < instancesPerPage; k++) {
				System.arraycopy(dClass_array[c].dataClassToByteArray(), 0, pArr[i],
						k * (dClass_array[0].dataLength + 4), (dClass_array[0].dataLength + 4));
				c++;
				if (c >= dClass_array[0].n)
					break;
			}
		}
		System.arraycopy(pArr, 0, pageArray, 0, pArr.length);
		return pArr;
	}

	public void printPageArray() {
		DataClass dc = new DataClass();
		byte[] buffer = new byte[dClass_array[0].dataLength + 4];
		for (int i = 0; i < totalPages; i++) {
			System.out.println("---------------Page " + (i + 1) + "--------------------");
			for (int k = 0; k < instancesPerPage; k++) {
				System.arraycopy(pageArray[i], k * (dClass_array[0].dataLength + 4), buffer, 0,
						dClass_array[0].dataLength + 4);
				dc = new DataClass(dClass_array[0].byteArrayToDataClass(buffer));
				dc.printDataClass();
			}
		}
		System.out.println("-----------------------------------------------------------");
	}

	public void makeIndexArray() {
		int[][] indexIntArray = new int[dClass_array[0].n][2];
		int c = 0;
		// to make the index array
		while (c != dClass_array[0].n) {
			if (dClass_array[c].key == 0)
				break;
			indexIntArray[c][0] = dClass_array[c].key;
			indexIntArray[c][1] = c / instancesPerPage;
			c++;
		}
		// printIntArray(indexIntArray);
		// to turn it to a byte array
		ByteBuffer bb = ByteBuffer.allocate(4);
		ByteBuffer bb1 = ByteBuffer.allocate(4);
		bb.order(ByteOrder.BIG_ENDIAN);
		bb1.order(ByteOrder.BIG_ENDIAN);
		c = 0;
		for (int k = 0; k < dClass_array[0].n / 32 + 1; k++) {
			for (int i = 0; i < 32; i++) {
				if (c == dClass_array[0].n)
					break;
				bb.putInt(indexIntArray[c][0]);
				bb1.putInt(indexIntArray[c][1]);
				System.arraycopy(bb.array(), 0, indexArray[k], i * 8, 4);
				System.arraycopy(bb1.array(), 0, indexArray[k], i * 8 + 4, 4);
				bb.clear();
				bb1.clear();
				c++;
			}
		}
		// printIntArray(byteArrayToIntArray(indexArray));
	}

	public void makeSortedArray() {
		int[][] buffer = new int[dClass_array[0].n][2];
		System.arraycopy(byteArrayToIntArray(indexArray), 0, buffer, 0, byteArrayToIntArray(indexArray).length);
		Arrays.sort(buffer, Comparator.comparingInt(a -> a[0]));
		int c = 0;
		ByteBuffer bb = ByteBuffer.allocate(4);
		ByteBuffer bb1 = ByteBuffer.allocate(4);
		bb.order(ByteOrder.BIG_ENDIAN);
		bb1.order(ByteOrder.BIG_ENDIAN);
		for (int k = 0; k < dClass_array[0].n / 32 + 1; k++) {
			for (int i = 0; i < 32; i++) {
				if (c == dClass_array[0].n)
					break;
				bb.putInt(buffer[c][0]);
				bb1.putInt(buffer[c][1]);
				System.arraycopy(bb.array(), 0, sortedIndexArray[k], i * 8, 4);
				System.arraycopy(bb1.array(), 0, sortedIndexArray[k], i * 8 + 4, 4);
				bb.clear();
				bb1.clear();
				c++;
			}
			// printIntArray(buffer);
		}

	}

	public int[][] byteArrayToIntArray(byte[][] arr) {
		byte[][] buffer = new byte[2][4];
		int[][] intArr = new int[dClass_array[0].n][2];
		int c = 0;
		for (int i = 0; i < dClass_array[0].n / 32 + 1; i++) {
			for (int k = 0; k < 32; k++) {
				if (c == dClass_array[0].n)
					break;
				System.arraycopy(arr[i], k * 8, buffer[0], 0, 4);
				System.arraycopy(arr[i], (k * 8 + 4), buffer[1], 0, 4);
				intArr[c][0] = ByteBuffer.wrap(buffer[0]).order(ByteOrder.BIG_ENDIAN).getInt();
				intArr[c][1] = ByteBuffer.wrap(buffer[1]).order(ByteOrder.BIG_ENDIAN).getInt();
				c++;
			}
		}
		return intArr;
	}

	public int[][] byteArrayToIntArray(byte[] arr) {
		byte[][] buffer = new byte[2][4];
		int[][] intArr = new int[32][2];
		int c = 0;
		for (int k = 0; k < 32; k++) {
			if (c == dClass_array[0].n)
				break;
			System.arraycopy(arr, k * 8, buffer[0], 0, 4);
			System.arraycopy(arr, (k * 8 + 4), buffer[1], 0, 4);
			intArr[c][0] = ByteBuffer.wrap(buffer[0]).order(ByteOrder.BIG_ENDIAN).getInt();
			intArr[c][1] = ByteBuffer.wrap(buffer[1]).order(ByteOrder.BIG_ENDIAN).getInt();
			c++;
		}
		return intArr;
	}

	public void printIntArray(int[][] arr) {
		for (int i = 0; i < 32; i++) {
			if (i == dClass_array[0].n)
				break;
			System.out.println(arr[i][0] + " " + (arr[i][1] + 1));
		}
		System.out.println("---------------------------------");
	}

}
