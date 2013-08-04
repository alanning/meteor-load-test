;(function () {

//////////////////////////////////////////////////////////////////////
// startup
//////////////////////////////////////////////////////////////////////

var settings = Meteor.settings || {},
    serverId = settings.serverId || Meteor.uuid(),
    entryIndex = 0;

Meteor.startup(function () {
  Meteor.entries._ensureIndex({createdAt: -1});

  createServerEntries();
});


//////////////////////////////////////////////////////////////////////
// methods
//////////////////////////////////////////////////////////////////////

Meteor.methods({
  addEntry: function (entry) {
    check(entry, Match.Optional({
      ownerId: String,
      name: String,
      type: String,
      createdAt: Match.Optional(Date)
    }));
   
    if (!entry) {
      entry = {
        ownerId: serverId,
        name: "entry-" + entryIndex++,
        type: "server"
      };
    }

    if (!entry.createdAt) {
      entry.createdAt = new Date()
    }

    return Meteor.entries.insert(entry);
  }
});


//////////////////////////////////////////////////////////////////////
// publish
//////////////////////////////////////////////////////////////////////

Meteor.publish("entry-count", function () {
  var self = this,
      count = 0,
      docId = 1,
      initializing = true,
      handle;

  handle = Meteor.entries.find({}).observeChanges({
    added: function (id) {
      count++;
      if (!initializing)
        self.changed("total-entry-count", docId, {count: count});
    },
    removed: function (id) {
      count--;
      self.changed("total-entry-count", docId, {count: count});
    }
    // don't care about moved or changed
  });

  initializing = false;
  self.added("total-entry-count", docId, {count: count});
  self.ready();

  self.onStop(function () {
    handle.stop();
  });
});

Meteor.publish("latest-entries", function (limit) {
  check(limit, Match.Optional(Number));

  var settings = Meteor.settings || {},
      query = settings.query || {},
      sort  = settings.sort,
      limit,
      options;

  limit = limit || settings.limit || 10,

  options = {
    sort: sort || { createdAt: -1 },
    limit: limit
  };

  return Meteor.entries.find(query, options);
});


//////////////////////////////////////////////////////////////////////
// misc
//////////////////////////////////////////////////////////////////////

function createServerEntries () {
  var i = 0,
      entry;

  entry = Meteor.entries.findOne({ownerId: serverId});

  if (entry) {
    // already have entries for this server
    return;
  }

  console.log('creating default entries for server id ' + serverId);

  for (; i < 1000; i++) {
    entry = {
      ownerId: serverId,
      name: "entry-" + (i+1),
      type: "server",
      createdAt: new Date()
    };
    Meteor.entries.insert(entry);
  }
}  // end createServerEntries


}());
