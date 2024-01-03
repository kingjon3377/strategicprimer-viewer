package drivers;

import java.nio.file.Path;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;

import java.util.stream.StreamSupport;
import java.util.stream.Collectors;

/**
 * An object to help us present files with only as much of their paths as
 * necessary to uniquely identify them, without their shared prefix.
 */
/* package */ final class SuffixHelper {
    private SuffixHelper() {
    }

    /**
     * Get the last "count" path elements in the given path.
     */
    private static String suffix(final Path file, final int count) {
        final Deque<Path> list = StreamSupport.stream(file.spliterator(), false)
                .collect(Collectors.toCollection(LinkedList::new));
        while (list.size() > count) {
            list.removeFirst();
        }
        return list.stream().map(Path::toString).collect(Collectors.joining("/"));
    }

    /**
     * Divide the given filename into prefix and suffix,
     * returning the suffix, such that the prefix is shared with all files
     * in "all", but is otherwise as long as possible.
     */
    public static String shortestSuffix(final Collection<Path> all, final Path file) {
        final int longestPath = all.stream().mapToInt(Path::getNameCount).max().orElse(1);
        final Set<String> localCache = new HashSet<>();
        for (int num = 1; num <= longestPath; num++) {
            boolean found = false;
            for (final Path key : all) {
                final String item = suffix(key, num);
                if (localCache.contains(item)) {
                    found = true;
                    break;
                } else {
                    localCache.add(item);
                }
            }
            if (!found) {
                return suffix(file, num);
            }
        }
        return file.toString();
    }
}

