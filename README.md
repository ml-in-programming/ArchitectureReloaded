[![See Travis CI build status at https://travis-ci.org/ml-in-programming/ArchitectureReloaded/](https://travis-ci.org/ml-in-programming/ArchitectureReloaded.svg?branch=master)](https://travis-ci.org/ml-in-programming/ArchitectureReloaded/)

ArchitectureReloaded
===============

Automated refactoring detection plugin for IntelliJ IDEA. The plugin’s objective is to analyze the code of a project opened in IDEA and to suggest developers possible directions of refactoring which would improve such overall characteristics as cohesion, coupling and a number of other object-oriented architecture metrics. In the ideal case there would be a possibility to apply these refactorings immediately and automatically using IDEA's internal refactoring tools.

After the plug-in is built and started, the `Analyze | Search for refactorings...` menu item becomes available, where you can select the algorithms to be run. Based on the results of the calculation, the `Suggested refactorings` tool window is opened. Each line of the table describes a refactoring: what and where to move and also the assessment of the adequacy of the refactoring based on the results of algorithms’ voting (in the range from 0 to 1). At the bottom of the window there is a slider, which allows you to set a threshold of Accuracy of displayed refactorings. By clicking on the cells of the `Entity` and `Move to` columns, the corresponding part of the project opens in the code editor window.

To apply any refactoring, you must check the corresponding checkbox in the leftmost column of the table and click the `Refactor` button. After that the built-in IDEA refactoring mechanisms are started and corresponding dialog boxes are displayed. But in some cases, proposed refactorings cannot be performed automatically correctly (for example, because of the use of private fields in method bodies), and it is better to consider them rather as a recommendation to the programmer to think in this direction.

The following buttons are to the left of the table (top to bottom):
- possibility to see the results of work of each particular algorithm or any combination thereof separately;
- enabling or disabling of colour highlighting of the table rows depending on the Accuracy value;
- displaying of calculation statistics: the number of processed classes, fields and methods, the running time of each algorithm, and so on.
- close button.
