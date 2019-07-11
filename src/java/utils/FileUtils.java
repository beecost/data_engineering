package utils;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.iq80.snappy.SnappyInputStream;
import org.iq80.snappy.SnappyOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;
import java.util.zip.*;

public final class FileUtils {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FileUtils.class);

	/**
	 * After a successful call, a directory with the given pathname will exist.
	 * Uses {@link File#mkdir()} to create the directory, if it doesn't exist
	 * yet.
	 */
	public static void mkdir(@NotNull File file) throws IOException {
		if (file.exists()) {
			if (!file.isDirectory()) {
				throw new IOException(String.format("failed to mkdirs '%s' : exists && !isDirectory", file));
			}
		} else if (!file.mkdir()) {
			throw new IOException(String.format("failed to mkdir '%s'", file));
		}
	}

	public static InputStream wrapWithSnappy(InputStream inputStream, boolean useSnappy)
			throws IOException {
		if (useSnappy) {
			return new SnappyInputStream(inputStream, true);
		} else {
			return inputStream;
		}
	}

	public static OutputStream wrapWithSnappy(OutputStream outputStream, boolean useSnappy)
			throws IOException {
		if (useSnappy) {
			return new SnappyOutputStream(outputStream);
		} else {
			return outputStream;
		}
	}

	/**
	 * After a successful call, a directory with the given pathname will exist.
	 * Uses {@link File#mkdirs()} to create the directory, if it doesn't exist
	 * yet.
	 */
	public static void mkdirs(@NotNull File file) throws IOException {
		if (file.exists()) {
			if (!file.isDirectory()) {
				throw new IOException(String.format("failed to mkdirs '%s' : exists && !isDirectory", file));
			}
		} else if (!file.mkdirs()) {
			throw new IOException(String.format("failed to mkdirs '%s'", file));
		}
	}

	/**
	 * Creates new file with a given full file name. If a file with the same
	 * path & name exists then it will be deleted first
	 *
	 * @param file full file name
	 * @throws IOException if delete or create file operation fails
	 */
	public static void createOrReplaceFile(File file) throws IOException {
		if (file.exists()) {
			LOG.warn("File exists already: {}", file.getAbsolutePath());
			if (!file.delete()) {
				throw new IOException("Failed to delte file: " + file.getAbsolutePath());
			}
		}

		if (!file.createNewFile()) {
			throw new IOException("Failed to create new file: " + file.getAbsolutePath());
		}
	}

	public static void moveToDirectory(@NotNull File from, @NotNull File dirTo) throws IOException {
		assertDirectoryExists(dirTo);
		File to = new File(dirTo, from.getName());
		renameTo(from, to);
	}

	public static void renameTo(@NotNull File from, @NotNull File to) throws IOException {
		assertExists(from);
		assertNotExist(to);
		if (!from.renameTo(to)) {
			throw new IOException(String.format("failed to rename '%s' to '%s'", from, to));
		}
	}

	/**
	 * After a successful call, a file (directory) with the given pathname won't
	 * exist. The directory must be empty in order to be deleted.
	 * <p>
	 *
	 * @throws IOException if operation failed
	 */
	public static void delete(@NotNull File file) throws IOException {
		if (file.exists() && !file.delete()) {
			if (file.exists()) {
				throw new IOException(String.format("failed to delete '%s'", file));
			}
		}
	}

	/**
	 * After a successful call, inner non-directory files will be disappear
	 * <p>
	 *
	 * @throws IOException if operation failed
	 */
	public static void deleteInnerFiles(@NotNull File file) throws IOException {
		if (file.exists() && file.isDirectory()) {
			for (File f : file.listFiles()) {
				if (f.isFile()) {
					delete(f);
				}
			}
		}
	}

	/**
	 * After a successful call, a directory will be empty
	 * <p>
	 *
	 * @throws IOException if operation failed
	 */
	public static void deleteRecursivelyInner(@NotNull File file) throws IOException {
		if (file.exists() && file.isDirectory()) {
			for (File f : file.listFiles()) {
				deleteRecursively(f);
			}
		}
	}

	/**
	 * Tries to delete a file (directory) with the given pathname and logs
	 * failed attempts. The directory must be empty in order to be deleted.
	 */
	public static void deleteQuietly(File file) {
		try {
			delete(file);
		} catch (IOException e) {
			LOG.warn(e.getMessage());
		}
	}

	/**
	 * After a successful call, a file (directory) with the given pathname won't
	 * exist. This method also works with non-empty directories.
	 * <p>
	 *
	 * @throws IOException if operation failed
	 */
	public static void deleteRecursively(@NotNull File file) throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				File[] childFiles = file.listFiles();
				if (childFiles != null) {
					for (File childFile : childFiles) {
						deleteRecursively(childFile);
					}
				}
			}
			delete(file);
		}
	}

	/**
	 * Throws an exception if a file (directory) with the given pathname doesn't
	 * exist.
	 * <p>
	 *
	 * @throws IOException if file doesn't exist
	 */
	public static void assertExists(@NotNull File file) throws IOException {
		if (!file.exists()) {
			throw new IOException(String.format("file '%s' doesn't exist", file));
		}
	}

	/**
	 * Throws an exception if the given pathname doesn't denote an existing
	 * normal file.
	 * <p>
	 *
	 * @throws IOException if file doesn't exist
	 */
	public static void assertFileExists(@NotNull File file) throws IOException {
		if (!file.exists()) {
			throw new IOException(String.format("file '%s' doesn't exist", file));
		}
		if (!file.isFile()) {
			throw new IOException(String.format("file '%s' isn't a normal file", file));
		}
	}

	public static String read(@NotNull BufferedReader bufferedReader, boolean closeInputStream) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		try {
			char[] buffer = new char[1024 * 1024];
			int size;
			while ((size = bufferedReader.read(buffer)) != -1) {
				stringBuilder.append(buffer, 0, size);
			}
			return stringBuilder.toString();
		} catch (EOFException e) {
			return stringBuilder.toString();
		} finally {
			if (closeInputStream) {
				bufferedReader.close();
			}
		}
	}

	public static String read(@NotNull BufferedReader bufferedReader) throws IOException {
		return read(bufferedReader, true);
	}

	public static String read(@NotNull InputStream stream) throws IOException {
		return read(new BufferedReader(new InputStreamReader(stream)), true);
	}

	public static String read(@NotNull InputStream stream, boolean closeInputStream) throws IOException {
		return read(new BufferedReader(new InputStreamReader(stream)), closeInputStream);
	}

	@NotNull
	public static String read(@NotNull File file) throws IOException {
		BufferedReader bufferedReader = openBufferReader(file);
		StringBuilder stringBuilder = new StringBuilder();
		try {
			char[] buffer = new char[1024 * 1024];
			int size;
			while ((size = bufferedReader.read(buffer)) != -1) {
				stringBuilder.append(buffer, 0, size);
			}
			return stringBuilder.toString();
		} catch (EOFException e) {
			return stringBuilder.toString();
		} finally {
			bufferedReader.close();
		}
	}

	private static final String POPULAR_EXTENSIONS = "zip,pdf,mp3,jpg,rar,exe,wmv,doc,avi,"
			+ "ppt,mpg,tif,wav,mov,psd,wma,sitx,sit,eps,cdr,ai,xls,mp4,txt,m4a,rmvb,bmp,pps,"
			+ "aif,pub,dwg,gif,qbb,mpeg,indd,swf,asf,png,dat,rm,mdb,chm,jar,htm,dvf,dss,dmg,iso,"
			+ "flv,wpd,cda,m4b,7z,gz,fla,qxd,rtf,aiff,msi,jpeg,3gp,cdl,vob,ace,m4p,divx,html,pst,"
			+ "cab,ttf,xtm,hqx,qbw,sea,ptb,bin,mswmm,ifo,tgz,log,dll,mcd,ss,m4v,eml,mid,ogg,ram,"
			+ "lnk,torrent,ses,mp2,vcd,bat,asx,ps,bup,cbr,amr,wps,sql,docx,xlsx,pptx,csv,json,bin";
	private static final Set<String> EXTENSIONS = new ObjectOpenHashSet<>(
			Arrays.asList(POPULAR_EXTENSIONS.split(",")));

	public static File getFileWithSuffix(File dir, File input, String suffix) {
		String name = input.getName();
		int index = name.lastIndexOf('.');
		if (index > 0) {
			String extendsion = name.substring(index + 1);
			if (EXTENSIONS.contains(extendsion)) {
				name = name.substring(0, index) + "." + suffix + "." + extendsion;
			} else {
				name = name + "." + suffix;
			}
		} else {
			name = name + "." + suffix;
		}
		return new File(dir, name);
	}

	public static File getFileWithSuffix(File input, String suffix) {
		return getFileWithSuffix(input.getParentFile(), input, suffix);
	}

	/**
	 * Method reads last line from file if file exists and not empty.
	 * <p>
	 *
	 * @param path file to read
	 * @return last line in file, null in case of empty file
	 * <p>
	 * @throws IOException
	 */
	@Nullable
	public static String readLastLine(@NotNull Path path) throws IOException {
		String prevLine = null;
		try (BufferedReader br = Files.newBufferedReader(path, Charset.forName("UTF-8"))) {
			String line;
			while ((line = br.readLine()) != null) {
				prevLine = line;
			}
		}

		return prevLine;
	}

	@NotNull
	public static List<String> readList(@NotNull File file) throws IOException {
		List<String> res = new ObjectArrayList<>();
		addLinesToCollection(file, res);
		return res;
	}

	public static void addLinesToCollection(
			@NotNull File file, @NotNull Collection<String> collection)
			throws IOException {
		String line;
		try (BufferedReader bufferedReader = openBufferReader(file)) {
			while ((line = bufferedReader.readLine()) != null) {
				collection.add(line);
			}
			bufferedReader.close();
		} catch (EOFException ignored) {
		}
	}

	@NotNull
	public static BufferedWriter write(@NotNull BufferedWriter bw, @NotNull String... values) throws IOException {
		for (String val : values) {
			bw.append(val);
		}
		return bw;
	}

	@NotNull
	public static BufferedWriter writeln(@NotNull BufferedWriter bw, @NotNull String... values) throws IOException {
		write(bw, values).newLine();
		return bw;
	}

	public static File write(File output, Collection<? extends Object> list) throws IOException {
		try (BufferedWriter writer = openWriter(output)) {
			for (Object obj : list) {
				write(writer, obj.toString()).newLine();
			}
			writer.close();
		}
		return output;
	}

	public static File write(File output, Object[] list) throws IOException {
		try (BufferedWriter writer = openWriter(output)) {
			for (Object obj : list) {
				write(writer, obj.toString()).newLine();
			}
			writer.close();
		}
		return output;
	}

	/**
	 * Throws an exception if the given pathname doesn't denote an existing
	 * directory.
	 * <p>
	 *
	 * @throws IOException if file doesn't exist
	 */
	public static void assertDirectoryExists(@NotNull File file) throws IOException {
		if (!file.exists()) {
			throw new IOException(String.format("file '%s' doesn't exist", file));
		}
		if (!file.isDirectory()) {
			throw new IOException(String.format("file '%s' isn't a directory", file));
		}
	}

	/**
	 * Throws an exception if a file (directory) with the given pathname exists.
	 * <p>
	 *
	 * @throws IOException if file exist
	 */
	public static void assertNotExist(@NotNull File file) throws IOException {
		if (file.exists()) {
			throw new IOException(String.format("file '%s' exists", file));
		}
	}

	public static void replaceFile(File src, File dest) throws IOException {
		delete(dest);
		renameTo(src, dest);
	}

	/**
	 * Calculates the checksum file's pathname for a hadoop SequenceFile denoted
	 * by the given pathname.
	 */
	public static File getSequenceFileCrcFile(@NotNull File file) {
		return new File(file.getParentFile(), "." + file.getName() + ".crc");

	}

	public static void copyFileIfExists(File from, File to, int bufferSize) throws IOException {
		if (from.exists()) {
			copyFile(from, to, bufferSize);
		}
	}

	public static void linkOrCopyFile(File from, File to, byte[] buffer) throws IOException {
		try {
			Files.createLink(to.toPath(), from.toPath());
		} catch (IOException ex) {
			copyFile(from, to, buffer);
		}
	}

	public static void linkOrCopyFile(File from, File to, int bufferSize) throws IOException {
		try {
			Files.createLink(to.toPath(), from.toPath());
		} catch (IOException ex) {
			copyFile(from, to, bufferSize);
		}
	}

	public static void copyFile(@NotNull File from, @NotNull File to, int bufferSize) throws IOException {
		if (bufferSize <= 0) {
			throw new IllegalArgumentException("bufferSize <= 0");
		}
		assertFileExists(from);
		assertNotExist(to);
		copyFile(from, to, new byte[bufferSize]);
	}

	public static void copyFile(@NotNull File from, @NotNull File to) throws IOException {
		assertFileExists(from);
		copyFile(from, to, new byte[DEFAULT_BUFFER_SIZE]);
	}

	public static void copy(InputStream input, OutputStream output) throws IOException {
		copy(input, output, new byte[DEFAULT_BUFFER_SIZE >>> 2]);
	}

	public static void copy(InputStream input, OutputStream output, byte[] buffer) throws IOException {
		// we don't need to fill buffer fully before writing it into output,
		// because input.read does this almost always - TODO check
		int cnt;
		while ((cnt = input.read(buffer)) != -1) {
			output.write(buffer, 0, cnt);
		}
	}

	/**
	 * Closes all streams independently.
	 */
	public static void closeAll(Iterable<? extends Closeable> closeables) throws IOException {
		if (closeables == null) {
			return;
		}
		IOException e2 = null;
		for (Closeable closeable : closeables) {
			if (closeable == null) {
				continue;
			}
			try {
				closeable.close();
			} catch (IOException e) {
				e2 = e;
			}
		}
		if (e2 != null) {
			throw e2;
		}
	}

	/**
	 * Closes all streams independently.
	 */
	public static void closeAll(Closeable... closeables) throws IOException {
		if (closeables != null) {
			closeAll(Arrays.asList(closeables));
		}
	}

	private static void copyFile(File from, File to, byte[] buffer) throws IOException {
		FileInputStream input = null;
		FileOutputStream output = null;
		try {
			input = new FileInputStream(from);
			output = new FileOutputStream(to);
			copy(input, output, buffer);
		} finally {
			closeAll(input, output);
		}
	}

	public static void linkOrCopyRecursively(File from, File to, int bufferSize) throws IOException {
		assertExists(from);
		assertNotExist(to);
		linkOrCopyRecursively(from, to, new byte[bufferSize]);
	}

	private static void linkOrCopyRecursively(File from, File to, byte[] buffer) throws IOException {
		if (from.isDirectory()) {
			FileUtils.mkdir(to);
			File[] childFiles = from.listFiles();
			if (childFiles != null) {
				for (File childFile : childFiles) {
					linkOrCopyRecursively(childFile, new File(to, childFile.getName()), buffer);
				}
			}
		} else {
			linkOrCopyFile(from, to, buffer);
		}
	}

	public static void copyRecursively(@NotNull File from, @NotNull File to, int bufferSize) throws IOException {
		if (bufferSize <= 0) {
			throw new IllegalArgumentException("bufferSize <= 0");
		}
		assertExists(from);
		assertNotExist(to);
		copyRecursively(from, to, new byte[bufferSize]);
	}

	/**
	 * Copies fromDir/* to toDir/ recursively. Overwrites existing files.
	 * <p>
	 *
	 * @throws IOException
	 */
	public static void copyDirectoryContents(File fromDir, File toDir, int bufferSize) throws IOException {
		if (fromDir == null) {
			throw new IllegalArgumentException("fromDir is null");
		}
		if (toDir == null) {
			throw new IllegalArgumentException("toDir is null");
		}
		if (!fromDir.equals(toDir)) {
			if (bufferSize <= 0) {
				throw new IllegalArgumentException("bufferSize <= 0");
			}
			assertDirectoryExists(fromDir);
			mkdirs(toDir);
			copyRecursively(fromDir, toDir, new byte[bufferSize]);
		}
	}

	public static void copyDirectoryContents(File fromDir, File toDir) throws IOException {
		copyDirectoryContents(fromDir, toDir, DEFAULT_BUFFER_SIZE);
	}

	public static void copyFileToDirectory(File fromFile, File toDir) throws IOException {
		copyContent(fromFile, new File(toDir, fromFile.getName()));
	}

	private static void copyRecursively(File from, File to, byte[] buffer) throws IOException {
		if (from.isDirectory()) {
			FileUtils.mkdir(to);
			File[] childFiles = from.listFiles();
			if (childFiles != null) {
				for (File childFile : childFiles) {
					copyRecursively(childFile, new File(to, childFile.getName()), buffer);
				}
			}
		} else {
			copyFile(from, to, buffer);
		}
	}

	private FileUtils() {
	}

	@NotNull
	public static File removeSuffix(@NotNull File file, @NotNull String suffix) throws IOException {
		if (suffix.isEmpty()) {
			throw new IllegalArgumentException("Suffix to remove is empty. File: " + file.getCanonicalPath());
		}
		String path = file.getCanonicalPath();
		if (!path.endsWith(suffix)) {
			throw new IllegalArgumentException("File doesn't ends with " + suffix
					+ ". File: " + file.getCanonicalPath());
		}

		path = path.substring(0, path.length() - suffix.length());
		File renamed = new File(path);
		FileUtils.renameTo(file, renamed);
		return renamed;
	}

	@NotNull
	public static File addSuffix(@NotNull File file, @NotNull String suffix) throws IOException {
		if (suffix.isEmpty()) {
			throw new IllegalArgumentException("Suffix to remove is empty. File: " + file.getCanonicalPath());
		}
		String path = file.getCanonicalPath();
		if (path.endsWith(suffix)) {
			throw new IllegalArgumentException("File ends with " + suffix
					+ " already. File: " + file.getCanonicalPath());
		}
		File renamed = new File(path + suffix);
		FileUtils.renameTo(file, renamed);
		return renamed;
	}

	public static boolean writeTo(@NotNull BufferedWriter br, boolean first, @NotNull String line)
			throws IOException {
		if (first) {
			first = false;
		} else {
			br.newLine();
		}
		br.write(line);

		return first;
	}

	public static void assertNotInFolder(@NotNull FilenameFilter filterToProhibit, @NotNull File... folders)
			throws IOException {
		for (File folder : folders) {
			String[] names = folder.list(filterToProhibit);
			if (names == null) {
				throw new IOException(folder + " is not a directory");
			} else {
				if (names.length > 0) {
					throw new IOException(folder + " contains prohibited files");
				}
			}
		}
	}

	public static void setPermissionsRecursively(
			@NotNull Path path, @NotNull Set<PosixFilePermission> permissions) throws IOException {
		Files.walkFileTree(path, new RecursivePermissionUpdater(permissions));
	}

	private static class RecursivePermissionUpdater extends SimpleFileVisitor<Path> {

		private final Set<PosixFilePermission> permissions;

		private RecursivePermissionUpdater(@NotNull Set<PosixFilePermission> permissions) {
			this.permissions = permissions;
		}

		@NotNull
		@Override
		public FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) throws IOException {
			Files.setPosixFilePermissions(file, permissions);
			return FileVisitResult.CONTINUE;
		}

		@NotNull
		@Override
		public FileVisitResult postVisitDirectory(@NotNull Path dir, @NotNull IOException exc) throws IOException {
			Files.setPosixFilePermissions(dir, permissions);
			return FileVisitResult.CONTINUE;
		}

	}

	public static void copyIfSourceExists(@NotNull Path source, @NotNull Path destination) throws IOException {
		if (Files.exists(source)) {
			Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
		}
	}

	@NotNull
	public static Path getDestination(@NotNull Path symlink) throws IOException {
		if (Files.exists(symlink)) {
			if (Files.isSymbolicLink(symlink)) {
				return Files.readSymbolicLink(symlink);
			} else {
				throw new IOException(symlink.toString() + " should be symbolic link.");
			}
		} else {
			throw new IOException(symlink.toString() + " doesn't exists.");
		}
	}

	public static final String DIRECTORY_NAME_TMP = "tmp";

	/*
	 * create a not-existed temporary file
	 * created file will be existed with empty content after this function
	 */
	public static File tmpFile(File sample, File temp, boolean gzip) throws IOException {
		String fileName = sample.getName() + ".tmp";
		String gz = gzip ? ".gz" : "";
		File tmp = new File(temp, fileName + gz);
		int id = 0;
		while (tmp.exists()) {
			tmp = new File(temp, fileName + id + gz);
			id++;
		}
		try {
			try (BufferedWriter writer = openWriter(tmp)) {
				writer.write("");
			}
		} catch (IOException e) {
			throw new IOException("Can't create tmp file " + tmp.getPath());
		}
		return tmp;
	}

	/*
	 * create a not-existed temporary file
	 * default is support gzip format
	 */
	public static File tmpFile(File sample, File temp) throws IOException {
		return tmpFile(sample, temp, true);
	}

	public static File tmpDir(File sample, File temp) throws IOException {
		String fileName = sample.getName() + "_tmp";
		File tmp = new File(temp, fileName);
		int id = 0;
		while (tmp.exists()) {
			tmp = new File(temp, fileName + id);
			id++;
		}
		mkdir(tmp);
		return tmp;
	}

	/*
	 * create a not-existed temporary file
	 */
	public static File tmpFile(File sample, boolean gzip) throws IOException {
		File temp = sample.getParentFile();
		if (temp == null) {
			throw new IOException("Don't support temporary files for current directory");
		}
		File tmpDir = new File(temp, DIRECTORY_NAME_TMP);
		if (tmpDir.exists() && tmpDir.isDirectory()) {
			temp = tmpDir;
		}
		return tmpFile(sample, temp, gzip);
	}

	public static final String DIRECTORY_NAME_BACKUP = "backup";

	public static File createBackupFile(File sample, File directory) throws IOException {
		String name = sample.getName();
		String extension = "";
		int index = name.lastIndexOf('.');
		if (index >= 0) {
			extension = name.substring(index);
			name = name.substring(0, index);
		}
		File backup = new File(directory, name + extension);
		int id = 0;
		while (backup.exists()) {
			backup = new File(directory, name + ".bak" + ++id + extension);
		}
		com.google.common.io.Files.copy(sample, backup);
		return backup;
	}

	public static File createBackupFile(File sample) throws IOException {
		File directory = sample.getParentFile();
		if (directory != null) {
			File backup = new File(directory, DIRECTORY_NAME_BACKUP);
			if (backup.exists() && backup.isDirectory()) {
				directory = backup;
			}
			return createBackupFile(sample, directory);
		} else {
			throw new IOException("Don't support backup for current directory");
		}
	}

	private static int DEFAULT_BUFFER_SIZE = 4 * 1024 * 1024;

	public static long copyContent(BufferedReader reader, BufferedWriter writer) throws IOException {
		long lines = 0;
		while (true) {
			try {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				writer.write(line);
				writer.newLine();
				lines++;
			} catch (EOFException e) {
				// ignore this
				LOG.warn("Catched EOFException - ", e);
				break;
			}
		}
		writer.close();
		return lines;
	}

	public static long lines(File file) {
		long count = 0;
		try {
			BufferedReader reader = openBufferReader(file);
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				count++;
			}
			reader.close();
		} catch (IOException ignore) {
		}
		return count;
	}

	public static long copyContent(File from, BufferedWriter writer) throws IOException {
		long lines;
		try (BufferedReader reader = openBufferReader(from);) {
			lines = copyContent(reader, writer);
		}
		return lines;
	}

	// return number of line
	public static long copyContent(File from, File to) throws IOException {
		long lines;
		try (BufferedReader reader = openBufferReader(from); BufferedWriter writer = openWriter(to)) {
			lines = copyContent(reader, writer);
		}
		return lines;
	}

	public static boolean isContentEqual(File file1, File file2) throws IOException {
		HashCode hash1 = com.google.common.io.Files.hash(file1, Hashing.sha1());
		HashCode hash2 = com.google.common.io.Files.hash(file2, Hashing.sha1());
		return hash1.equals(hash2);
	}

	public static BufferedReader openBufferReader(File file) throws IOException {
		return openBufferReader(file, false);
	}

	public static BufferedReader openBufferReader(InputStream inputStream) throws IOException {
		return new BufferedReader(new InputStreamReader(inputStream), DEFAULT_BUFFER_SIZE);
	}

	public static BufferedReader openBufferReader(File file, final boolean forceGzip) throws IOException {
		if (forceGzip || file.getName().endsWith(".gz")) {
			return new BufferedReader(new InputStreamReader(new GZIPInputStream(
					new BufferedInputStream(new FileInputStream(file)))),
					(int) Math.min(DEFAULT_BUFFER_SIZE, file.length() + 1));
		} else {
			return new BufferedReader(new InputStreamReader(openInputStream(file)),
					(int) Math.min(DEFAULT_BUFFER_SIZE, file.length() + 1));
		}
	}

	public static InputStream openInputStream(File file) throws IOException {
		if (file.getName().endsWith(".gz")) {
			return new GZIPInputStream(new BufferedInputStream(new FileInputStream(file), DEFAULT_BUFFER_SIZE >> 1));
		} else {
			return new BufferedInputStream(new FileInputStream(file), DEFAULT_BUFFER_SIZE);
		}
	}

	public static InputStream openZipInputStreamFromSingleZippedFile(File file) throws IOException {
		ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)));
		zipInputStream.getNextEntry();
		return zipInputStream;
	}

	public static OutputStream openOutputStream(File file) throws IOException {
		if (file.getName().endsWith(".gz")) {
			return new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(file), DEFAULT_BUFFER_SIZE >>> 1)) {
				{
					def.setLevel(Deflater.BEST_SPEED);
				}

			};
		} else {
			return new BufferedOutputStream(new FileOutputStream(file), DEFAULT_BUFFER_SIZE);
		}
	}

	/**
	 * Open a print stream to write into a file
	 * <p>
	 *
	 * @param file
	 * @return
	 */
	public static BufferedWriter openWriter(File file) throws IOException {
		return openWriter(openOutputStream(file));
	}

	public static BufferedWriter openWriter(OutputStream outputStream) throws IOException {
		return new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Open print stream to append into a file
	 */
	public static BufferedWriter openAppendWriter(File file) throws Exception {
		if (!file.getName().endsWith(".gz")) {
			return new BufferedWriter(new FileWriter(file, true));
		} else {
			FileOutputStream fout = new FileOutputStream(file, true);
			GZIPOutputStream zstream = new GZIPOutputStream(fout, DEFAULT_BUFFER_SIZE >>> 1) {
				{
					def.setLevel(Deflater.BEST_SPEED);
				}

			};
			return new BufferedWriter(new OutputStreamWriter(zstream), DEFAULT_BUFFER_SIZE);
		}
	}


	public static void appendContentToFile(File inputfile, File outputFile) throws Exception {
		BufferedWriter newWriter = openAppendWriter(outputFile);
		copyContent(inputfile, newWriter);

	}

	/**
	 * including sub-directory's children, too
	 */
	public static Iterator<File> getChildrenFileIteration(final File file) {
		final Queue<File> queue = new LinkedList<>();
		if (file != null && file.exists() && file.isDirectory()) {
			queue.add(file);
		}
		return new Iterator<File>() {
			@Override
			public boolean hasNext() {
				return queue.size() > 0;
			}

			@Override
			public File next() {
				File file = queue.poll();
				if (file != null && file.isDirectory()) {
					queue.addAll(Arrays.asList(file.listFiles()));
				}
				return file;
			}
		};
	}

	public static void zipFiles(List<File> inputs, File zipFile) throws IOException {
		zipFiles(inputs, zipFile, false);
	}

	public static void zipFiles(List<File> inputs, File zipFile, boolean deleteAfterZip) throws IOException {
		FileOutputStream fout = null;
		ZipOutputStream zout = null;
		for (File file : inputs) {
			if (file == null || !file.exists()) {
				continue;
			}
			if (fout == null) {
				fout = new FileOutputStream(zipFile);
				zout = new ZipOutputStream(new BufferedOutputStream(fout));
			}
			byte[] buffer = new byte[100 * 1024];
			InputStream fin = openInputStream(file);
			zout.putNextEntry(new ZipEntry(file.getName()));
			int length;
			while ((length = fin.read(buffer)) > 0) {
				zout.write(buffer, 0, length);
			}
			zout.closeEntry();
			fin.close();
		}
		if (zout != null) {
			zout.close();
		}
		if (deleteAfterZip) {
			for (File file : inputs) {
				delete(file);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		File zipFile = new File("/home/tungmeo/test/test.zip");

		ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
		for (ZipEntry entry = zipInputStream.getNextEntry(); entry != null; entry = zipInputStream.getNextEntry()) {
			if (!entry.isDirectory()) {
				System.out.println("File:" + entry.getName());
				BufferedReader reader = new BufferedReader(new InputStreamReader(zipInputStream));
				for (String line = reader.readLine(); line != null; line = reader.readLine()) {
					System.out.println(line);
				}
				System.out.println();
			}
		}
		zipInputStream.closeEntry();
		zipInputStream.close();
	}

}
