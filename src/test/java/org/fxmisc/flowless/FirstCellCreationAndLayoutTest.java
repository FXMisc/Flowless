package org.fxmisc.flowless;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FirstCellCreationAndLayoutTest extends FlowlessTestBase
{
    private VirtualFlow<Label, ?> flow;

    @Override
    public void start(Stage stage)
    {
    	Label first = new Label( "First Item" );
    	Label second = new Label( "Second Item" );
        ObservableList<Label> items = FXCollections.observableArrayList( first, second );
        flow = VirtualFlow.createVertical( items, Cell::wrapNode );

        stage.setScene( new Scene( flow, 200, 100 ) );
        stage.show();
    }

    @Test
    public void does_the_first_cell_layout_correctly()
    {
        assertEquals( 0, flow.getFirstVisibleIndex() );
    }
}