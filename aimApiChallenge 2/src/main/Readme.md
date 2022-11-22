Readme.md
Service description
Create and Update operations are through HTTP POSTs
  POST https://1ryu4whyek.execute-api.us-west-2.amazonaws.com/dev/skus
Posts expect a body with SKU, Description and Price
{
    "sku":"berliner", 
    "description": "Jelly donut", 
    "price":"2.99"
}

Read operations are through HTTP GETs
  GET https://1ryu4whyek.execute-api.us-west-2.amazonaws.com/dev/skus 
  GET https://1ryu4whyek.execute-api.us-west-2.amazonaws.com/dev/skus/{id}
Delete operations are through HTTP DELETEs
  DELETE https://1ryu4whyek.execute-api.us-west-2.amazonaws.com/dev/skus/{id} 

TEST CASES

Get All tests
Happy
Get all the skus
Verify the status code
Verify the output
Well formed json —not done
Field values OK —not done
Counts are OK —not done
Abnormal
Try to force all the error conditions —not done
400
500
Populate the sku list to 10000 items and then try the get —not done
Populate with incorrect fields and access —not done
Run the api with many concurrent users  —not done

GET single sku tests

POST tests 
Happy: Post a new sku and verify it got created
Happy: Post an updated version of a sku and see if update happened
Post with incorrect field name —not done
Post with incorrect field values —not done

DELETE single tests
Delete an existing SKU
Delete an SKU which does not exist
Run same delete twice in a row

Running instructions
Unzip the folder 
In Terminal, navigate to folder and run: mvn test -Dtest=apiTests
	
	


