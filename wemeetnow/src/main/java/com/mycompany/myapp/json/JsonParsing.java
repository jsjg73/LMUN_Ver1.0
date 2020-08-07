package com.mycompany.myapp.json;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Service;

import com.mycompany.myapp.model.Place;
import com.mycompany.myapp.model.stationXY;

@Service("jsonParsing")
public class JsonParsing {
	private Place stcoor = null;	
	
	public List<Place> getPlaceInfo(String jsonData) {
		List<Place> stationList = new ArrayList<Place>();
		try {
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonOb = (JSONObject) jsonParser.parse(jsonData);
			JSONArray docArray = (JSONArray) jsonOb.get("documents");

			for (int i = 0; i < docArray.size(); i++) {
				JSONObject docOb = (JSONObject) docArray.get(i);
				stcoor = new Place();
				//String pname = docO
				stcoor.setName((String) docOb.get("place_name"));
				stcoor.setX((String) docOb.get("x"));
				stcoor.setY((String) docOb.get("y"));
				stcoor.setAddress((String) docOb.get("address_name"));
				stcoor.setPhone((String) docOb.get("phone"));
				stcoor.setPlace_url((String) docOb.get("place_url"));
				stationList.add(stcoor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stationList;
	}
}
