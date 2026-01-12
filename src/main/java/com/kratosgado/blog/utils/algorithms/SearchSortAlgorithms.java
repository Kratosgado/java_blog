package com.kratosgado.blog.utils.algorithms;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class providing sorting and searching algorithms for cached data.
 * Implements QuickSort and Binary Search with performance tracking.
 */
public class SearchSortAlgorithms {
  private static final Logger logger = LoggerFactory.getLogger(SearchSortAlgorithms.class);
  
  /**
   * QuickSort implementation for sorting lists.
   * Time Complexity: O(n log n) average, O(n^2) worst case
   * Space Complexity: O(log n) for recursion stack
   */
  public static <T> void quickSort(List<T> list, Comparator<T> comparator) {
    if (list == null || list.size() <= 1) {
      return;
    }
    
    long startTime = System.nanoTime();
    quickSortHelper(list, 0, list.size() - 1, comparator);
    long endTime = System.nanoTime();
    
    logger.debug("QuickSort completed {} items in {}ms", 
      list.size(), (endTime - startTime) / 1_000_000);
  }
  
  private static <T> void quickSortHelper(List<T> list, int low, int high, Comparator<T> comparator) {
    if (low < high) {
      int pivotIndex = partition(list, low, high, comparator);
      quickSortHelper(list, low, pivotIndex - 1, comparator);
      quickSortHelper(list, pivotIndex + 1, high, comparator);
    }
  }
  
  private static <T> int partition(List<T> list, int low, int high, Comparator<T> comparator) {
    T pivot = list.get(high);
    int i = low - 1;
    
    for (int j = low; j < high; j++) {
      if (comparator.compare(list.get(j), pivot) <= 0) {
        i++;
        swap(list, i, j);
      }
    }
    
    swap(list, i + 1, high);
    return i + 1;
  }
  
  private static <T> void swap(List<T> list, int i, int j) {
    T temp = list.get(i);
    list.set(i, list.get(j));
    list.set(j, temp);
  }
  
  /**
   * Binary Search for sorted lists.
   * Time Complexity: O(log n)
   * Space Complexity: O(1)
   * 
   * @return index of element if found, -1 otherwise
   */
  public static <T, K extends Comparable<K>> int binarySearch(
    List<T> sortedList, 
    K searchKey, 
    Function<T, K> keyExtractor
  ) {
    if (sortedList == null || sortedList.isEmpty()) {
      return -1;
    }
    
    long startTime = System.nanoTime();
    int result = binarySearchHelper(sortedList, searchKey, keyExtractor, 0, sortedList.size() - 1);
    long endTime = System.nanoTime();
    
    logger.debug("Binary search in {} items took {}µs", 
      sortedList.size(), (endTime - startTime) / 1_000);
    
    return result;
  }
  
  private static <T, K extends Comparable<K>> int binarySearchHelper(
    List<T> list,
    K searchKey,
    Function<T, K> keyExtractor,
    int low,
    int high
  ) {
    if (low > high) {
      return -1;
    }
    
    int mid = low + (high - low) / 2;
    K midKey = keyExtractor.apply(list.get(mid));
    int comparison = searchKey.compareTo(midKey);
    
    if (comparison == 0) {
      return mid;
    } else if (comparison < 0) {
      return binarySearchHelper(list, searchKey, keyExtractor, low, mid - 1);
    } else {
      return binarySearchHelper(list, searchKey, keyExtractor, mid + 1, high);
    }
  }
  
  /**
   * Linear search with predicate filter.
   * Time Complexity: O(n)
   * Use for unsorted data or complex search criteria.
   */
  public static <T> List<T> linearSearch(List<T> list, Predicate<T> predicate) {
    if (list == null) {
      return new ArrayList<>();
    }
    
    long startTime = System.nanoTime();
    List<T> results = new ArrayList<>();
    
    for (T item : list) {
      if (predicate.test(item)) {
        results.add(item);
      }
    }
    
    long endTime = System.nanoTime();
    logger.debug("Linear search found {} matches in {} items, took {}µs", 
      results.size(), list.size(), (endTime - startTime) / 1_000);
    
    return results;
  }
  
  /**
   * Find top N elements by comparator.
   * Time Complexity: O(n log n)
   */
  public static <T> List<T> topN(List<T> list, int n, Comparator<T> comparator) {
    if (list == null || list.isEmpty() || n <= 0) {
      return new ArrayList<>();
    }
    
    long startTime = System.nanoTime();
    List<T> copy = new ArrayList<>(list);
    quickSort(copy, comparator);
    
    int resultSize = Math.min(n, copy.size());
    List<T> topN = copy.subList(0, resultSize);
    
    long endTime = System.nanoTime();
    logger.debug("TopN({}) from {} items took {}ms", 
      n, list.size(), (endTime - startTime) / 1_000_000);
    
    return new ArrayList<>(topN);
  }
  
  /**
   * Merge two sorted lists.
   * Time Complexity: O(n + m)
   */
  public static <T> List<T> mergeSorted(
    List<T> list1, 
    List<T> list2, 
    Comparator<T> comparator
  ) {
    List<T> result = new ArrayList<>(list1.size() + list2.size());
    int i = 0, j = 0;
    
    while (i < list1.size() && j < list2.size()) {
      if (comparator.compare(list1.get(i), list2.get(j)) <= 0) {
        result.add(list1.get(i++));
      } else {
        result.add(list2.get(j++));
      }
    }
    
    while (i < list1.size()) {
      result.add(list1.get(i++));
    }
    
    while (j < list2.size()) {
      result.add(list2.get(j++));
    }
    
    return result;
  }
  
  /**
   * Check if a list is sorted according to comparator.
   */
  public static <T> boolean isSorted(List<T> list, Comparator<T> comparator) {
    if (list == null || list.size() <= 1) {
      return true;
    }
    
    for (int i = 0; i < list.size() - 1; i++) {
      if (comparator.compare(list.get(i), list.get(i + 1)) > 0) {
        return false;
      }
    }
    
    return true;
  }
}
