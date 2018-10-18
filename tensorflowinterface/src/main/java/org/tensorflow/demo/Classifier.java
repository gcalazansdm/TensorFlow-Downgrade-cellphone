package org.tensorflow.demo;

import android.graphics.Bitmap;
import android.graphics.RectF;

import java.util.List;

/**
 * Created by gcmoura on 15/02/18.
 */

public interface Classifier {
	class Recognition {
		private final String id;
		private final String title;
		private final Float confidence;
		private final RectF location;

		public Recognition(final String id, final String title,
						   final Float confidence, final RectF location) {
			this.id = id;
			this.title = title;
			this.confidence = confidence;
			this.location = location;
		}

		public String getId() {
			return id;
		}

		public String getTitle() {
			return title;
		}

		public Float getConfidence() {
			return confidence;
		}

		public RectF getLocation() {
			return new RectF(location);
		}
	}

	List<Recognition> recognizeImage(Bitmap bitmap);
}
