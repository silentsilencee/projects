
Field Calibration

### Dependencies: ###

1) install opencv 
2) install numpy

### To run: ###

python ./app.py

click points in overhead view. click points in image view. press submit.
the homography should appear immediately. adjust by moving around the
points. homography will reappear automatically with every point adjusted.

images that it runs on are located in {rootdir}/static/imgs/*.png

annotations are saved in {rootdir}/static/annotations/*.json

temporary overlaid pool/cage planes are saved in
{rootdir}/static/imgs/output/*.png

that directory needs to be cleaned out, but for now, i left it as is, so
you can see them. we shouldn't be pushing .png files to the git, but
again, those are in here so people can play around with the tool.

### We need: ###

1) update existing json file with annotation coordinates.
2) select image from drop down
3) load existing json coordinates, if any, and display

### To make changes: ###

1) Back end is in app.py
2) Front end is in static/js/script.js
3) Webpage is in templates/index.html
