import java.util.*;

public class ConcurrentArrayList<T> implements List<T> {

    private int initialCapacity = 10;

    private T[] MainArray = (T[]) new Object[initialCapacity];

    public ConcurrentArrayList(int initialCapacity) {
        this.initialCapacity = initialCapacity;
    }

    public ConcurrentArrayList() {}

    private void expand(int additionalCapacity) {
        synchronized (this) {
            MainArray = Arrays.copyOf(MainArray, size() + additionalCapacity + 1);
        }
    }

    private void trim() {
        synchronized (this) {
            MainArray = Arrays.copyOf(MainArray, size() < initialCapacity ? initialCapacity : size() + 1);
        }
    }

    @Override
    public int size() {
        synchronized (this) {
            int size = 0;
            for (T aMainArray : MainArray) {
                if (aMainArray == null) break;
                else size++;
            }
            return size;
        }
    }

    @Override
    public boolean isEmpty() {
        synchronized (this) {
            return size() == 0;
        }
    }

    @Override
    public boolean contains(Object o) {
        synchronized (this) {
            return o != null && indexOf(o) >= 0;
        }
    }

    class MyIterator implements Iterator<T> {

        int cursor = -1;

        @Override
        public boolean hasNext() {
            synchronized (this) {
                return MainArray[cursor + 1] != null;
            }
        }

        @Override
        public T next() {
            synchronized (this) {
                if (hasNext()) return MainArray[cursor + 1];
                else return null;
            }
        }

        @Override
        public void remove() {
            synchronized (this) {
                System.arraycopy(MainArray,
                        cursor + 1,
                        MainArray,
                        cursor,
                        size() - cursor);
                trim();
            }
        }
    }

    @Override
    public MyIterator iterator() {
        synchronized (this) {
            return new MyIterator();
        }
    }

    @Override
    public Object[] toArray() {
        synchronized (this) {
            return Arrays.copyOf(MainArray, size());
        }
    }

    @Override
    public boolean add(T t) {
        synchronized (this) {
            expand(1);
            MainArray[size()] = t;
            return contains(t);
        }
    }

    @Override
    public boolean remove(Object o) {
        synchronized (this) {
            int index = 0;
            for (int i = 0; i < size(); i++) {
                if (Objects.equals(MainArray[i], o)) break;
                else index++;
            }
            trim();
            return Objects.equals(remove(index), o);
        }
    }

    @Override
    public boolean addAll(Collection c) {
        synchronized (this) {
            if (c == null || c.isEmpty()) return  false;
            int count = 0;
            expand(c.size());
            Object[] collectionToArray = c.toArray();
            for (Object aCollectionToArray : collectionToArray) {
                add((T) aCollectionToArray);
                count++;
            }
            return count == c.size();
        }
    }

    @Override
    public boolean addAll(int i, Collection c) {
        synchronized (this) {
            if (i < 0 || i > size() || c == null || c.isEmpty()) return false;
            int count = 0;
            expand(c.size());
            int lastIndex = size() - 1;
            Object[] collectionToArray = c.toArray();
            System.arraycopy(MainArray,
                    lastIndex + 1 - collectionToArray.length,
                    MainArray,
                    lastIndex + 1,
                    lastIndex + collectionToArray.length - lastIndex);
            for (int j = i; j < i + collectionToArray.length; j++) {
                MainArray[j] = (T) collectionToArray[j - i];
                count++;
            }
            return count == c.size();
        }
    }

    @Override
    public void clear() {
        synchronized (this) {
            MainArray = (T[]) new Object[initialCapacity];
        }
    }

    @Override
    public T get(int i) {
        synchronized (this) {
            if (i < 0 || i >= size()) return null;
            return MainArray[i];
        }
    }

    @Override
    public T set(int i, T t) {
        synchronized (this) {
            if (i < 0 || i >= size() || t == null) return null;
            MainArray[i] = t;
            return MainArray[i];
        }
    }

    @Override
    public void add(int i, T t) {
        synchronized (this) {
            if (i >= 0 && i < size() && t != null) {
                expand(1);
                System.arraycopy(MainArray,
                        i,
                        MainArray,
                        i + 1,
                        size() - i);
                set(i, t);
            }
        }
    }

    @Override
    public T remove(int i) {
        synchronized (this) {
            T t = MainArray[i];
            System.arraycopy(MainArray,
                    i + 1,
                    MainArray,
                    i,
                    size() - i);
            trim();
            return t;
        }
    }

