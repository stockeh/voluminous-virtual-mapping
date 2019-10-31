package distributed.common.util;

import distributed.common.wireformats.Event;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Instant;

public class MessageMarshaller {
    private final ByteArrayOutputStream baOutStream;
    private final DataOutputStream dout;

    public MessageMarshaller() {
      baOutStream = new ByteArrayOutputStream();
      dout = new DataOutputStream(new BufferedOutputStream(baOutStream));
    }

    public void writeInt(int value) throws IOException {
      dout.writeInt(value);
    }

    public void writeBoolean(boolean value) throws IOException {
      dout.writeBoolean(value);
    }

    public void writeLong(long value) throws IOException {
      dout.writeLong(value);
    }

    public void writeDouble(double value) throws IOException {
      dout.writeDouble(value);
    }

    public void writeString(String str) throws IOException {
      byte[] strBytes = str.getBytes();
      dout.writeInt(strBytes.length);
      dout.write(strBytes);
    }

    public void writeByteArr(byte[] arr) throws IOException {
      dout.writeInt(arr.length);
      dout.write(arr);
    }

    public void writeObject(Class c, Event event) {
      System.out.println("CLASS: " + c.getName());
      Field[] fields = c.getDeclaredFields();
      try {
        for (Field field : fields) {
          String type = field.getAnnotatedType().getType().getTypeName();
          if(type.equals("int") || type.equals("Integer")) {
            writeInt(field.getInt(event));
          }else if(type.equals("String")) {
            writeString((String) field.get(event));
          }else if(type.equals("double") || type.equals("Double")) {
            writeDouble(field.getDouble(event));
          }else if(type.equals("long") || type.equals("Long")) {
            writeLong(field.getLong(event));
          }else if(type.equals("byte[]") || type.equals("Byte[]")) {
            writeByteArr((byte[]) field.get(event));
          }else if(type.equals("boolean") || type.equals("Boolean")) {
            writeBoolean(field.getBoolean(event));
          }
        }
      }catch(IllegalAccessException iae) {
        iae.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    public byte[] getMarshalledData() throws IOException {
      dout.flush();
      byte[] marshalledData = baOutStream.toByteArray();
      baOutStream.close();
      dout.close();
      return marshalledData;
    }

}
