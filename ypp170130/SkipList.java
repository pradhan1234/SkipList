package ypp170130;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 *     Team No: 39
 *     @author Pranita Hatte: prh170230
 *     @author Prit Thakkar: pvt170000
 *     @author Shivani Thakkar: sdt170030
 *     @author Yash Pradhan: ypp170130
 *     Long Project 2: SkipList - Generalization of sorted linked lists for implementing Dictionary ADT (add, remove,
 *                                  contains, floor, ceiling) in O(log n) per operation "with high probability".
 */

public class SkipList<T extends Comparable<? super T>> {

    static final int PossibleLevels = 33;
    Entry<T> head, tail;
    int size, maxLevel; // maxLevel: stores current MaxLevel
    Entry<T>[] last; // used by contains to keep track when it switched to lower level
    Random random;
    int[] pos; // used by contains to keep a track of elements spanned per level

    /**
     * Entry class: represents node of a SkipList
     * */
    static class Entry<E> {
        E element;
        Entry[] next;
        Entry prev;
        int[] span; // stores the number of elements that can be spanned by next[i]

        public Entry(E x, int lev) {
            element = x;
            next = new Entry[lev];
            span = new int[lev];
        }

        public E getElement() {
            return element;
        }
    }

    /**
     * Used by rebuild: To store the elements of skiplist while rebuilding the skiplist
     * */
    private class SkipListElement<E>{
        E element;
        private SkipListElement(E e){
            element = e;
        }
        private E getElement(){
            return element;
        }
    }

    /**
     * Constructor
     * */
    public SkipList() {
        head = new Entry<>(null, PossibleLevels);
        tail = new Entry<>(null, PossibleLevels);
        size = 0;
        maxLevel = 10;
        last = new Entry[PossibleLevels];
        pos = new int[PossibleLevels];
        random = new Random();

        //initialization
        for(int i = 0; i<maxLevel; i++){
            head.next[i] = tail;
            last[i] = head;
        }
        tail.prev = head;
    }

    /***
     * helpermethos: Used by add, to compute span
     * @return total elements spanned by contains method
     */
    public int getIndex(){
        int index = 0;
        for(int i = maxLevel-1; i >= 0; i--){
            index += pos[i];
        }
        return index;
    }

    /**
     * Add x to list
     * If x already exists, reject it.
     * @param x
     * @return true if new node is added to list
     */
    public boolean add(T x) {

        // skiplist cannot contain null
        if(x == null){
            throw new NullPointerException();
        }

        if(contains(x)) {
            return false;
        }
        add(x, chooseLevel());
        return true;
    }

    /**
     * helper method: performs actual addition of node in skiplist
     * computes span of new node and the ones that were affected by insertion
     * precondition: x is not in skiplist
     * @param x element being inserted
     * @param lev level of node
     */
    private void add(T x, int lev){

        Entry ent = new Entry(x, lev);

        // computes span
        if(size() == 0){
            for(int i = 0; i < maxLevel; i++){
                head.span[i] = 1;
            }
            for(int i = 0; i < lev; i++){
                ent.span[i] = 0;
            }
        }
        else {
            int index = getIndex();
            int prevIndex = -1;

            //LI: span[i] of new node and last[i] is computed
            for(int i = maxLevel-1; i>=0; i--){
                prevIndex += pos[i];
                if(i<lev){
                    ent.span[i] = last[i].span[i] + 1 - (index - prevIndex);
                    last[i].span[i] = index - prevIndex;
                }
                else{
                    last[i].span[i]++;
                }
            }
        }

        // rearrangement
        for(int i = 0; i<lev; i++){
            ent.next[i] = last[i].next[i];
            last[i].next[i] = ent;
        }

        ent.next[0].prev = ent;
        ent.prev = last[0];
        size += 1;
    }

    /**
     * ceiling(x)
     * @param x
     * @return smallest element that is greater or equal to x, null if no such element exists
     */
    public T ceiling(T x) {
        if(contains(x) || last[0].next[0] != tail)
            return (T)last[0].next[0].getElement();
        else{
            return null;
        }
    }

    /**
     * Does list contain x?
     * @param x
     * @return true if x in list, false otherwise
     */
    public boolean contains(T x) {
        find(x);
        return last[0].next[0] != tail && ((T)last[0].next[0].getElement()).compareTo(x) == 0;
    }

    /**
     * helper method: used by contains
     * populates array last and span
     * @param x
     */
    private void find(T x){
        Entry<T> p = head;
        for(int i = maxLevel - 1; i >= 0; i--){
            pos[i] = 0;
            while(p.next[i] != tail && ((T)p.next[i].getElement()).compareTo(x)<0){
                pos[i] += p.span[i];
                p = p.next[i];
            }
            last[i] = p;
        }
    }

    /**
     * @return first element of list, null if list is empty
     */
    public T first() {
        if(isEmpty())
            return null;
        return (T)head.next[0].getElement();
    }

