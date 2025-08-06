package org.fxmisc.flowless;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.reactfx.collection.ListModification;
import org.reactfx.collection.LiveList;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class IndexedMappedListTest
{
    @Test
    public void testIndexedList() {
        ObservableList<String> strings = FXCollections.observableArrayList("1", "22", "333");
        // Live map receives index,item and returns %d-%d index item
        LiveList<String> lengths = new IndexedMappedList<>(strings, (index, item) -> String.format("%d-%d", index, item.length()));
        assertArrayEquals(new String[] {"0-1", "1-2", "2-3"}, lengths.stream().toArray());

        List<String> removed = new ArrayList<>();
        List<String> added = new ArrayList<>();
        lengths.observeChanges(ch -> {
            for(ListModification<? extends String> mod: ch.getModifications()) {
                removed.addAll(mod.getRemoved());
                added.addAll(mod.getAddedSubList());
            }
        });

        // Set a value in the list and check changes
        strings.set(1, "4444");
        assertArrayEquals(new String[] {"0-1", "1-4", "2-3"}, lengths.stream().toArray());
        assertEquals(Collections.singletonList("1-4"), added);
        assertEquals(Collections.singletonList("1-2"), removed);

        // Add an entry to the list and check changes
        strings.add("7777777");
        assertArrayEquals(new String[] {"0-1", "1-4", "2-3", "3-7"}, lengths.stream().toArray());
        assertEquals(Arrays.asList("1-4", "3-7"), added);
        assertEquals(Collections.singletonList("1-2"), removed);

        // Remove an entry to the list and check changes (note that 3-7 becomes 2-7)
        strings.remove(2);
        assertArrayEquals(new String[] {"0-1", "1-4", "2-7"}, lengths.stream().toArray());
        assertEquals(Arrays.asList("1-4", "3-7"), added);
        assertEquals(Arrays.asList("1-2", "2-3"), removed);
    }

    @Test
    public void testLazyIndexedList() {
        ObservableList<String> strings = FXCollections.observableArrayList("1", "22", "333");
        IntegerProperty evaluationsCounter = new SimpleIntegerProperty(0);
        LiveList<String> lengths = new IndexedMappedList<>(strings, (index, elem) -> {
            evaluationsCounter.set(evaluationsCounter.get() + 1);
            return String.format("%d-%d", index, elem.length());
        });

        lengths.observeChanges(ch -> {});
        strings.remove(1);

        assertEquals(0, evaluationsCounter.get());

        // Get the first element and the counter has increased
        assertEquals("0-1", lengths.get(0));
        assertEquals(1, evaluationsCounter.get());

        // Get the second element, it will evaluate one item
        assertEquals("1-3", lengths.get(1));
        assertEquals(2, evaluationsCounter.get());

        // Get again the first, it will reevaluate it
        assertEquals("0-1", lengths.get(0));
        assertEquals(3, evaluationsCounter.get());
    }
}
