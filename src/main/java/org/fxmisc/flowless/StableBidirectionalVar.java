package org.fxmisc.flowless;

import java.util.function.Consumer;

import org.reactfx.Subscription;
import org.reactfx.value.ProxyVal;
import org.reactfx.value.Val;
import org.reactfx.value.Var;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
/**
 *  This class overrides the <code>Var.bindBidirectional</code> method implementing a mechanism to prevent looping.
 *  <br>By default <code>bindBidirectional</code> delegates to <code>Bindings.bindBidirectional</code>
 *  which isn't always stable for the Val -> Var paradigm, sometimes producing a continuous feedback loop.<br>
 */
class StableBidirectionalVar<T> extends ProxyVal<T, T> implements Var<T>
{
    private final Consumer<T> setval;
    private Subscription binding = null;
    private ChangeListener<T> left, right;
    private T last = null;

    StableBidirectionalVar( Val<T> underlying, Consumer<T> setter )
    {
        super( underlying );
        setval = setter;
    }

    @Override
    public T getValue()
    {
        return getUnderlyingObservable().getValue();
    }

    @Override
    protected Consumer<? super T> adaptObserver( Consumer<? super T> observer )
    {
        return observer; // no adaptation needed
    }

    @Override
    public void bind( ObservableValue<? extends T> observable )
    {
        unbind();
        binding = Val.observeChanges( observable, (ob,ov,nv) -> setValue( nv ) );
        setValue( observable.getValue() );
    }

    @Override
    public boolean isBound()
    {
        return binding != null;
    }

    @Override
    public void unbind()
    {
        if( binding != null ) binding.unsubscribe();
        binding = null;
    }

    @Override
    public void setValue( T newVal )
    {
        setval.accept( newVal );
    }

    @Override
    public void unbindBidirectional( Property<T> prop )
    {
        if ( right != null ) prop.removeListener( right );
        if ( left != null ) removeListener( left );
        left = null;  right = null;
    }

    @Override
    public void bindBidirectional( Property<T> prop )
    {
        unbindBidirectional( prop );
        prop.addListener( right = (ob,ov,nv) -> adjustOther( this, nv ) );
        addListener( left = (ob,ov,nv) -> adjustOther( prop, nv ) );
    }

    private void adjustOther( Property<T> other, T nv )
    {
        if ( ! nv.equals( last ) )
        {
            Platform.runLater( () -> other.setValue( nv ) );
            last = nv;
        }
    }
}
