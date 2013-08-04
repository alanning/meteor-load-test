## What is this?
A load testing tool for Meteor applications.

It is an extension of the test setup that Andy Kriger demonstrated at Clojure/West 2012 which load-tested stateless websites.  Andy's code can be found here: [load-testing-with-clojure](https://github.com/locopati/load-testing-with-clojure).

Specifically, a [Grinder](http://grinder.sourceforge.net/) script was added which connects to a Meteor application using a [Java DDP client](https://github.com/kutrumbo/java-ddp-client) and executes DDP calls.

Users can specify properties such as:
 * Meteor method calls to perform
 * Meteor subscriptions to initiate
 * Number of threads to start
 * Wait time between thread start
 * Process ramp-up (increment)


## Preliminaries
<b>Install pre-req's</b>
  * Java
  * [leiningen](https://github.com/technomancy/leiningen)

<b>Setup the project</b>

```bash
git clone git://github.com/alanning/meteor-load-test.git
cd meteor-load-test
lein deps  # downloads dependencies
```

## The System Under Test (SUT)
<b>To start the server</b>

```bash
cd sut
./run
```

<b>Open app in browser</b>

http://localhost:3000/


## The Grinder
<b>To start an agent</b>

```bash
# in meteor-load-test directory
bin/grinder agent start [optional host url of console - defaults to localhost]
```

<b>Monitor agent log file</b>

```bash
# in separate console window, from meteor-load-test directory
cd log
tail -f agent_1.log
```

<b>To start the console</b>

```bash
# in separate console window, from meteor-load-test directory
bin/grinder console start
```

<b>To run tests</b>

In the Script tab, set the root directory to $PROJECT_HOME/grinder

Select `working.properties` and set it as the properties file to use (the star button)

Open `working.properties` and adjust as appropriate.  Default setup will load test http://localhost:3000/

Click the play button in the top left (tooltip says, "Start the worker processes")

In the results tab, you should see test results


## How to use

Modify `working.properties` as appropriate.  
See the [Grinder documentation](http://grinder.sourceforge.net/g3/properties.html) for more options

Grinder agents should be started on separate boxes (not your webserver).

Note: Currently collection updates are received via Meteor subscriptions but there are no metrics gathered for how long it takes an update to be delivered under load.  The closest we have right now is peak and mean TPS for DDP calls.

Given that, probably the most realistic way to test responsiveness of your app under load is to spin up your agents, kick off the tests, wait for them to saturate your server, and then visit your site via your own browser.


## Future work

Create Chef or Pallet scripts to automate creation of Grinder agent instances in the cloud

Explore recording timing information for length of time to finish receiving all collection updates


## Acknowledgements

Based on [load-testing-with-clojure](https://github.com/locopati/load-testing-with-clojure) by Andy Kriger

Uses [java-ddp-client](https://github.com/kutrumbo/java-ddp-client) by Peter Kutrumbos to communicate with Meteor server
