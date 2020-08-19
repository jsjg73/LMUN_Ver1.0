package com.mycompany.myapp.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mycompany.myapp.model.Coordinate;
import com.mycompany.myapp.model.Place;
import com.mycompany.myapp.model.Route;
import com.mycompany.myapp.model.stationXY;
import com.mycompany.myapp.service.MapService;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeAction {
	
	@Autowired
	private MapService ms;

	@RequestMapping("test.do")
	public String home_push(HttpSession session, HttpServletRequest request) {
		return "map/home";
	}
	
	@RequestMapping("session_del.do")
	public String testtest(HttpServletRequest request, Place place, Model model) {
		if(place.getName()==null)return "map/home";
		
		ArrayList<Place> startPlaceList = new ArrayList<Place>();
		int n = place.getName().split(",").length;
		for(int i=0; i<n; i++) {
			Place p = new Place();
			p.setAddress(place.getAddress().split(",")[i]);
			p.setName(place.getName().split(",")[i]);
			p.setX(place.getX().split(",")[i]);
			p.setY(place.getY().split(",")[i]);
			startPlaceList.add(p);
		}
		int listSize = startPlaceList.size();
		request.getSession().setAttribute("startPlaceList", startPlaceList);
		
		return "map/home";
	}
	@RequestMapping("sendAddr.do")
	public String sendAddr(HttpServletRequest request, @ModelAttribute Place place, Model model) throws Exception {
		
		
		ArrayList<Place> startPlaceList = (ArrayList<Place>) place.getPlaces();
		

		// 세션
		HttpSession session = request.getSession();
		session.setAttribute("startPlaceList", startPlaceList);
		//resquest.setAttribute("startPlaceList", startPlaceList);
		
		
		//---------------------------------중점 좌표 get--------------------------------
		Coordinate center_coor = ms.getCenter(startPlaceList);

		//--------------------------------------------------------------------------
		
		
		//--------------------------------가까운 지하철역 5개 get-------------------------------
		// 중점좌표 기준 station 데이터를 API에 요청 (물리적으로 가까운 순)
		// category_group_code:SW8(지하철), page:1, size:15(기본값), radius:2000 으로 제한하여 요청
		String option = "x/" + center_coor.getX() + "/y/" + center_coor.getY() + "/page/1/radius/2000";
		List<Place> endplaceList = ms.categorySearch("SW8", option);
		//--------------------------------------------------------------------------------
		
		
		
		//---------------------------08/03 권은지 : 추천역 DB로부터 가져오기 ------------------------------------	
		// 중점좌표 기준 모든 station 데이터를 API에 요청 (radius:2000 동일)
//		String rcm_option = "x/" + center_coor.getX() + "/y/" + center_coor.getY() + "/radius/2000";
//		
//		// rcm_placeList : 검색된 모든 지하철 객체(Place DTO)를 리스트로 반환받음
//		List<Place> rcm_placeList = ms.categorySearch("SW8", rcm_option);
//		
//		// rcm_stationList : foundPlace.jsp 페이지에 넘길 추천역(stationXY DTO) 리스트
//		List<stationXY> rcm_stationList = new ArrayList<stationXY>();
//		
//		//검색된 리스트에서 역 이름(p.getName()) 하나씩 꺼내와 DB에 존재하는지 비교
//		for (Place p : rcm_placeList) {
//			String subName = p.getName().trim();			
//			stationXY st = ms.getRcm_station(subName);  // 역 이름으로 selectOne(조회)하여 매칭되는 객체 가져오기(DB에 없으면 NULL)
//			if (rcm_stationList.size() > 5) { // 사이즈 5개 넘어가지 않도록
//				break;
//			} else {
//				if (st != null) {
//					rcm_stationList.add(st); // 검색 된 객체가 있다면 객체 리스트에 추가
//				}
//			}
//		}
		//--------------------------------------------------------------------------
			
		
		
		// 중심좌표 return
		model.addAttribute("center", center_coor);

		//endplacelist 정보 넘기기
		//javascript에서 사용하기 편하게하기위해
		//구분차 '#'를 사용해서 문자열로 형태 변환		
		StringBuilder epl_x = new StringBuilder().append(center_coor.getX()).append("#");
		StringBuilder epl_y = new StringBuilder().append(center_coor.getY()).append("#");;
		StringBuilder epl_name = new StringBuilder().append("중심").append("#");;
		for(Place p : endplaceList) {
			epl_x.append(p.getX()).append("#");
			epl_y.append(p.getY()).append("#");
			epl_name.append(p.getName()).append("#");
		}
		
		model.addAttribute("endPlaceList_x", epl_x.toString());
		model.addAttribute("endPlaceList_y", epl_y.toString());
		model.addAttribute("endPlaceList_name", epl_name.toString());
		
		//startplacelist 정보 넘기기
		StringBuilder spl_x = new StringBuilder();
		StringBuilder spl_y = new StringBuilder();
		StringBuilder spl_name = new StringBuilder();
		for(Place p : startPlaceList) {
			spl_x.append(p.getX()).append("#");
			spl_y.append(p.getY()).append("#");
			spl_name.append(p.getName()).append("#");
		}
		
		model.addAttribute("startPlaceList_x", spl_x.toString());
		model.addAttribute("startPlaceList_y", spl_y.toString());
		model.addAttribute("startPlaceList_name", spl_name.toString());
		
		// 각 후보지에 대해서 소요시간 보여주기.		
		// 공공데이터포털에서 경로 찾아오기
		// 필요한 정보 : 출발지 정보, 도착 후보지 정보
		String pathInfo = ms.getPathInfo(startPlaceList, endplaceList,"BusNSub");		
		model.addAttribute("pathInfo",pathInfo);
		model.addAttribute("x_num",startPlaceList.size());
		
		return "map/foundplace";
	}

	@RequestMapping("category.do")
	public String categorySelect(Place place, Model model, HttpSession session) {

		
		model.addAttribute("place", place);

		//-------------------- 카테고리별 추천 장소 5개 ----- 08/05 김가을  --------------------
		String option = "x/" + place.getX() + "/y/" + place.getY() + "/page/1/size/5/radius/2000";
		//String option = "x/" + place.getX() + "/y/" + place.getY() + "/page/1/radius/2000";
		
		StringBuilder sb = new StringBuilder();
		List<Place> startPlaceList = (List<Place>)session.getAttribute("startPlaceList");
		for(Place p : startPlaceList)
			sb.append(p.getAddress());
		// CT1 문화시설
		List<Place> ct1placeList = ms.categorySearch("CT1", option);
		ms.createId(ct1placeList, sb.toString());
		model.addAttribute("ct1placeList", ct1placeList);
		// FD6 음식점
		List<Place> fd6placeList = ms.categorySearch("FD6", option);
		ms.createId(fd6placeList, sb.toString());
		model.addAttribute("fd6placeList", fd6placeList);
		// CE7 카페
		List<Place> ce7placeList = ms.categorySearch("CE7", option);
		ms.createId(ce7placeList, sb.toString());
		model.addAttribute("ce7placeList", ce7placeList);
		// AT4 관광명소
		List<Place> at4placeList = ms.categorySearch("AT4", option);
		ms.createId(at4placeList, sb.toString());
		model.addAttribute("at4placeList", at4placeList);
		//------------------------------------------------------------
		
		return "map/category";
	}
	@RequestMapping("route.do")
	public String route(@ModelAttribute("id") String id, HttpSession session, Place place, Model model) {
		//지하철,버스,
		//공유하기 버튼 분기
		model.addAttribute("original", 0);
		System.out.println(id+"  ::  " +session.getAttribute("id"));
		//boolean check = ms.idCheck(id); // already : true, first : false
		Route r = ms.routeSearch(id);
		// r ==null  ==> 요구하는 경로가 db에 저장돼있는지?
		// session.getAttribute("id") == null  ==> 공유된 링크를 통해서 진입한 상황 고려.
		// !session.getAttribute("id").equals(id)  ==> 뒤로가기 했다가 다시 경로를 선택한 상황 고려.
		if(r == null && (session.getAttribute("id") == null || !session.getAttribute("id").equals(id)) ) { // && session.getAttribute("id") != null
			session.setAttribute("id", id);
			Place endPlace = place;
			List<Place> startPlaceList =(List<Place>) session.getAttribute("startPlaceList");			
			
			ms.finalDBSetting(startPlaceList,endPlace,id);
			//최종 DB에 들어갈 칼럼 데이터 만들기,넣기
			// survlrxbymwjdghmbboawthlitovss 서울 은평구 진관동 88/서울 서대문구 대현동 11-1/서울 종로구 명륜3가 53-21/ 신도초교.은평메디텍고등학교#720#무악재역#30#/봉원사입구#서대문11#무악재역#19#/명륜시장.성대후문#종로08#연건동#이화동(이화장)#7025#무악재역#34#/ 신도초교.은평메디텍고등학교#720#무악재역#30#/봉원사입구#서대문11#무악재역#19#/명륜시장.성대후문#종로08#연건동#이화동(이화장)#7025#무악재역#34#/ NULL     NULL
			model.addAttribute("original", 1);
			r = ms.routeSearch(id);
		}
		
			//Route r = ms.routeSearch(id);

			String[] departure = r.getDeparture().split("/");
			String[] bus_route = new String[departure.length];
			String[] bus_time = new String[departure.length];

			String[] br1 = r.getBus_route().split("/");
			String rstr = "";
			String tstr = "";

			for(int i=0; i<departure.length; i++) {
				if(br1[i].equals("NONE")) {
					bus_route[i] = "검색된 대중 교통 경로가 없습니다.";
					bus_time[i] = "*";
					continue;
				}
				String[] br2 = br1[i].split("#");
				for(int j=0; j<br2.length-1; j++) {
					rstr += br2[j] + "-";
				}				
				bus_route[i] = rstr;
				bus_time[i] = br2[br2.length-1];
			}
			
			String[] complex_route = new String[departure.length];
			String[] complex_time = new String[departure.length];

			br1 = r.getComplex_route().split("/");

			for(int i=0; i<departure.length; i++) {
				if(br1[i].equals("NONE")) {
					complex_route[i] = "검색된 대중 교통 경로가 없습니다.";
					complex_time[i] = "*";
					continue;
				}
				String[] br2 = br1[i].split("#");
				for(int j=0; j<br2.length-1; j++) {
					tstr += br2[j] + "-";
				}				
				complex_route[i] = tstr;
				complex_time[i] = br2[br2.length-1];
			}

		model.addAttribute("id",id);
		model.addAttribute("departure", departure);
		model.addAttribute("bus_route", bus_route);
		model.addAttribute("complex_route", complex_route);
		model.addAttribute("bus_time", bus_time);
		model.addAttribute("complex_time", complex_time);

		return "map/route";
	}
}
// gterlyvsmtzaeejytlbhwdoasnosqe 서울 종로구 명륜3가 53-21/서울 서대문구 대현동 11-1/서울 은평구 진관동 88/ NONE/이대부고#272#서울지방경찰청.경복궁역#사직동주민센터#8002#자하문터널입구.석파정#36#/제각말5단지.은평뉴타운도서관#7211#구기터널입구#구기터널입구#7022#자하문고개.윤동주문학관#46#/ NONE/이대부고#272#서울지방경찰청.경복궁역#사직동주민센터#8002#자하문터널입구.석파정#36#/제각말5단지.은평뉴타운도서관#7211#구기터널입구#구기터널입구#7022#자하문고개.윤동주문학관#46#/ NULL     NULL

