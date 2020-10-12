package com.google.gwt.sample.stockwatcher.client;

import com.google.gwt.sample.stockwatcher.shared.FieldVerifier;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyEvent;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class StockWatcher implements EntryPoint {
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network " + "connection and try again.";

	/**
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */
	private final GreetingServiceAsync greetingService = GWT.create(GreetingService.class);

	/**
	 * This is the entry point method.
	 */
	private static final int REFRESH_INTERVAL = 5000; // ms
	private VerticalPanel mainPanel = new VerticalPanel();
	private FlexTable stocksFlexTable = new FlexTable();
	private HorizontalPanel addPanel = new HorizontalPanel();
	private TextBox newSymbolTextBox = new TextBox();
	private Button addStockButton = new Button("Add");
	private Label lastUpdatedLabel = new Label();
	private ArrayList<String> stocks = new ArrayList<>();

		  /**
		   * Entry point method.
		   */
	public void onModuleLoad() {
		    // Create table for stock data.
		stocksFlexTable.setText(0, 0, "Symbol");
		stocksFlexTable.setText(0, 1, "Price");
		stocksFlexTable.setText(0, 2, "Change");
		stocksFlexTable.setText(0, 3, "Remove");

		    // Assemble Add Stock panel.
		addPanel.add(newSymbolTextBox);
		addPanel.add(addStockButton);

		    // Assemble Main panel.
		mainPanel.add(stocksFlexTable);
		mainPanel.add(addPanel);
		mainPanel.add(lastUpdatedLabel);

		    // Associate the Main panel with the HTML host page.
		RootPanel.get("stockList").add(mainPanel);

		    // Move cursor focus to the input box.
		newSymbolTextBox.setFocus(true);
		newSymbolTextBox.addKeyPressHandler(new KeyPressHandler() {
			
			@Override
			public void onKeyPress(KeyPressEvent event) {
				// TODO Auto-generated method stub
				if (event.getCharCode() == KeyCodes.KEY_ENTER) {
					addStock();
				}
				
			}
		});
		Timer timer = new Timer() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				refreshWatchList();
			}	
		};
		timer.scheduleRepeating(REFRESH_INTERVAL);
		
		//events
		addStockButton.addClickHandler(new ClickHandler(){			
			@Override
			public void onClick(ClickEvent event) {
				// TODO Auto-generated method stub
				addStock();
			}
		});
	}
	
	private void addStock() {
		final String symbol = newSymbolTextBox.getText().toUpperCase().trim();
		newSymbolTextBox.setFocus(true);
		if (!symbol.matches("^[0-9A-Z\\.]{1,10}$")) {
			Window.alert("'" + symbol + "' is not a valid symbol.");
			newSymbolTextBox.selectAll();
			return;
		}
		// TODO Don't add the stock if it's already in the table.
		if (stocks.contains(symbol)) {
			Window.alert("'" + symbol + "' already added to the list");
			newSymbolTextBox.selectAll();
			return;
		}
	    // TODO Add the stock to the table
		int row = stocksFlexTable.getRowCount();
		stocks.add(symbol);
		stocksFlexTable.setText(row, 0, symbol);
		stocksFlexTable.setWidget(row, 2, new Label());
		Window.alert("Add successfully");
		// TODO Add a button to remove this stock from the table.
		Button removeButton = new Button("Remove");
		removeButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				// TODO Auto-generated method stub
				int removedIndex = stocks.indexOf(symbol);
				stocks.remove(removedIndex);
				stocksFlexTable.removeRow(row);
			}
		});
		removeButton.getElement().setId("removeButton");
		stocksFlexTable.setWidget(row, 3, removeButton);
		stocksFlexTable.getRowFormatter().addStyleName(0, "watchListHeader");
		stocksFlexTable.addStyleName("watchList");
	    // TODO Get the stock price.
		refreshWatchList();
	}
	
	private void refreshWatchList() {
		final double MAX_PRICE = 100.0;
		final double MAX_PRICE_CHANGE = 0.02;
		StockPrice[] prices = new StockPrice[stocks.size()];
		for (int i=0; i < stocks.size(); i++) {
			double price = Random.nextDouble() * MAX_PRICE;
			double chance = price * MAX_PRICE_CHANGE * (Random.nextDouble() * 2.0 - 1.0);
			prices[i] = new StockPrice(stocks.get(i), price, chance);
		}
		updateTable(prices);
	}
	
	private void updateTable(StockPrice[] prices) {
		  // TODO Auto-generated method stub
		for (int i=0; i < prices.length; i++) {
			updateTable(prices[i]);
		}
	}

	private void updateTable(StockPrice stockPrice) {
		// TODO Auto-generated method stub
		if (!stocks.contains(stockPrice.getSymbol())) {
			return;
		}
		int row = stocks.indexOf(stockPrice.getSymbol()) + 1;
		String priceText = NumberFormat.getFormat("#,##0.00").format(stockPrice.getPrice());
		NumberFormat changeFormat = NumberFormat.getFormat("+#,##0.00; -#,##0.00");
		String changePercentText = changeFormat.format(stockPrice.getChangePercent());
		stocksFlexTable.setText(row, 1, priceText);
		Label changeWidget = (Label) stocksFlexTable.getWidget(row, 2);
		changeWidget.setText(stockPrice.getChange() + " (" + changePercentText+ " %)");
		String changeStyleName = "noChange";
		if (stockPrice.getChangePercent() < -0.0f) {
			changeStyleName = "negativeChange";
		} else if (stockPrice.getChangePercent() > 0.0f) {
			changeStyleName = "positiveChange";
		}
		changeWidget.setStyleName(changeStyleName);
		//update DateTime
		DateTimeFormat dateFormat = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM);
		lastUpdatedLabel.setText("Last update : " + dateFormat.format(new Date()));
	}
}