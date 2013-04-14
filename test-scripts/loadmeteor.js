var page = require('webpage').create(),
    system = require('system'),
    url, 
    readyHandle,
    debug = false;


if (system.args.length === 1) {
  console.log('Usage: meteor-loader.js <url> [debug]');
  phantom.exit();
}
url = system.args[1];

if (system.args.length > 2) {
  if ("true" === system.args[2] ||
      "debug" === system.args[2]) {
    debug = true;
  }
}

page.onLoadStarted = function () {
  page.startTime = new Date();
};

if (debug) {
  page.onResourceRequested = function (request) {
    console.log('Request ' + JSON.stringify(request, undefined, 2));
  };
  page.onResourceReceived = function (response) {
    console.log('Response ' + JSON.stringify(response, undefined, 2));
  };
}


page.open(url);

// wait until Meteor client has fully loaded
readyHandle = setInterval(function() {
  var ready = page.evaluate(function () {
    if (typeof Meteor !== 'undefined' 
        && typeof(Meteor.status) !== 'undefined' 
        && Meteor.status().connected) {
      Deps.flush();
      return Meteor._LivedataConnection._allSubscriptionsReady();
    }
    return false;
  });

  if (ready) {
    clearInterval(readyHandle);
    onMeteorReady(page);
  }
}, 50);


// called once Meteor client has loaded
function onMeteorReady (page) {
  var time; 

  time = new Date() - page.startTime;
  console.log('Loaded ' + url + ' in ' + time + ' ms');

  // trigger a callback to server
  page.evaluate(function () {
    $('button').click();
  });

  // give client time to communicate with server
  setTimeout(function () {
    phantom.exit();
  }, 1000);
}
