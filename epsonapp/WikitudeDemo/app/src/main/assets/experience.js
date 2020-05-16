var World = {
    init: function() {
        var targetCollectionResource = new AR.TargetCollectionResource("assets/magazine.wtc");
        var tracker = new AR.ImageTracker(targetCollectionResource);

        var img = new AR.ImageResource("assets/imageOne.png");
        var overlay = new AR.ImageDrawable(img, 1, {
            translate: {
                x:-0.15
            }
        });

        var trackable = new AR.ImageTrackable(tracker, "*", {
            drawables: {
                cam: overlay
            }
        });
    },
};

World.init();