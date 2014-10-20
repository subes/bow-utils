package be.bagofwords.iterator;

import be.bagofwords.util.KeyValue;

import java.lang.reflect.Array;
import java.util.*;

public class IterableUtils {

    public static <T extends Object> DataIterable<T> createIterable(final DataIterable<? extends T>... iterables) {
        return createIterable(CombineMethod.SEQUENTIAL, iterables);
    }

    public static <T extends Object> DataIterable<T> createIterable(CombineMethod combineMethod, final DataIterable<? extends T>... iterables) {
        return createIterable(combineMethod, Arrays.asList(iterables));
    }

    public static <T extends Object> DataIterable<T> createIterable(final List<DataIterable<? extends T>> iterables) {
        return createIterable(CombineMethod.SEQUENTIAL, iterables);
    }

    public static <T> DataIterable<T> createIterable(final Collection<T> collection) {
        return new DataIterable<T>() {
            @Override
            public CloseableIterator<T> iterator() {
                return IterableUtils.iterator(collection.iterator());
            }

            @Override
            public long apprSize() {
                return collection.size();
            }
        };
    }

    public static <T extends Object> DataIterable<T> createIterable(final CombineMethod combineMethod, final List<DataIterable<? extends T>> iterables) {
        return new DataIterable<T>() {
            @Override
            public CloseableIterator<T> iterator() {
                List<CloseableIterator<? extends T>> iterators = new ArrayList<>();
                for (DataIterable<? extends T> iterable : iterables) {
                    iterators.add(iterable.iterator());
                }
                if (combineMethod == CombineMethod.SEQUENTIAL) {
                    return new SequentialIteratorOfIterators<>(iterators);
                } else {
                    return new InterleavedIteratorOfIterators<>(iterators);
                }
            }

            @Override
            public long apprSize() {
                long result = 0;
                for (DataIterable<? extends T> iterable : iterables) {
                    result += iterable.apprSize();
                }
                return result;
            }
        };
    }

    public static <T> CloseableIterator<T> iterator(final SimpleIterator<T> simpleIt) {
        return iterator(simpleIt, null);
    }

    public static <T> CloseableIterator<T> iterator(final SimpleIterator<T> simpleIt, final T lastValue) {
        return new CloseableIterator<T>() {

            private T nextValue;

            {
                findNext();
            }

            private void findNext() {
                try {
                    nextValue = simpleIt.next();
                    if (nextValue == null) {
                        if (lastValue == null) {
                            close();
                        }
                    } else {
                        if (lastValue != null && nextValue.equals(lastValue)) {
                            close();
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Could not read next value ", e);
                }
            }

            @Override
            public boolean hasNext() {
                if (lastValue == null) {
                    return nextValue != null;
                } else {
                    return !lastValue.equals(nextValue);
                }
            }

            @Override
            public synchronized T next() {
                T result = nextValue;
                findNext();
                return result;
            }

            @Override
            public void remove() {
                throw new RuntimeException("Not implemented");
            }

            @Override
            public void closeInt() {
                synchronized (this) {
                    try {
                        simpleIt.close();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to close iterator", e);
                    }
                }
            }
        };
    }

    public static <T extends Object> CloseableIterator<T> iterator(final Iterator<T> iterator) {
        return new CloseableIterator<T>() {
            @Override
            public void closeInt() {
                //do nothing
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                return iterator.next();
            }
        };
    }

    public static <T extends Object> CloseableIterator<T> maxSizeIterator(final long maxIterations, final CloseableIterator<T> iterator) {
        return new CloseableIterator<T>() {

            private long numDone = 0;

            @Override
            protected void closeInt() {
                iterator.close();
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext() && numDone < maxIterations;
            }

            @Override
            public T next() {
                numDone++;
                return iterator.next();
            }
        };
    }

    public static <T extends Object> DataIterable<T> maxSizeIterable(final long maxIterations, final DataIterable<T> iterable) {
        if (maxIterations <= 0) {
            throw new RuntimeException("Incorrect number of iterations " + maxIterations);
        }
        return new DataIterable<T>() {
            @Override
            public CloseableIterator<T> iterator() {
                return maxSizeIterator(maxIterations, iterable.iterator());
            }

            @Override
            public long apprSize() {
                return Math.min(maxIterations, iterable.apprSize());
            }
        };
    }

    public static enum CombineMethod {
        SEQUENTIAL, INTERLEAVED
    }

    public static <T> DataIterable<KeyValue<T[]>> createGroupingIterable(final Class<T> objectClass, final List<DataIterable<KeyValue<T>>> iterables) {
        final List<CloseableIterator<KeyValue<T>>> iterators = new ArrayList<>();
        for (DataIterable<KeyValue<T>> iterable : iterables) {
            iterators.add(iterable.iterator());
        }

        return new DataIterable<KeyValue<T[]>>() {
            @Override
            public CloseableIterator<KeyValue<T[]>> iterator() {
                return new CloseableIterator<KeyValue<T[]>>() {

                    private KeyValue<T>[] storedValues = new KeyValue[iterators.size()];

                    @Override
                    protected void closeInt() {
                        for (CloseableIterator<KeyValue<T>> iterator : iterators) {
                            iterator.close();
                        }
                    }

                    @Override
                    public boolean hasNext() {
                        for (int i = 0; i < iterators.size(); i++) {
                            if (iterators.get(i).hasNext() || storedValues[i] != null) {
                                return true;
                            }
                        }
                        return false;
                    }

                    @Override
                    public KeyValue<T[]> next() {
                        long smallestKey = Long.MAX_VALUE;
                        for (int i = 0; i < iterators.size(); i++) {
                            CloseableIterator<KeyValue<T>> currIt = iterators.get(i);
                            if (storedValues[i] == null && currIt.hasNext()) {
                                storedValues[i] = currIt.next();
                            }
                            if (storedValues[i] != null) {
                                smallestKey = Math.min(smallestKey, storedValues[i].getKey());
                            }
                        }
                        T[] result = (T[]) Array.newInstance(objectClass, storedValues.length);
                        for (int i = 0; i < iterators.size(); i++) {
                            if (storedValues[i] != null && storedValues[i].getKey() == smallestKey) {
                                result[i] = storedValues[i].getValue();
                                storedValues[i] = null;
                            }
                        }
                        return new KeyValue<>(smallestKey, result);
                    }
                };
            }

            @Override
            public long apprSize() {
                long maxSize = 0;
                for (DataIterable<KeyValue<T>> iterable : iterables) {
                    maxSize = Math.max(maxSize, iterable.apprSize());
                }
                return maxSize;
            }
        };
    }


}
