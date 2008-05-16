/*
 * $Id: TrackControl.java,v 1.69 2007/08/21 11:32:08 jeffmc Exp $
 *
 * Copyright 1997-2004 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package ucar.unidata.idv.control.storm;


import ucar.unidata.data.DataChoice;
import ucar.unidata.data.DataUtil;
import ucar.unidata.data.point.PointOb;
import ucar.unidata.data.point.PointObFactory;
import ucar.unidata.data.storm.*;


import ucar.unidata.idv.ControlContext;
import ucar.unidata.idv.DisplayConventions;
import ucar.unidata.idv.control.DisplayControlImpl;

import ucar.unidata.idv.control.chart.*;




import ucar.unidata.ui.drawing.*;
import ucar.unidata.ui.symbol.*;
import ucar.unidata.util.ColorTable;

import ucar.unidata.util.DateUtil;
import ucar.unidata.util.GuiUtils;

import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.Range;
import ucar.unidata.util.TwoFacedObject;

import ucar.visad.*;

import ucar.visad.Util;
import ucar.visad.display.*;
import ucar.visad.display.*;


import ucar.visad.display.*;
import ucar.visad.display.Animation;
import ucar.visad.display.DisplayableData;
import ucar.visad.display.SelectRangeDisplayable;
import ucar.visad.display.SelectorPoint;
import ucar.visad.display.StationModelDisplayable;
import ucar.visad.display.TrackDisplayable;



import visad.*;

import visad.bom.Radar2DCoordinateSystem;

import visad.georef.EarthLocation;

import visad.georef.EarthLocationLite;

import visad.georef.EarthLocationTuple;
import visad.georef.LatLonPoint;
import visad.georef.LatLonTuple;

import visad.util.DataUtility;

import java.awt.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.*;

import java.beans.*;

import java.rmi.RemoteException;

import java.util.ArrayList;


import java.util.Arrays;


import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.*;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.*;



/**
 *
 * @author Unidata Development Team
 * @version $Revision: 1.69 $
 */

public class StormTrackChart {

    /** _more_ */
    public static final int MODE_FORECASTTIME = 0;

    /** _more_ */
    public static final int MODE_FORECASTHOUR = 1;

    /** _more_ */
    private StormDisplayState stormDisplayState;

    /** _more_ */
    private int mode;

    /** _more_ */
    private TimeSeriesChart timeSeries;

    /** _more_ */
    private JPanel chartLeft;

    /** _more_ */
    private JPanel chartTop;

    /** _more_ */
    private boolean madeChart = false;

    /** _more_ */
    private DateTime forecastTime;

    /** _more_ */
    private int forecastHour = 0;

    /** _more_ */
    private List<StormParam> chartParams = new ArrayList<StormParam>();

    /** _more_ */
    private List<Way> chartWays = new ArrayList();

    /** _more_ */
    private boolean chartDifference = false;


    /** _more_ */
    private JComboBox chartTimeBox;

    /** _more_ */
    private boolean ignoreChartTimeChanges = false;


    /** _more_ */
    private JComponent contents;


    /** _more_ */
    private String name;

    /**
     * _more_
     */
    public StormTrackChart() {}


    /**
     * _more_
     *
     * @param stormDisplayState _more_
     * @param name _more_
     *
     * @throws Exception _more_
     */
    public StormTrackChart(StormDisplayState stormDisplayState, String name) {
        this(stormDisplayState, name, MODE_FORECASTTIME);
    }

