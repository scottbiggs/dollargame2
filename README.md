# dollar_game
Android version of the mathematical Dollar Game

The "Dollar Game" is a game where the nodes of a graph have a numerical amount (thinking of them as dollars is convenient).  The amount may be positive or negative (negative numbers can be thought of as being in debt).  A node may give a unit (or dollar) to EACH of the nodes it is connected to, or may take a unit (aka dollar) from EACH of the connected nodes.  That is the basic play.  The object is to distribute the wealth of all the nodes until no one node has a negative amount (or is free from debt).

The game has two modes: <strong>Build</strong> and <strong>Solve</strong>

## Build
Graphs are constructed or modified in this mode.

Simply tap on an empty area to create a node.  Connect nodes by tapping on one, then tapping on the node to connect.  Connections are removed in a similar manner.

Nodes can be moved around by dragging.

Nodes are modified and deleted by long touching.  A dialog will come up allowing you to change the contents of the node or delete it completely.

Changing all the nodes is pretty tedious.  So there's a button to change all the nodes to random numbers.  It's pretty stupid right now, so I just press it until I get a genus/count combination that I want.

#### Genus & Count
The genus of a graph is simply the number of loops in a graph.  The more loops, the more connected the graph is, and the harder it is to solve.

The count is the sum of all the dollar amounts in all the nodes.  Add 'em up (subtract for negative amounts of course), and you get the count.

In general if the genus is less than or equal to the count, the game is solvable.  I like to make sure that the genus == count; this makes for an interesting game.  But there are some games that are solvable even if the genus is greater than the count.  These can be pretty hard to figure out (you've been warned!).


## Solve
This is where the game is actually played, yay!

But only connected graphs can be played.  If the graph is not connected, Solve mode won't be available.  The connection icon (four dots on the top-right) will change once the graph is completely connected.

By tapping on a node, two buttons will pop up.  The left button will cause this node to TAKE a dollar from every neighbor.  This will decrease each neighbor's amount by one, and increase this node's amount by the number of neighbors.

The other button will do the opposite: it will GIVE a dollar to each neighbor with the the expected amounts changed appropriately.

Once all the nodes are no longer in debt, the icon at the top right will change to a check-mark and you have solved this puzzle!  Congratulations, go celebrate with a beer, a mint, a kiss, or whatever's convenient.

----------
Lots of things are still in the works.  For instance, after many tries, I'm still not happy with the give/take icons.  If you have a suggestion, I'd like to hear it.

Only a few menu items do anything right now. You can see where I want to go.  But you CAN turn the hints on/off via settings.  This can save a lot of screen real-estate.

----------

Graph theory is the mathematics of connections.  There has been a great many aspects of graph theory that are well documented, including the shortest path problem and the traveling salesman problem.  But lots of graph theory issues have no known solution.  This game allows people to explore one of these issues in a a fun and easy way.

One interesting graph theory issue is that no one knows how to calculate the best solution to the dollar game.  In fact, no one even knows how to figure out if a game is solvable or not.  All that anyone has figured out is that if the count is greater or equal to the genus, then is IS solvable.  Plenty of games are also solvable is the genus is larger than the count, but no one has come up with a way to figure out which games.  If you got it, consider making your knowledge public; a PhD in math is yours for the taking!

This program was inspired by the "Dollar Game" discussed by Dr. Holly Kreiger on the Numberphile channel.  Her video can be found at:
https://www.youtube.com/watch?v=U33dsEcKgeQ

More in-depth mathematics about this game can be found here:
https://mattbaker.blog/2013/10/18/riemann-roch-for-graphs-and-applications/
