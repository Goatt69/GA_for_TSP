package org.example;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointRenderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.Set;

public class CustomWaypointPainter extends WaypointPainter<Waypoint> {
    private Set<? extends Waypoint> waypoints;

    public void setWaypoints(Set<? extends Waypoint> waypoints) {
        this.waypoints = waypoints;
    }

    @Override
    protected void doPaint(Graphics2D g, JXMapViewer map, int width, int height) {
        if (waypoints == null) return;

        for (Waypoint w : waypoints) {
            paintWaypoint(g, map, w);
        }
    }

    private void paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint w) {
        Color color = Color.BLACK; // Default color

        if (w instanceof CustomWaypoint) {
            color = ((CustomWaypoint) w).getColor();
        }

        g.setColor(color);
        Point2D point = map.getTileFactory().geoToPixel(w.getPosition(), map.getZoom());
        Rectangle rect = map.getViewportBounds();
        int x = (int) point.getX() - rect.x;
        int y = (int) point.getY() - rect.y;

        // Draw a small circle for the waypoint
        g.fillOval(x - 5, y - 5, 10, 10);
    }
}
