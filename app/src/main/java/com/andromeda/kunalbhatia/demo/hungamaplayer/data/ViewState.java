package com.andromeda.kunalbhatia.demo.hungamaplayer.data;

import android.graphics.Matrix;

public class ViewState {
    private Matrix matrix;
    private float[] suppMatrixValues;

    public ViewState(Matrix matrix, float[] suppMatrixValues) {
        this.matrix = matrix;
        this.suppMatrixValues = suppMatrixValues;
    }

    public Matrix getMatrix() {
        return matrix;
    }

    public float[] getSuppMatrixValues() {
        return suppMatrixValues;
    }
}
