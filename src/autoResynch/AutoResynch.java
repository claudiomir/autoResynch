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
	
	public static void main(String[] args) throws IOException {
				
		srtParse();
		Matcher match = srtParse();
		int hours;
		int minutes;
		int seconds;
		int millisecs;
		
		int count = 0;
		while (match.find())
		    count++;
		int[] lineSample = new int[count];
		int index = 0;
		
		//parsing each line in the srt file
        while(match.find()){
        	
        	//get the time for each line, convert in milliseconds, then in sample number
            hours = Integer.parseInt(match.group(2));
            minutes = Integer.parseInt(match.group(3));
            seconds = Integer.parseInt(match.group(4));
            millisecs = Integer.parseInt(match.group(5));
            
            millisecs += hours*60*60*1000 + minutes*60*1000 + seconds*1000;
            
            lineSample[index] = 16*millisecs;
            index++;
            //find the closest sample to the line start
            
        }
		RandomAccessFile in = new RandomAccessFile("test/22.bin", "r");
		in.seek(0);
		byte[] byteArray = new byte[2]; 

		int[] samples = new int[(int) in.length()/2];
		for(int i = 0; i < (int) in.length()/2; i++){
			
			
			in.readFully(byteArray);
			ByteBuffer bb = ByteBuffer.wrap(byteArray);
			bb.order( ByteOrder.LITTLE_ENDIAN);
			short data = bb.getShort();

			//System.out.println((double)i/16000+ " " +data);
		}
		
		
	}
	
	public static Matcher srtParse() throws IOException{
		
		//pattern for reading srt format
		String nl = "\\r?\\n";
		String sp = "[ \\t]*";
		Pattern pattern = Pattern.compile(
		                    "(\\d+)" + sp + nl
		                    + "(\\d{1,2}):(\\d\\d):(\\d\\d),(\\d\\d\\d)" + sp
		                    + "-->" + sp + "(\\d\\d):(\\d\\d):(\\d\\d),(\\d\\d\\d)" + sp
		                    + "(X1:\\d.*?)??" + nl + "([^\\|]*?)" + nl + nl);
		
		String srt = readFile("test/sample.srt", StandardCharsets.UTF_8);
        Matcher matcher = pattern.matcher(srt);


        return matcher;
	}
	
	static String readFile(String path, Charset encoding) 
			  throws IOException 
			{
			  byte[] encoded = Files.readAllBytes(Paths.get(path));
			  return new String(encoded, encoding);
			}

}

