package stock_exchange;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import org.json.*;

public class get_stock_data {
	
	String start_date;
	String end_date;
	int length_trainingset;
	int max_stock_price;
	int num_trainingsets;
	double[][] training_set_array;
	
	public get_stock_data(String symbol, String start_date_input, String end_date_input, int length_training_input, int max_stock_price_input) throws Exception{
		start_date = start_date_input;
		end_date = end_date_input;
		length_trainingset = length_training_input;
		max_stock_price = max_stock_price_input;
		
		String url = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.historicaldata%20where%20symbol%20%3D%20%22"+symbol+"%22%20and%20startDate%20%3D%20%22"+start_date+"%22%20and%20endDate%20%3D%20%22"+end_date+"%22&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		//add request header
		String USER_AGENT = "Mozilla/5.0";
		con.setRequestProperty("User-Agent", USER_AGENT);
		
		// get repsonse code
		int responseCode = con.getResponseCode();
		//System.out.println("\nSending 'GET' request to URL : " + url);
		
		
		// get the response and transform to something usefull
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		JSONObject json = new JSONObject(response.toString());
		
		// Total results found and total training sets created from it.
		int num_results = json.getJSONObject("query").getJSONObject("results").getJSONArray("quote").length();
		num_trainingsets = (int) ( num_results - length_trainingset );
		
		//array with all training sets. Per result we take 4 values that we use.
		training_set_array = new double[num_trainingsets][length_trainingset*4];
		
		//The last few result that doesnt fit in a trainingset are ignored.
		//NOTE: first result is the newest result. aka .get(0) is has the newest date.
		for (int j = 0; j < num_trainingsets; j++) {
			for (int i = 0; i < length_trainingset; i++) {
				JSONObject temp_object = (JSONObject) json.getJSONObject("query").getJSONObject("results").getJSONArray("quote").get(j+i);
				// encog nearal network only takes num between 1 and 0 so /max_stock_price
				training_set_array[j][i*4]		= temp_object.getDouble("High")	/ max_stock_price;
				training_set_array[j][i*4 + 1]	= temp_object.getDouble("Low")	/ max_stock_price;
				training_set_array[j][i*4 + 2]	= temp_object.getDouble("Close")/ max_stock_price;
				training_set_array[j][i*4 + 3]	= temp_object.getDouble("Open")	/ max_stock_price;
			}
		}
		
		
		//Print out stats
		System.out.println("***Get stock data stats***");
		System.out.println("Response Code : " + responseCode);
		System.out.println("Number of results found: " + num_results);
		JSONObject last_object = (JSONObject) json.getJSONObject("query").getJSONObject("results").getJSONArray("quote").get(num_results-1);
		JSONObject first_object = (JSONObject) json.getJSONObject("query").getJSONObject("results").getJSONArray("quote").get(0);
		System.out.println("data: from " + last_object.getString("Date")+ " to " + first_object.getString("Date"));
		
		
	}
	
	double[] get_trainingset(int trainingset_select){	
		double[] temp_array = training_set_array[trainingset_select];
		double[] return_array = new double[temp_array.length-4];
		for (int i = 0; i < return_array.length; i++) {
			return_array[i] = temp_array[i+4];
		}
		return return_array;
	}
	
	double[] get_ideal_trainingset(int trainingset_select){
		double[] return_array = new double[2];
		return_array[0] = training_set_array[trainingset_select][2];		//close
		return_array[1] = training_set_array[trainingset_select][3];		//open
		return return_array;
	}
	
	int get_num_trainingsets(){
		return num_trainingsets;
	}
	
	double[] get_test_stock_values(){
		double[] temp_array = training_set_array[0];
		double[] return_array = new double[temp_array.length-4];
		for (int i = 0; i < return_array.length; i++) {
			return_array[i] = temp_array[i+4];
		}
		
		return return_array;
	}
	
	double[] get_latest_stock_values(){
		double[] temp_array = training_set_array[0];
		double[] return_array = new double[temp_array.length-4];
		for (int i = 0; i < return_array.length; i++) {
			return_array[i] = temp_array[i];
		}
		
		return return_array;
		
	}
	
}
