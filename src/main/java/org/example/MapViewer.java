/*
 *   Author: Quốc Duy
 *   Created: 25/7/2024
 *   Inspire: https://github.com/msteiger/jxmapviewer2
 * */
package org.example;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.Waypoint;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapViewer {
    public static void displayMap(List<double[]> coords, List<String> names, int[] bestSolution) {
        List<GeoPosition> geoPositions = convertToGeoPositions(coords, bestSolution);

        JXMapViewer mapViewer = new JXMapViewer();

        // Display the viewer in a JFrame
        JFrame frame = new JFrame("Map Viewer");
        frame.getContentPane().add(mapViewer);
        frame.setSize(800, 600);
        frame.setVisible(true);

        // Create a TileFactoryInfo for OpenStreetMap
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        mapViewer.setTileFactory(tileFactory);

        // Create a track from the geo-positions
        RoutePainter routePainter = new RoutePainter(geoPositions);

        // Set the focus
        mapViewer.zoomToBestFit(new HashSet<>(geoPositions), 0.7);

        // Create waypoints with different colors for the first and last markers
        Set<Waypoint> waypoints = new HashSet<>();
        for (int i = 0; i < geoPositions.size(); i++) {
            GeoPosition gp = geoPositions.get(i);
            CustomWaypoint waypoint;
            if (i == 0) {
                waypoint = new CustomWaypoint(gp, Color.RED); // Start marker
            } else if (i == geoPositions.size() - 2) {
                waypoint = new CustomWaypoint(gp, Color.YELLOW); // Return to start marker
            } else {
                waypoint = new CustomWaypoint(gp, Color.BLUE); // Intermediate markers
            }
            waypoints.add(waypoint);
        }

        // Create a waypoint painter that takes all the waypoints
        CustomWaypointPainter waypointPainter = new CustomWaypointPainter();
        waypointPainter.setWaypoints(waypoints);

        // Create a compound painter that uses both the route-painter and the waypoint-painter
        List<Painter<JXMapViewer>> painters = new ArrayList<>();
        painters.add(routePainter);
        painters.add(waypointPainter);

        CompoundPainter<JXMapViewer> compoundPainter = new CompoundPainter<>(painters);
        mapViewer.setOverlayPainter(compoundPainter);
    }

    private static List<GeoPosition> convertToGeoPositions(List<double[]> coords, int[] bestSolution) {
        List<GeoPosition> geoPositions = new ArrayList<>();
        for (int index : bestSolution) {
            double[] coord = coords.get(index);
            geoPositions.add(new GeoPosition(coord[0], coord[1]));
        }
        // Add the start point again at the end to close the loop for TSP
        if (bestSolution.length > 0) {
            double[] startCoord = coords.get(bestSolution[0]);
            geoPositions.add(new GeoPosition(startCoord[0], startCoord[1]));
        }
        return geoPositions;
    }
}
