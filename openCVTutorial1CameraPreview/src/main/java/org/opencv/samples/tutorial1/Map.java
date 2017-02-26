package org.opencv.samples.tutorial1;

/**
 * Created by Rahul Tiwari on 2/25/2017.
 */

public class Map {
    Landmark[] elements;

    public Map(Landmark[] inputs){
        elements = inputs;
    }

    public Landmark findGivenID(int ID){
        for(int i = 0; i < elements.length; i++) {
            if (ID == elements[i].getLandmarkID()) {
                return elements[i];
            }
        }
        return(new Landmark(1, "Error", "Error ", 0,0,0));

    }

    private double findDistance(Landmark current, Landmark target){
        int xc = current.getxcoordinate();
        int yc = current.getycoordinate();

        int xt = target.getxcoordinate();
        int yt = target.getycoordinate();

        return Math.pow((float) (Math.pow((xc-xt),2)+ Math.pow((yc-yt),2))  ,2 );

    }
    // I may have done the following incorrectly, not sure how to redefine the closest variable every time it
// finds the smallest distance
    public Landmark getClosest(Landmark current){
        int closeval = 0;
        Landmark closest = new Landmark(1, "Error", "Error ", 0,0,0);
        for(int i = 0; i < elements.length; i++){
            if(findDistance(current, elements[i]) <= closeval && findDistance(current, elements[i]) != 0){
                closest = elements[i];
            }
        }
        return closest;
    }

    public int[] getDirections(Landmark current, Landmark target){
        int[] result = new int[]{current.getxcoordinate()-target.getxcoordinate(), current.getycoordinate()-target.getycoordinate()};
        return(result);
    }


}
