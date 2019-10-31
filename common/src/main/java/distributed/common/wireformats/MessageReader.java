package distributed.common.wireformats;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class MessageReader {
  private final DataInputStream din;

  MessageReader(byte[] bytes) {
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
    this.din = new DataInputStream(byteArrayInputStream);

  }
  private int readInt() throws IOException {
    return din.readInt();
  }

  private boolean readBoolean() throws IOException {
    return din.readBoolean();
  }

  private long readLong() throws IOException {
    return din.readLong();
  }

  private double readDouble() throws IOException {
    return din.readDouble();
  }


  private String readString() throws IOException{
    return new String(readByteArr());
  }

  private byte[] readByteArr() throws IOException {
    byte[] bytes = new byte[din.readInt()];
    din.readFully(bytes);
    return bytes;
  }

  private String[] readStringArray() throws IOException {
    int length = readInt();
    String[] arr = new String[length];
    for(int i = 0; i < length; i++) {
      arr[i] = readString();
    }
    return arr;
  }

  private void readStringList(Collection<String> list) throws IOException {
    int size = readInt();
    for(int i = 0; i < size; i++) {
      list.add(readString());
    }
  }

  public void readEvent(Class c, Event event) {
    Field[] fields = c.getDeclaredFields();
    try {
      for (Field field : fields) {
        String type = field.getAnnotatedType().getType().getTypeName();
        switch (type) {
          case "int":
          case "java.lang.Integer":
            field.setInt(event, readInt());
            break;
          case "java.lang.String":
            field.set(event, readString());
            break;
          case "double":
          case "java.lang.Double":
            field.set(event, readDouble());
            break;
          case "long":
          case "java.lang.Long":
            field.set(event, readLong());
            break;
          case "boolean":
          case "java.lang.Boolean":
            field.set(event, readBoolean());
            break;
          case "byte[]":
          case "java.lang.Byte[]":
            field.set(event, readByteArr());
            break;
          case "java.lang.String[]":
            field.set(event, readStringArray());
            break;
          case "java.util.HashSet<java.lang.String>":
          case "java.util.Set<java.lang.String>":
            HashSet<String> set = new HashSet<>();
            readStringList(set);
            field.set(event, set);
            break;
          case "java.util.List<java.lang.String>":
          case "java.util.ArrayList<java.lang.String>":
            ArrayList<String> list = new ArrayList<>();
            readStringList(list);
            field.set(event, list);
            break;
        }
      }
    }catch(IllegalAccessException iae) {
      iae.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
