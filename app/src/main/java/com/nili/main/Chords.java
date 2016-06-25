package com.nili.main;

import com.nili.globals.Globals;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by USER on 24/06/2016.
 */
public class Chords {
    private ArrayList<ChordObject> chordList = new ArrayList<ChordObject>();
    private int         currentChordIndex;
    private ChordObject currentChord;
    private ChordObject nextChord;
    private boolean lastChord;

    public boolean isLastChord()
    {
        return currentChordIndex==chordList.size()-1;
    }

    static public class ChordObject {
        public String               positionString = null;
        public ArrayList<Integer>   stringList = new ArrayList<Integer>();
        public int                  index;
        public int                  positionCount = 0;

    }

    public void addChordToList(String jsonChordString) throws Exception
    {
        ChordObject chord = createChordFromJson(new JSONObject(jsonChordString), chordList.size());
        chordList.add(chord);
    }

    public ChordObject getCurrentChord() {
        return currentChord;
    }

    public ChordObject getNextChord()
    {
        return nextChord;
    }

    public void clearList()
    {
        this.chordList.clear();
    }

    public int getListSize()
    {
        return this.chordList.size();
    }

    public boolean isChordString(ChordObject chord)
    {
        if(chord.stringList.size()>0) return true;
        else return false;
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

    public void setToFirstChord()
    {
        setCurrentChord(0);
    }

    private int getPositionCount(String positionString)
    {
        int positionCount = 0;
        for(int i=0; i<positionString.length(); i++)
            if(positionString.charAt(i)=='1') positionCount++;
        return positionCount;
    }

    private boolean setCurrentChord(int index)
    {
        if (this.chordList.size() == 0 || index > this.chordList.size())
            return false;

        this.currentChordIndex = index;
        this.currentChord = this.chordList.get(index);

        String positionString = getCurrentChord().positionString;

        if(index<this.chordList.size()-1)
            nextChord = chordList.get(index+1);

        return true;
    }

    private ChordObject createChordFromJson(JSONObject jsonChord, int index) throws Exception
    {
        Chords.ChordObject createdChord = new Chords.ChordObject();

        createdChord.positionString = jsonChord.getString("positionString");

        JSONArray stringListJson = jsonChord.getJSONArray("emptyStringList");
        for(int i=0; i<stringListJson.length(); i++)
            createdChord.stringList.add(Integer.parseInt(stringListJson.get(i).toString()));

        createdChord.index = index;

        createdChord.positionCount = getPositionCount(createdChord.positionString);

        return createdChord;
    }
}
