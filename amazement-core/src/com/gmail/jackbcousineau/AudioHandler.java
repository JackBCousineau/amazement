package com.gmail.jackbcousineau;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioHandler{

	/*(public void pause(){
		if(!isPaused){
			System.out.println("Pausing audio");
			isPaused = true;
		}
		else unPause();
	}

	public void unPause(){
		System.out.println("Unpausing audio");
		isPaused = false;
	}*/

	String path;

	boolean isPaused = false;
	
	public AudioHandler(String path) throws IOException, LineUnavailableException, UnsupportedAudioFileException{

		//final String PATH = "/Users/Jack/Satisfaction.wav";
		this.path = path;
		final File file = new File(path);
		AudioInputStream in = null;
		in = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(file)));

		AudioInputStream din = null;
		final AudioFormat baseFormat = in.getFormat();
		final AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
				baseFormat.getSampleRate(),
				16,
				baseFormat.getChannels(),
				baseFormat.getChannels() * 2,
				baseFormat.getSampleRate(),
				false);
		din = AudioSystem.getAudioInputStream(decodedFormat, in);  
		long frames = in.getFrameLength();
		double initDur = (frames+0.0) / baseFormat.getFrameRate();
		System.out.println("Initial dur: " + initDur);
		rawplay(decodedFormat, din, initDur);
		in.close();     
		System.out.println("Completely done decoding audio");
		//durationInSeconds = (frames+0.0) / baseFormat.getFrameRate();
	}

	private static synchronized void rawplay(final AudioFormat targetFormat, final AudioInputStream din, double duration) throws IOException, LineUnavailableException {              
		final byte[] data = new byte[4096];
		final SourceDataLine line = getLine(targetFormat);               
		if (line != null) {
			System.out.println(data.length);
			System.out.println("Entering ...");
			// Start
			line.start();
			int nBytesRead = 0, nBytesWritten = 0, i = 0;
			while (nBytesRead != -1) {
				nBytesRead = din.read(data, 0, data.length);
				if (nBytesRead != -1) {
					nBytesWritten = line.write(data, 0, nBytesRead);
					//System.out.println("... -->" + data[0] + " bytesWritten:" + nBytesWritten);
				}
				i++;
			}
			double constant = i/duration;
			// End of while //            
			System.out.println("Done ...");
			System.out.println("Duration: " + duration + ", constant: " + constant);
			// Stop
			line.drain();
			line.stop();
			line.close();
			din.close();
		} // End of the if //
	}

	private static synchronized SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException {
		SourceDataLine res = null;
		final DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		res = (SourceDataLine) AudioSystem.getLine(info);
		res.open(audioFormat);
		return res;
	}

}
