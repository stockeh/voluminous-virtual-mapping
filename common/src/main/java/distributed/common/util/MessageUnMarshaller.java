package distributed.common.util;

import distributed.common.util.Sector;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MessageUnMarshaller {
  private static DataInputStream din;

  private static int readInt() throws IOException {
    return din.readInt();
  }

  private static boolean readBoolean() throws IOException {
    return din.readBoolean();
  }

  private static long readLong() throws IOException {
    return din.readLong();
  }

  private static double readDouble() throws IOException {
    return din.readDouble();
  }


  private static String readString() throws IOException{
    return new String(readByteArr());
  }

  private static Sector readSector() throws IOException {
   Sector sector = new Sector();
   read(sector.getClass(), sector);
   return sector;
  }

  private static Set<Sector> readSectorSet() throws IOException {
    int size = readInt();
    Set<Sector> set = new HashSet<>();
    for(int i = 0; i < size; i++) {
      set.add(readSector());
    }
    return set;
  }

  private static byte[] readByteArr() throws IOException {
    byte[] bytes = new byte[din.readInt()];
    din.readFully(bytes);
    return bytes;
  }

  private static byte[][] readByteArrArr() throws IOException {
    byte[][] bytes = new byte[din.readInt()][];
    for(int i = 0; i < bytes.length; i++) {
      bytes[i] = readByteArr();
    }
    return bytes;
  }

  private static String[] readStringArray() throws IOException {
    int length = readInt();
    String[] arr = new String[length];
    for(int i = 0; i < length; i++) {
      arr[i] = readString();
    }
    return arr;
  }

  private static void readStringList(Collection<String> list) throws IOException {
    int size = readInt();
    for(int i = 0; i < size; i++) {
      list.add(readString());
    }
  }

  public static void readEvent(Class<?> c, Object event, byte[] bytes) {
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
    din = new DataInputStream(byteArrayInputStream);
    read(c, event);
  }
  
  private static int[] readIntArr() throws IOException {
    int s = din.readInt();
    int[] ints = new int[ s ];
    for ( int i = 0; i < s; ++i )
    {
      int temp = din.readInt();
      ints[ i ] = temp;
    }
    return ints;
  }

  private static void read(Class<?> c, Object event) {
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
          case "distributed.common.util.Sector":
            field.set(event, readSector());
            break;
          case "java.util.Set<distributed.common.util.Sector>":
          case "java.util.HashSet<distributed.common.util.Sector>":
            field.set(event, readSectorSet());
            break;
          case "byte[]":
          case "java.lang.Byte[]":
            field.set(event, readByteArr());
            break;
          case "byte[][]":
          case "java.lang.Byte[][]":
            field.set(event, readByteArrArr());
            break;
          case "java.lang.String[]":
            field.set(event, readStringArray());
            break;
          case "int[]":
          case "java.lang.Integer[]":
            field.set(event, readIntArr());
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
