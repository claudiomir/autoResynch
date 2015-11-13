package autoResynch;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class AutoResynch {
	
	static int sampling = 16;
	
	public static void main(String[] args) throws IOException {
				
		SRTParser srt1 = new SRTParser("test/sample.srt", sampling);
		
		
		RandomAccessFile in2 = new RandomAccessFile("test/2.bin", "r");

	
		
		
	}
	

	


}

