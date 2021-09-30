/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package SpineJ.Tools.Misc;

import ij.IJ;

/**
 *
 * @author Florian Levet
 */
public class TimeEstimator {
    long time = 0;

    public TimeEstimator(){

    }

    public void initTime(){
        time = System.currentTimeMillis();
    }

    public void timeNow(String msg){
        System.out.print(msg + ": ");
        timeNow(false);
    }

    public void timeNowLog(String msg){
        IJ.log(msg + ": ");
        timeNow(true);
    }

    public void timeNow(boolean log){
        long time2 = System.currentTimeMillis();
        long diff = time2 - time;
        if(diff > 60000){
            //means we have a time superior to 1 minute
            long min = diff / 60000;//
            long diff2 = diff - (min*60000);
            long sec = diff2 / 1000;
            long mill = diff2 - (sec * 1000);
            if(log)
                IJ.log(diff + " milliseconds -> " + min + " minutes, " + sec + " secondes and " + mill + " milliseconds");
            else
                System.out.println(diff + " milliseconds -> " + min + " minutes, " + sec + " secondes and " + mill + " milliseconds");
        }
        else if (diff > 1000){
            long sec = diff / 1000;
            long mill = diff - (sec * 1000);
            if(log)
                IJ.log(diff + " milliseconds -> " + sec + " secondes and " + mill + " milliseconds");
            else
                System.out.println(diff + " milliseconds -> " + sec + " secondes and " + mill + " milliseconds");
        }
        else
            if(log)
                IJ.log(diff + " milliseconds");
            else
                System.out.println(diff + " milliseconds");
        time = time2;
    }
}
