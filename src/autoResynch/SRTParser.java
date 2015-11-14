package autoResynch;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SRTParser implements Cloneable {
	
	private String srt;
	private int sampling;
	
	private int nSubtitles = 0;
	
	private int[][] startTimings;
	private int[][] stopTimings;
	
	private String[] lines;
	
	public SRTParser(){
		
		srt = "";
		sampling = 0;
		
	}
	
	public SRTParser(String filename, int samp){
		
		try {
			
			srt = readFile(filename, StandardCharsets.UTF_8);
			sampling = samp;
			this.srtParse();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public SRTParser(SRTParser s){
		
		this.srt = s.srt;
		this.sampling = s.sampling;
		this.nSubtitles = s.nSubtitles;
		this.startTimings = s.startTimings;
		this.stopTimings = s.stopTimings;
		this.lines = s.lines;
		
	}
	
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
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
        	
        	lines[count] = matcher.group(11);//matcher.group(10); + "\n" + 
            
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
		return sampling*(startTimings[i][0]*60*60*1000 + startTimings[i][1]*60*1000 + startTimings[i][2]*1000 + startTimings[i][3]);
		
	}
	
	public int getStopPeak(int i){
		
		return sampling*(stopTimings[i][0]*60*60*1000 + stopTimings[i][1]*60*1000 + stopTimings[i][2]*1000 + stopTimings[i][3]);

	}
	
	public String getLineNumber(int i){
	
		return lines[i];
		
	}
	
	static String readFile(String path, Charset encoding) throws IOException{
		
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		
		return new String(encoded, encoding);
		
	}
	
	public int[] sampleNumberToHHMMSSMS(int sampleNumber){
		
		int[] HHMMSSMS = new int[4];
		
		int totalMilliseconds = (int)((double)sampleNumber/sampling);

		int milliseconds = totalMilliseconds % 1000;
		
		int totalSeconds = (totalMilliseconds - milliseconds)/1000;
		
		int seconds = totalSeconds % 60;
		
		int totalMinutes = (totalSeconds-seconds)/60;
		
		int minutes = totalMinutes%60;
		
		int hours = (totalMinutes-minutes)/60;
		
		//System.out.println("0" + hours + ":0" + minutes + ":" + seconds + "," + milliseconds);
		
		HHMMSSMS[0] = hours;
		HHMMSSMS[1] = minutes;
		HHMMSSMS[2] = seconds;
		HHMMSSMS[3] = milliseconds;
		
		return HHMMSSMS;
		
	}
	
	public void applyDelay(int delay){
				
		for(int i=0; i < this.nSubtitles; i++){
			
			startTimings[i] = sampleNumberToHHMMSSMS(this.getPeak(i)+delay);
			stopTimings[i] = sampleNumberToHHMMSSMS(this.getStopPeak(i)+delay);

		}
	}

	public void print(){
		
		for(int i=0; i < this.nSubtitles; i++){

			System.out.println(i);
			
			if(startTimings[i][0] < 10){ System.out.print("0"+startTimings[i][0]); } else { System.out.print(startTimings[i][0]); };
			System.out.print(":");
			if(startTimings[i][1] < 10){ System.out.print("0"+startTimings[i][1]); } else { System.out.print(startTimings[i][1]); };
			System.out.print(":");
			if(startTimings[i][2] < 10){ System.out.print("0"+startTimings[i][2]); } else { System.out.print(startTimings[i][2]); };
			System.out.print(",");
			if(startTimings[i][3] < 10){ System.out.print("00"+startTimings[i][3]); } else if (startTimings[i][3] < 100){ System.out.print("0"+startTimings[i][3]); } else { System.out.print(startTimings[i][3]); };
			
			System.out.print(" --> ");
			
			if(stopTimings[i][0] < 10){ System.out.print("0"+stopTimings[i][0]); } else { System.out.print(stopTimings[i][0]); };
			System.out.print(":");
			if(stopTimings[i][1] < 10){ System.out.print("0"+stopTimings[i][1]); } else { System.out.print(stopTimings[i][1]); };
			System.out.print(":");
			if(stopTimings[i][2] < 10){ System.out.print("0"+stopTimings[i][2]); } else { System.out.print(stopTimings[i][2]); };
			System.out.print(",");
			if(stopTimings[i][3] < 10){ System.out.print("00"+stopTimings[i][3]); } else if (stopTimings[i][3] < 100){ System.out.print("0"+stopTimings[i][3]); } else { System.out.print(stopTimings[i][3]); };
			
			System.out.println();
			
			System.out.println(lines[i]);
			
			System.out.println();
			
		}
		
	}
	
	public void print(int i){
		

			System.out.println(i+1);
			
			if(startTimings[i][0] < 10){ System.out.print("0"+startTimings[i][0]); } else { System.out.print(startTimings[i][0]); };
			System.out.print(":");
			if(startTimings[i][1] < 10){ System.out.print("0"+startTimings[i][1]); } else { System.out.print(startTimings[i][1]); };
			System.out.print(":");
			if(startTimings[i][2] < 10){ System.out.print("0"+startTimings[i][2]); } else { System.out.print(startTimings[i][2]); };
			System.out.print(",");
			if(startTimings[i][3] < 10){ System.out.print("00"+startTimings[i][3]); } else if (startTimings[i][3] < 100){ System.out.print("0"+startTimings[i][3]); } else { System.out.print(startTimings[i][3]); };
			
			System.out.print(" --> ");
			
			if(stopTimings[i][0] < 10){ System.out.print("0"+stopTimings[i][0]); } else { System.out.print(stopTimings[i][0]); };
			System.out.print(":");
			if(stopTimings[i][1] < 10){ System.out.print("0"+stopTimings[i][1]); } else { System.out.print(stopTimings[i][1]); };
			System.out.print(":");
			if(stopTimings[i][2] < 10){ System.out.print("0"+stopTimings[i][2]); } else { System.out.print(stopTimings[i][2]); };
			System.out.print(",");
			if(stopTimings[i][3] < 10){ System.out.print("00"+stopTimings[i][3]); } else if (stopTimings[i][3] < 100){ System.out.print("0"+stopTimings[i][3]); } else { System.out.print(stopTimings[i][3]); };
			
			System.out.println();
			
			System.out.println(lines[i]);
			
			System.out.println();
			
		
		
	}
}
