package library.util;

import java.util.*;
import java.util.function.Function;

/**
 * Utility helpers for safe collection operations, grouping, and pagination.
 */
public final class CollectionUtil {

    private CollectionUtil() {
    }

    public static <T> List<T> nullToEmpty(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    public static <T> List<T> safeSubList(List<T> list, int fromIndex, int toIndex) {
        if (list == null || list.isEmpty() || fromIndex >= list.size()) {
            return Collections.emptyList();
        }
        int start = Math.max(0, fromIndex);
        int end = Math.min(list.size(), toIndex);
        if (start >= end) {
            return Collections.emptyList();
        }
        return list.subList(start, end);
    }

    public static <T, K> Map<K, List<T>> groupBy(Collection<T> items, Function<T, K> keyExtractor) {
        Map<K, List<T>> map = new LinkedHashMap<>();
        if (items == null) return map;
        for (T item : items) {
            K key = keyExtractor.apply(item);
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
        }
        return map;
    }

    public static <T, K> Map<K, Integer> countBy(Collection<T> items, Function<T, K> keyExtractor) {
        Map<K, Integer> map = new LinkedHashMap<>();
        if (items == null) return map;
        for (T item : items) {
            K key = keyExtractor.apply(item);
            map.merge(key, 1, Integer::sum);
        }
        return map;
    }
}
