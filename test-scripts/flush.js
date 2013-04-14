var page = require('webpage').create(),
    system = require('system'),
    url;

console.log('running...');

if (system.args.length === 1) {
  console.log('Usage: flush.js <some URL>');
  phantom.exit();
}
url = system.args[1];

page.open(url);

setInterval(function() {
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
    onMeteorReady(page);
  }
}, 100);


function onMeteorReady (page) {
  var out = page.content;
  out = out.replace(/<script[^>]+>(.|\n|\r)*?<\/script\\s*>/ig, '');
  out = out.replace('<meta name="fragment" content="!">', '');
  console.log(out);
  phantom.exit();
}

