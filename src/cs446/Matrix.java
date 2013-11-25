package cs446;

import java.util.HashMap;

// 
public class Matrix {
	private Double[][] entries;
	private int rows;
	private int cols;
	
	public Double get(int i, int j){
		return entries[i][j];
	}
	public void put(Double entry, int i, int j)
	{
		entries[i][j]=entry;
	}

	public Matrix(Double[][] data) {
        rows = data.length;
        cols = data[0].length;
        this.entries = new Double[rows][cols];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                    this.entries[i][j] = data[i][j];
    }

    public Matrix(int cols, int rows) {
	}
    public Matrix transpose() {
        Matrix A = new Matrix(cols, rows);
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                A.entries[j][i] = this.entries[i][j];
        return A;
    }

}
