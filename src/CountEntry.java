public class CountEntry<T> implements Comparable<CountEntry<T>> {
  public T word;
  public int frequency;

  public CountEntry(T word, int frequency) {
    this.word = word;
    this.frequency = frequency;
  }

  public int compareTo(CountEntry<T> countEntry) {
    // Unlike usual compareTo methods, this returns -1 if the object the
    // method is called on is greater than the specified one, and 1 if it
    // is less than.
    // This is done to give us descending order instead of ascending order
    // when we use Collections.sort.
    if (this.frequency == countEntry.frequency) {
      return 0;
    } else if (this.frequency < countEntry.frequency) {
      return -1;
    } else {
      return 1;
    }
  }
}
