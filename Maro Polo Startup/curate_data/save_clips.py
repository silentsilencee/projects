import numpy as np
import pdb
import xml.etree.ElementTree
import json
#from moviepy.editor import VideoFileClip
from moviepy.editor import *
import os
import glob
import xmltodict
import pandas as pd
import sys

def get_xml_events(fname):
    with open(fname) as fd:
        doc = xmltodict.parse(fd.read())
    events = doc['file']['ALL_INSTANCES']['instance']
    #pdb.set_trace()
    return events

def get_game_metadata(game_id,game_metadata,keylist):
    metadata = game_metadata.iloc[game_id]
    metadata = metadata.where(pd.notnull(metadata),None).to_dict()
    metadata = { key: metadata[key] for key in keylist }
    return metadata

def get_all_times(events,event_name):
    t = []
    for (i,e) in enumerate(events):
        if e['code'] == event_name:
            t.append((float(e['start']),float(e['end']),str(e['code'])))
    return t

def merge_ranges(ranges):
    """
    Merge overlapping and adjacent ranges and yield the merged ranges
    in order. The argument must be an iterable of triplets (start, stop, code).

    >>> input: [(37.884, 77.884, 'Dark Offense'),
                (44.684, 59.694, 'Dark Center Entry'),
                (49.264, 62.264, 'Dark Shot'),
                (86.134, 111.134, 'Dark Counter Attack'),
                (104.374, 144.384, 'Dark Offense'),
                (104.864, 117.864, 'Dark Goal'),
                (106.864, 119.874, 'Dark Shot'),
                (149.814, 174.814, 'Dark Counter Attack'),
                (157.204, 197.214, 'Dark Offense'),
                (171.454, 184.464, 'Dark Shot')]
    >>> output: [[(37.884, 77.884, 'Dark Offense'),
                  (44.684, 59.694, 'Dark Center Entry'),
                  (49.264, 62.264, 'Dark Shot')],
                 [(86.134, 111.134, 'Dark Counter Attack'),
                  (104.374, 144.384, 'Dark Offense'),
                  (104.864, 117.864, 'Dark Goal'),
                  (106.864, 119.874, 'Dark Shot')],
                 [(149.814, 174.814, 'Dark Counter Attack'),
                  (157.204, 197.214, 'Dark Offense'),
                  (171.454, 184.464, 'Dark Shot')]]
    """
    result = []
    current_start = -1
    current_stop = -1
    current_code = ''

    for start, stop, code in sorted(ranges):
        if start > current_stop:
            # this segment starts after the last segment stops
            # just add a new segment
            result.append( [(start, stop, code)] )
            current_start, current_stop, current_code = start, stop, code
        else:
            # segments overlap, replace
            result[-1].append((start, stop, code))
            # current_start already guaranteed to be lower
            #current_stop = max(current_stop, stop)
    return result

def get_clip_times(event_list,codes):
    clip_times = []
    for el in event_list:
        labels = [e[2] for e in el]
        if len(set(labels).intersection(set(codes))) is not 0:
            t_start = []
            t_end = []
            for el_i in el:
                if el_i[2] in codes:
                    t_start = t_start + [el_i[0]]
                    t_end = t_end + [el_i[1]]
            clip_times = clip_times+[[(min(t_start),max(t_end))]+el]
    return clip_times

def process_game(rootdir,game_id,game_metadata,meta_keys,codes,codes_keep,teams):
    filename = '{}/{}'.format(rootdir,game_metadata.xml_file.values[game_id])
    events = get_xml_events(filename)
    meta = get_game_metadata(game_id,game_metadata,meta_keys)

    event_clips = []
    for team in teams:
        codes_keep_list = [ck.format(team) for ck in codes_keep]
        event_list = []
        for co in codes:
            event_list = event_list+get_all_times(events,co.format(team))
        #pdb.set_trace()
        event_list = merge_ranges(event_list)
        event_clips.append(get_clip_times(event_list,codes_keep_list))
    return event_clips[0],event_clips[1],meta

def save_clip_video(rootdir,meta,clip_info,clip_save_loc):
    # get the clip
    full_game_loc = '{}/{}'.format(rootdir,meta['game_file'])
    t_start = clip_info[0][0]
    t_end = clip_info[0][1]
    clip = VideoFileClip(full_game_loc).subclip(t_start, t_end)
    # clip name convention GAME_CODE +  event_id + clip number
    # t_end - t_start
    clip.set_duration(t_end-t_start).fl_image(invert_image).write_videofile(clip_save_loc,codec=codec)
    del clip

