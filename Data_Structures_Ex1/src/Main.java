import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {

	public static void main(String[] args) throws IOException {
		int N[] = {50, 100 ,200 ,500 ,800, 1000, 2000, 5000, 10000, 50000, 100000, 200000};
		int dataLengths[] = {27, 55};
		for(int i =0; i<dataLengths.length ; i++) {
			System.out.println("--------------------------for data length =" + dataLengths[i]);
			for(int k=0; k<N.length; k++) {
				System.out.println("--------------------------for n =" + N[k]);
				DataClass[] dClass_array = new DataClass[N[k]];
				System.arraycopy(DataClass.randomInitialize(N[k], dataLengths[i]), 0, dClass_array, 0, dClass_array.length);
				PageHandler ph = new PageHandler(dClass_array);
				ph.printStats();
				ph.printPageArray();
				FileHandler fh = new FileHandler(ph.pageArray, dClass_array);
				fh.makeA();
				fh.searchA();
				fh.makeB();
				fh.searchB();
				fh.makeC();
				fh.searchC();
				System.out.println("--------------------------for n =" + N[k]);
				for(int j =0; j<3 ;j++) {
					fh.file[j].close();
				}
			}
			System.out.println("--------------------------for data length =" + dataLengths[i]);
		}
		
	}

}
