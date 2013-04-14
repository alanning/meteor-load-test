meteor-load-test
================

Exploration of load testing a Meteor app.

The 'sut' (System Under Test) directory includes an example Meteor app which publishes data and accepts new data from the client on button press.


Instructions:
=============

1. Install phantomjs
2. Install Meteor
3. In one console window: `cd sut; ./run`
4. In another console window: `phantomjs test-scripts/loadmeteor.js http://localhost:3000`


What's it do?
=============

The `loadmeter.js` script waits for the meteor app to finish loading then pushes the lone button which adds two new records to the database.


Next steps
==========

* Integrate Bees with Machine Guns for scaling up
* Figure out how to get phantomjs to receive updated (published) data after new records are added.

