# Change Log

## [v0.6.4](https://github.com/FXMisc/Flowless/tree/v0.6.4) (2021-07-08)
[Full Changelog](https://github.com/FXMisc/Flowless/compare/v0.6.3...v0.6.4)

**Fixed issues:**

- Improved getCellIfPresent parameter check [\#90](https://github.com/FXMisc/Flowless/issues/90)

**Merged pull requests:**

- Fix for negative toItem variable [\#92](https://github.com/FXMisc/Flowless/pull/92) ([TomaszChudyk](https://github.com/tchudyk))

## [v0.6.3](https://github.com/FXMisc/Flowless/tree/v0.6.3) (2021-02-23)
[Full Changelog](https://github.com/FXMisc/Flowless/compare/v0.6.2...v0.6.3)

**Fixed issues:**

- VirtualizedScrollPane causes high CPU and GPU on idle with 125% screen scaling [\#81](https://github.com/FXMisc/Flowless/issues/81)

**Merged pull requests:**

- Scrollbars height/width ceiled to nearest pixel if this region's snapToPixel property is true [\#82](https://github.com/FXMisc/Flowless/pull/82) ([FredericThevenet](https://github.com/fthevenet))

## [v0.6.2](https://github.com/FXMisc/Flowless/tree/v0.6.2) (2020-10-02)
[Full Changelog](https://github.com/FXMisc/Flowless/compare/v0.6.1...v0.6.2)

**Fixed issues:**

- Bidirectional binding of length properties does not update both values correctly [\#43](https://github.com/FXMisc/Flowless/issues/43)
- Suspend VirtualFlow's scroll-related values until fully rendered [\#60](https://github.com/FXMisc/Flowless/issues/60)
- Fix cells larger than viewport layout efficiency [\#70](https://github.com/FXMisc/Flowless/issues/70)
- Fix SizeTracker horizontal resize [\#72](https://github.com/FXMisc/Flowless/issues/72)
- Keyboard paging computation error [\#73](https://github.com/FXMisc/Flowless/issues/73)

## [v0.6.1](https://github.com/FXMisc/Flowless/tree/v0.6.1) (2018-04-18)
[Full Changelog](https://github.com/FXMisc/Flowless/compare/v0.6...v0.6.1)

**Merged pull requests:**

- Add accessors for the index of the first and last visible cell [\#59](https://github.com/FXMisc/Flowless/pull/59) ([DavinMcCall](https://github.com/davmac314))
- Set 'content' as default VirtualizedScrollPane FXML property [\#61](https://github.com/FXMisc/Flowless/pull/61) ([HollisWaite](https://github.com/hwaite))
- Correct trackpad scrolling gestures under MacOS [\#64](https://github.com/FXMisc/Flowless/pull/64) ([RachelGreenham](https://github.com/StrangeNoises) and [EduGarcia](https://github.com/Arcnor))

## [v0.6](https://github.com/FXMisc/Flowless/tree/v0.6) (2017-10-08)
[Full Changelog](https://github.com/FXMisc/Flowless/compare/v0.5.2...v0.6)

**Closed issues:**

- Regression: lengthening viewport "height" when last cell is visible does not fill extra space towards ground with more content. [\#51](https://github.com/FXMisc/Flowless/issues/51)
- If content is scrolled right, viewport does not rescroll left as the viewport size is increased [\#40](https://github.com/FXMisc/Flowless/issues/40)
- Undesired scrolling occurs when anchor cell is replaced, not deleted [\#39](https://github.com/FXMisc/Flowless/issues/39)
- Exception during layout pass \(seems to cause unresponsive GUI\) [\#37](https://github.com/FXMisc/Flowless/issues/37)
- Add `focused` pseudo css class to VirtualizedScrollPane when its content is focused [\#36](https://github.com/FXMisc/Flowless/issues/36)
- Make VirtualFlow's gravity styleable via CSS [\#34](https://github.com/FXMisc/Flowless/issues/34)
- Unneccessary lower-right corner in VirtualizedScrollPane [\#12](https://github.com/FXMisc/Flowless/issues/12)

**Merged pull requests:**

- Revert - Add cells under the ground when item index is the last one [\#52](https://github.com/FXMisc/Flowless/pull/52) ([JordanMartinez](https://github.com/JordanMartinez))
- Add default methods to get scroll-related properties' values [\#50](https://github.com/FXMisc/Flowless/pull/50) ([JordanMartinez](https://github.com/JordanMartinez))
- Change example properties file to use correct key names for nexus plugin. [\#49](https://github.com/FXMisc/Flowless/pull/49) ([JordanMartinez](https://github.com/JordanMartinez))
- Fix bug: version should only be outputted when task is called [\#48](https://github.com/FXMisc/Flowless/pull/48) ([JordanMartinez](https://github.com/JordanMartinez))
- Also migrate to FXMisc in Travis [\#47](https://github.com/FXMisc/Flowless/pull/47) ([JordanMartinez](https://github.com/JordanMartinez))
- Migrate build info to FXMisc [\#46](https://github.com/FXMisc/Flowless/pull/46) ([JordanMartinez](https://github.com/JordanMartinez))
- Fix \#40 by setting lower offset value based on viewport size increase [\#42](https://github.com/FXMisc/Flowless/pull/42) ([JordanMartinez](https://github.com/JordanMartinez))
- Add pseudo class for when content is focused [\#38](https://github.com/FXMisc/Flowless/pull/38) ([JordanMartinez](https://github.com/JordanMartinez))
- Make gravity of virtual flow styleable and dynamically alterable [\#35](https://github.com/FXMisc/Flowless/pull/35) ([twistedsquare](https://github.com/twistedsquare))
- Write classes' javadoc [\#33](https://github.com/FXMisc/Flowless/pull/33) ([JordanMartinez](https://github.com/JordanMartinez))
- Transform a change by using an undeleted visible cell or by ignoring exact replacements [\#32](https://github.com/FXMisc/Flowless/pull/32) ([JordanMartinez](https://github.com/JordanMartinez))
- Expose scrolling api [\#29](https://github.com/FXMisc/Flowless/pull/29) ([JordanMartinez](https://github.com/JordanMartinez))
- Make VirtualizedScrollPane FXML ready [\#28](https://github.com/FXMisc/Flowless/pull/28) ([hwaite](https://github.com/hwaite))

## [v0.5.2](https://github.com/FXMisc/Flowless/tree/v0.5.2) (2017-03-02)
[Full Changelog](https://github.com/FXMisc/Flowless/compare/v0.5.1...v0.5.2)

**Closed issues:**

- Make VirtualizedScrollPane FXML ready [\#25](https://github.com/FXMisc/Flowless/issues/25)
- VirtualizedScrollPane Doesn't appear in the jar [\#18](https://github.com/FXMisc/Flowless/issues/18)

**Merged pull requests:**

- Fix horizontal position calculation [\#26](https://github.com/FXMisc/Flowless/pull/26) ([shoaniki](https://github.com/shoaniki))

## [v0.5.1](https://github.com/FXMisc/Flowless/tree/v0.5.1) (2016-05-16)
[Full Changelog](https://github.com/FXMisc/Flowless/compare/v0.5...v0.5.1)

## [v0.5](https://github.com/FXMisc/Flowless/tree/v0.5) (2016-05-13)
[Full Changelog](https://github.com/FXMisc/Flowless/compare/v0.4.7...v0.5)

**Closed issues:**

- Layout-dependent methods need to call layout\(\) first. [\#19](https://github.com/FXMisc/Flowless/issues/19)
- How to programmatically scroll to Node with ScrollPane that is bound to VirtualizedScrollPane [\#17](https://github.com/FXMisc/Flowless/issues/17)
- Make overriding VirtualizedScrollpane's layoutChildren\(\) more developer-friendly [\#14](https://github.com/FXMisc/Flowless/issues/14)
- Make VirtualFlow's scrollBars optional [\#9](https://github.com/FXMisc/Flowless/issues/9)

**Merged pull requests:**

- Make jar an OSGi bundle. [\#24](https://github.com/FXMisc/Flowless/pull/24) ([timebowl](https://github.com/timebowl))
- Removed empty "@return" tag to get rid of javadoc warning [\#23](https://github.com/FXMisc/Flowless/pull/23) ([JordanMartinez](https://github.com/JordanMartinez))
- Added option to control the ScrollBar policy of each scroll bar [\#22](https://github.com/FXMisc/Flowless/pull/22) ([JordanMartinez](https://github.com/JordanMartinez))
- Added call to `layout\(\)` to insure that layout-dependent methods return valid results [\#20](https://github.com/FXMisc/Flowless/pull/20) ([JordanMartinez](https://github.com/JordanMartinez))
- Added ScaledVirtualized class [\#16](https://github.com/FXMisc/Flowless/pull/16) ([JordanMartinez](https://github.com/JordanMartinez))
- Make get/remove content public. [\#13](https://github.com/FXMisc/Flowless/pull/13) ([JordanMartinez](https://github.com/JordanMartinez))
- Scroll bars now optional [\#11](https://github.com/FXMisc/Flowless/pull/11) ([JordanMartinez](https://github.com/JordanMartinez))

## [v0.4.7](https://github.com/FXMisc/Flowless/tree/v0.4.7) (2015-10-31)
[Full Changelog](https://github.com/FXMisc/Flowless/compare/v0.4.6...v0.4.7)

**Merged pull requests:**

- Changed estimatedScrollX/Y to return Var\<Double\> instead of Val\<Double\>. [\#8](https://github.com/FXMisc/Flowless/pull/8) ([JordanMartinez](https://github.com/JordanMartinez))

## [v0.4.6](https://github.com/FXMisc/Flowless/tree/v0.4.6) (2015-10-29)
[Full Changelog](https://github.com/FXMisc/Flowless/compare/v0.4.5...v0.4.6)

**Closed issues:**

- VirtualFlow returns incorrect height of first cell when in need of layout pass [\#4](https://github.com/FXMisc/Flowless/issues/4)
- TextFlow not displayed with formatting  [\#3](https://github.com/FXMisc/Flowless/issues/3)

**Merged pull requests:**

- Expose scrolling values & add scrollToPixel capabilities [\#7](https://github.com/FXMisc/Flowless/pull/7) ([JordanMartinez](https://github.com/JordanMartinez))

## [v0.4.5](https://github.com/FXMisc/Flowless/tree/v0.4.5) (2015-08-18)
[Full Changelog](https://github.com/FXMisc/Flowless/compare/v0.4.4...v0.4.5)

## [v0.4.4](https://github.com/FXMisc/Flowless/tree/v0.4.4) (2015-05-01)
[Full Changelog](https://github.com/FXMisc/Flowless/compare/v0.4.3u1...v0.4.4)

**Implemented enhancements:**

- Stack items from bottom [\#2](https://github.com/FXMisc/Flowless/issues/2)

## [v0.4.3u1](https://github.com/FXMisc/Flowless/tree/v0.4.3u1) (2015-02-28)
[Full Changelog](https://github.com/FXMisc/Flowless/compare/v0.4.3...v0.4.3u1)

## [v0.4.3](https://github.com/FXMisc/Flowless/tree/v0.4.3) (2015-02-28)
[Full Changelog](https://github.com/FXMisc/Flowless/compare/v0.4.2...v0.4.3)

## [v0.4.2](https://github.com/FXMisc/Flowless/tree/v0.4.2) (2015-02-24)
[Full Changelog](https://github.com/FXMisc/Flowless/compare/v0.4.1...v0.4.2)

**Closed issues:**

- StackOverflowError [\#1](https://github.com/FXMisc/Flowless/issues/1)

## [v0.4.1](https://github.com/FXMisc/Flowless/tree/v0.4.1) (2015-02-23)
[Full Changelog](https://github.com/FXMisc/Flowless/compare/v0.4...v0.4.1)

## [v0.4](https://github.com/FXMisc/Flowless/tree/v0.4) (2015-02-09)
[Full Changelog](https://github.com/FXMisc/Flowless/compare/v0.3...v0.4)

## [v0.3](https://github.com/FXMisc/Flowless/tree/v0.3) (2014-08-12)
[Full Changelog](https://github.com/FXMisc/Flowless/compare/v0.2...v0.3)

## [v0.2](https://github.com/FXMisc/Flowless/tree/v0.2) (2014-07-14)
[Full Changelog](https://github.com/FXMisc/Flowless/compare/v0.1...v0.2)

## [v0.1](https://github.com/FXMisc/Flowless/tree/v0.1) (2014-07-05)


\* *This Change Log was automatically generated by [github_changelog_generator](https://github.com/skywinder/Github-Changelog-Generator)*
