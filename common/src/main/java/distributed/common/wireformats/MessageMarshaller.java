package distributed.common.wireformats;


import distributed.common.util.Sector;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;

public class MessageMarshaller {
    private static  ByteArrayOutputStream baOutStream;
    private static DataOutputStream dout;

    private static void writeInt(int value) throws IOException {
      dout.writeInt(value);
    }

    private static void writeBoolean(boolean value) throws IOException {
      dout.writeBoolean(value);
    }

    private static void writeLong(long value) throws IOException {
      dout.writeLong(value);
    }

    private static void writeDouble(double value) throws IOException {
      dout.writeDouble(value);
    }

    private static void writeString(String str) throws IOException {
      byte[] strBytes = str.getBytes();
      dout.writeInt(strBytes.length);
      dout.write(strBytes);
    }

    private static void writeSector(Sector sector) throws IOException {
      sector.writeSector();
    }

    private static void writeByteArr(byte[] arr) throws IOException {
      dout.writeInt(arr.length);
      dout.write(arr);
    }

  private static void writeByteArrArr(byte[][] arr) throws IOException {
    dout.writeInt(arr.length);
    for(byte[] bytes : arr) {
      writeByteArr(bytes);
    }
  }

    private static void writeStringArr(String[] arr) throws IOException {
      writeInt(arr.length);
      for(String s : arr) {
        writeString(s);
      }
    }

    private static void writeStringList(Collection<String> list) throws IOException {
     writeInt(list.size());
     for(String s : list) {
       writeString(s);
     }
    }

    public static byte[] writeEvent(Class<?> c, Object event) throws IOException {
      baOutStream = new ByteArrayOutputStream();
      dout = new DataOutputStream(new BufferedOutputStream(baOutStream));
      write(c, event);
      return getMarshalledData();
    }

    @SuppressWarnings( "unchecked" )
    private static void write(Class<?> c, Object event) {
      Field[] fields = c.getDeclaredFields();
      try {
        for (Field field : fields) {
          String type = field.getAnnotatedType().getType().getTypeName();
          switch (type) {
            case "int":
            case "java.lang.Integer":
              writeInt(field.getInt(event));
              break;
            case "java.lang.String":
              writeString((String) field.get(event));
              break;
            case "double":
            case "java.lang.Double":
              writeDouble(field.getDouble(event));
              break;
            case "long":
            case "java.lang.Long":
              writeLong(field.getLong(event));
              break;
            case "boolean":
            case "java.lang.Boolean":
              writeBoolean(field.getBoolean(event));
              break;
            case "Sector":
              writeSector((Sector) field.get(event));
              break;
            case "byte[]":
            case "java.lang.Byte[]":
              writeByteArr((byte[]) field.get(event));
              break;
            case "byte[][]":
            case "java.lang.Byte[][]":
              writeByteArrArr((byte[][]) field.get(event));
              break;
            case "java.lang.String[]":
              writeStringArr((String[])field.get(event));
              break;
            case "java.util.Collection<java.lang.String>":
            case "java.util.HashSet<java.lang.String>":
            case "java.util.List<java.lang.String>":
            case "java.util.ArrayList<java.lang.String>":
            case "java.util.LinkedList<java.lang.String>":
            case "java.util.Set<java.lang.String>":
              HashSet<String> set = new HashSet<>();
              writeStringList((Collection<String>) field.get(event));
              field.set(event, set);
              break;
          }
        }
      }catch(IllegalAccessException iae) {
        iae.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }


    private static byte[] getMarshalledData() throws IOException {
      dout.flush();
      byte[] marshalledData = baOutStream.toByteArray();
      baOutStream.close();
      dout.close();
      return marshalledData;
    }

}
