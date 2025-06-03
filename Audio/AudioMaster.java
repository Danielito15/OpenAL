package Audio;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.WaveData;

public class AudioMaster {
	
	private static List<Integer> buffers = new ArrayList<Integer>();
	
	public static void init() {
		try {
			AL.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void setListenerData(float x, float y, float z) {
		AL10.alListener3f(AL10.AL_POSITION, x, y, z);
		AL10.alListener3f(AL10.AL_VELOCITY, x, y, z);
	}
	
	public static int loadSound(String path) {
	    int buffer = AL10.alGenBuffers();
	    buffers.add(buffer);

	    // Usa getResourceAsStream para cargar archivos desde el classpath
	    try (InputStream is = AudioMaster.class.getClassLoader().getResourceAsStream(path)) {
	        if (is == null) {
	            throw new IOException("No se encontró el archivo de audio en: " + path);
	        }

	        WaveData waveFile = WaveData.create(is);
	        AL10.alBufferData(buffer, waveFile.format, waveFile.data, waveFile.samplerate);
	        waveFile.dispose();
	        return buffer;

	    } catch (IOException e) {
	        e.printStackTrace();
	        return -1;
	    }
	}

	
	public static void cleanUp() {
		for (int buffer : buffers) {
			AL10.alDeleteBuffers(buffer);
		}
		AL.destroy();
	}

}
