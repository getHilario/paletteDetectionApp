# paletteDetectionApp
Android application for detecting palette within an image and drawing out contours based on the selected colors. The goal is to create a simplified image for art purposes, similar to an image found in a coloring book. The app reduces the image to a range of 8 colors through a Python backend using a KMeans approach. 
The application then gives the user view three possible options:

-The reduced image
-The reduced image with contours
-A black and white image with contours (with numbers representing matching color)


Android Interface:
![Screenshot_1606721640](https://user-images.githubusercontent.com/54413952/110193715-34bd0c00-7dfb-11eb-91fc-c791a13f5af0.png)

![Screenshot_1606721690](https://user-images.githubusercontent.com/54413952/110193718-3daddd80-7dfb-11eb-8cdf-3f0f6929eb28.png)

![Screenshot_1606721743](https://user-images.githubusercontent.com/54413952/110193719-43a3be80-7dfb-11eb-8f04-a1b1a6ccbecd.png)

![Screenshot_1606721763](https://user-images.githubusercontent.com/54413952/110193720-44d4eb80-7dfb-11eb-86c1-76a56cc8e6ff.png)

![Screenshot_1606721765](https://user-images.githubusercontent.com/54413952/110193722-47cfdc00-7dfb-11eb-9604-2d25136316e6.png)

Python scrpting for palette detection using a Flask API:

![palette_values(half)](https://user-images.githubusercontent.com/54413952/110193725-4c949000-7dfb-11eb-9bff-08fbd0974fd4.png)
