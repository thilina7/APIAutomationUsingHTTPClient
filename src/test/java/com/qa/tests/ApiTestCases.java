package com.qa.tests;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qa.base.TestBase;
import com.qa.client.RestClient;
import com.qa.util.TestUtil;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class ApiTestCases extends TestBase {
	TestBase testBase;
	String serviceUrl;
	String authUrl;
	String url;
	String bookingIdUrl;
	RestClient restClient;
	CloseableHttpResponse closebaleHttpResponse;
	public static String tokenValue;
	public static HashMap<String, String> headerMap;

	@BeforeMethod
	public void setUp() throws ClientProtocolException, IOException {
		testBase = new TestBase();
		serviceUrl = prop.getProperty("URL");
		authUrl = prop.getProperty("authURL");
		bookingIdUrl = prop.getProperty("postCreateBooking");
		// https://reqres.in/api/users

		url = serviceUrl + authUrl;

	}

	@Test(priority = 1)
	public void postAuth() throws JsonGenerationException, JsonMappingException, IOException {
		restClient = new RestClient();
		headerMap = new HashMap<String, String>();
		headerMap.put("Content-Type", "application/json");

		String usersJsonString = "{\n" + "    \"username\" : \"admin\",\n" + "    \"password\" : \"password123\"\n"
				+ "}";
		System.out.println(usersJsonString);

		closebaleHttpResponse = restClient.post(url, usersJsonString, headerMap); // call the API

		// validate response from API:
		// 1. status code:
		int statusCode = closebaleHttpResponse.getStatusLine().getStatusCode();
		Assert.assertEquals(statusCode, testBase.RESPONSE_STATUS_CODE_200);

		// 2. JsonString:
		String responseString = EntityUtils.toString(closebaleHttpResponse.getEntity(), "UTF-8");

		JSONObject responseJson = new JSONObject(responseString);
		System.out.println("The response from API is:" + responseJson);

		tokenValue = TestUtil.getValueByJPath(responseJson, "/token");
		System.out.println("Token Value ------->" + tokenValue);
	}

	@Test(priority = 2)
	public void postCreateBooking() throws JsonGenerationException, JsonMappingException, IOException {
		restClient = new RestClient();
		headerMap = new HashMap<String, String>();
		headerMap.put("Content-Type", "application/json");
		headerMap.put("Accept", "application/json");
		headerMap.put("Authorization", tokenValue);
		String usersJsonString = "{\n" + "    \"firstname\" : \"Jim\",\n" + "    \"lastname\" : \"Brown\",\n"
				+ "    \"totalprice\" : 111,\n" + "    \"depositpaid\" : true,\n" + "    \"bookingdates\" : {\n"
				+ "        \"checkin\" : \"2018-01-01\",\n" + "        \"checkout\" : \"2019-01-01\"\n" + "    },\n"
				+ "    \"additionalneeds\" : \"Breakfast\"\n" + "}";

		System.out.println(usersJsonString);

		closebaleHttpResponse = restClient.post(serviceUrl + bookingIdUrl, usersJsonString, headerMap); // call the API
		// validate response from API:
		// 1. status code:
		int statusCode = closebaleHttpResponse.getStatusLine().getStatusCode();
		Assert.assertEquals(statusCode, testBase.RESPONSE_STATUS_CODE_200);

		// 2. JsonString:
		String responseString = EntityUtils.toString(closebaleHttpResponse.getEntity(), "UTF-8");

		JSONObject responseJson = new JSONObject(responseString);
		System.out.println("The response from API is:" + responseJson);

		String firstname = TestUtil.getValueByJPath(responseJson, "/booking/firstname");
		System.out.println("First name Value ------->" + firstname);

		String lastname = TestUtil.getValueByJPath(responseJson, "/booking/lastname");
		System.out.println("Second name Value ------->" + lastname);

		assertEquals("Jim", TestUtil.getValueByJPath(responseJson, "/booking/firstname"));
		assertEquals("Brown", TestUtil.getValueByJPath(responseJson, "/booking/lastname"));
	}

	@Test(priority = 3)
	public void getBookingByIds() throws JsonGenerationException, JsonMappingException, IOException {

		RestAssured.baseURI = serviceUrl + bookingIdUrl;
		RequestSpecification httpRequest = RestAssured.given();
		Response response = httpRequest.get("");
		System.out.println(response);
		JsonPath jsonPathEvaluator = response.jsonPath();

		List<Integer> allBookingIds = jsonPathEvaluator.get("bookingid");

		for (int bookingId : allBookingIds) {
			System.out.println("bookingId: " + bookingId);
		}
		int statusCode = response.getStatusCode();
		Assert.assertEquals(statusCode, testBase.RESPONSE_STATUS_CODE_200);
	}

	@Test(priority = 4)
	public void getBookingById() throws JsonGenerationException, JsonMappingException, IOException {

		RestAssured.baseURI = serviceUrl + bookingIdUrl;
		RequestSpecification httpRequest = RestAssured.given();
		Response response = httpRequest.get("");
		System.out.println(response);
		JsonPath jsonPathEvaluator = response.jsonPath();

		List<Integer> allBookingIds = jsonPathEvaluator.get("bookingid");

		for (int bookingId : allBookingIds) {
			RestAssured.baseURI = serviceUrl + bookingIdUrl + "/" + bookingId;
			RequestSpecification httpRequests = RestAssured.given();
			Response responses = httpRequests.get("");

			int statusCode = responses.getStatusCode();
			Assert.assertEquals(statusCode, testBase.RESPONSE_STATUS_CODE_200);
		}
	}

	@Test(priority = 5)
	public void partialUpdateBooking() throws JsonGenerationException, JsonMappingException, IOException {
		restClient = new RestClient();
		headerMap = new HashMap<String, String>();
		headerMap.put("Content-Type", "application/json");
		headerMap.put("Accept", "application/json");
		headerMap.put("Cookie", "token=abc123");
		String usersJsonString = "{\n"
				+ "    \"firstname\" : \"James\",\n"
				+ "    \"lastname\" : \"Brown\",\n"
				+ "    \"totalprice\" : 111,\n"
				+ "    \"depositpaid\" : true,\n"
				+ "    \"bookingdates\" : {\n"
				+ "        \"checkin\" : \"2018-01-01\",\n"
				+ "        \"checkout\" : \"2019-01-01\"\n"
				+ "    },\n"
				+ "    \"additionalneeds\" : \"Breakfast\"\n"
				+ "}";

		System.out.println(usersJsonString);

		closebaleHttpResponse = restClient.patch(serviceUrl + bookingIdUrl+"/"+260, usersJsonString, headerMap); // call the API
		// validate response from API:
		// 1. status code:
		int statusCode = closebaleHttpResponse.getStatusLine().getStatusCode();
		Assert.assertEquals(statusCode, testBase.RESPONSE_STATUS_CODE_200);

		// 2. JsonString:
		String responseString = EntityUtils.toString(closebaleHttpResponse.getEntity(), "UTF-8");

		JSONObject responseJson = new JSONObject(responseString);
		System.out.println("The response from API is:" + responseJson);

		String firstname = TestUtil.getValueByJPath(responseJson, "/booking/firstname");
		System.out.println("First name Value ------->" + firstname);

		String lastname = TestUtil.getValueByJPath(responseJson, "/booking/lastname");
		System.out.println("Second name Value ------->" + lastname);

		assertEquals("James", TestUtil.getValueByJPath(responseJson, "/booking/firstname"));
		assertEquals("Brown", TestUtil.getValueByJPath(responseJson, "/booking/lastname"));
	}
}
