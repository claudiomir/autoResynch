package autoResynch;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class WaveformParser {

	private RandomAccessFile in;
	private String fileName; 
	private int sampling;
	private int nSamples;
	private double averageAmplitude;
	private int[] samples;
	private double[] mobileAverage;
	private boolean[] silence;
	private int numberOfBreaks;
	
	public WaveformParser(String name, int samp){
				
			fileName = name;
			sampling = samp;
			
			try {
				this.loadWaveform();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}
	
	public void loadWaveform() throws IOException{
		
		in = new RandomAccessFile(fileName, "r");
		nSamples = (int) (in.length()/2);
		silence = new boolean[nSamples];
		numberOfBreaks = 0;
		
		byte[] byteArray = new byte[2]; 

		samples = new int[nSamples];
		averageAmplitude = 0;
		int count = 0;
		boolean isSilence = false;
		
		for(int i = 0; i < nSamples; i++){
				
			in.readFully(byteArray);
			ByteBuffer bb = ByteBuffer.wrap(byteArray);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			short data = bb.getShort();

			samples[i] = data;
			if(Math.abs(samples[i]) < 100){
				count++;
				if(count > 300){
					isSilence = true;
				}
			}else{
				
				if(isSilence){
					silence[i-1] = true;
					//System.out.println("Silence interrupted at "+ (i-1) + "\n");
					
					/*for(int r=0; r < 100; r++){
						
						System.out.println(samples[i-r]);

					}*/

					numberOfBreaks++;
				}
				count = 0;
				isSilence = false;
			}
			
			//if(count > 300){
				//isSilence = true;
				
				//silence[i] = true;
				//System.out.println((double)i/16000);
				//count = 0;
			//}else{
				//silence[i] = false;
			//}
			averageAmplitude += Math.abs(data);
			
		}
		averageAmplitude/=nSamples;
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
	
	//compare a set of samples against a set of samples of the same size from This
	public double compareNSamplesWithNSamples(int[] sampleSet, int index){
		
		double acc = 0;
		
		for(int i = 0; i < sampleSet.length && (index + i) < nSamples; i++){
			
			acc += Math.abs(sampleSet[i] - samples[index+i]);
			
		}
		
		return acc/(double)sampleSet.length;
	}
	
	//compare a set of samples against a set of samples of the same size from This. The set from This is normalised by the ratio between its average amplitude ant the sampleSet's
	public double compareNSamplesWithNSamplesNormalised(int[] sampleSet, int index, double sampleSetAverage){
		
		double acc = 0;
		double ratio = sampleSetAverage/mobileAverage[index];
		
		for(int i = 0; i < sampleSet.length && (index + i) < nSamples; i++){
			
			acc += Math.abs(sampleSet[i] - (samples[index+i] * ratio));
			
		}
		
		return acc/(double)sampleSet.length;
	}
	
	//compares a set of samples against all samples, finds the closest subset of samples from This and returns the corresponding index (sample number)
	public int compareNSamplesWithAllSamples(int[] sampleSet){
		
		double currentDistance = 0;
		double minDistance = Double.MAX_VALUE;
		int minIndex = -1;
		
		for(int i = 0; i < nSamples - sampleSet.length; i++){
			
			currentDistance = compareNSamplesWithNSamples(sampleSet, i);
			
			if(currentDistance < minDistance){
				
				minIndex = i;
				minDistance = currentDistance;
				
			}
			
		}
		
		return minIndex;
	}
	
	//only compare a set of samples against a subset of samples from This that are within "bound" samples of distance from a given index
	public int compareNSamplesWithAllSamplesBound(int[] sampleSet, int index, int bound){
		
		double currentDistance = 0;
		double minDistance = Double.MAX_VALUE;
		int minIndex = -1;
		
		for(int i = Math.max(0, index - bound); i < Math.min(index + bound, nSamples - sampleSet.length); i++){
			
			currentDistance = compareNSamplesWithNSamples(sampleSet, i);
			
			if(currentDistance < minDistance){
				
				minIndex = i;
				minDistance = currentDistance;
				
			}
			
		}
		
		return minIndex;
	}
	
	//only compare a set of samples against a subset of samples from This that are within "bound" samples of distance from a given index
	public int compareNSamplesWithAllSamplesBoundNormalised(int[] sampleSet, int index, int bound, double sampleSetAverage){
		
		double currentDistance = 0;
		double minDistance = Double.MAX_VALUE;
		int minIndex = -1;
		
		//double sampleSetAverage = 0;
		
		//calculate the average amplitude for sampleSet
		/*for(int k = 0; k < sampleSet.length; k++){
			
			sampleSetAverage = (double)sampleSet[k]/sampleSet.length;
			
		}*/
		
		for(int i = Math.max(0, index - bound); i < Math.min(index + bound, nSamples - sampleSet.length); i++){
			
			currentDistance = compareNSamplesWithNSamplesNormalised(sampleSet, i, sampleSetAverage);
			
			if(currentDistance < minDistance){
				
				minIndex = i;
				minDistance = currentDistance;
				
			}
			
		}
		return minIndex;
	}
	
	public void setMobileAverage(int windowSize){
		
		mobileAverage = new double[this.nSamples];
		
		mobileAverage[nSamples - 1] = 1;
		
		for(int i = nSamples-2; i >= 0; i--){

			mobileAverage[i] = (Math.abs(samples[i]) + mobileAverage[i+1]);

			if(i + windowSize < nSamples){

				mobileAverage[i] -= Math.abs(samples[i+windowSize]);
				
			}

		}

		for(int i = nSamples-2; i >= 0; i--){

			mobileAverage[i] /= windowSize;
		}
	}
	
	
	public int[] getNSamples(int i, int n){
		
		return Arrays.copyOfRange(samples, i, i+n);
		
	}
	
	public int getSampleNumber(int i){
		
		return samples[i];
		
	}
	
	public double getAverageAmplitude(){
		
		return averageAmplitude;
		
	}
	
	public int getNumberOfSamples(){
		
		return nSamples;
		
	}
	
	public int getNumberOfBreaks(){
		
		return numberOfBreaks;
		
	}
	
	public double getAverageAmplitudeNumber(int i){
		
		return mobileAverage[i];
		
	}
	
	public boolean[] getSilentSamples(){
		
		return silence;
		
	}

}
