# -*- coding: utf-8 -*-
"""
Created on Fri Nov 20 20:21:06 2020

@author: hplaw
"""

import os.path
import numpy as np
from numpy import asarray 
from skimage import io, color
from skimage.transform import resize
from sklearn.cluster import KMeans
import cv2
import json
from flask import Flask, request, Response
import uuid
from PIL import Image

#Function detect face form image
"""
def faceDetect(img):
    face_cascade = cv2.CascadeClassifier('face_detect_cascade.xml')
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    faces = face_cascade.detectMultiScale(gray,1.3,5)
    for(x,y,w,h) in faces:
        img = cv2.rectangle(img,(x,y),(x+w,y+h),(0,255,0))
    #save file
    path_file = ('static/%s.jpg' %uuid.uuid4().hex)
    cv2.imwrite(path_file,img)
    return json.dumps(path_file) #return image file name
"""
def rgb_to_hex(rgb):
    return '#%s' % ''.join(('%02x' % p for p in rgb))

def getPalette(img):
    #set image to rgb and set as numpy array
    rgb = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    resized = cv2.resize(rgb, (1024,576))
    numpyData = asarray(resized)
    
    n_colors = 8
    
    arr = numpyData.reshape((-1, 3))
    kmeans = KMeans(n_clusters=n_colors, random_state=42).fit(arr)
    labels = kmeans.labels_
    centers = kmeans.cluster_centers_
    less_colors = centers[labels].reshape(numpyData.shape).astype('uint8')
    
    path_file = ('static/%s.png' %uuid.uuid4().hex)
    #cv2.imwrite(path_file,img)
    io.imsave(path_file,less_colors)
    
    return json.dumps(path_file) #return image file name
#API
app = Flask(__name__)

#route http post to this method
@app.route('/api/upload', methods = ['POST'])
def upload():
    #retrieve image from client
    img = cv2.imdecode(np.fromstring(request.files['image'].read(),np.uint8),cv2.IMREAD_UNCHANGED)
    #process image
    #img_processed = faceDetect(img)
    img_processed = getPalette(img)
    #response
    return Response(response = img_processed, status = 200, mimetype = "application/json") #return json string

#start server
app.run(host = "0.0.0.0", port = 5000)

    