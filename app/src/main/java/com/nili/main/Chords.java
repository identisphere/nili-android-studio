package com.nili.main;

import com.nili.globals.Globals;

import java.util.ArrayList;

/**
 * Created by USER on 24/06/2016.
 */
public class Chords {
    private ArrayList<ChordObject> chordList = new ArrayList<ChordObject>();
    private int         currentChordIndex;
    private ChordObject currentChord;
    private int 		positionCount;

    static public class ChordObject {
        public String               positionString = null;
        public ArrayList<Integer>   stringList = new ArrayList<Integer>();
        public int                  index;
    }

    public void addChordToList(ChordObject chord) {
        chordList.add(chord);
    }

    public ChordObject getCurrentChord() {
        return currentChord;
    }

    public void clearList()
    {
        this.chordList.clear();
    }

    public int getListSize()
    {
        return this.chordList.size();
    }

    public int getPositionCoumt()
    {
        return this.positionCount;
    }

    public boolean setCurrentChord(int index)
    {

        if (this.chordList.size() == 0 || index > this.chordList.size())
            return false;

        this.currentChordIndex = index;
        this.currentChord = this.chordList.get(index);
        this.positionCount = 0;

        String positionString = getCurrentChord().positionString;
        for (int i = 0; i < positionString.length(); i++) {
            if (positionString.charAt(i) == '1') this.positionCount++;
        }

        return true;
    }

    public boolean goToNextChord()
    {
        if(this.currentChordIndex == chordList.size()-1) return false;
        this.currentChordIndex++;
        setCurrentChord(this.currentChordIndex);
        return true;
    }

    public boolean goToPreviousChord()
    {
        if(this.currentChordIndex == 0) return false;
        this.currentChordIndex--;
        setCurrentChord(this.currentChordIndex);
        return true;
    }
}
