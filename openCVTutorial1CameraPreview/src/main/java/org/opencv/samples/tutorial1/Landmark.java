package org.opencv.samples.tutorial1;

/**
 * Created by Rahul Tiwari on 2/25/2017.
 */


public class Landmark {
    int Id;
    String Name;
    String NorthText;
    String EastText;
    String SouthText;
    String WestText;

    public Landmark(int id, String name, String north, String east, String south, String west){
        Id = id;
        Name = name;
        NorthText = north;
        EastText = east;
        SouthText = south;
        WestText = west;

    }

    public int getId(){
        return Id;
    }
    public String getName(){
        return Name;
    }

    public String getNorthText(){
        return NorthText;
    }
    public String getEastText(){
        return EastText;
    }
    public String getSouthText(){
        return SouthText;
    }
    public String getWestText(){
        return WestText;
    }
}

