;(function () {

// runs on client and server
Meteor.entries = new Meteor.Collection("entries");


/////////////////////////////////////////////////////////////////
// Client
//
if (Meteor.isClient) {
  var clientId = Meteor.uuid(),
      entryIndex = 0;

  Meteor.startup(function () {
    Meteor.subscribe("entries", function () {
      Session.set('topTenReady', true);
    });
  });

  Template.hello.helpers({
    greeting: function () {
      return "Welcome to this load-test example app!";
    },

    topTen: function () {
      if (Session.equals('topTenReady', true)) {
        return getTopTen();
      } else {
        var tmp = [];
        for (var i = 0; i < 10; i++) {
          tmp.push({
            name: 'temp' + (i+1),
            createdAt: new Date(),
            type: "client",
            ownerId: clientId
          });
        }
        //return tmp;
      }
    }
  });

  function getTopTen () {
    var options;
    
    options = {
      sort: {
        createdAt: -1
      },
      limit: 10
    };

    return Meteor.entries.find({}, options);
  }

  Template.hello.events({
    'click button' : function (evt) {
      var i = 0,
          entry;

      evt.preventDefault();

      // create 2 unique entries
      for (; i < 2; i++) {

        entryIndex++;

        entry = {
          ownerId: clientId,
          name: "entry-" + entryIndex,
          type: "client",
          createdAt: new Date()
        };

        //console.log('creating entry');

        Meteor.call('addEntry', entry, function(error, id) { 
          if (error) {
            console.log(error);
          } else {
            //console.log('id ' + id);
          }
        });

      }
    }
  });

}  // end client


/////////////////////////////////////////////////////////////////
// Server
//
if (Meteor.isServer) {
  var settings = Meteor.settings || {},
      serverId = settings.serverId || Meteor.uuid();

  Meteor.methods({
    addEntry: function (entry) {
      Meteor.entries.insert(entry);
    }
  });

  Meteor.startup(function () {
    Meteor.entries._ensureIndex({createdAt: -1});

    createServerEntries();
  });

  function createServerEntries () {
    var i = 0,
        entry;

    entry = Meteor.entries.findOne({ownerId: serverId});

    if (entry) {
      // already have entries for this server
      return;
    }

    console.log('creating server entries: ' + serverId);

    for (; i < 1000; i++) {
      entry = {
        ownerId: serverId,
        name: "entry" + (i+1),
        type: "server",
        createdAt: new Date()
      };
      Meteor.entries.insert(entry);
    }
  }  // end createServerEntries


  Meteor.publish("entries", function () {
    var settings = Meteor.settings || {},
        query = settings.query || {},
        sort  = settings.sort,
        limit = settings.limit,
        options;

    options = {
      sort: sort || { createdAt: -1 },
      limit: limit || 10
    };

    return Meteor.entries.find(query, options);
  });

}  // end server

}());
