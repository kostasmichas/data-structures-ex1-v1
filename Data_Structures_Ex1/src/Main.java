import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {
	static final int randomSearches = 1000;
	public static void main(String[] args) throws IOException {
		long totalTime = System.nanoTime();
		int N[] = {50, 100 ,200 ,500 ,800, 1000, 2000, 5000, 10000, 50000, 100000, 200000};
		int dataLengths[] = {27, 55};
		for(int i =0; i<dataLengths.length ; i++) {
			System.out.println("--------------------------for data length =" + dataLengths[i]);
			for(int k=0; k<N.length; k++) {
				System.out.println("--------------------------for n =" + N[k]);
				DataClass[] dClass_array = new DataClass[N[k]];
				System.arraycopy(DataClass.randomInitialize(N[k], dataLengths[i]), 0, dClass_array, 0, dClass_array.length);
				PageHandler ph = new PageHandler(dClass_array);
				//ph.printStats();
				//ph.printPageArray();
				FileHandler fh = new FileHandler(ph.pageArray, dClass_array);
				fh.makeA();
				long timeA =System.nanoTime();
				System.out.println("Averages disk reaches for A:" + (fh.searchA(randomSearches))/randomSearches);
				System.out.println("Time spent :" + (System.nanoTime()-timeA));
				
				fh.makeB();
				long timeB =System.nanoTime();
				System.out.println("Averages disk reaches for B:" + (fh.searchB(randomSearches))/randomSearches);
				System.out.println("Time spent :" + (System.nanoTime()-timeB));
				
				fh.makeC();
				long timeC =System.nanoTime();
				System.out.println("Averages disk reaches for C:" + (fh.searchC(randomSearches))/randomSearches);
				System.out.println("Time spent :" + (System.nanoTime()-timeC));
				System.out.println("--------------------------for n =" + N[k]);
				for(int j =0; j<3 ;j++) {
					fh.file[j].close();
				}
			}
			System.out.println("--------------------------for data length =" + dataLengths[i]);
		}
		System.out.println("Total time :" + (System.nanoTime() - totalTime));
	}

}
