package it.michelelacorte.androidshortcuts.util;

import android.util.Log;

/**
 * Created by Michele on 09/12/2016.
 */

public class GridSize{

    private static final String TAG = "GridSize";

    private int nColumn;
    private int nRow;

    /**
     * GridSize public constructor
     * @param nColumn int
     * @param nRow int
     */
    public GridSize(int nColumn, int nRow){
        this.nColumn = nColumn;
        this.nRow = nRow;
        Log.d(TAG, "GridSize created!");
    }

    /**
     * Get number of column
     * @return int
     */
    public int getColumnCount() {
        return nColumn;
    }

    /**
     * Get number of row
     * @return int
     */
    public int getRowCount() {
        return nRow;
    }
}
