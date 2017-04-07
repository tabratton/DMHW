import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;


public class YouKnowJustARandomThingManGetOutOfMyFace {
  private static ArrayList<CountEntry<String>> regularCountEntryList = new ArrayList<>();
  private static ArrayList<CountEntry<String>> spamCountEntryList = new
      ArrayList<>();
  private static ArrayList<PercentEntry<String>> percentEntryList = new ArrayList<>();
  private static HashMap<String, Double> regularPercents = new HashMap<>();
  private static HashMap<String, Double> spamPercents = new HashMap<>();
  private static HashMap<String, Integer> regularCounts = new HashMap<>();
  private static HashMap<String, Integer> spamCounts = new HashMap<>();
  private static int spamTotalWords = 0;
  private static int regularTotalWords = 0;
  private static final int TOP_N = 10;

  public static void main(String[] args) throws FileNotFoundException {
    File trainingPath = new File(System.getProperty("user.dir")
        + "\\train");
    System.out.println(System.getProperty("user.dir"));
    File[] files = trainingPath.listFiles();

    for (File file : files) {
      parseFile(file);
    }

    addPercents();
    printResults(regularCounts);
    printResults(spamCounts);
    System.out.println("Regular emails word total: " + regularCounts.size());
    System.out.println("Spam emails word total: " + spamCounts.size());
    System.out.printf("Top %d words in regular emails: %n", TOP_N);
    count(regularCounts, regularCountEntryList);
    System.out.printf("Top %d words in spam emails: %n", TOP_N);
    count(spamCounts, spamCountEntryList);
    System.out.printf("Top %d words by spam percent: %n", TOP_N);
    spamPerc();
  }

  private static void parseFile(File file) throws FileNotFoundException {
    Scanner scanner = new Scanner(file);
    String line;
    while (scanner.hasNext()) {
      line = scanner.nextLine();
      StringTokenizer st = new StringTokenizer(line, " .\t\n\r\f");
      while (st.hasMoreTokens()) {
        String word = st.nextToken().toLowerCase();
        if (file.getName().contains("sp")) {
          addToMap(word, spamCounts);
          spamTotalWords++;
        } else {
          addToMap(word, regularCounts);
          regularTotalWords++;
        }
      }
    }
  }

  private static void addToMap(String word, HashMap<String, Integer> hashMap) {
    if (hashMap.containsKey(word)) {
      hashMap.put(word, hashMap.get(word) + 1);
    } else {
      hashMap.put(word, 1);
    }
  }

  private static void addPercents() {
    for (String word : regularCounts.keySet()) {
      regularPercents.put(word, (double) regularCounts.get(word) / regularTotalWords);
    }
    for (String word : spamCounts.keySet()) {
      spamPercents.put(word, (double) spamCounts.get(word) / spamTotalWords);
    }
  }

  private static void printResults(HashMap<String, Integer> map) {
    int distinctWordCount = 0;
    Set<String> keys = map.keySet();
    for (String key : keys) {
      System.out.printf("Word #%d: %s, appears %d times%n",
          ++distinctWordCount, key, map.get(key));
    }
  }

  private static void count(HashMap<String, Integer> source,
                            ArrayList<CountEntry<String>> dest) {
    for (String word : source.keySet()) {
      CountEntry countEntry = new CountEntry(word, source.get(word));
      dest.add(countEntry);
    }
    Collections.sort(dest);

    for (int i = dest.size() - 1; i > dest.size() - TOP_N - 1; i--) {
      System.out.println("\t" + dest.get(i).word + " appears "
          + dest.get(i).frequency + " time(s).");
    }
  }

  private static void spamPerc() {
    HashSet<PercentEntry<String>> test = new HashSet<>();
    HashSet<String> actualfuckingthing = new HashSet<>();
    actualfuckingthing.addAll(regularPercents.keySet());
    actualfuckingthing.addAll(spamPercents.keySet());

    for (String word : actualfuckingthing) {
      Double spamPercent = spamPercents.get(word);
      Double regularPercent = regularPercents.get(word);
      PercentEntry<String> percentEntry;
      if (regularPercent != null && spamPercent != null) {
        percentEntry = new PercentEntry<>(word, regularPercent, spamPercent);
        percentEntryList.add(percentEntry);
      } else if (regularPercent == null && spamPercent != null) {
        percentEntry = new PercentEntry<>(word, 0, spamPercent);
        percentEntryList.add(percentEntry);
      } else if (regularPercent != null && spamPercent == null) {
        percentEntry = new PercentEntry<>(word, regularPercent, 0);
        percentEntryList.add(percentEntry);
      } else {
        // Pretty sure this case is impossible, but whatever. A+ for
        // thoroughness.
        percentEntry = new PercentEntry<>(word, 0, 0);
        percentEntryList.add(percentEntry);
      }
    }
    Collections.sort(percentEntryList);

    int size = percentEntryList.size();
    for (int i = size - 1; i > size - TOP_N - 1; i--) {
      System.out.printf("\t%s: Conditional Reg Percent: %.2f, Conditional"
              + " Spam Percent: %.2f%n",
          percentEntryList.get(i).word,
          percentEntryList.get(i).regPerc * 100,
          percentEntryList.get(i).spamPerc * 100);
    }
  }
}
