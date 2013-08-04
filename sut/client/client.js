;(function () {

//////////////////////////////////////////////////////////////////////
// setup
//////////////////////////////////////////////////////////////////////

var clientId = Meteor.uuid(),
    entryIndex = 0,
    TotalEntryCount,
    colorIdMap = {};

TotalEntryCount = new Meteor.Collection("total-entry-count");

Meteor.startup(function () {
  Session.set('numEntriesToDisplay', 10);

  Deps.autorun(function () {
    var num = Session.get('numEntriesToDisplay');
    console.log('subscribing to get', num, ' latest entries');
    Meteor.subscribe("latest-entries", num, function () {
      Session.set('entriesReady', true);
    });
  });

  Meteor.subscribe("entry-count", function () {
    Session.set('countReady', true);
  });
});


//////////////////////////////////////////////////////////////////////
// templates
//////////////////////////////////////////////////////////////////////

Template.main.helpers({
  entries: function () {
    var options = {
        sort: { createdAt: -1 }
      };

    if (Session.equals('entriesReady', true)) {
      return Meteor.entries.find({}, options);
    }
  },

  totalEntryCount: function () {
    if (Session.equals('countReady', true)) {
      var doc = TotalEntryCount.findOne({_id:1});
      return doc ? doc.count : 'calculating...';
    } else {
      return 'calculating...';
    }
  },

  /**
   * Uses the 'ownerId' field to compute a color for current
   * entry.
   *
   * @method colorTag
   * @return {String} color in hex format '#ff0000'
   */
  colorTag: function () {
    var id = this.ownerId,
        defaultColor = '#ff0000';

    if ('undefined' == typeof id || null === id)
      return defaultColor;

    if (!colorIdMap[id]) {
      colorIdMap[id] = randomColor();
    }

    return colorIdMap[id];
  }
});

Template.main.events({
  'change #numEntries' : function (evt) {
    evt.preventDefault();
    Session.set('numEntriesToDisplay', parseInt(evt.target.value, 10));
  },

  'click button' : function (evt) {
    var entry;

    evt.preventDefault();

    entryIndex++;

    entry = {
      ownerId: clientId,
      name: "entry-" + entryIndex,
      type: "client",
      createdAt: new Date()
    };

    Meteor.call('addEntry', entry, function(error, id) { 
      if (error) {
        console.log(error);
      }
    });
  }
});


//////////////////////////////////////////////////////////////////////
// misc
//////////////////////////////////////////////////////////////////////

function randomColor() {
  var text = '',
      possible = 'abcdef0123456789';

  for( var i=0; i < 6; i++ ) {
    text += possible.charAt(Math.floor(Math.random() * possible.length));
  }

  return '#' + text;
}

}());
