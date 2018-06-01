"""Field calibration app."""
from flask import Flask, request, render_template, jsonify
import json
import cv2
import numpy as np
import random
import glob
import pdb

app = Flask(__name__)

app.config.update(dict(DEBUG=True))

#folder_path = 'static/imgs/20180128_SJSUvCAL/'

@app.route('/')
def index():
    """Call webpage."""
    #print(folder_path)
    #files = glob.glob(folder_path + '*png')
    #files.sort()
    #filenames = []
    #for f in files:
    #    filenames.append(f.replace(folder_path, ""))
    return render_template('index.html') #,files=filenames,path=folder_path)


@app.route('/calibrate/')
def calibrate():
    """The calibration function."""
    print("calibrate")
    input = int(request.args.get("input"))
    camera_direction = str(request.args.get('camera_direction'))
    game_folder = str(request.args.get('game_folder'))
    # ## coordinates in image plane
    img_anno = np.array(json.loads(request.args.get("img_anno")),
                        dtype="float32")
    # ## coordinates in ground plane
    ground_anno = np.array(json.loads(request.args.get("ground_anno")),
                           dtype="float32")
    #pdb.set_trace()
    img1 = cv2.imread('static/imgs/{}/'.format(game_folder) + str(input) + '.png')  # image plane
    img2 = cv2.imread('static/imgs/wp_field.png')  # ground plane
    img3 = cv2.imread('static/imgs/wp_field.png')

    ## pool field projection matrix from ground plane to image
    #pdb.set_trace()
    M = cv2.findHomography(ground_anno, img_anno[:-2,:], cv2.RANSAC, 5.0)
    # warp the overhead view so we can overlay it on the image
    warp = cv2.warpPerspective(img2, M[0],(1920,1080))

    ## cage plane projection matrix
    # the size of the overhead .png 520, 702
    # we don't select the gt matching points for this because we just want to warp
    # the entire image to the cage plane

    #M_c = cv2.findHomography(np.array([[0,0],
    #     [702,0],
    #     [0, 520],
    #     [702,  520 ]]),img_anno[4:,:],cv2.RANSAC,5.0)
    ## wow.. this is inefficient, but it's just changing the color of the overhead field
    ## so the cage plane will be all one color (magenta-ish)
    #img3 = img3*0+1
    ## critical that all these are non-zero, otherwise can't rely on sign to zero out later
    #img3[:,:,0]=img3[:,:,0]*102
    #img3[:,:,1]=img3[:,:,1]*1
    #img3[:,:,2]=img3[:,:,2]*204
    #warp_cage = cv2.warpPerspective(img3,M_c[0],(1280,720))
    ## get rid of the part that overlaps with the cage plane.. otherwise, not visually pleasing
    #warp_cage_c = 1-np.sign(warp_cage)
    #warp = np.multiply(warp_cage_c,warp)

    random_file_name = ''.join(random.choice('abcde1234')
                               for _ in range(10)) + '.png'
    #pdb.set_trace()
    cv2.imwrite('static/imgs/output/' + random_file_name, img1/2 + warp/2)# + warp_cage/2)

    json_filename = 'static/annotation/{}/'.format(game_folder) + str(input)+'.json'
    # load existing annotation so that we just update the json, instead of overwrite
    with open(json_filename,'r') as fp:
        json_data = json.load(fp)

    # save annotation to a local json file
    json_data['image_file'] = str(input) + '.png'
    json_data['homography'] = M[0].tolist()
    json_data['img_pts'] = img_anno.tolist()
    json_data['template_pts'] = ground_anno.tolist()
    json_data['camera_direction'] = camera_direction
    with open(json_filename, 'w') as outfile:
        json.dump(json_data, outfile, sort_keys=True,indent=4, separators=(',', ': '))
    return jsonify({"name": random_file_name})

@app.route('/reset/')
def reset():
    """Reset the annotation."""
    print("reset")
    input = int(request.args.get("input"))
    game_folder = str(request.args.get('game_folder'))
    # ## coordinates in image plane
    json_filename = 'static/annotation/{}/'.format(game_folder) + str(input)+'.json'
    # load existing annotation so that we just update the json, instead of overwrite
    with open(json_filename,'r') as fp:
        json_data = json.load(fp)

    # save annotation to a local json file
    json_data['image_file'] = str(input) + '.png'
    json_data['homography'] = []
    json_data['img_pts'] = []
    json_data['template_pts'] = []
    json_data['camera_direction'] = ''
    with open(json_filename, 'w') as outfile:
        json.dump(json_data, outfile, sort_keys=True,indent=4, separators=(',', ': '))
    return jsonify({'return': 0})


if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5029)
