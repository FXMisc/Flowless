package org.fxmisc.flowless;

import java.time.Duration;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import org.reactfx.AwaitingEventStream;
import org.reactfx.EventStream;
import org.reactfx.EventStreamBase;
import org.reactfx.Subscription;
import org.reactfx.util.FxTimer;
import org.reactfx.util.Timer;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;

class PausableSuccessionStream<O> extends EventStreamBase<O> implements AwaitingEventStream<O> {
    private final EventStream<O> input;
    private final Function<? super O, ? extends O> initial;
    private final BiFunction<? super O, ? super O, ? extends O> reduction;
    private final Timer timer;

    private boolean hasEvent = false;
    private BooleanBinding pending = null;
    private BooleanProperty successionOff;
    private Predicate<O> successionOffCond;
    private O event = null;

    /**
     * Returns an event stream that, when events are emitted from this stream
     * in close temporal succession, emits only the last event of the
     * succession. What is considered a <i>close temporal succession</i> is
     * defined by {@code timeout}: time gap between two successive events must
     * be at most {@code timeout}.
     *
     * <p><b>Note:</b> This function can be used only when this stream and
     * the returned stream are used from the JavaFX application thread.</p>
     *
     * @param timeout the maximum time difference between two subsequent events
     * in <em>close</em> succession.
     * @param realTime when true immediately emits the next event and sets
     * realTime back to <em>false</em>.
     */
    public PausableSuccessionStream( EventStream<O> input, Duration timeout, BooleanProperty realTime )
    {
        this( input, timeout, realTime, a -> realTime.get() );
    }

    /**
    * @param timeout the maximum time difference between two subsequent events
    * in <em>close</em> succession.
    * @param condition when true immediately emits the event, otherwise
    * waits for <em>timeout</em> before emitting the last received event.
    */
    public PausableSuccessionStream( EventStream<O> input, Duration timeout, Predicate<O> condition )
    {
        this( input, timeout, new SimpleBooleanProperty(), condition );
    }

    private PausableSuccessionStream(
            EventStream<O> input,
            java.time.Duration timeout,
            BooleanProperty realTime,
            Predicate<O> condition) {

        this.input = input;
        this.initial = Function.identity();
        this.reduction = (a,b) -> b;
        this.successionOff = realTime;
        this.successionOffCond = condition;

        this.timer = FxTimer.create(timeout, this::handleTimeout);
    }

    @Override
    public ObservableBooleanValue pendingProperty() {
        if(pending == null) {
            pending = new BooleanBinding() {
                @Override
                protected boolean computeValue() {
                    return hasEvent;
                }
            };
        }
        return pending;
    }

    @Override
    public boolean isPending() {
        return pending != null ? pending.get() : hasEvent;
    }

    @Override
    protected final Subscription observeInputs() {
        return input.subscribe(this::handleEvent);
    }

    private void handleEvent(O i) {
    	timer.stop();
        if(successionOffCond.test(i))
        {
            hasEvent = false;
            event = null;
            emit(i);
            successionOff.setValue(false);
        }
        else
        {
            if(hasEvent) {
                event = reduction.apply(event, i);
            } else {
                event = initial.apply(i);
                hasEvent = true;
                invalidatePending();
            }
            timer.restart();
        }
    }

    private void handleTimeout() {
        hasEvent = false;
        O toEmit = event;
        event = null;
        emit(toEmit);
        invalidatePending();
    }

    private void invalidatePending() {
        if(pending != null) {
            pending.invalidate();
        }
    }
}