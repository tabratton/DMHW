public class PercentEntry<T> implements Comparable<PercentEntry<T>> {
  public T word;
  public double totalProbability;

  public PercentEntry(T word, double regPerc, double spamPerc) {
    this.word = word;
    this.totalProbability = regPerc + spamPerc;
  }

  public int compareTo(PercentEntry<T> percentEntry) {
    // Unlike usual compareTo methods, this returns -1 if the object the
    // method is called on is greater than the specified one, and 1 if it
    // is less than.
    // This is done to give us descending order instead of ascending order
    // when we use Collections.sort.
    if (this.totalProbability == percentEntry.totalProbability) {
      return 0;
    } else if (this.totalProbability < percentEntry.totalProbability) {
      return -1;
    } else {
      return 1;
    }
  }
}