def save_clip_json(meta,clip_info,json_save_loc,clip_save_loc):
    # get the json
    clip_data = {}
    clip_data['game_meta'] = meta
    clip_data['time'] = {'t_start':clip_info[0][0],'t_end':clip_info[0][1]}
    clip_data['video_loc'] = clip_save_loc[31:]
    events_dict = []
    # the first element in clip info has the start and end times for the clip itself
    for e in clip_info[1:]:
        events_dict.append({'t_start':e[0],'t_end':e[1],'code':e[2]})
    clip_data['events'] = events_dict
    with open(json_save_loc, 'w') as fp:
        json.dump(clip_data, fp, sort_keys=True,indent=4)

def save_frame_image(rootdir,meta,clip_info,frame_save_loc):
    # get the clip
    full_game_loc = '{}/{}'.format(rootdir,meta['game_file'])
    t_start = clip_info[0][0]
    t_end = clip_info[0][1]
    clip = VideoFileClip(full_game_loc)
    clip.save_frame(frame_save_loc,t=(t_start+t_end)/2.)
    del clip.reader
    del clip
    # clip name convention GAME_CODE +  event_id + clip number
    # t_end - t_start
    #clip.set_duration(t_end-t_start).fl_image(invert_image).write_videofile(clip_save_loc,codec=codec)
    #del clip

def save_frame_json(meta,clip_info,json_save_loc,frame_save_loc):
    # get the json
    frame_data = {}
    frame_data['game_meta'] = meta
    # t is the time of the frame. t_start and t_end are the times of the clip from which it was pulled
    frame_data['time'] = {'t_start':clip_info[0][0],'t_end':clip_info[0][1],'t':(clip_info[0][0]+clip_info[0][1])/2.}
    frame_data['image_file'] = frame_save_loc
    events_dict = []
    # the first element in clip info has the start and end times for the clip itself
    for e in clip_info[1:]:
        events_dict.append({'t_start':e[0],'t_end':e[1],'code':e[2]})
    frame_data['events'] = events_dict
    frame_data['homography'] = []
    frame_data['img_pts'] = []
    frame_data['template_pts'] = []
    frame_data['camera_direction'] = ''
    with open(json_save_loc, 'w') as fp:
        json.dump(frame_data, fp, sort_keys=True,indent=4, separators=(',', ': '))

inversion = [2,1,0]; codec = 'rawvideo'
inversion = [0,1,2]; codec = 'libx264'

# for avi rawvideo, inversion: [2,1,0]
def invert_image(image):
    return image[:,:,inversion]

if __name__ == '__main__':
    rootdir = '/Volumes/scet/CalWWP-2_2017-18/_2017-18'
    game_metadata_file = '{}/game_metadata.csv'.format(rootdir)
    game_metadata = pd.read_csv(game_metadata_file)

    meta_keys = ['game_file','xml_file','fosh_url','pool_location',
                 'game_time','game_date','home_team','away_team',
                 'home_score','away_score']

    teams = ['Dark','White']
    codes = ['{} 6 X 5','{} 6 X 5 TO','{} After Goal','{} Center Entry','{} Counter Attack',
             '{} Goal','{} Offense','{} Penalty Earned','{} Shot','{} TO']

    codes_keep = ['{} Center Entry','{} Goal','{} Shot']

    game_id = int(sys.argv[1])

    dark_clips,white_clips,meta = process_game(rootdir,game_id,game_metadata,meta_keys,codes,codes_keep,teams)

    for (i,dc) in enumerate(dark_clips+white_clips):
        print(i)
        frame_save_loc = '/Volumes/scet/CalWWP-2_2017-18/extracted_frames/{}/{}.png'.format(os.path.split(meta['game_file'])[1][:-4],i)
        json_save_loc = '/Volumes/scet/CalWWP-2_2017-18/extracted_frames/{}/{}.json'.format(os.path.split(meta['game_file'])[1][:-4],i)
        save_frame_image(rootdir,meta,dc,frame_save_loc)
        save_frame_json(meta,dc,json_save_loc,frame_save_loc)
