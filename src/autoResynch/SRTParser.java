package autoResynch;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SRTParser {
	
	private String srt;
	private int sampling;
	
	int nSubtitles = 0;
	
	private int[][] startTimings;
	private int[][] stopTimings;
	
	private String[] lines;
	
	public SRTParser(String filename, int samp){
		
		try {
			
			srt = readFile(filename, StandardCharsets.UTF_8);
			sampling = samp;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public Matcher srtParse() throws IOException{
		
		//pattern for reading srt format
		String nl = "\\r?\\n";
		String sp = "[ \\t]*";
		Pattern pattern = Pattern.compile(
		                    "(\\d+)" + sp + nl
		                    + "(\\d{1,2}):(\\d\\d):(\\d\\d),(\\d\\d\\d)" + sp
		                    + "-->" + sp + "(\\d\\d):(\\d\\d):(\\d\\d),(\\d\\d\\d)" + sp
		                    + "(X1:\\d.*?)??" + nl + "([^\\|]*?)" + nl + nl);
		
        Matcher matcher = pattern.matcher(srt);
		
        while(matcher.find()) nSubtitles++;
        
        matcher.reset();
        
        //timing is made of hh:mm:ss,ms
        startTimings = new int[nSubtitles][4];
        stopTimings = new int[nSubtitles][4];
        
        lines = new String[nSubtitles];

        int count = 0;
		//parsing each line in the srt file
        while(matcher.find()){
        	
        	//get the time for each line
        	startTimings[count][0] = Integer.parseInt(matcher.group(2));
        	startTimings[count][1] = Integer.parseInt(matcher.group(3));
        	startTimings[count][2] = Integer.parseInt(matcher.group(4));
        	startTimings[count][3] = Integer.parseInt(matcher.group(5));
        	
        	stopTimings[count][0] = Integer.parseInt(matcher.group(6));
        	stopTimings[count][1] = Integer.parseInt(matcher.group(7));
        	stopTimings[count][2] = Integer.parseInt(matcher.group(8));
        	stopTimings[count][3] = Integer.parseInt(matcher.group(9));
        	
        	lines[count] = matcher.group(10) + "\n" + matcher.group(11);
            
            //millisecs += hours*60*60*1000 + minutes*60*1000 + seconds*1000;
            
            count++;
            
        }
        
        return matcher;
	}
	
	public int getNumberLines(){
		
		return 	nSubtitles;
		
	}
	
	public int[][] getStartTimings(){
		
		return startTimings;
		
	}

	public int[][] getStopTimings(){
		
		return stopTimings;
		
	}
	
	public String[] getLines(){
		
		return lines;
		
	}
	
	public int[] getStartTimingNumber(int i){
		
		return startTimings[i];
		
	}
	
	public int[] getStopTimingNumber(int i){
		
		return stopTimings[i];
		
	}
	
	public int getPeak(int i){
		
		//returns peak sample for i-th line
		return sampling*startTimings[i][0]*60*60*1000 + startTimings[i][1]*60*1000 + startTimings[i][2]*1000 + startTimings[i][3];
		
	}
	
	public String getLineNumber(int i){
	
		return lines[i];
		
	}
	
	static String readFile(String path, Charset encoding) throws IOException{
		
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		
		return new String(encoded, encoding);
		
	}

}
