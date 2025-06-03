package Audio;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import javazoom.jl.converter.Converter;

public class AudioConverter {

	public static File convertToMono(String mp3Path, String nombreArchivoWav) {
	    try {
	        // Ruta base del proyecto
	        String basePath = System.getProperty("user.dir");

	        // Construir ruta absoluta a src/Audio/
	        File audioDir = new File(basePath + File.separator + "res" + File.separator + "Audios");
	        if (!audioDir.exists()) {
	            audioDir.mkdirs(); // Crea el directorio si no existe
	        }

	        // Paso 1: convertir MP3 a WAV estéreo temporal
	        File tempWav = new File(audioDir, "temp_stereo.wav");
	        Converter converter = new Converter();
	        converter.convert(mp3Path, tempWav.getAbsolutePath());

	        // Paso 2: leer WAV generado (estéreo)
	        AudioInputStream stereoStream = AudioSystem.getAudioInputStream(tempWav);
	        AudioFormat stereoFormat = stereoStream.getFormat();

	        // Paso 3: crear nuevo formato mono PCM
	        AudioFormat monoFormat = new AudioFormat(
	            stereoFormat.getSampleRate(),
	            16,
	            1, // 1 canal
	            true,
	            false
	        );

	        // Paso 4: convertir a mono
	        AudioInputStream monoStream = AudioSystem.getAudioInputStream(monoFormat, stereoStream);

	        // Paso 5: guardar archivo WAV mono
	        File monoFile = new File(audioDir, nombreArchivoWav);
	        AudioSystem.write(monoStream, AudioFileFormat.Type.WAVE, monoFile);

	        // Cerrar y limpiar
	        monoStream.close();
	        stereoStream.close();
	        tempWav.delete();

	        return monoFile;

	    } catch (Exception e) {
	        System.err.println("Error al convertir MP3 a WAV mono:");
	        e.printStackTrace();
	        return null;
	    }
	}
	
	public static List<File> convertirMultiplesMP3(List<String> rutasMP3, List<String> nombresWAV) {
	    List<File> wavs = new ArrayList<>();
	    for (int i = 0; i < rutasMP3.size(); i++) {
	        File wav = AudioConverter.convertToMono(rutasMP3.get(i), nombresWAV.get(i));
	        if (wav != null && wav.exists()) {
	            wavs.add(wav);
	        } else {
	            System.err.println("Error al convertir: " + rutasMP3.get(i));
	        }
	    }
	    return wavs;
	}
	
	public static String extraerRutaRelativa(String rutaCompleta) {
	    File archivo = new File(rutaCompleta);
	    File carpeta = archivo.getParentFile(); // obtiene la carpeta contenedora
	    if (carpeta != null) {
	        return carpeta.getName() + "/" + archivo.getName(); // ej: OpenAL/audio.wav
	    } else {
	        return archivo.getName(); // solo el nombre si no tiene carpeta padre
	    }
	}

}


