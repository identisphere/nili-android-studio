package com.nili.operator;

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

    static public class ChordObject {
        public String               positionString = null;
        public ArrayList<Integer> emptyStringList = new ArrayList<Integer>();
        public int                  index;
        public int                  positionCount = 0;
        int                         topString = -1;

    }

    public void addChordToList(String jsonChordString) throws Exception
    {
        ChordObject chord = createChordFromJson(new JSONObject(jsonChordString), chordList.size());
        chordList.add(chord);
    }

    public ChordObject current() {
        return currentChord;
    }

    public ChordObject next()
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

    public boolean isChordEmptyString(ChordObject chord)
    {
        if(chord.emptyStringList.size()>0) return true;
        else return false;
    }

    public boolean goToNextChord()
    {
        if(this.currentChordIndex == chordList.size()-1) return false;
        this.currentChordIndex++;
        setCurrent(this.currentChordIndex);
        return true;
    }

    public boolean goToPreviousChord()
    {
        if(this.currentChordIndex == 0) return false;
        this.currentChordIndex--;
        setCurrent(this.currentChordIndex);
        return true;
    }

    public void setToFirstChord()
    {
        setCurrent(0);
    }

    private int getPositionCount(String positionString)
    {
        int positionCount = 0;
        for(int i=0; i<positionString.length(); i++)
            if(positionString.charAt(i)=='1') positionCount++;
        return positionCount;
    }

    private boolean setCurrent(int index)
    {
        if (this.chordList.size() == 0 || index > this.chordList.size())
            return false;

        this.currentChordIndex = index;
        this.currentChord = this.chordList.get(index);

        String positionString = current().positionString;

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
            createdChord.emptyStringList.add(Integer.parseInt(stringListJson.get(i).toString()));

       createdChord.topString = Integer.parseInt(jsonChord.getString("topString"));

        createdChord.index = index;

        createdChord.positionCount = getPositionCount(createdChord.positionString);

        return createdChord;
    }
}
