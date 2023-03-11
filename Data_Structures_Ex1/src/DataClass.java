import java.lang.invoke.StringConcatException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class DataClass {
	public int dataLength;
	public int n;
	public int key;
	public String data;

	public DataClass(int key, String data) {
		this.key = key;
		this.data = data;
	}

	public DataClass(DataClass dc) {
		this.key = dc.key;
		this.data = dc.data;
	}

	public DataClass() {
		this.key = 0;
		this.data = "not initialized";
	}

	public byte[] dataClassToByteArray() {
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.putInt(this.key);
		byte[] byteArr1 = bb.array();
		byte[] byteArr2 = this.data.getBytes();
		byte[] result = new byte[byteArr1.length + byteArr2.length];
		System.arraycopy(byteArr1, 0, result, 0, byteArr1.length);
		System.arraycopy(byteArr2, 0, result, byteArr1.length, byteArr2.length);
		return result;
	}

	public DataClass byteArrayToDataClass(byte[] byteArr) {
		byte[] key = new byte[4];
		byte[] data = new byte[dataLength];
		System.arraycopy(byteArr, 0, key, 0, 4);
		System.arraycopy(byteArr, 4, data, 0, byteArr.length - 4);
		int resultKey = ByteBuffer.wrap(key).order(ByteOrder.BIG_ENDIAN).getInt();
		String resultData = new String(data);
		return new DataClass(resultKey, resultData);

	}

	static public DataClass[] randomInitialize(int n, int dLen) {
		DataClass[] dClass_array = new DataClass[n];
		Random rand = new Random();
		int[] randomInts = rand.ints(1, n*2 +1).distinct().limit(n).toArray();
		for(int i =0; i<dClass_array.length; i++) {
			dClass_array[i] = new DataClass();
			dClass_array[i].key = randomInts[i];
			dClass_array[i].n=n;
			dClass_array[i].dataLength=dLen;
		}

		Set<String> set2 = new HashSet<>();
		for (int i = 0; i < n; i++) {
			StringBuilder sb = new StringBuilder(dLen);
			while (sb.length() < dLen) {
				char c = (char) (rand.nextInt(26) + 'a');
				sb.append(c);
			}
			String str = sb.toString();
			while (set2.contains(str)) {
				sb.setLength(0);
				while (sb.length() < dLen) {
					char c = (char) (rand.nextInt(26) + 'a');
					sb.append(c);
				}
				str = sb.toString();
			}
			dClass_array[i].data = str;
			set2.add(str);
		}

		return dClass_array;

	}

	public void printDataClass() {
		System.out.println(this.key + " " + this.data);
	}

	public int getKey() {
		return key;
	}

	public void setKey(int key) {
		this.key = key;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

}
