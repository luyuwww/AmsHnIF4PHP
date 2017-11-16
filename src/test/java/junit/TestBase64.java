package junit;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class TestBase64 {

    private String imageURL = "c:/test.doc";
    @Test
    public void testBase64Encoder(){
        BASE64Encoder encoder = new BASE64Encoder();
        
        try {
            StringBuilder pictureBuffer = new StringBuilder(); 
            InputStream input = new FileInputStream(new File(imageURL));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] temp = new byte[1024];
            for(int len = input.read(temp); len != -1;len = input.read(temp)){
                out.write(temp, 0, len);
                pictureBuffer.append(encoder.encode(out.toByteArray()));
                //out(pictureBuffer.toString());
                out.reset();
            }
            
            out(pictureBuffer.toString());
            out("Encoding the picture Success");
            
            
//        String base64 = "";
        BASE64Decoder decoder = new BASE64Decoder();
        FileOutputStream write = new FileOutputStream(new File("c:/test2.doc"));
        byte[] decoderBytes = decoder.decodeBuffer(pictureBuffer.toString());
        write.write(decoderBytes);
        out("Decoding the picture Success");
            
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    
    public void out(Object o){
        System.out.println(o.toString());
    }
    public static void main(String[] args) {
    	new TestBase64().testBase64Encoder();
	}
}