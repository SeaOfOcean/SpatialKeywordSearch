CONTENTS OF THIS FILE
---------------------
   
 * Introduction
 * Package and File Description
 * Requirements
 * Contact
 
# Introduction
This is the implementation code for CS6203 project. 
In this project, we build a publish/subscribe application over POIs and twitter stream data. 
Keywords in POIs are the subscribed content, and we annotate POIs with fresh tweets that are related in text and near in location.

# Package and File Description
## topology
**Main.java** is the Main Entrance of the project. 
 * You can simply build and run in local mode.
 * If you want to run in the cluster, assume the Storm is correctly deployed, you may run in this way:
 storm jar %your_project_package%.jar %your_topology_main% %topology_name%

## bolt
This package contains three bolt class.

## spout
This package contains three spout class. 
In specific, TweetFileSpout is to monitor tweet stream by reading from file, 
while TwitterApiSpout is the real twitter stream by connecting to Twitter API.

## index
This package contains two sets of index. One is the tweet index(i3) and one is the POI index(quadtree).
And there is a zorder curve partition method in sub-package **partition**

## structure
This package contains frequently used data structure in this project.

## util
This package contains frequently used method.

## config.properties
This configure file contains all parameters that used in our project.
They can be easily changed for experiment requirement.

# Requirement
All the packages required are listed in pom.xml


 
 
