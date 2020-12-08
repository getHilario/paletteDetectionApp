# -*- coding: utf-8 -*-
"""
Created on Tue Nov 24 21:04:48 2020

@author: hplaw
"""

# -*- coding: utf-8 -*-
"""
Created on Fri Nov 20 20:21:06 2020

@author: hplaw
"""

import os.path
import numpy as np
from numpy import asarray 
import cv2
import uuid
import pymysql.cursors
from skimage import io
from sklearn.cluster import KMeans
from flask import Flask, request, jsonify
from PIL import Image, ImageColor



def rgb_to_hex(rgb):
    return '#%s' % ''.join(('%02x' % p for p in rgb))

def findColor(img, myColors,imgContours,imgBlank):
    imgRGB = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    count = 1
    for color in myColors:
        lower = np.array(color[0:3])
        upper = np.array(color[0:3])
        mask = cv2.inRange(imgRGB, lower, upper)
        getContours(mask,count,color[0:3],imgContours,imgBlank)
        count = count + 1

    
def getContours(img,labelNum,color,imgContours,imgBlank):
    contours, hierarchy = cv2.findContours(img, cv2.RETR_TREE, cv2.CHAIN_APPROX_NONE)[-2:]
    r = color[0]
    g = color[1]
    b = color[2]
    color[0]=b
    color[1]=g
    color[2]=r
    
    for cnt in contours:
        area = cv2.contourArea(cnt)
#        print(area)
        if area>400:
            temp = str(labelNum)
            cv2.drawContours(imgContours, cnt, -1,(0,0,0),-1)
            cv2.drawContours(imgBlank, cnt, -1,(0,0,0),-1)
            peri = cv2.arcLength(cnt, True)
            approx = cv2.approxPolyDP(cnt,0.2*peri,True)
#            print(len(approx))
#            objCor = len(approx)
            x,y,w,h = cv2.boundingRect(approx)
            #cv2.rectangle(imgContours,(x,y),(x+w,y+h),(0,255,0),2)
            cv2.putText(imgContours,temp,(x+(w//2),y+(h//2)),cv2.FONT_HERSHEY_COMPLEX_SMALL,0.1,(0,0,255),1)
            cv2.putText(imgBlank,temp,(x+(w//2),y+(h//2)),cv2.FONT_HERSHEY_SIMPLEX,0.5,color,2)


def getPalette(original):
    #set image to rgb and set as numpy array
    p1 = "8_color_"
    p2 = "outline_bw_"
    p3 = "outline_color_"
    
    rgb = cv2.cvtColor(original, cv2.COLOR_BGR2RGB)
    resized = cv2.resize(rgb, (960,540))
    numpyData = asarray(resized)
    
    n_colors = 8
    
    arr = numpyData.reshape((-1, 3))
    kmeans = KMeans(n_clusters=n_colors, tol = 0.001, algorithm = 'elkan', random_state=42).fit(arr)
    labels = kmeans.labels_
    centers = kmeans.cluster_centers_
    less_colors = centers[labels].reshape(numpyData.shape).astype('uint8')
    
    #path_file_1 = ('static/%s.png' %uuid.uuid4().hex)
    path_file_1 = ('static/'+p1+'%s.png' %uuid.uuid4().hex)
    #cv2.imwrite(path_file,img)
    io.imsave(path_file_1,less_colors)
    
    rgbs = [map(int, c) for c in centers]
    colors = list(map(rgb_to_hex, rgbs))
    ",".join(colors)
    
    f = open('colors8', 'w')

    f.write(colors[0])
    print(colors[0])
    for clr in colors[1:]:
        f.write(","+clr)
        #print(clr)
    #f.close()
    
    with open("colors8") as f:
        palette = f.read().split(",")
    f.close()
#palette

    myColors = []
    
    for clr in palette:    
        myColors.append(list(ImageColor.getcolor(clr,"RGB")))
    myColors
    
    img = cv2.imread(path_file_1)
    blur = cv2.medianBlur(img,5)

    imgContours = img.copy()
    
    imgBlank = Image.new("RGB", (960, 540), (255, 255, 255))
    imgBlank.save("blank.png", "PNG")
    imgBlank = cv2.imread("blank.png")
    
    findColor(blur,myColors,imgContours,imgBlank)


    path_file_2 = ('static/'+p2+'%s.png' %uuid.uuid4().hex)
    path_file_3 = ('static/'+p3+'%s.png' %uuid.uuid4().hex)

    cv2.imwrite(path_file_2,imgBlank)
    cv2.imwrite(path_file_3, imgContours)
    try: 
        os.remove("static/blank.png")
    except: pass
    
    r= {"color_8":path_file_1
        ,"outline_bw":path_file_2
        ,"outline_color":path_file_3
        ,"palette":colors}

    return r 


def addToDB(r,t):
    
    connection = pymysql.connect(host='localhost',
                                 user='root',
                                 password='',
                                 db='palette_database',
                                 cursorclass=pymysql.cursors.DictCursor)
    
    try:
        with connection.cursor() as cursor:
            sqlQuery = """INSERT INTO image_bundles(`user`,`file_name`,`color`,`outlinebw`,`outlinecolor`,`hexvalues`) VALUES (%s,%s,%s,%s,%s,%s)"""
            cursor.execute(sqlQuery,("user",t,r["color_8"],r["outline_bw"],r["outline_color"],listToString(r["palette"])))
            connection.commit()
    finally:
        connection.close()
        
def listToString(s):  
    
    # initialize an empty string 
    str1 = "," 
    
    # return string   
    return (str1.join(s))     

#API    
app = Flask(__name__)
#route http post to this method
@app.route('/api/upload', methods = ['POST'])
def upload():
    #retrieve image from client
    img = cv2.imdecode(np.fromstring(request.files['image'].read(),np.uint8),cv2.IMREAD_UNCHANGED)    
    default_name = 'file'
    title = request.form.get('desc', default_name)
    #process image
    response = getPalette(img) 
    #upload response content to database server
    addToDB(response,title)    
    #return a Response
    return jsonify(response)

#start server
app.run(host = "0.0.0.0", port = 5000)

