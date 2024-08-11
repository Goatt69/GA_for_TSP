package org.example;

import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.Color;

public class CustomWaypoint extends DefaultWaypoint {
    private final Color color;

    public CustomWaypoint(GeoPosition position, Color color) {
        super(position);
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}