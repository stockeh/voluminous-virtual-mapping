package distributed.application.util;

public class Functions {
  public static byte[][] reshape(byte[] bytes) {
    int size = (int)Math.sqrt(bytes.length);
    byte[][] reshapedBytes = new byte[size][size];

    for(int i = 0,j = 0; i < bytes.length; i+=size,j++) {
      System.arraycopy(bytes, i, reshapedBytes[j], 0, size);
    }
    return reshapedBytes;
  }
}
