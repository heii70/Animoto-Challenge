package com.project.AnimotoChallenge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class PersonalValidation {
	
	@Test
	public void invalidIdDetector() {
		
		try {
			File file = new File("cover_letter.json"); 
						
			// read JSON file and store in a StringBuilder object
			BufferedReader in = new BufferedReader(new FileReader(file));
				
			String inputLine;
			StringBuffer coverLetterBuffer = new StringBuffer();
			    
			while ((inputLine = in.readLine()) != null) {
				coverLetterBuffer.append(inputLine);
			}
			    
			in.close();
					 
			// Initialize JSON parser and declare object handles
			String coverLetter = coverLetterBuffer.toString();
			JSONParser jsonParser = new JSONParser();
			Object parsedObject;
			ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
			PrintWriter out = new PrintWriter("out.json");
		
			// parse response and store return value in object handle
			parsedObject = jsonParser.parse(coverLetter);
			
			String parsedObjectPretty = mapper.writeValueAsString(parsedObject);
			out.println(parsedObjectPretty);			
			out.close();
			
			// retrieve JSON nested object values from keys
			JSONObject jsonResponseObject = (JSONObject) parsedObject;
			String name = jsonResponseObject.get("name").toString().trim();
			JSONObject contactDetails = (JSONObject) jsonResponseObject.get("contact_details");
			JSONObject content = (JSONObject) jsonResponseObject.get("content");
			
			// Assert top-level keys in correct form
			
			// Name
			Assert.assertTrue(!name.isEmpty());
			
			// Contact details
			
			// Object handles
			String phone;
			String email;
			String website;
			JSONArray other;
			int numberOfContactDetailsObjects = contactDetails.keySet().size();
			
			// Required regex patterns
			String phonePattern = "^([0-9 +()-]#?){7,30}$";
			String emailPattern = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-]+$";
			String websitePattern = "^((https?|ftp|smtp)://)([a-zA-Z0-9]+\\.){1,2}[a-zA-Z]+(/[a-zA-Z0-9]+)*/?$";
			
			// Assert phone and email present and in correct form
			try {
				// Attempt to get phone and email Objects
				phone = contactDetails.get("phone").toString().trim();
				email = contactDetails.get("email").toString().trim();
								
				// Assert phone and email matches pattern and number of objects to be at least 2 and at most 4
				Assert.assertTrue(Pattern.compile(phonePattern).matcher(phone).find());
				Assert.assertTrue(Pattern.compile(emailPattern).matcher(email).find());
				Assert.assertTrue(numberOfContactDetailsObjects <= 4 && numberOfContactDetailsObjects >= 2);
			}
			catch(AssertionError | ClassCastException | NullPointerException e) {
				throw new AssertionError();
			}
			
			// Assert website is in correct form if provided			
			try {
				// Attempt to get website Object and match pattern if not null
				website = contactDetails.get("website").toString().trim();
				Assert.assertTrue(Pattern.compile(websitePattern).matcher(website).find());
			}
			catch(NullPointerException e) {
				// Optional website not provided; skip
			}
			catch(AssertionError | ClassCastException e) {
				// Optional website provided; pattern or object type not valid
				throw new AssertionError();
			}
			
			// Assert other is in correct form if provided
			try {
				// Attempt to get other Object as a JSONArray
				try {
					other = (JSONArray) contactDetails.get("other");
					Assert.assertNotNull(other);
				}
				catch(AssertionError e) {
					throw new NullPointerException();
				}
				
				// Loop through array and attempt to get each nested objects as a JSONObject
				try {
					// Object handles
					JSONObject otherArrayObject;
					String type;
					String value;
					
					for(int i = 0; i < other.size(); i++) {
						otherArrayObject = (JSONObject) other.get(i);
						
						// Attempt to get type and value objects
						type = otherArrayObject.get("type").toString().trim();
						value = otherArrayObject.get("value").toString().trim();
												
						// Assert objects are not empty and are the only keys present
						Assert.assertTrue(!type.isEmpty());
						Assert.assertTrue(!value.isEmpty());
						Assert.assertTrue(otherArrayObject.keySet().size() == 2);
					}
				}
				catch(NullPointerException | ClassCastException | AssertionError e) {
					// At least one of the objects has invalid form, is missing type and/or value Objects, and/or more than two keys
					throw new AssertionError();
				}
			}
			catch(NullPointerException e) {
				// optional other not provided; skip
			}
			catch(AssertionError | ClassCastException e) {
				// optional other provided; pattern or object type not valid
				throw new AssertionError();
			}
			
			// Content

			// Object handles
			String letterBody;
			String checkValue;
			int numberOfContentObjects = content.keySet().size();
			
			// Required regex patterns
			String checkValuePattern = "^[0-9a-z]+$";
			
			try {
				// Attempt to get letter_body Object
				letterBody = content.get("letter_body").toString().trim();
				Assert.assertTrue(!letterBody.isEmpty());
				
				// Assert challenge_checkvalue is in correct form if provided
				try {
					// Attempt to get challenge_checkvalue object
					checkValue = content.get("challenge_checkvalue").toString().trim();
					
//					System.out.println(checkValue);
//					System.out.println(Pattern.compile(checkValuePattern).matcher(checkValue).find());
			
					Assert.assertTrue(Pattern.compile(checkValuePattern).matcher(checkValue).find());
					Assert.assertTrue(numberOfContentObjects >= 1 && numberOfContentObjects <= 2);
				}
				catch(NullPointerException e){
					// optional checkValue not provided; skip
				}
				catch(AssertionError e) {
					// optional checkValue provided; pattern or object type not valid
					throw new AssertionError();
				}
			}
			catch(NullPointerException | AssertionError e) {
				// letter_body isn't present or invalid object type and/or challenge_checkvalue has incorrect form
				throw new AssertionError();
			}
		}
		catch (NullPointerException | AssertionError e) {
			System.out.println("Cover Letter Invalid");
		}
		catch (IOException | ParseException e) {
			e.printStackTrace();
		} 
	}
	
}
