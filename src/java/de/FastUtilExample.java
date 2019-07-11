package de;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import objectexplorer.MemoryMeasurer;

import java.util.*;

public class FastUtilExample {

	private static final Random RAND = new Random();

	// sinh ra số nguyên ngẫu nhiên
	private static int randomInt() {
		return RAND.nextInt();
	}

	// tạo String ngẫu nhiên chứa kí tự từ 'a' - 'z'
	private static String randomString() {
		final int len = RAND.nextInt(100) + 1;
		final char[] value = new char[len];
		for (int i=0; i < len; i++) {
			value[i] = (char)('a' + RAND.nextInt('z' - 'a'));
		}
		return new String(value);
	}

	public static void testList() {
		final int size = 1_000_000;
		System.out.println("\n- - - - - - - - ");
		System.out.println("Test memory java DEFAULT LIST and FAST LIST, test size = " + size);
		final List<Integer> list = new ArrayList<>(size);
		final List<Integer> fastList = new IntArrayList(size);
		for (int i=0; i<size; i++) {
			int value = randomInt();
			list.add(value);
			fastList.add(value);
		}
		final long listBytes = MemoryMeasurer.measureBytes(list) / 1000;
		final long fastListBytes = MemoryMeasurer.measureBytes(fastList) / 1000;
		System.out.println("Default list = " + listBytes + " KB");
		System.out.println("Fast list = " + fastListBytes + " KB (" + ((100.0 * fastListBytes) / listBytes) + "%)");
	}

	public static void testNativeMap() {
		final int size = 1_000_000;
		System.out.println("\n- - - - - - - - ");
		System.out.println("Test memory java DEFAULT MAP and FAST MAP with NATIVE TYPE, test size = " + size);
		final Map<Long, Integer> map = new HashMap<>(size);
		final Map<Long, Integer> fastMap = new Long2IntOpenHashMap(size);
		for (int i=0; i<size; i++) {
			long key = (long) randomInt();
			int value = randomInt();
			map.put(key, value);
			fastMap.put(key, value);
		}
		final long defaultBytes = MemoryMeasurer.measureBytes(map) / 1000;
		final long fastBytes = MemoryMeasurer.measureBytes(fastMap) / 1000;
		System.out.println("Default map = " + defaultBytes + " KB");
		System.out.println("Fast map = " + fastBytes + " KB (" + (100.0 * fastBytes / defaultBytes) + "%)");
	}

	public static void testObjectMap() {
		final int size = 1_000_000;
		System.out.println("\n- - - - - - - - ");
		System.out.println("Test memory java DEFAULT MAP and FAST MAP with OBJECT TYPE, test size = " + size);
		final Map<String, Integer> map = new HashMap<>(size);
		final Map<String, Integer> fastMap = new Object2IntOpenHashMap<>(size);
		for (int i=0; i<size; i++) {
			String key = randomString();
			int value = randomInt();
			map.put(key, value);
			fastMap.put(key, value);
		}
		final long defaultBytes = MemoryMeasurer.measureBytes(map) / 1000;
		final long fastBytes = MemoryMeasurer.measureBytes(fastMap) / 1000;
		System.out.println("Default map = " + defaultBytes + " KB");
		System.out.println("Fast map = " + fastBytes + " KB (" + (100.0 * fastBytes / defaultBytes) + "%)");
	}

	public static void main(String[] args) throws Throwable {
		testList();
		testNativeMap();
		testObjectMap();
	}
}