    @Override
    public int indexOf(Object o) {
        synchronized (this) {
            if (o == null) return -1;
            int index = 0;
            for (int i = 0; i < size(); i++) {
                if (Objects.equals(MainArray[i], o)) break;
                else index++;
            }
            if (index == size()) return -1;
            else return index;
        }
    }

    @Override
    public int lastIndexOf(Object o) {
        synchronized (this) {
            if (o == null || !contains(o)) return -1;
            int index = size() - 1;
            for (int i = size() - 1; i > -1; i--) {
                if (Objects.equals(MainArray[i], o)) break;
                else index--;
            }
            return index;
        }
    }

    class MyListIterator extends MyIterator implements ListIterator<T> {

        int cursor = super.cursor;

        @Override
        public boolean hasPrevious() {
            synchronized (this){
                return cursor > 0;
            }
        }

        @Override
        public T previous() {
            synchronized (this) {
                if (hasPrevious()) return MainArray[cursor - 1];
                else return null;
            }
        }

        @Override
        public int nextIndex() {
            synchronized (this) {
                return cursor + 1;
            }
        }

        @Override
        public int previousIndex() {
            synchronized (this) {
                return cursor - 1;
            }
        }

        @Override
        public void set(T t) {
            synchronized (this) {
                MainArray[cursor] = t;
            }
        }

        @Override
        public void add(T t) {
            synchronized (this) {
                expand(1);
                System.arraycopy(MainArray,
                        cursor,
                        MainArray,
                        cursor + 1,
                        size() - cursor);
                MainArray[cursor] = t;
            }
        }
    }

    @Override
    public MyListIterator listIterator() {
        synchronized (this) {
            return new MyListIterator();
        }
    }

    @Override
    public MyListIterator listIterator(int i) {
        synchronized (this) {
            MyListIterator myListIterator = new MyListIterator();
            myListIterator.cursor = i;
            return myListIterator;
        }
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        synchronized (this) {
            if (fromIndex < 0 || toIndex < 0 ||
                    fromIndex > size() -1 || toIndex > size() - 1 ||
                    fromIndex > toIndex) return null;
            ConcurrentArrayList<T> subList = new ConcurrentArrayList<>();
            subList.addAll(Arrays.asList(MainArray).subList(fromIndex, toIndex));
            return subList;
        }
    }

    @Override
    public boolean retainAll(Collection c) {
        synchronized (this) {
            if (c == null || c.isEmpty()) return false;
            int count = 0;
            Object[] collectionToArray = c.toArray();
            T[] retained = (T[]) new Object[collectionToArray.length];
            for (Object aCollectionToArray : collectionToArray) {
                if (contains(aCollectionToArray)) {
                    retained[count] = (T) aCollectionToArray;
                    count++;
                }
            }
            MainArray = retained;
            trim();
            return count != 0;
        }
    }

    @Override
    public boolean removeAll(Collection collection) {
        synchronized (this) {
            if (collection == null || collection.isEmpty()) return false;
            int count = 0;
            Object[] collectionToArray = collection.toArray();
            for (Object aCollectionToArray : collectionToArray) {
                if (contains(aCollectionToArray)) {
                    remove(aCollectionToArray);
                    count++;
                }
            }
            trim();
            return count != 0;
        }
    }

    @Override
    public boolean containsAll(Collection c) {
        synchronized (this) {
            if (c == null || c.isEmpty()) return false;
            int count = 0;
            Object[] collectionToArray = c.toArray();
            for (Object aCollectionToArray : collectionToArray) {
                if (contains(aCollectionToArray)) count++;
            }
            return count == c.size();
        }
    }


    @Override
    public <T> T[] toArray(T[] t) {
        synchronized (this) {
            return Arrays.copyOf((T[]) MainArray, size());
        }
    }

    @Override
    public String toString() {
        synchronized (this) {
            return Arrays.toString(Arrays.copyOf(MainArray, size()));
        }
    }

    @Override
    public boolean equals(Object o) {
        synchronized (this) {
            if (this == o) return true;
            if (!(o instanceof ConcurrentArrayList)) return false;

            ConcurrentArrayList<?> that = (ConcurrentArrayList<?>) o;

            if (initialCapacity != that.initialCapacity) return false;
            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            return Arrays.equals(MainArray, that.MainArray);
        }
    }

    @Override
    public int hashCode() {
        synchronized (this) {
            int result = initialCapacity;
            result = 31 * result + Arrays.hashCode(MainArray);
            return result;
        }
    }
}
