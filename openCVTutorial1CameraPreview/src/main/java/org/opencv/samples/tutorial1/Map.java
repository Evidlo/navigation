package org.opencv.samples.tutorial1;

/**
 * Created by Rahul Tiwari on 2/25/2017.
 */

public class Map {
    Landmark[] elements;

    public Map(Landmark[] inputs){
        elements = inputs;
    }

    public Landmark findGivenID(int Id){
        for(int i = 0; i < elements.length; i++) {
            if (Id == elements[i].getId()) {
                return elements[i];
            }
        }
        return(new Landmark(1, "Error", "Error ", "Error ","Error ","Error "));

    }



}
