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

			mobileAverage[i] += (samples[i] + mobileAverage[i+1]);
			
			if(i + windowSize < nSamples){
				
				mobileAverage[i] -= mobileAverage[i+windowSize];
				
			}
			
			mobileAverage[i]/= Math.min(windowSize, nSamples - i);
			
		}
		
	}
	
	public int[] getNSamples(int i, int n){
		
		int[] nSamples;// = new int[n];
		
		//for(int l = 0; l < n; l++){
			
			//nSamples[l] = samples[i+l];
			
		//}
		
		return nSamples = Arrays.copyOfRange(samples, i, i+n);
	}
	
	public int getSampleNumber(int i){
		
		return samples[i];
		
	}
	
	public double getAverageAmplitude(){
		
		return averageAmplitude;
		
	}

	public double getAverageAmplitudeNumber(int i){
		
		return mobileAverage[i];
		
	}

}
