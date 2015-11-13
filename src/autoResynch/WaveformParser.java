package autoResynch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class WaveformParser {

	private RandomAccessFile in;
	private String fileName; 
	private int sampling;
	private int nSamples;
	private double averageAmplitude;
	private int[] samples;
	
	public WaveformParser(String name, int samp){
				
			fileName = name;
			sampling = samp;

	}
	
	public void loadWaveform() throws IOException{
		
		in = new RandomAccessFile(fileName, "r");
		nSamples = (int) (in.length()/2);
		
		byte[] byteArray = new byte[2]; 

		samples = new int[nSamples];
		averageAmplitude = 0;
	
		for(int i = 0; i < nSamples; i++){
				
			in.readFully(byteArray);
			ByteBuffer bb = ByteBuffer.wrap(byteArray);
			bb.order( ByteOrder.LITTLE_ENDIAN);
			short data = bb.getShort();

			samples[i] = data;
			averageAmplitude += Math.abs(data/nSamples);
			
		}
		
		in.close();
	}
	
	public void normaliseBy(double n){
		
		for(int i = 0; i < nSamples; i++){
			
			samples[i] *= n;
			
		}
		
	}
	
	public int[] getSamples(){
		
		return samples;
		
	}
	
	public int[] getNSamples(int i, int n){
		
		int[] nSamples = new int[n];
		
		for(int l = 0; l < n; l++){
			
			nSamples[l] = samples[i+l];
			
		}
		
		return nSamples;
	}
	
	public int getSampleNumber(int i){
		
		return samples[i];
		
	}
	
	public double getAverageAmplitude(){
		
		return averageAmplitude;
		
	}
	
}
