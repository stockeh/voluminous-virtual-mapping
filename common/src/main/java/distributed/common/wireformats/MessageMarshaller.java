package distributed.common.wireformats;


import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

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

    public void writeStringList(Collection<String> list) throws IOException {
     writeInt(list.size());
     for(String s : list) {
       writeString(s);
     }
    }

    public void writeEvent(Class c, Event event) {
      Field[] fields = c.getDeclaredFields();
      try {
        for (Field field : fields) {
          String type = field.getAnnotatedType().getType().getTypeName();
          if(type.equals("int") || type.equals("java.lang.Integer")) {
            writeInt(field.getInt(event));
          }else if(type.equals("String")) {
            writeString((String) field.get(event));
          }else if(type.equals("double") || type.equals("java.lang.Double")) {
            writeDouble(field.getDouble(event));
          }else if(type.equals("long") || type.equals("java.lang.Long")) {
            writeLong(field.getLong(event));
          }else if(type.equals("byte[]") || type.equals("java.lang.Byte[]")) {
            writeByteArr((byte[]) field.get(event));
          }else if(type.equals("boolean") || type.equals("java.lang.Boolean")) {
            writeBoolean(field.getBoolean(event));
          }else if(type.equals("java.util.List<java.lang.String>") || type.equals("java.util.Set<java.lang.String>") || type.equals("java.util.HashSet<java.lang.String>")) {
            writeStringList((Collection<String>) field.get(event));
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
