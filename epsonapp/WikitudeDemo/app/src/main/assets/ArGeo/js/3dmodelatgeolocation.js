var config = {
  apiKey: "AIzaSyDmgLkV4lnjvN7GE4DMQU5hejHRdCTCszo",
  authDomain: "navi-9e6bf.firebaseapp.com",
  databaseURL: "https://navi-9e6bf.firebaseio.com/",
  projectId: "navi-9e6bf",
  storageBucket: "navi-9e6bf.appspot.com",
  appID: "1:15852780522:web:f9299965f2b215a7995093",
};

firebase.initializeApp(config);

var World = {

    init: function initFn() {
        this.createModelAtLocation();
    },

    createModelAtLocation: function createModelAtLocationFn() {

        /*
            First a location where the model should be displayed will be defined. This location will be relative to
            the user.
        */
        var database = firebase.database();

var getlat;

function get_lat() {
   getlat = database.ref('latitude/');
   var lat;
   getlat.on('value',function(data) {
    lat = data.val();
    console.log(data.val());
    return lat;
   });
   return lat;
}

    var getlong;
    function get_long() {
   getlong = database.ref('longitude/');

   getlong.on('value',function(data) {
        var long = data.val();
            console.log(data.val());
                    return long;
        });

    }

        getlat = get_lat();
        getlong = get_long();

        console.log(getlat);
        console.log(getlong);

        var location = new AR.GeoLocation(getlat, getlong)

        /* Next the model object is loaded. */
        var modelEarth = new AR.Model("assets/earth.wt3", {
            onError: World.onError,
            scale: {
                x: 0.1,
                y: 0.1,
                z: 0.1
            },
            rotate: {
                x: 180,
                y: 180
            }
        });

        var indicatorImage = new AR.ImageResource("assets/indi.png", {
            onError: World.onError
        });

        var indicatorDrawable = new AR.ImageDrawable(indicatorImage, 0.1, {
            verticalAnchor: AR.CONST.VERTICAL_ANCHOR.TOP
        });

        /* Putting it all together the location and 3D model is added to an AR.GeoObject. */
        this.geoObject = new AR.GeoObject(location, {
            drawables: {
                cam: [modelEarth],
                indicator: [indicatorDrawable]
            }
        });
    },

    onError: function onErrorFn(error) {
        alert(error);
    }
};

World.init();