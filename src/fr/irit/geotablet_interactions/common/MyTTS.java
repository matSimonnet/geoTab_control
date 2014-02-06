package fr.irit.geotablet_interactions.common;

import java.util.Locale;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;

/**
 * Provides an instance of TextToSpeech to use throughout the application
 * 
 * @author helene jonin
 * @mail helene.jonin@gmail.com
 * 
 */
public class MyTTS implements OnInitListener {
	private static TextToSpeech tts;

	private MyTTS() {
	}

	/**
	 * Get TTS instance
	 * 
	 * @param context
	 *            The context
	 * @return The TTS instance
	 */
	public static TextToSpeech getInstance(Context context) {
		if (tts == null) { // Instantiate TTS if not done yet
			tts = new TextToSpeech(context, new MyTTS());
		}
		return tts;
	}

	@Override
	/**
	 * Initialize TTS
	 */
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			tts.setLanguage(Locale.getDefault());
			tts.setSpeechRate(1.5f);
		} else {
			Log.e("TTS", "Initialization failed");
		}
	}

	/**
	 * Release TTS instance
	 */
	public static void release() {
		if (tts != null) {
			tts.stop();
			tts.shutdown();
			tts = null;
		}
	}

}
