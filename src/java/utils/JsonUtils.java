package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

/**
 * Created on 14/11/2015.
 */
public class JsonUtils {
	private static final Gson GSON_INLINE = new Gson();
	private static final Gson GSON_PRETTY = new GsonBuilder().setPrettyPrinting().create();

	public static String toJsonInline(Object obj) {
		return GSON_INLINE.toJson(obj);
	}

	public static String toJsonPretty(Object obj) {
		return GSON_PRETTY.toJson(obj);
	}

	public static <T> T fromJson(String json, Class<T> classOfT) throws JsonSyntaxException {
		return GSON_PRETTY.fromJson(json, classOfT);
	}

	public static <T> JsonArrayReader<T> openJsonArrayReader(
			final File jsonArrayFile, Class<T> classOfT) throws IOException {
		return new JsonArrayReader<>(jsonArrayFile, classOfT);
	}

	public static JsonArrayWriter openJsonArrayWriter(final File jsonArrayFile) throws IOException {
		return new JsonArrayWriter(jsonArrayFile);
	}

	public static <T> void writeArray(Iterable<T> list, final File jsonArrayFile) throws IOException {
		JsonArrayWriter writer = openJsonArrayWriter(jsonArrayFile);
		try {
			writer.writeAll(list);
		} finally {
			writer.close();
		}
	}

	public static <T> void writeArray(T[] list, final File jsonArrayFile) throws IOException {
		writeArray(Arrays.asList(list), jsonArrayFile);
	}

	public static class JsonArrayReader<T> {
		private final JsonReader reader;
		private final Gson gson = new Gson();
		private final Class<T> classOfT;
		private boolean finish = false;

		public JsonArrayReader(File jsonArrayFile, Class<T> classOfT) throws IOException {
			this(FileUtils.openBufferReader(jsonArrayFile), classOfT);
		}

		public JsonArrayReader(Reader reader, Class<T> classOfT) throws IOException {
			this.reader = new JsonReader(reader);
			this.classOfT = classOfT;
			this.reader.beginArray();
		}

		public boolean hasNext() throws IOException {
			return reader == null ? false : reader.hasNext();
		}

		public T next() throws IOException {
			if (finish) {
				return null;
			} else if (reader.hasNext()) {
				return gson.fromJson(reader, classOfT);
			} else {
				finish = true;
				reader.endArray();
			}
			return null;
		}

		public List<T> readAll() throws IOException {
			try {
				List<T> result = new ObjectArrayList<>();
				for (T val = next(); val != null; val = next()) {
					result.add(val);
				}
				return result;
			} finally {
				reader.close();
			}
		}

		public void close() throws IOException {
			reader.close();
		}
	}

	public static class JsonArrayWriter {
		private final BufferedWriter writer;
		private int lines = 0;
		private boolean isClosed = false;

		private JsonArrayWriter(File file) throws IOException {
			this.writer = FileUtils.openWriter(file);
			this.writer.write("[");
		}

		public synchronized void write(@NotNull Object object) throws IOException {
			if (lines > 0) {
				writer.write(",");
				writer.newLine();
			}
			writer.write(toJsonInline(object));
			lines++;
		}

		public synchronized <T> void writeAll(Iterable<T> list) throws IOException {
			for (T object : list) {
				write(object);
			}
		}

		public synchronized <T> void writeAll(T[] list) throws IOException {
			for (T object : list) {
				write(object);
			}
		}

		public void close() throws IOException {
			if (isClosed) {
				return;
			} else {
				synchronized (this) {
					if (!isClosed) {
						this.writer.write("]");
						this.writer.close();
						isClosed = true;
					}
				}
			}

		}
	}
}
