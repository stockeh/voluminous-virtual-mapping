package distributed.common.wireformats;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Field;

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

  public void readEvent(Class c, Event event) {
    Field[] fields = c.getDeclaredFields();
    try {
      for (int i = 0; i < fields.length; i++) {
        Field field = fields[i];
        String type = field.getAnnotatedType().getType().getTypeName();
        if(type.equals("int") || type.equals("Integer")) {
          int reading = readInt();
          System.out.println("READING INT: " + reading);
         field.setInt(event, reading);
        }else if(type.equals("String")) {
          field.set(event, readString());
        }else if(type.equals("double") || type.equals("Double")) {
          field.setDouble(event, readDouble());
        }else if(type.equals("long") || type.equals("Long")) {
          field.setLong(event, readLong());
        }else if(type.equals("byte[]") || type.equals("Byte[]")) {
          field.set(event, readByteArr());
        }else if(type.equals("boolean") || type.equals("Boolean")) {
          field.set(event, readBoolean());
        }
      }
    }catch(IllegalAccessException iae) {
      iae.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
