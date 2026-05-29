package com.elfmcys.yesstevemodel.resource.models;

public class GeometryDescription {
    private final String identifier;
    private final double textureWidth;
    private final double textureHeight;
    private final double visibleBoundsWidth;
    private final double visibleBoundsHeight;
    private final double[] visibleBoundsOffset;

    public GeometryDescription(String identifier, double textureWidth, double textureHeight, double visibleBoundsWidth, double visibleBoundsHeight, double[] visibleBoundsOffset) {
        this.identifier = identifier;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.visibleBoundsWidth = visibleBoundsWidth;
        this.visibleBoundsHeight = visibleBoundsHeight;
        this.visibleBoundsOffset = visibleBoundsOffset;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public Double getTextureWidth() {
        return this.textureWidth;
    }

    public Double getTextureHeight() {
        return this.textureHeight;
    }

    public Double getVisibleBoundsWidth() {
        return this.visibleBoundsWidth;
    }

    public Double getVisibleBoundsHeight() {
        return this.visibleBoundsHeight;
    }

    public double[] getVisibleBoundsOffset() {
        return this.visibleBoundsOffset;
    }
}