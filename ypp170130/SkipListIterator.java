package ypp170130;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *     Team No: 39
 *     @author Pranita Hatte: prh170230
 *     @author Prit Thakkar: pvt170000
 *     @author Shivani Thakkar: sdt170030
 *     @author Yash Pradhan: ypp170130	
 *     Long Project 2: SkipList - Generalization of sorted linked lists for implementing Dictionary ADT (add, remove,
 *                                  contains, floor, ceiling) in O(log n) per operation "with high probability".
 *     SkipListIterator implements Iterator Interface: provides hasNext(), next() and remove()
 */

public class SkipListIterator<T extends Comparable<? super T>> implements Iterator {

    SkipList<T> skipList;
    SkipList.Entry<T> cursor, tail;
    boolean ready; // checks if item ready to be removed

    SkipListIterator(SkipList<T> skipList)
    {
        this.skipList = skipList;
        cursor = skipList.head;
        tail = skipList.tail;
        ready = false;
    }

    @Override
    // checks if there is element following cursor
    public boolean hasNext() {
        if(cursor.next[0] == tail) return false;
        return true;
    }

    @Override
    // returns next element
    public T next() {
        cursor = cursor.next[0];
        ready = true;
        return cursor.element;
    }

    @Override
    // removes element referenced by cursor
    public void remove() {
        if(!ready){
            throw new NoSuchElementException();
        }
        cursor = cursor.prev;
        skipList.remove((T) cursor.next[0].element);
        ready = false;
    }
}