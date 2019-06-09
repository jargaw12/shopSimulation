package msk.gui;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import msk.shop.ShopFederate;

import java.util.stream.IntStream;

public class GuiController {
    private int MAX_DATA_POINTS = 30;

    @FXML
    private LineChart<Number, Number> chartCostumerCount;
    @FXML
    private LineChart<Number, Number> chartAvgTime;
    @FXML
    private LineChart<Number, Number> chartAvgQueueLength;


    @FXML
    public void initialize() {
        initChart(chartCostumerCount);
        initChart(chartAvgQueueLength);
        initChart(chartAvgTime);
    }

    private void initChart(LineChart<Number, Number> lineChart) {
        lineChart.getXAxis().setLabel("Time");
        lineChart.getYAxis().setLabel("Value");

        IntStream.rangeClosed(1, ShopFederate.DEFAULT_CASH_REGISTER_COUNT)
                .forEach(cashRegisterId -> addNewSeries(lineChart));
    }


    public void chaneServedCustomersState(int series, double time, int value) {
        chaneChartState(chartCostumerCount, series, time, value);
    }

    public void chaneAvgQueueLengthState(int series, double time, double value) {
        chaneChartState(chartAvgQueueLength, series, time, value);
    }

    public void chaneAvgWaitingTimeState(int series, double time, double value) {
        chaneChartState(chartAvgTime, series, time, value);
    }

    private void chaneChartState(LineChart<Number, Number> chart, int series, double time, double value) {
        Platform.runLater(() -> {
            String chartName = chart.getClass().getSimpleName();
            System.out.println("chart = " + chartName + ",  series = " + series + ",  [" + time + "; " + value + "];  ");

            if (chart.getData().size() == series) {
                System.out.println(" -> chartsSize " + chart.getData().size());
                System.out.println(" -> new series -> for cash " + series);
                addNewSeries(chart);
            }

            ObservableList<XYChart.Data<Number, Number>> seriesData = chart.getData().get(series).getData();
//            if (seriesData.size() > MAX_DATA_POINTS) {
//                seriesData.remove(0);
//            }
            Platform.runLater(() -> seriesData.add(new XYChart.Data<>(time, value)));
        });
    }

    private void addNewSeries(LineChart<Number, Number> chart) {
        System.out.println("addNewSeries()");
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Kasa " + (chart.getData().size() + 1));
        chart.getData().add(series);
    }
}