    /**
     *
     * @return chooses number of levels randomly
     */
    private int chooseLevel(){
        int nLevel = 1 + Integer.numberOfTrailingZeros(random.nextInt());
        nLevel = Integer.min(nLevel, maxLevel + 1);

        // increases level gradually
        if(nLevel > maxLevel){
            maxLevel = nLevel;

            head.next[maxLevel-1] = tail;
            last[maxLevel-1] = head;

            head.span[maxLevel-1] = size();
        }
        return nLevel;
    }

    /**
     * floor(x)
     * @param x
     * @return largest element that is less than or equal to x, null if no such element exists
     */
    public T floor(T x) {
        if(contains(x))
            return (T)last[0].next[0].getElement();
        if(last[0]!=head)
            return last[0].getElement();
        return null;
    }

    /**
     *
     * @param n
     * @return element at index n of list, First element is at index 0.
     */
    public T get(int n) {
        return getLog(n);
    }

    /**
     * O(n) algorithm for get(n)
     * @param n
     * @return element at index n, null if no such element exists
     */
    public T getLinear(int n) {
        if(n < 0 || n > size - 1){
            return null;
        }
        Entry p = head;
        for(int i = 0; i <= n; i++){
            p = p.next[0];
        }
        return (T)p.getElement();
    }


    /**
     * O(log n) expected time for get(n).
     * Computes in O(log n) using span
     * @param n
     * @return element at index n, null if no such element exists
     */
    public T getLog(int n) {

        if(n < 0 || n > size - 1){
            return null;
        }

        int travelledSoFar = -1;
        Entry p = head;

        for(int i = maxLevel-1; i>=0; i--){
            while(travelledSoFar+p.span[i] <= n && p.next[i] != tail){
                travelledSoFar += p.span[i];
                p = p.next[i];
            }
        }
        return (T)p.getElement();
    }

    /**
     * Is the list empty?
     * @return true if list is empty, false otherwise
     */
    public boolean isEmpty() {
        if(size() == 0)
            return true;
        return false;
    }

    /**
     * Iterate through the elements of list in sorted order
     * @return iterator of skiplist
     */
    public Iterator<T> iterator() {
        SkipListIterator<T> skipListIterator = new SkipListIterator<>(this);
        return skipListIterator;
    }

    /**
     * @return last element of list, null if skiplist empty
     */
    public T last() {
        if(isEmpty())
            return null;
        return (T)tail.prev.getElement();
    }

    /**
     * Reorganizes the elements of the list into a perfect skip list
     */
    public void rebuild() {
        SkipListElement[] skipListElements = new SkipListElement[size()];
        int[] newLev = new int[size()];

        int index = 0;
        int nElements = size();
        int count = nElements;

        while(count > 0){
            T element = this.remove(getLog(0));
            skipListElements[index++] = new SkipListElement(element);
            count--;
        }

        maxLevel = (int)(Math.ceil(Math.log10(nElements)/Math.log10(2)));

        // initialize
        for(int i = 0; i< maxLevel; i++) {
            head.next[i] = tail;
            last[i] = head;
            head.span[i] = 0;
        }

        computeLevels(newLev);

        //building a perfect skiplist
        for(int i = 0; i < nElements; i++){
            add((T)skipListElements[i].getElement(), newLev[i]);
        }
    }

    /**
     * helper method used by rebuild to compute levels for perfect skiplist
     * @param levels
     */
    private void computeLevels(int[] levels){
        computeLevels(levels, 0, levels.length-1);
    }

    /**
     * recursively computes the level for each position for forming a perfect skiplist
     * Using Divide and Conquer
     * @param levels function will populate this
     * @param start
     * @param end
     */
    private void computeLevels(int[] levels, int start, int end){
        int n = end - start + 1;

        if(end<start){
            return;
        }
        if(start == end){
            levels[start] = 1;
            return;
        }

        int mid = n/2;
        int level = (int)(Math.ceil(Math.log10(n)/Math.log10(2)));

        levels[start+mid] = level;

        computeLevels(levels, start, start+mid-1);
        computeLevels(levels, start+mid+1, end);
    }

    /**
     * Remove x from list
     * @param x
     * @return removed element, null if x not in list
     */
    public T remove(T x) {
        if(!contains(x))
            return null;
        Entry ent = last[0].next[0];

        for(int i = 0; i < ent.next.length; i++){
            last[i].next[i] = ent.next[i];
            last[i].span[i] += ent.span[i] - 1;
        }

        for(int i = ent.next.length; i<maxLevel; i++){
            last[i].span[i]--;
        }

        ent.next[0].prev = ent.prev;
        size -= 1;
        return (T)ent.getElement();
    }

    /**
     * @return number of elements in the list
     */
    public int size() {
        return size;
    }

    /**
     * method created for printing the skiplist with span information
     */
    public void printSkipList(){
        Entry p;
        for(int i =0; i<maxLevel; i++){
            System.out.print("\nLevel "+i+": ");
            p = head;
            System.out.print("(H,"+p.span[i]+")");
            while(p.next[i].getElement() != null){
                p = p.next[i];
                System.out.print("("+p.getElement()+","+p.span[i]+") ");
            }
            System.out.print("(Tail)");
        }
        System.out.println();
    }
}