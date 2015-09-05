package com.example.fleps.trackmap;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.FloatMath;

import com.example.fleps.trackmap.entity.TrackPoint;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ArrayList<TrackPoint> trackPoints;
    private ArrayList<LatLng> allPointsLatLong;
    private ArrayList<LatLng> reallyAllPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        trackPoints = new ArrayList<>();
        allPointsLatLong = new ArrayList<>();
        reallyAllPoints = new ArrayList<>();
        LoadThread loadThread = new LoadThread();
        loadThread.start();
        setUpMapIfNeeded();

    }

    public class LoadThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                InputStream inputStream = getAssets().open(getIntent().getStringExtra("filename"));
                decodeGPX(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(allPointsLatLong.get(allPointsLatLong.size() - 1),
                    //       13));
                    sort();
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(allPointsLatLong.get(allPointsLatLong.size() - 1),
                            13));
                    setUpMap();
                }
            });

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void decodeGPX(InputStream fileInputStream) {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(fileInputStream);
            Element elementRoot = document.getDocumentElement();

            NodeList noteListTrkpt = elementRoot.getElementsByTagName("trkpt");
            NodeList nodelistExtra = elementRoot.getElementsByTagName("extraData");

            for (int i = 0; i < noteListTrkpt.getLength(); i++) {

                Node node = noteListTrkpt.item(i);
                Node noteExtra = nodelistExtra.item(i);

                NamedNodeMap attributes = node.getAttributes();
                NamedNodeMap attributesExtra = noteExtra.getAttributes();

                Double latitude = Double.parseDouble(attributes.getNamedItem("lat").getTextContent());
                Double longitude = Double.parseDouble(attributes.getNamedItem("lon").getTextContent());
                Double azimuth = Double.parseDouble(attributesExtra.getNamedItem("azimuth").getTextContent());
                Double speedInKnots = Double.parseDouble(attributesExtra.getNamedItem("speedInKnots").getTextContent());

                NodeList childNodes = node.getChildNodes();
                String newTime = searchNodeListForTaget(childNodes, "time");

                TrackPoint newGpxNode = new TrackPoint(latitude, longitude, speedInKnots, azimuth, newTime);
                allPointsLatLong.add(new LatLng(latitude, longitude));
                reallyAllPoints.add(new LatLng(latitude, longitude));
                if (trackPoints.size() > 0) {
                    if (Math.abs(azimuth - trackPoints.get(trackPoints.size() - 1).getAzimuth()) > 1
                            && speedInKnots > 3
                            ) {
                        // if((int) newGpxNode.getAzimuth() != (int) trackPoints.get(trackPoints.size() - 1).getAzimuth()) {
                        trackPoints.add(newGpxNode);
                    }
                } else {
                    trackPoints.add(newGpxNode);
                }
            }

            fileInputStream.close();

        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    private double gps2m(double lat_a, double lng_a, double lat_b, double lng_b) {
        float pk = (float) (180 / 3.14169);

        double a1 = lat_a / pk;
        double a2 = lng_a / pk;
        double b1 = lat_b / pk;
        double b2 = lng_b / pk;

        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) *   Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);
        double dist = 6366000 * tt;
        return dist;
    }

    private LatLng average(int i, int step) {
        double averageLat = 0, averageLong = 0;
        for (int j = 0; j < step; j++) {
            averageLat += allPointsLatLong.get(j + i).latitude;
            averageLong += allPointsLatLong.get(j + i).longitude;
        }
        return new LatLng(averageLat/step, averageLong/step);
    }

    public void sort() {
        int niceDistance = 30;
        int step = 50;
        for (int i = 0; i < allPointsLatLong.size() - step; ) {
            LatLng average = average(i, step);
            if (
                    gps2m(allPointsLatLong.get(i).latitude, allPointsLatLong.get(i).longitude, average.latitude, average.longitude) < niceDistance
                    && gps2m(allPointsLatLong.get(i + (step/4)).latitude, allPointsLatLong.get(i + (step/4)).longitude, average.latitude, average.longitude) < niceDistance
                    && gps2m(allPointsLatLong.get(i + (step/2)).latitude, allPointsLatLong.get(i + (step/2)).longitude, average.latitude, average.longitude) < niceDistance
                    && gps2m(allPointsLatLong.get(i + (3*step/4)).latitude, allPointsLatLong.get(i + (3*step/4)).longitude, average.latitude, average.longitude) < niceDistance
                    && gps2m(allPointsLatLong.get(i + step).latitude, allPointsLatLong.get(i + step).longitude, average.latitude, average.longitude) < niceDistance
            ) {
                for (int j = 1; j < step; j++) {
                    allPointsLatLong.remove(i+1);
                    continue;
                }
            }
            i++;
        }
        System.out.println(123);
    }

    private String searchNodeListForTaget(NodeList src, String target) {
        for (int i = 0; i < src.getLength(); i++) {
            Node n = src.item(i);
            if (n.getNodeName().equals(target)) {
                return n.getFirstChild().getNodeValue();
            }
        }
        return "";
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        /*for (TrackPoint trackPoint : trackPoints) {
            mMap.addMarker(new MarkerOptions().snippet("speed: " + trackPoint.getSpeed() + "azimuth: " + trackPoint.getAzimuth()).position(new LatLng(trackPoint.getLatitude(), trackPoint.getLongitude())).title(String.valueOf(trackPoint.getTime())));
        }*/
        if (trackPoints.size() > 0) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(trackPoints.get(0).getLatitude(), trackPoints.get(0).getLongitude())).title("Started " + String.valueOf(trackPoints.get(0).getTime())));
            mMap.addMarker(new MarkerOptions().position(new LatLng(trackPoints.get(trackPoints.size() - 1).getLatitude(), trackPoints.get(trackPoints.size() - 1).getLongitude())).title("Finished " + String.valueOf(trackPoints.get(trackPoints.size() - 1).getTime())));
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.color(Color.GREEN);
            polylineOptions.width(3);
            polylineOptions.addAll(allPointsLatLong);

            PolylineOptions polylineOptions2 = new PolylineOptions();
            polylineOptions2.color(Color.RED);
            polylineOptions2.width(10);
            polylineOptions2.addAll(reallyAllPoints);
            mMap.addPolyline(polylineOptions2);
            mMap.addPolyline(polylineOptions);
        }
    }


}
