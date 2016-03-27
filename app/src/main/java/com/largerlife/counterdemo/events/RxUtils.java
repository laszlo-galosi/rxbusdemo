package com.largerlife.counterdemo.events;

/**
 * Created by László Gálosi on 27/03/16
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import rx.Observable;
import rx.Subscription;
import rx.observables.BlockingObservable;
import rx.subscriptions.CompositeSubscription;

public class RxUtils {

    public static void unsubscribeIfNotNull(Subscription subscription) {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    public static CompositeSubscription getNewCompositeSubIfUnsubscribed(
          CompositeSubscription subscription) {
        if (subscription == null || subscription.isUnsubscribed()) {
            return new CompositeSubscription();
        }

        return subscription;
    }

    /**
     * Returns an unmodifiable list from the source {@link Observable <T>}
     *
     * @param source the source Observable which emits the elements to be contained in the list
     * @param capacity the capacity of the returning List
     * @param comparators comparators which sorts the list. All {@link Collections#sort(List)} will
     * be
     * @param <T> the type of the returning elements
     * @return an unmodifiable list from the source Observable
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> toUnmutableList(Observable<T> source, int capacity,
          Comparator<T>... comparators) {
        Iterator<T> iterator = BlockingObservable.from(source).getIterator();
        List<T> list = new ArrayList<>(capacity);
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        for (int i = 0, len = comparators.length; i < len; i++) {
            Collections.sort(list, comparators[i]);
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * Returns a modifiable list from the source {@link Observable<T>}
     *
     * @param source the source Observable which emits the elements to be contained in the list
     * @param capacity the capacity of the returning List
     * @param comparators comparators which sorts the list. All {@link Collections#sort(List)} will
     * be
     * @param <T> the type of the returning elements
     * @return an modifiable list from the source Observable
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> toMutableList(Observable<T> source, int capacity,
          Comparator<T>... comparators) {
        List<T> list = new ArrayList<>(capacity);
        Iterator<T> iterator = BlockingObservable.from(source).getIterator();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        for (int i = 0, len = comparators.length; i < len; i++) {
            Collections.sort(list, comparators[i]);
        }
        return list;
    }
}
