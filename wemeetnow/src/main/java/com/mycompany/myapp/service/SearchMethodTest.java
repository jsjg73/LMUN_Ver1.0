package com.mycompany.myapp.service;

public class SearchMethodTest {

	public static void main(String[] args) {
//		MapServiceImpl ms = new MapServiceImpl();
//		System.out.println(ms.addressSearch("공항대로 58가길16").toString());
//		PublicDataService pd = new PublicDataService();
		PublicDataService pd2 = new PublicDataService();
		String bus = pd2.getPath("126.94852219358815", "37.56498949199894", "126.865572139231", "37.5507280806214","BusNSub");
		System.out.println(bus);
		//이대 126.94852219358815	37.56498949199894
		//등촌 126.865572139231	37.5507280806214
	}
}
