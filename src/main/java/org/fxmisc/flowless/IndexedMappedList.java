package org.fxmisc.flowless;

import java.util.List;
import java.util.function.BiFunction;

import org.reactfx.Subscription;
import org.reactfx.collection.LiveList;
import org.reactfx.collection.LiveListBase;
import org.reactfx.collection.QuasiListChange;
import org.reactfx.collection.QuasiListModification;
import org.reactfx.collection.UnmodifiableByDefaultLiveList;
import org.reactfx.util.Lists;

import javafx.collections.ObservableList;

public class IndexedMappedList<E,F> extends LiveListBase<F> implements UnmodifiableByDefaultLiveList<F>
{
    private final ObservableList<? extends E> source;
    private final BiFunction<Integer, ? super E, ? extends F> mapper;

    public IndexedMappedList(ObservableList<? extends E> source,
                             BiFunction<Integer, ? super E, ? extends F> mapper) {
        this.source = source;
        this.mapper = mapper;
    }

    @Override
    public F get(int index) {
        return mapper.apply(index, source.get(index));
    }

    @Override
    public int size() {
        return source.size();
    }

    @Override
    protected Subscription observeInputs() {
        return LiveList.<E>observeQuasiChanges(source, this::sourceChanged);
    }

    protected void sourceChanged(QuasiListChange<? extends E> change) {
        notifyObservers(mappedChangeView(change));
    }

    private QuasiListChange<F> mappedChangeView(QuasiListChange<? extends E> change) {
        return () -> {
            List<? extends QuasiListModification<? extends E>> mods = change.getModifications();
            return Lists.<QuasiListModification<? extends E>, QuasiListModification<F>>mappedView(mods, mod -> new QuasiListModification<>() {

                @Override
                public int getFrom() {
                    return mod.getFrom();
                }

                @Override
                public int getAddedSize() {
                    return mod.getAddedSize();
                }

                @Override
                public List<? extends F> getRemoved() {
                    return Lists.mappedView(mod.getRemoved(), elem -> mapper.apply(mod.getFrom(), elem));
                }
            });
        };
    }
}
