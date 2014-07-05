Flowless
========

Efficient VirtualFlow for JavaFX. VirtualFlow is a layout container that lays out _cells_ in a vertical or horizontal _flow_. The main feature of a _virtual_ flow is that only the currently visible cells are rendered in the scene. You may have a list of thousands of items, but only, say, 30 cells are rendered at any given time.

JavaFX has its own VirtualFlow, which is not part of the public API, but is used, for example, in the implementation of [ListView](http://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/ListView.html). It is, however, [not very efficient](https://javafx-jira.kenai.com/browse/RT-35395) when updating the viewport on items change or scroll. Here is a comparison of JavaFX's ListView vs. Flowless on a list of 80 items, 25 of which fit into the viewport.

|                                     | ListView (# of `updateItem` calls) | Flowless (# of cell creations) |
|-------------------------------------|:----------------------------------:|:------------------------------:|
| update an item in the viewport      | 25                                 | 1                              |
| update an item outside the viewport | 25                                 | 0                              |
| scroll 5 items down                 | 5                                  | 5                              |
| scroll 50 items down                | 75                                 | 25                             |

Here is the [source code](https://gist.github.com/TomasMikula/1dcee2cc4e5dab421913) of this mini-benchmark. The results were obtained with JDK 8u20-b21.

We see that whenever a list item is updated in the ListView, every cell in the viewport is updated. This is regardless whether the updated item is in the viewport or not. Flowless achieves the optimal number of cell updates, i.e. 1, resp. 0. When scrolling 5 items down, both implementations need to update/create 5 cells. When scrolling 50 items down, Flowless creates only the 25 cells that will end up in the viewport. For ListView, there are 75 cell updates. I'm not sure what exactly happens to get this number of updates.

Use case for Flowless
---------------------

You will benefit from Flowless (compared to ListView) the most if you have many item updates and an expensive [updateItem](http://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/Cell.html#updateItem-T-boolean-) method.

Note, however, that Flowless is a low-level layout component and does not provide higher-level features like selection model or inline editing. One can, of course, implement those on top of Flowless.

Conceptual differences from ListView
------------------------------------

**Dumb cells.** This is the most important difference. For flowless, cells are just [Node](http://docs.oracle.com/javase/8/javafx/api/javafx/scene/Node.html)s and don't encapsulate any logic regarding virtual flow. A cell does not even necessarily store the index of the item it is displaying.

**Cell reuse is opt-in,** not forced. Cells are not reused by default. A new cell is created for each item. This simplifies cell implementation and does not impair performance if reusing a cell is about as expensive as creating a new one (i.e. `updateItem` is expensive).

**No empty cells.** There are no _empty cells_ used to fill the viewport when there are too few items. A cell is either displaying an item, or is not displayed at all.

Assumptions about cells
-----------------------

As noted before, for the purposes of virtual flow in Flowless the cells are just `Node`s. You are not forced to inherit from `ListCell`. However, they are expected to behave according to the following rules:

* Cells of a vertical virtual flow should properly implement methods
  * `computeMinWidth(-1)`
  * `computePrefWidth(-1)`
  * `computePrefHeight(width)`
* Cells of a horizontal virtual flow should properly implement methods
  * `computeMinHeight(-1)`
  * `computePrefHeight(-1)`
  * `computePrefWidth(height)`
* Cells are not expected to change their size _spontaneously_, i.e. without changing the item they are holding. Changes to their min/pref/max size will be ignored.

Downloads
---------

Download the [0.1 binaries](https://github.com/TomasMikula/Flowless/releases/tag/v0.1). Maven artifacts will be available shortly.

Documentation
-------------

[Javadoc](http://www.fxmisc.org/flowless/javadoc/org/fxmisc/flowless/package-summary.html)
