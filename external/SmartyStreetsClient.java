package external;

import com.smartystreets.api.StaticCredentials;
import com.smartystreets.api.SharedCredentials;
import com.smartystreets.api.exceptions.BatchFullException;
import com.smartystreets.api.exceptions.SmartyException;
import com.smartystreets.api.us_street.*;
import com.smartystreets.api.ClientBuilder;

import java.io.IOException;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class SmartyStreetsClient {
	// private static final String AUTH_ID = "f748fd62-a78a-a8a9-57e1-39d9a59bc6e3";
	// private static final String AUTH_TOKEN = "EKqmZ8JEx0BO7FVbiwbp";

	// for Server-deserver requests, use this code:

	// Documentation for input fields can be found at:
	// https://smartystreets.com/docs/us-street-api#input-fields

	public static List<List<Candidate>> isValidAddress(String senderAddr, String receiverAddr) {
		List<List<Candidate>> result = new ArrayList<>();

		String authId = "f748fd62-a78a-a8a9-57e1-39d9a59bc6e3";
		String authToken = "EKqmZ8JEx0BO7FVbiwbp";
		StaticCredentials credentials = new StaticCredentials(authId, authToken);

		Client client = new ClientBuilder(credentials).buildUsStreetApiClient();
		Batch batch = new Batch();

		Lookup address0 = new Lookup(senderAddr);

		Lookup address1 = new Lookup(receiverAddr);

		try {
			batch.add(address0);
			batch.add(address1);

			client.send(batch);
		} catch (BatchFullException ex) {
			System.out.println("Oops! Batch was already full.");
		} catch (SmartyException ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		Vector<Lookup> lookups = batch.getAllLookups();

		for (int i = 0; i < batch.size(); i++) {
			ArrayList<Candidate> candidates = lookups.get(i).getResult();
			result.add(candidates);

			if (candidates.isEmpty()) {
				if (i == 0) {
					System.out.println("The sender address is invalid!");
				} else {
					System.out.println("The receiver address is invalid!");
				}
				continue;
			}

			if (i == 0) {
				System.out.println("The sender address is valid! See details below:");
			} else {
				System.out.println("The receiver address is valid! See details below:");
			}

			for (Candidate candidate : candidates) {
				final Components components = candidate.getComponents();
				final Metadata metadata = candidate.getMetadata();

				System.out.println("\nCandidate " + candidate.getCandidateIndex() + ":");
				System.out.println("Input ID: " + candidate.getInputId());
				System.out.println("Delivery line 1: " + candidate.getDeliveryLine1());
				System.out.println("Last line:       " + candidate.getLastLine());
				System.out.println("ZIP Code:        " + components.getZipCode() + "-" + components.getPlus4Code());
				System.out.println("County:          " + metadata.getCountyName());
				System.out.println("Latitude:        " + metadata.getLatitude());
				System.out.println("Longitude:       " + metadata.getLongitude());
			}
			System.out.println();

		}
		return result;
	}
}