    /**
     * _more_
     *
     * @param stormDisplayState _more_
     * @param name _more_
     * @param mode _more_
     */
    public StormTrackChart(StormDisplayState stormDisplayState, String name,
                           int mode) {
        this.stormDisplayState = stormDisplayState;
        this.name              = name;
        this.mode              = mode;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    protected boolean isHourly() {
        return mode == MODE_FORECASTHOUR;
    }

    /**
     * _more_
     */
    public void deactivate() {
        madeChart = false;
        contents  = null;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    protected JComponent getContents() {
        if (contents == null) {
            contents = doMakeContents();
        }
        return contents;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    private JComponent doMakeContents() {
        chartTop  = new JPanel(new BorderLayout());
        chartLeft = new JPanel(new BorderLayout());
        JComponent chartComp = GuiUtils.leftCenter(chartLeft,
                                   GuiUtils.topCenter(chartTop,
                                       getChart().getContents()));


        return chartComp;
    }



    /**
     * Get the chart
     *
     * @return The chart_
     */
    public TimeSeriesChart getChart() {
        if (timeSeries == null) {
            timeSeries =
                new TimeSeriesChart(stormDisplayState.getStormTrackControl(),
                                    "Track Charts") {
                protected void makeInitialChart() {}
            };
            timeSeries.setAlwaysShowFirstChart(false);
            timeSeries.showAnimationTime(true);
            timeSeries.setDateFormat("MM/dd HH:mm z");
        }
        return timeSeries;
    }


    /**
     * _more_
     *
     * @param time _more_
     */
    protected void timeChanged(Real time) {
        getChart().timeChanged();
    }











    /**
     * _more_
     *
     * @return _more_
     */
    private List<DateTime> findForecastTimes() {
        List<DateTime> forecastTimes    = new ArrayList<DateTime>();
        Hashtable      seenForecastTime = new Hashtable();
        for (StormTrack track : stormDisplayState.getTrackCollection()
                .getTracks()) {
            if (track.getWay().isObservation()) {
                continue;
            }
            if ( !chartWays.contains(track.getWay())) {
                continue;
            }
            DateTime dttm = track.getStartTime();
            if (seenForecastTime.get(dttm) == null) {
                seenForecastTime.put(dttm, dttm);
                forecastTimes.add(dttm);
            }
        }
        return (List<DateTime>) Misc.sort(forecastTimes);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    private List<Integer> findForecastHours() {
        List<Integer> forecastHours    = new ArrayList<Integer>();
        Hashtable     seenForecastHour = new Hashtable();
        Hashtable     seen             = new Hashtable();
        for (StormTrack track : stormDisplayState.getTrackCollection()
                .getTracks()) {
            if (track.getWay().isObservation()) {
                continue;
            }
            if ( !chartWays.contains(track.getWay())) {
                continue;
            }
            for (StormTrackPoint stormTrackPoint : track.getTrackPoints()) {
                Integer forecastHour =
                    new Integer(stormTrackPoint.getForecastHour());
                if (seenForecastHour.get(forecastHour) == null) {
                    seenForecastHour.put(forecastHour, forecastHour);
                    forecastHours.add(forecastHour);
                }
            }
        }
        return (List<Integer>) Misc.sort(forecastHours);
    }


    /**
     * _more_
     */
    protected void createChart() {

        if (madeChart) {
            return;
        }
        madeChart = true;
        final JCheckBox chartDiffCbx = new JCheckBox("Use Difference",
                                           chartDifference);
        chartDiffCbx.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                chartDifference = chartDiffCbx.isSelected();
                updateChart();
            }
        });

        chartTimeBox = new JComboBox();
        chartTimeBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (ignoreChartTimeChanges) {
                    return;
                }
                if (isHourly()) {
                    int hour =
                        (Integer) ((TwoFacedObject) chartTimeBox
                            .getSelectedItem()).getId();
                    if (forecastHour != hour) {
                        forecastHour = hour;
                        updateChart();
                    }
                } else {
                    DateTime dateTime =
                        (DateTime) chartTimeBox.getSelectedItem();
                    if ( !Misc.equals(dateTime, forecastTime)) {
                        forecastTime = dateTime;
                        updateChart();
                    }
                }
            }
        });





        //Get the types from the first forecast track
        List<StormParam> params = new ArrayList<StormParam>();
        for (StormTrack track : stormDisplayState.getTrackCollection()
                .getTracks()) {
            if (track == null) {
                continue;
            }
            if ( !track.isObservation()) {
                params = track.getParams();
                break;
            }
        }



        //If we didn't get any from the forecast track use the obs track
        if (params.size() == 0) {
            StormTrack obsTrack =
                stormDisplayState.getTrackCollection().getObsTrack();
            if (obsTrack != null) {
                params = obsTrack.getParams();
            }
        }

        Insets inset      = new Insets(2, 7, 0, 0);
        List   chartComps = new ArrayList();

        List<Way> ways =
            Misc.sort(stormDisplayState.getTrackCollection().getWayList());
        List wayComps = new ArrayList();
        for (Way way : ways) {
            final Way theWay = way;
            if (way.isObservation() && !chartWays.contains(way)) {
                chartWays.add(way);
            }
            final JCheckBox cbx = new JCheckBox(way.toString(),
                                      chartWays.contains(theWay));
            cbx.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    if (cbx.isSelected()) {
                        addChartWay(theWay);
                    } else {
                        removeChartWay(theWay);
                    }
                }
            });
            if ( !way.isObservation()) {
                wayComps.add(cbx);
            } else {
                wayComps.add(0, cbx);
            }
        }

        chartComps.add(GuiUtils.filler(5, 10));
        chartComps.add(
            new JLabel(
                stormDisplayState.getStormTrackControl().getWaysName()
                + ":"));
        JComponent chartWayComp = GuiUtils.vbox(wayComps);
        if (wayComps.size() > 6) {
            chartWayComp = makeScroller(chartWayComp, 100, 150);
        }
        chartComps.add(GuiUtils.inset(chartWayComp, inset));
        chartComps.add(GuiUtils.filler(5, 10));
        chartComps.add(GuiUtils.lLabel((isHourly()
                                        ? "Forecast Hour:"
                                        : "Forecast Time:")));
        chartComps.add(GuiUtils.inset(chartTimeBox, inset));

        List paramComps = new ArrayList();
        for (StormParam param : params) {
            final StormParam theParam      = param;
            boolean          useChartParam = chartParams.contains(theParam);
            final JCheckBox cbx = new JCheckBox(param.toString(),
                                      useChartParam);
            cbx.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    if (cbx.isSelected()) {
                        addChartParam(theParam);
                    } else {
                        removeChartParam(theParam);
                    }
                }
            });
            paramComps.add(cbx);
        }
        chartComps.add(GuiUtils.filler(5, 10));
        chartComps.add(new JLabel("Parameters:"));
        JComponent paramComp = GuiUtils.vbox(paramComps);
        if (paramComps.size() > 6) {
            paramComp = makeScroller(paramComp, 100, 150);
        }
        chartComps.add(GuiUtils.inset(paramComp, inset));
        chartComps.add(chartDiffCbx);

        JButton removeBtn = GuiUtils.makeButton("Remove Chart", this,
                                "removeChart");
        chartComps.add(GuiUtils.filler(5, 10));
        chartComps.add(removeBtn);



        //        JComponent top = GuiUtils.left(GuiUtils.hbox(
        //                                                     GuiUtils.label("Forecast Time: ", chartTimeBox),
        //                                                     chartDiffCbx));
        //        top = GuiUtils.inset(top,5);
        //        chartTop.add(BorderLayout.NORTH, top);

        chartLeft.add(BorderLayout.NORTH,
                      GuiUtils.inset(GuiUtils.vbox(chartComps), 5));
        chartLeft.invalidate();
        chartLeft.validate();
        chartLeft.repaint();

    }

    /**
     * _more_
     */
    public void removeChart() {
        if (GuiUtils.askOkCancel("Remove Chart",
                                 "Do you want to remove this chart?")) {
            stormDisplayState.removeChart(this);

        }

    }

    /**
     * _more_
     *
     * @param comp _more_
     * @param width _more_
     * @param height _more_
     *
     * @return _more_
     */
    JScrollPane makeScroller(JComponent comp, int width, int height) {
        JScrollPane scroller = GuiUtils.makeScrollPane(comp, width, height);
        scroller.setBorder(BorderFactory.createLoweredBevelBorder());
        scroller.setPreferredSize(new Dimension(width, height));
        scroller.setMinimumSize(new Dimension(width, height));
        return scroller;
    }




    /**
     * _more_
     *
     * @param stormTrack _more_
     * @param param _more_
     *
     * @return _more_
     */
    protected LineState makeLine(StormTrack stormTrack, StormParam param) {
        List<Real>            values      = new ArrayList<Real>();
        List<DateTime>        times       = stormTrack.getTrackTimes();
        List<StormTrackPoint> trackPoints = stormTrack.getTrackPoints();
        double                min         = 0;
        double                max         = 0;
        Unit                  unit        = null;
        for (int pointIdx = 0; pointIdx < times.size(); pointIdx++) {
            Real value = trackPoints.get(pointIdx).getAttribute(param);
            if (value == null) {
                continue;
            }
            if (unit == null) {
                unit = ((RealType) value.getType()).getDefaultUnit();
            }
            values.add(value);
            double dvalue = value.getValue();
            //            System.err.print(","+dvalue);
            if ((pointIdx == 0) || (dvalue > max)) {
                max = dvalue;
            }
            if ((pointIdx == 0) || (dvalue < min)) {
                min = dvalue;
            }
        }
        if (values.size() == 0) {
            return null;
        }
        //        System.err.println("");
        String    paramLabel = param.toString();
        String    label = stormTrack.getWay().toString();  // +":" + paramLabel;
        LineState lineState  = new LineState();
        lineState.setRangeIncludesZero(true);
        if (stormTrack.getWay().isObservation()) {
            lineState.setWidth(2);
        } else {
            lineState.setWidth(1);
        }
        lineState.setRange(new Range(min, max));
        lineState.setChartName(paramLabel);
        lineState.setAxisLabel("[" + unit + "]");

        //        System.err.println (param + " " +  StormDataSource.TYPE_STORMCATEGORY);
        if (Misc.equals(param, StormDataSource.PARAM_STORMCATEGORY)) {
            //            lineState.setShape(LineState.LINETYPE_BAR);
            lineState.setLineType(LineState.LINETYPE_BAR);
            lineState.setLineType(LineState.LINETYPE_AREA);
        } else {
            lineState.setLineType(LineState.LINETYPE_SHAPES_AND_LINES);
            lineState.setShape(LineState.SHAPE_LARGEPOINT);
        }

        lineState.setColor(
            stormDisplayState.getWayDisplayState(
                stormTrack.getWay()).getColor());
        lineState.setName(label);
        lineState.setTrack(times, values);
        return lineState;

    }





    /**
     * _more_
     *
     * @param theRealType _more_
     *
     * @param param _more_
     */
    private void removeChartParam(StormParam param) {
        while (chartParams.contains(param)) {
            chartParams.remove(param);
        }
        updateChart();
    }

    /**
     * _more_
     *
     *
     * @param param _more_
     */
    private void addChartParam(StormParam param) {
        if ( !chartParams.contains(param)) {
            chartParams.add(param);
        }
        updateChart();
    }


    /**
     * _more_
     *
     * @param way _more_
     */
    private void removeChartWay(Way way) {
        while (chartWays.contains(way)) {
            chartWays.remove(way);
        }
        updateChart();
    }

    /**
     * _more_
     *
     * @param way _more_
     */
    private void addChartWay(Way way) {
        if ( !chartWays.contains(way)) {
            chartWays.add(way);
        }
        updateChart();
    }



    /**
     * _more_
     */
    protected void updateChart() {

        try {
            if ( !madeChart) {
                createChart();
            }

            ignoreChartTimeChanges = true;
            if (isHourly()) {
                List<Integer>        forecastHours = findForecastHours();
                List<TwoFacedObject> tfos = new ArrayList<TwoFacedObject>();
                for (Integer i : forecastHours) {
                    tfos.add(new TwoFacedObject(i + "H", i));
                }
                GuiUtils.setListData(chartTimeBox, tfos);
                TwoFacedObject selected =
                    TwoFacedObject.findId(new Integer(forecastHour), tfos);
                if ((selected == null) && (tfos.size() > 0)) {
                    selected     = tfos.get(0);
                    forecastHour = ((Integer) selected.getId()).intValue();
                }
                chartTimeBox.setSelectedItem(selected);
            } else {
                List<DateTime> forecastTimes = findForecastTimes();
                if (forecastTimes.size() > 0) {
                    if ((forecastTime == null)
                            || !forecastTimes.contains(forecastTime)) {
                        forecastTime = forecastTimes.get(0);
                    }
                }
                GuiUtils.setListData(chartTimeBox, forecastTimes);
                chartTimeBox.setSelectedItem(forecastTime);
            }
            ignoreChartTimeChanges = false;
            chartTimeBox.repaint();
            Hashtable<Way, List> wayToTracks = new Hashtable<Way, List>();
            for (Way way : chartWays) {
                wayToTracks.put(way, new ArrayList<Way>());
            }

            List<StormTrack> tracksToUse = new ArrayList<StormTrack>();
            for (StormTrack track : stormDisplayState.getTrackCollection()
                    .getTracks()) {
                List trackList = wayToTracks.get(track.getWay());
                if (trackList == null) {
                    continue;
                }
                if (track.getWay().isObservation()) {
                    tracksToUse.add(track);
                } else {
                    trackList.add(track);
                }
            }


            StormTrack obsTrack =
                stormDisplayState.getTrackCollection().getObsTrack();

            if (isHourly()) {
                for (Way way : chartWays) {
                    List<StormTrack> tracksFromWay = wayToTracks.get(way);
                    List<StormTrackPoint> points =
                        new ArrayList<StormTrackPoint>();
                    for (StormTrack track : tracksFromWay) {
                        StormTrackPoint stp =
                            track.findPointWithForecastHour(forecastHour);
                        if (stp != null) {
                            //TODO: What time do we use???
                            points.add(stp);
                        }
                    }
                    if (points.size() > 0) {
                        points = (List<StormTrackPoint>) Misc.sort(points);
                        tracksToUse.add(
                            new StormTrack(
                                stormDisplayState.getStormInfo(), way,
                                points));
                    }
                }

            } else {
                for (Way way : chartWays) {
                    List<StormTrack> tracksFromWay = wayToTracks.get(way);
                    for (StormTrack track : tracksFromWay) {
                        if (Misc.equals(forecastTime, track.getStartTime())) {
                            tracksToUse.add(track);
                        }
                    }
                }
            }

            List<LineState> lines = new ArrayList<LineState>();

            for (StormParam param : chartParams) {
                List<LineState> linesForParam = new ArrayList<LineState>();
                for (StormTrack track : tracksToUse) {
                    LineState lineState = null;
                    if (chartDifference && param.getCanDoDifference()) {
                        if (track.getWay().isObservation()) {
                            continue;
                        }
                        track = StormDataSource.difference(obsTrack, track,
                                param);
                        if (track != null) {
                            lineState = makeLine(track, param);
                        }
                    } else {
                        lineState = makeLine(track, param);
                    }
                    if (lineState == null) {
                        continue;
                    }
                    //Only add it if there are values
                    if (lineState.getRange().getMin()
                            == lineState.getRange().getMin()) {
                        linesForParam.add(lineState);
                    }
                }
                double max = Double.NEGATIVE_INFINITY;
                double min = Double.POSITIVE_INFINITY;;
                for (LineState lineState : linesForParam) {
                    Range r = lineState.getRange();
                    min = Math.min(min, r.getMin());
                    max = Math.max(max, r.getMax());
                }
                //                System.err.println(param + " min/max:" + min + "/" + max);
                boolean first = true;
                for (LineState lineState : linesForParam) {
                    lineState.setAxisVisible(first);
                    first = false;
                    lineState.setRange(new Range(min, max));
                }
                lines.addAll(linesForParam);
            }
            getChart().setTracks(lines);
        } catch (Exception exc) {
            stormDisplayState.getStormTrackControl().logException(
                "Updating chart", exc);
        }

    }








    /**
     * Set the ChartParams property.
     *
     * @param value The new value for ChartParams
     */
    public void setChartParams(List<StormParam> value) {
        chartParams = value;
    }

    /**
     * Get the ChartParams property.
     *
     * @return The ChartParams
     */
    public List<StormParam> getChartParams() {
        return chartParams;
    }

    /**
     * Set the ChartWays property.
     *
     * @param value The new value for ChartWays
     */
    public void setChartWays(List<Way> value) {
        chartWays = value;
    }

    /**
     * Get the ChartWays property.
     *
     * @return The ChartWays
     */
    public List<Way> getChartWays() {
        return chartWays;
    }

    /**
     *  Set the ChartForecastTime property.
     *
     *  @param value The new value for ChartForecastTime
     */
    public void setForecastTime(DateTime value) {
        forecastTime = value;
    }

    /**
     *  Get the ChartForecastTime property.
     *
     *  @return The ChartForecastTime
     */
    public DateTime getForecastTime() {
        return forecastTime;
    }

    /**
     * Set the ChartDifference property.
     *
     * @param value The new value for ChartDifference
     */
    public void setChartDifference(boolean value) {
        chartDifference = value;
    }

    /**
     * Get the ChartDifference property.
     *
     * @return The ChartDifference
     */
    public boolean getChartDifference() {
        return chartDifference;
    }


    /**
     * Set the Name property.
     *
     * @param value The new value for Name
     */
    public void setName(String value) {
        name = value;
    }

    /**
     * Get the Name property.
     *
     * @return The Name
     */
    public String getName() {
        return name;
    }


    /**
     *  Set the StormDisplayState property.
     *
     *  @param value The new value for StormDisplayState
     */
    public void setStormDisplayState(StormDisplayState value) {
        stormDisplayState = value;
    }

    /**
     *  Get the StormDisplayState property.
     *
     *  @return The StormDisplayState
     */
    public StormDisplayState getStormDisplayState() {
        return stormDisplayState;
    }

    /**
     * Set the Mode property.
     *
     * @param value The new value for Mode
     */
    public void setMode(int value) {
        mode = value;
    }

    /**
     * Get the Mode property.
     *
     * @return The Mode
     */
    public int getMode() {
        return mode;
    }


    /**
     * Set the ForecastHour property.
     *
     * @param value The new value for ForecastHour
     */
    public void setForecastHour(int value) {
        forecastHour = value;
    }

    /**
     * Get the ForecastHour property.
     *
     * @return The ForecastHour
     */
    public int getForecastHour() {
        return forecastHour;
    }





}

