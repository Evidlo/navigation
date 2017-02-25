package org.opencv.samples.tutorial1;

/**
 * Created by Rahul Tiwari on 2/25/2017.
 */


public class Landmark {
    int LandmarkID;
    String LandmarkName;
    String LandmarkDescription;
    int xcoordinate;
    int ycoordinate;
    int orientation;

    public Landmark(int ID, String name, String description, int x, int y, int o){
        LandmarkID = ID;
        LandmarkName = name;
        LandmarkDescription = description;
        xcoordinate = x;
        ycoordinate = y;
        orientation = o;
    }

    public String getLandmarkName(){
        return LandmarkName;
    }
    public String getLandmarkDescription(){
        return LandmarkDescription;
    }
    public int getxcoordinate(){
        return xcoordinate;
    }
    public int getLandmarkID(){
        return LandmarkID;
    }
    public int getycoordinate(){
        return ycoordinate;
    }
    public int getorientation(){
        return orientation;
    }

}

