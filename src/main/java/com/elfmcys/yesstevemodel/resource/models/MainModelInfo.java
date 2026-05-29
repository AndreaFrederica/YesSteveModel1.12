package com.elfmcys.yesstevemodel.resource.models;

public class MainModelInfo {
    private final int bones;
    private final int cubes;
    private final int faces;

    public MainModelInfo(int bones, int cubes, int faces) {
        this.bones = bones;
        this.cubes = cubes;
        this.faces = faces;
    }

    public int getBones() {
        return this.bones;
    }

    public int getCubes() {
        return this.cubes;
    }

    public int getFaces() {
        return this.faces;
    }
}