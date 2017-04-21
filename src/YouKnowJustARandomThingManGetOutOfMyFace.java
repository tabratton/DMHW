import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class YouKnowJustARandomThingManGetOutOfMyFace {
  private static ArrayList<CountEntry<String>> regularCountEntryList = new
      ArrayList<>();
  private static ArrayList<CountEntry<String>> spamCountEntryList = new
      ArrayList<>();
  private static ArrayList<PercentEntry<String>> percentEntryList = new
      ArrayList<>();
  private static HashMap<String, Double> regularPercents = new HashMap<>();
  private static HashMap<String, Double> spamPercents = new HashMap<>();
  private static HashMap<String, Integer> regularCounts = new HashMap<>();
  private static HashMap<String, Integer> spamCounts = new HashMap<>();
  private static int spamTotalWords = 0;
  private static int regularTotalWords = 0;
  private static final int LEAST_COMMON = 50;
  private static ArrayList<String> filteredWords= new ArrayList<>();

  // Variables for Naive Bayes
  private static double trainingSpamFileCount = 0;
  private static double trainingRegFileCount = 0;
  private static double numTestSpam = 0.0;
  private static double numTestReg = 0.0;
  private static double numSpamCorrect = 0.0;
  private static double numRegularCorrect = 0.0;
  private static double numSpamClassified = 0.0;
  private static double numRegClassified = 0.0;
  private static double numSpamWrong = 0.0;
  private static double numRegWrong = 0.0;

  // Variables for KNN
  private static int trainEmailCount;
  private static int testEmailCount;

  private static HashSet<String> allWords = new HashSet<>();
  private static ArrayList<String> allWordList = new ArrayList<>();
  @SuppressWarnings("unchecked")
  private static HashMap<String, Integer>[] emailWordDict;
  private static HashMap<String, Integer>[] emailWordDictFiltered;
  private static int[][] knnDict;
  private static int emailCount = 0;

  private static int[] testKnnDict;
  private static boolean[] spam;
  private static HashMap<String, Integer> testWords = new HashMap();
  private static int[] neighbors = {1, 3, 5, 20};
  private static int[] knnCorrect = {0, 0, 0, 0};
  private static int[] knnFP = {0, 0, 0, 0};
  private static int[] knnFN = {0, 0, 0, 0};
  // end variables for KNN

  public static void main(String[] args) throws FileNotFoundException {
    startAnalysis();
    resetVariables();
    startFilteredAnalysis();
  }

  private static void startFilteredAnalysis() throws FileNotFoundException {
    File testPath = new File(System.getProperty("user.dir") + File.separator +
        "test");
    filterWords();
    emailWordDict = emailWordDictFiltered;
    // create the List of words for the vectors for KNN
    knnList();
    // bring in the real files to be tested

    // get the files from the test path
    File[] testFiles = testPath.listFiles();

    for (File file : testFiles) {
      // parse each file
      parseFile(file, false);
    }

    showBayes();
    showKnn();
  }

  private static void startAnalysis() throws FileNotFoundException {
    File trainingPath = new File(System.getProperty("user.dir") + File
        .separator + "train");
    File testingPath = new File(System.getProperty("user.dir") + File
        .separator + "test");
    // get the number of files in the folder for training data
    trainEmailCount = trainingPath.listFiles().length;
    // get the number of files in the folder for testing data
    testEmailCount = testingPath.listFiles().length;
    // create arrays based on the number of emails in the training data
    // this is a dictionary of words in each email
    emailWordDict = new HashMap[trainEmailCount];
    emailWordDictFiltered = new HashMap[trainEmailCount];
    // initialize a 2D array of ints for the vectors
    knnDict = new int[trainEmailCount][];
    // initialize the spam array
    spam = new boolean[trainEmailCount];

    // get a list of all files
    File[] files = trainingPath.listFiles();

    for (File file : files) {
      parseFile(file, true);
      // make the array of booleans true or false for each email depending on
      // whether its spam
      if (file.getName().contains("sp")) {
        spam[emailCount] = true;
      } else {
        spam[emailCount] = false;
      }
      emailCount++;
    }

    addPercents();

    // create the List of words for the vectors for KNN
    knnList();
    // bring in the real files to be tested

    File testPath = new File(System.getProperty("user.dir") + File.separator +
        "test");
    // get the files from the test path
    File[] testFiles = testPath.listFiles();

    for (File file : testFiles) {
      // parse each file
      parseFile(file, false);
    }

    showBayes();
    showKnn();
  }

  private static void resetVariables() {
    // Variables for Naive Bayes
    numTestSpam = 0.0;
    numTestReg = 0.0;
    numSpamCorrect = 0.0;
    numRegularCorrect = 0.0;
    numSpamClassified = 0.0;
    numRegClassified = 0.0;
    numSpamWrong = 0.0;
    numRegWrong = 0.0;

    // Variables for KNN
    allWordList.clear();
    // initialize the spam array
    testWords.clear();
  }

  private static void showKnn() {
    // for the 4 different values of neighbor print out the accuracy and
    // false positives and false negatives
    for (int x = 0; x < 4; x++) {
      System.out.println("Stats for KNN with " + neighbors[x] + " neighbors");
      System.out.println("knnAccuracy: " + (knnCorrect[x] / (double)
          testEmailCount) +
          " (" + knnCorrect[x] + "/" + testEmailCount + ")");
      System.out.println("knnFalsePositive: " + (knnFP[x] / (double)
          testEmailCount) +
          " (" + knnFP[x] + "/" + testEmailCount + ")");
      System.out.println("knnFalseNegative: " + (knnFN[x] / (double)
          testEmailCount) +
          " (" + knnFN[x] + "/" + testEmailCount + ")");
      System.out.println("-----------");
    }
    // print out the total number of words
    System.out.println("Number of unique words :" + allWords.size());
  }

  private static void showBayes() {
    // Print Naive Bayes results.
    System.out.println("Spam Classified: " + numSpamClassified);
    System.out.println("Spam Correct: " + numSpamCorrect);
    System.out.println("Spam Wrong: " + numSpamWrong);
    System.out.println("Reg Classified: " + numRegClassified);
    System.out.println("Reg Correct: " + numRegularCorrect);
    System.out.println("Reg Wrong: " + numRegWrong);
    System.out.printf("Naive Bayes Accuracy: %.2f%n", ((numSpamCorrect
        + numRegularCorrect) / (numTestReg + numTestSpam)) * 100);
    System.out.println();
  }

  private static char naiveBayes(String[] words) {
    double probabilities = 0.0;

    for (String word : words) {
      Integer spamCount = spamCounts.get(word);
      Integer regCount = regularCounts.get(word);
      double spamFreq = spamCount != null ? spamCount : 0.5;
      double regFreq = regCount != null ? regCount : 0.5;
      probabilities += Math.log(spamFreq / regFreq);
    }

    double probSpam = trainingSpamFileCount / trainEmailCount;
    double probHam = trainingRegFileCount / trainEmailCount;
    probabilities += Math.log(probSpam / probHam);
    return probabilities > 0 ? 's' : 'r';
  }

  private static void parseFile(File file, boolean trainingData) throws
      FileNotFoundException {
    Scanner scanner = new Scanner(file);
    StringBuilder sb = new StringBuilder(1000);
    while (scanner.hasNext()) {
      sb.append(scanner.nextLine());
    }
    if (trainingData) {
      if (file.getName().contains("sp")) {
        trainingSpamFileCount++;
      } else {
        trainingRegFileCount++;
      }
      // creates a map of all the words in both spam and non spam emails
      emailWordDict[emailCount] = new HashMap<>();
      emailWordDictFiltered[emailCount] = new HashMap<>();
      String[] tokens = sb.toString()
          .split("(?:(?:\\s+|(?:(?<=\\w)\\p{P}" + "(?=\\s))|((?<=\\s)\\p{P}" +
              "(?=\\w)))+)");

      for (String token : tokens) {
        if (file.getName().contains("sp")) {
          // add word to spam map and increase total number of spam
          addToMap(token, spamCounts);
          spamTotalWords++;
        } else {
          // add word to non spam map and increase number of non spam
          addToMap(token, regularCounts);
          regularTotalWords++;
        }
        // add word to map of this specific email
        addToMap(token, emailWordDict[emailCount]);
        if (!filteredWords.contains(token)) {
          addToMap(token, emailWordDictFiltered[emailCount]);
        }
        // add word to all words map
        allWords.add(token);
      }

    } else {
      String[] tokens = sb.toString()
          .split("(?:(?:\\s+|(?:(?<=\\w)\\p{P}" + "(?=\\s))|((?<=\\s)\\p{P}" +
              "(?=\\w)))+)");
      for (String token : tokens) {
        addToMap(token, testWords);
      }

      char bayesResult = naiveBayes(tokens);
      if (file.getName().contains("sp") && (bayesResult == 's')) {
        numTestSpam++;
        numSpamClassified++;
        numSpamCorrect++;
      } else if (file.getName().contains("sp") && (bayesResult == 'r')) {
        numTestSpam++;
        numRegWrong++;
        numRegClassified++;
      } else if (!file.getName().contains("sp") && (bayesResult == 's')) {
        numTestReg++;
        numSpamWrong++;
        numSpamClassified++;
      } else if (!file.getName().contains("sp") && (bayesResult == 'r')) {
        numTestReg++;
        numRegClassified++;
        numRegularCorrect++;
      }

      int k = 0;
      int length = allWordList.size();
      // create an array for the words in the current test email - will be
      // the vector
      testKnnDict = new int[length];
      for (String word : allWordList) {

        if (testWords.get(word) != null) {
          // assign the number of times the word appears
          testKnnDict[k] = testWords.get(word);
        } else {
          // otherwise insert a zero
          testKnnDict[k] = 0;
        }
        k++;
      }
      boolean spamEmail;
      // for each of the 4 values of neighbors (1,3,5,20)
      for (int x = 0; x < neighbors.length; x++) {
        // test knn to see whether it is a spam or not
        spamEmail = knnTest(testKnnDict, knnDict, neighbors[x]);
        // if it is a true positive or a true negative, increment the number
        // of correct
        if ((file.getName().contains("sp") && spamEmail) || (!file.getName()
            .contains("sp") && !spamEmail)) {
          knnCorrect[x]++;
          // otherwise increment false negative
        } else if (file.getName().contains("sp") && !spamEmail) {
          knnFN[x]++;
          // or false positive
        } else {
          knnFP[x]++;
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
      regularPercents.put(word, (double) regularCounts.get(word) /
          regularTotalWords);
    }
    for (String word : spamCounts.keySet()) {
      spamPercents.put(word, (double) spamCounts.get(word) / spamTotalWords);
    }
  }

  private static void filterWords() {
    HashSet<String> set = new HashSet<>();
    set.addAll(regularPercents.keySet());
    set.addAll(spamPercents.keySet());

    for (String word : set) {
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

    for (int i = 0; i < LEAST_COMMON; i++) {
      String word = percentEntryList.get(i).word;
      filteredWords.add(word);
      if (regularCounts.containsKey(word)) {
        regularPercents.remove(word);
        regularCounts.remove(word);
        regularTotalWords--;
      }
      if (spamCounts.containsKey(word)) {
        spamPercents.remove(word);
        spamCounts.remove(word);
        spamTotalWords--;
      }
      allWords.remove(word);
    }

    String[] symbols = { "`", "~", "!", "@", "#", "$", "%", "^", "&", "*", "" +
        "(", ")", "-", "_", "=", "+", "[", "]", "{", "}", "|", "\\", ";",
        ":", "'", "\"", "<", ">", ",", ".", "/", "?" };

    for (String symbol : symbols) {
      filteredWords.add(symbol);
      if (allWords.contains(symbol)) {
        allWords.remove(symbol);
      }
      if (regularCounts.containsKey(symbol)) {
        regularCounts.remove(symbol);
        regularPercents.remove(symbol);
        regularTotalWords--;
      }
      if (spamCounts.containsKey(symbol)) {
        spamCounts.remove(symbol);
        spamPercents.remove(symbol);
        spamTotalWords--;
      }
    }

  }

  private static void knnList() {
    // for each word in the allwords map
    for (String word : allWords) {
      // add to a list for sorting
      allWordList.add(word);
    }

    // sort the list alphabetically
    Collections.sort(allWordList);
    int length = allWordList.size();
    // for each of the training set emails
    for (int i = 0; i < trainEmailCount; i++) {
      // make an array of ints for each word - will be the vector of that email
      knnDict[i] = new int[length];
      int k = 0;
      // for each word
      for (String word : allWordList) {
        // if there is a value
        if (emailWordDict[i].get(word) != null) {
          // put the value in that location
          knnDict[i][k] = emailWordDict[i].get(word);
        } else {
          // otherwise put zero
          knnDict[i][k] = 0;
        }
        // System.out.println("knn["+ i+ "]["+k+ "]"+knnDict[i][k]);
        k++;
      }
    }
  }

  public static double cosSim(int[] emailTested, int[] emailTrained) {
    // initialize totals
    double relation = 0;
    int multTotal = 0;
    int testedTotal = 0;
    int trainedTotal = 0;
    // for each of the values in the vector
    for (int i = 0; i < emailTested.length; i++) {
      // multiply it by the other vector at the same location and add it to
      // the running total
      multTotal += emailTested[i] * emailTrained[i];
      // multiply tested email vector by itself and add it to the running total
      testedTotal += emailTested[i] * emailTested[i];
      // multiply training email vector by itself and add it to the running
      // total
      trainedTotal += emailTrained[i] * emailTrained[i];
    }
    // calculate the cosine similarity
    relation = multTotal / (Math.sqrt(testedTotal) * Math.sqrt(trainedTotal));

    // return the similarity
    return relation;
  }

  public static boolean knnTest(int[] testEmail, int[][] trainingEmails, int
      k) {

    int len = trainingEmails.length;
    // create an array of doubles for the cosSimilarity
    double[] similarity = new double[trainEmailCount];
    // create an arraylist for sortability
    ArrayList<Double> similarityList = new ArrayList<>();
    for (int i = 0; i < len; i++) {
      // put the numbers in the array and the list
      similarity[i] = cosSim(testEmail, trainingEmails[i]);
      similarityList.add(similarity[i]);
    }
    // sort the list in reverse order to get the highest first
    Collections.sort(similarityList, Collections.reverseOrder());
    int voteSpam = 0;
    int voteNon = 0;
    // for K neighbors
    for (int i = 0; i < k; i++) {
      // go through each email
      for (int j = 0; j < similarity.length; j++) {
        // find the one that matches the highest value
        if (similarityList.get(i) == similarity[j]) {
          // if it is spam, increment a vote for spam on this test email
          if (spam[j] == true) {
            voteSpam++;
            // if it isnt spam, increment a vote for non spam
          } else {
            voteNon++;
          }
        }

      }
    }
    // if there are more votes spam than non -> return true
    if (voteSpam > voteNon) {
      return true;
      // otherwise return false
    } else {
      return false;
    }
  }

}
