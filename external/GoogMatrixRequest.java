package external;

import java.io.IOException;

import com.google.maps.DirectionsApi.RouteRestriction;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.TravelMode;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class GoogMatrixRequest {
  //Earth's mean radius in meter
  private static final int R = 6378137;
  private static final String API_KEY = "AIzaSyC91YIc2UnTLDQ8FX3sfgHce1lr1AWmyjY";
  //set up key
  private static final GeoApiContext distCalcer = new GeoApiContext.Builder()
		  .apiKey(API_KEY)
		  .build();
  
  private OkHttpClient client;
//  private String addrOne;
//  private String addrTwo;
//  private double weight;
//  private boolean mode;
  
//  public GoogMatrixRequest(String addrOne, String addrTwo, double weight, boolean mode) {
//	   this.client = new OkHttpClient();
//	   this.addrOne = addrOne;
//	   this.addrTwo = addrTwo;
//	   this.weight = weight;
//	   this.mode = mode;
//  }

  public String run(String url) throws IOException {
	  Request request = new Request.Builder()
			  .url(url)
			  .build();
	  okhttp3.Response response = client.newCall(request).execute();
	  // response.body().rows[0].elements[0].distance.value
	  System.out.println(response);
	  return response.body().string();
  }
  
  public static double getBicyclingDistance(String addrOne, String addrTwo) throws ApiException, InterruptedException, IOException{
	   	
	  DistanceMatrixApiRequest req = DistanceMatrixApi.newRequest(distCalcer); 
	  TravelMode travel;
	  
	  travel = TravelMode.BICYCLING;
	  
	  DistanceMatrix result = req.origins(addrOne)
			  .destinations(addrTwo)
	          .mode(travel)
	          .avoid(RouteRestriction.TOLLS)
	          .language("en-US")
	          .await();
	  double distApart = result.rows[0].elements[0].distance.inMeters * 0.000621371192;
	  return distApart;
  }
  
  public static double getDirectDistance (String addrOne, String addrTwo) throws ApiException, InterruptedException, IOException {
	  
	  GeocodingResult[] resultOrigin = GeocodingApi.geocode(distCalcer,"1600 Amphitheatre Parkway Mountain View, CA 94043").await();
	  GeocodingResult[] resultDestination = GeocodingApi.geocode(distCalcer,"164 Jefferson Dr, Menlo Park, CA 94025").await();
	  double lat1 = resultOrigin[0].geometry.location.lat;
	  double lng1 = resultOrigin[0].geometry.location.lng;
	  double lat2 = resultDestination[0].geometry.location.lat;
	  double lng2 = resultDestination[0].geometry.location.lng;
	  
	  double dlng = Radians(lng2 - lng1);
	  double dlat = Radians(lat2 - lat1);
	  
	  double a = (Math.sin(dlat / 2) * Math.sin(dlat / 2)) + Math.cos(Radians(lat1)) * Math.cos(Radians(lat2)) * (Math.sin(dlng / 2) * Math.sin(dlng / 2));
      double angle = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
      //return the distance in miles
      return R * angle * 0.000621371192;
  }
  
  private static double Radians (double x) {
	  return x * Math.PI / 180;
  }

  public static double[][] getDistance(String addrOne, String addrTwo, double weight) throws IOException, ApiException, InterruptedException {
	  // method 1 drone
	  double[][] result = new double[2][2];
	  double droneDistance = getDirectDistance(addrOne, addrTwo);
	  result[0][0] = droneDistance / 50;
	  result[0][1] = calculatePrice(weight, droneDistance, true);  
	  // method 2 robot
	  double robotDistance = getBicyclingDistance(addrOne, addrTwo);
	  result[1][0] = robotDistance / 10;
	  result[1][1] = calculatePrice(weight, robotDistance, false);
	  
//	  GoogMatrixRequest request = new GoogMatrixRequest();
//	  String url_request = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=Seattle&destinations=San+Francisco&mode=bicycling&language=en-GB&key=" + API_KEY;
//	  String response = request.run(url_request);
//	  long robotDistance = response.rows[0].elements[0].distance.value;
//	  System.out.println(calculatePrice(weight, robotDistance, false));
	  return result;
  }
  
  // price = base fare (surcharge) + distance/speed as constant * price per minute + weight * price per lb
  public static double calculatePrice(double weight, double distance, boolean mode) {
	  // from, to, weight, mode
	  double cost = 0.0;
	  
	  // query db to get data, assume we already have base price, additional rate
	  if (mode == true) {
		  // method 1 drone
		  cost = 3.99 + distance * weight * 0.4;
	  } else {
		  // method 2 robot
		  cost = 1.99 + distance * weight * 0.2;
	  }	  
	  if (mode) {
		  System.out.println("Vehicle: Drone; The shipping cost: " + cost + "; The distance: " + distance);
	  } else {
		  System.out.println("Vehicle: Robot; The shipping cost: " + cost + "; The distance: " + distance);
	  }
	  
	  return cost;
  }
}