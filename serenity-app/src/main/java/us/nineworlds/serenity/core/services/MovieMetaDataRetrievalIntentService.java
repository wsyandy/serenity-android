/**
 * The MIT License (MIT)
 * Copyright (c) 2012 David Carver
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF
 * OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package us.nineworlds.serenity.core.services;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import us.nineworlds.plex.rest.model.impl.MediaContainer;
import us.nineworlds.plex.rest.model.impl.Stream;
import us.nineworlds.serenity.core.model.impl.Subtitle;

/**
 * @author dcarver
 *
 */
public class MovieMetaDataRetrievalIntentService extends AbstractPlexRESTIntentService {

	private String key;
	private ArrayList<Subtitle> subtitles;
	private static final String MOVIES_RETRIEVAL_INTENT_SERVICE = "MovieMetaDataRetrievalIntentService";


	/**
	 * @param name
	 */
	public MovieMetaDataRetrievalIntentService() {
		super(MOVIES_RETRIEVAL_INTENT_SERVICE);
	}

	/* (non-Javadoc)
	 * @see us.nineworlds.serenity.core.services.AbstractPlexRESTIntentService#sendMessageResults(android.content.Intent)
	 */
	@Override
	public void sendMessageResults(Intent intent) {
		Bundle extras = intent.getExtras();
		if (extras != null) {
			Messenger messenger = (Messenger) extras.get("MESSENGER");
			Message msg = Message.obtain();
			msg.obj = subtitles;
			try {
				messenger.send(msg);
			} catch (RemoteException ex) {
				Log.e(getClass().getName(), "Unable to send message", ex);
			}
		}
		
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		key = intent.getExtras().getString("key", "");
		try {
			MediaContainer mc = factory.retrieveMovieMetaData("/library/metadata/" + key);
			findSubtitle(mc);
			sendMessageResults(intent);			
		} catch (Exception ex) {
			Log.e(MOVIES_RETRIEVAL_INTENT_SERVICE, "Error retreiving metadata." + ex.getMessage(), ex);
		}
	}
	
	protected void findSubtitle(MediaContainer mc) {
		List<Stream> streams = mc.getVideos().get(0).getMedias().get(0).getVideoPart().get(0).getStreams();
		subtitles = new ArrayList<Subtitle>();
		for (Stream stream : streams) {
			if ("srt".equals(stream.getFormat()) ||
				"ass".equals(stream.getFormat())) {
				
				Subtitle subtitle = new Subtitle();
				subtitle = new Subtitle();
				subtitle.setFormat(stream.getFormat());
				subtitle.setLanguageCode(stream.getLanguageCode());
				subtitle.setKey(factory.baseURL() + stream.getKey().replaceFirst("/", ""));
				if (stream.getLanguage() == null) {
					subtitle.setDescription("Unknown (" + stream.getFormat() + ")");
				} else {
					subtitle.setDescription(stream.getLanguage() + " (" + stream.getFormat() + ")");
				}
				subtitles.add(subtitle);
			}
		}
	}
}
