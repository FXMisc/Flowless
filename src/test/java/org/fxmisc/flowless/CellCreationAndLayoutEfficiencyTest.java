package org.fxmisc.flowless;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CellCreationAndLayoutEfficiencyTest extends FlowlessTestBase {

    private ObservableList<String> items;
    private Counter cellCreations = new Counter();
    private Counter cellLayouts = new Counter();
    private VirtualFlow<String, ?> flow;

    @Override
    public void start(Stage stage) {
        // set up items
        items = FXCollections.observableArrayList();
        for(int i = 0; i < 20; ++i) {
            items.addAll("red", "green", "blue", "purple");
        }

        // set up virtual flow
        flow = VirtualFlow.createVertical(
                items,
                color -> {
                    cellCreations.inc();
                    Region reg = new Region() {
                        @Override
                        protected void layoutChildren() {
                            cellLayouts.inc();
                            super.layoutChildren();
                        }
                    };
                    reg.setPrefHeight(16.0);
                    reg.setStyle("-fx-background-color: " + color);
                    return Cell.wrapNode(reg);
                });

        StackPane stackPane = new StackPane();
        // 25 cells (each 16px high) fit into the viewport
        stackPane.getChildren().add(flow);
        stage.setScene(new Scene(stackPane, 200, 400));
        stage.show();
    }

    @Before
    public void setup() {
        cellCreations.reset();
        cellLayouts.reset();
    }

    @Test
    public void updating_an_item_in_viewport_only_creates_and_lays_out_cell_once() {
        // update an item in the viewport
        interact(() -> items.set(10, "yellow"));
        assertEquals(1, cellCreations.getAndReset());
        assertEquals(1, cellLayouts.getAndReset());
    }
    @Test
    public void updating_an_item_outside_viewport_does_not_create_or_lay_out_cell() {
        // update an item outside the viewport
        interact(() -> items.set(30, "yellow"));
        assertEquals(0, cellCreations.getAndReset());
        assertEquals(0, cellLayouts.getAndReset());
    }

    @Test
    public void refreshing_cells_in_viewport_creates_and_lays_them_out_once() {
        // refresh cells in the viewport
        interact(() -> flow.refreshCells(10,12));
        assertEquals(2, cellCreations.getAndReset());
        assertEquals(2, cellLayouts.getAndReset());
    }
    @Test
    public void refreshing_cells_outside_viewport_does_not_create_or_lay_them_out() {
        // refresh 10 cells, 5 in and 5 outside the viewport
        interact(() -> flow.refreshCells(20,30));
        assertEquals(5, cellCreations.getAndReset());
        assertEquals(5, cellLayouts.getAndReset());
    }

    @Test
    public void deleting_an_item_in_viewport_only_creates_and_lays_out_cell_once() {
        // delete an item in the middle of the viewport
        interact(() -> items.remove(12));
        assertEquals(1, cellCreations.getAndReset());
        assertEquals(1, cellLayouts.getAndReset());
    }

    @Test
    public void adding_an_item_in_viewport_only_creates_and_lays_out_cell_once() {
        // add an item in the middle of the viewport
        interact(() -> items.add(12, "yellow"));
        assertEquals(1, cellCreations.getAndReset());
        assertEquals(1, cellLayouts.getAndReset());
    }

    @Test
    public void scrolling_so_partial_viewport_update_creates_and_lays_out_equal_number_of_cells_scrolled() {
        // scroll 5 items down
        interact(() -> flow.showAsFirst(5));
        assertEquals(5, cellCreations.getAndReset());
        assertEquals(5, cellLayouts.getAndReset());
    }

    @Test
    public void scrolling_so_full_viewport_update_creates_and_lays_out_max_cells_renderable_in_viewport_bounds() {
        // scroll 50 items down (only 25 fit into the viewport)
        interact(() -> flow.showAsFirst(55));
        assertEquals(25, cellCreations.getAndReset());
        assertEquals(25, cellLayouts.getAndReset());
    }
}