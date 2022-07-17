package com.dotdash.recruiting.bookreview;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.dotdash.recruiting.bookreview.models.BooksResponse;

public class Main {
	/**
	 * Main method of the bookreview client application.
	 * @param args - parameters to set values when running application.
	 */
	public static void main(String[] args) {
		String hostName = "127.0.0.1";
		String path = "/books";
		String sortBy = "title";
		String terms = null;
		
		int page = 1;
		int port = 8080;
		
		// handle arguments	
		for (int i = 0; i < args.length; i++) {
			switch(args[i].toLowerCase()) {
			case "--help":
				printHelp();
				return;
			case "-s":
			case "--search":
				terms = args[++i];
				break;
			case "--sort":
				sortBy = args[++i];
				break;
			case "-p":
				try {
					page = Integer.parseInt(args[++i]);
				} catch(NumberFormatException e) {
					System.out.printf("Page %s was not a valid number. Please re-enter and try again.", args[i]);
					return;
				}
				break;
			case "-h":
			case "--host":
				hostName = args[++i];
				break;
			}	
		}
		
		// validate that we should be safe to proceed
		if (!validateSearchParameters(terms, sortBy, page)) {
			return;
		}
		
		// build client, URI, and request
		HttpClient client = HttpClient.newHttpClient();
		
		String query = "query=" + terms + "&page=" + page + "&sortBy=" + sortBy;
		URI serverURI;
		try {
			serverURI = new URI("http", null, hostName, port, path, query, null);
		} catch (URISyntaxException e) {
			System.out.println("An error occurred. Please try again. Error data: ");
			e.printStackTrace();
			return;
		}

		HttpRequest request = HttpRequest.newBuilder()
				.uri(serverURI)
				.GET().build();

		// fetch data from server
		HttpResponse<InputStream> response;
		try {
			// using synchronous sending here because the client is a simple, single thread, that isn't handling any other tasks.
			response = client.send(request, BodyHandlers.ofInputStream());
		} catch (IOException | InterruptedException e) {
			System.out.println("Failed to successfully retrieve a response from the server. Please try again. Error data: ");
			e.printStackTrace();
			return;
		}
		
		if (response.statusCode() != 200) {
			System.out.println("An error was received from the server. Please Try Again.");
			return;
		}
		
		// parse response JSON
		ObjectMapper mapper = new ObjectMapper();
		BooksResponse booksResponse;
		try {
			booksResponse = mapper.readValue(response.body(), new TypeReference<BooksResponse>() {});
		} catch (IOException e) {
			System.out.println("Failed to successfully parse the response from the server. Please try again. Error data: ");
			e.printStackTrace();
			return;
		}
		
		// print results
		System.out.printf("Results (Page %s of %s):\n", page, booksResponse.getNumberOfPages());
		booksResponse.getBooks().forEach((book) -> 
			System.out.printf("Title: %s\n Author: %s\n Image: %s\n\n", book.getTitle(), book.getAuthor(), book.getImageUrl()));
	}
	
	/**
	 * Perform basic validation of search parameters to make sure we are OK to run.
	 * If errors are found, user feedback handled here.
	 * @param terms - search terms to validate. These must be set to something.
	 * @param sortBy - sort by option, this must be "title" or "author" (ignore case)
	 * @param page - page number to fetch, must be greater than 0
	 * @return pass/fail status
	 */
	public static boolean validateSearchParameters(String terms, String sortBy, int page) {
		boolean passed = true;
		StringBuilder errors = new StringBuilder("The following errors have occurred: ");
		if (terms == null) {
			errors.append("Please enter search terms.\n");
			passed = false;
		}
		if (!(sortBy.equalsIgnoreCase("title") || sortBy.equalsIgnoreCase("author"))) {
			errors.append("Sort By must be \"title\" or \"author\". \n");
			passed = false;
		}
		if (page <= 0) {
			errors.append("Page must be greater than 0.");
			passed = false;
		}
		if (!passed) {
			System.out.print(errors.toString());
		}
		return passed;
	}
	
	/**
	 * Helper function to bury usage instructions. Prints usage instructions to screen.
	 * It might be nice to have this text stored in a separate text file (or any external source) that's 
	 * easily maintained without requiring code changes or recompilation. That seemed drastic for this project.
	 */
	public static void printHelp() {
		System.out.print("Usage Instructions: \n"
				+ "--help - Output a usage message and exit. \n"
				+ "-s, --search TERMS - The terms to search for. If TERMS contains spaces, it must be fuilly quoted. \n"
				+ "--sort FIELD -  where field is one of \"author\" or \"title\". Sorts the results by the specified field. Defaults to title. \n"
				+ "-p NUMBER - display the NUMBER page of results. \n"
				+ "-h, --host HOSTNAME - the hostname or ip address where the server can be found. Defaults to 127.0.0.1. \n");
	}
}
