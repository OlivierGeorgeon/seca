package gwt.seca.client;

import com.allen_sauer.gwt.voices.client.Sound;
import com.allen_sauer.gwt.voices.client.SoundController;

/**
 * The manager to manipulate sounds
 * @author Olivier
 */
public class SoundManager {
	
	/**
	 * Constructor instantiate a sound controller.
	 * Loads a set of predefined sounds.
	 */
	public SoundManager() {
		mSoundController = new SoundController();
		loadSounds();
	}
	
	/**
	 * Create a sound from an mp3 at a url.
	 * @param url the url of the mp3 file.
	 * @return the created sound.
	 */
	public Sound createMp3(String url) {
		return mSoundController.createSound(Sound.MIME_TYPE_AUDIO_MPEG_MP3, url);
	}
	
	private void loadSounds() {
//		bubble9 = createMp3("http://media.freesound.org/data/104/previews/104948__Glaneur_de_sons__bubble_9__preview.mp3");
//		cinematicBoomNorm = createMp3("http://media.freesound.org/data/33/previews/33637__HerbertBoland__CinematicBoomNorm_preview.mp3");
//		bubble9 =           createMp3("http://liris.cnrs.fr/ideal/demo/sound/goutte_eau.mp3");
		bubble9 =           createMp3("http://liris.cnrs.fr/ideal/demo/sound/goutte_eau.mp3");
		cinematicBoomNorm = createMp3("http://liris.cnrs.fr/ideal/demo/sound/ressort3.mp3");
		terminate =         createMp3("http://liris.cnrs.fr/ideal/demo/sound/terminate.mp3");
		cuddle =         createMp3("http://liris.cnrs.fr/ideal/demo/sound/cuddle.mp3");
	}
	
	private SoundController mSoundController;
	
	public static Sound bubble9;
	public static Sound cinematicBoomNorm;
	public static Sound terminate;
	public static Sound cuddle;
	
}
