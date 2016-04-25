import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.zip.CRC32;

public class CRC 
{
    public static void main(String[] args) 
    {
        byte[] bytes = new byte[100];
        byte upper_byte = 0;
        byte lower_byte = 0;
        
        try 
        {
            Socket socket = new Socket("codebank.xyz", 38102);
            InputStream input_stream = socket.getInputStream();
            
            for(int i = 0; i < 100; i++)
            {
                // get first byte (need half of it)
                upper_byte = (byte) input_stream.read();
                //System.out.printf("SECOND BYTE: %02x\n", upper_byte);
                // get second byte (need half of it)
                lower_byte = (byte) input_stream.read();
                //System.out.printf("SECOND BYTE: %02x\n", lower_byte);
                // shift first byte to the second empty half)
                //System.out.printf("BEFORE SHIFT : %02x\n", upper_byte);
                upper_byte  = shiftByte2Left(upper_byte, 4);
                //System.out.printf("AFTER SHIFT : %02x\n",  temp);
                // xor two half bytes into one
                //System.out.printf("BEFORE XOR : %02x\n",  temp);
                upper_byte = xorBytes(upper_byte, lower_byte);
                //System.out.printf("After XOR : %02x\n",  temp);
                bytes[i] = upper_byte;
            }
            
            System.out.println("Received Bytes:");
            // display for debugging
            for(int i = 0; i < 100; i++)
            {
                if(i % 10 == 0 & i != 0)
                    System.out.println();
                System.out.printf("%02x", bytes[i]);
            }
            //generate CRC32
            System.out.println("\n\nCRC32: " + generateCRC32(bytes));
            
            // long = 64 bytes
            // we need to send back 4 bytes to check
            // 64 / 4 = 16
            // we need first 16 bytes
            // example checksum FA 12 8A 79 
            // this is a long, cast it to int to get rid of all leading zeros, since this example checksum is the size of an int
            //then after doing that, shift it by 24, 16, 8, then 0 into  a byte,
            // so shift it by 24 into a byte t have the FA as the far left of a byte, then same concept for rest except only enough bits to shift
            // then send those 4 bits back
            
            // cast to int to get rid of not needed bits
            int CRC = (int)generateCRC32(bytes);
            // create new array to hold CRC bytes to send back
            byte[] new_bytes = new byte[4];
            // used for shifting purposes
            int[] shifts = {24,16,8}; 
            // get bytes to send back to server
            
            System.out.println(CRC);
            for(int i = 0; i < shifts.length; i++)
            {
                new_bytes[i] = shiftByte2Right(CRC, shifts[i]);
                System.out.println(new_bytes[i]);
            }
            // create output stream to send data back
            OutputStream output_stream = socket.getOutputStream();
            // send server the CRC32 bytesww
            output_stream.write(new_bytes);
            // if 1, it worked, else did not work
            System.out.println("Success (if = 1): " + input_stream.read());
        } 
        catch (IOException ex) 
        {
            System.out.println("Error");
        }
    }
    
    static long generateCRC32(byte[] bytes)
    {
        CRC32 check_sum = new CRC32();
        check_sum.update(bytes);
        return check_sum.getValue();
    }
    
    static byte shiftByte2Left(int original_byte, final int shift_size)
    {
        return  (byte) (original_byte << shift_size);
    }
    
    static byte shiftByte2Right(int original_byte, final int shift_size)
    {
        return  (byte) (original_byte >> shift_size);
    }
    
    static byte xorBytes(byte first, byte second)
    {
        // xor both bytes
        return first ^= second;
    }
}
    