package com.mycompany.myapp.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mycompany.myapp.model.FriendBean;
import com.mycompany.myapp.model.FriendPush;
import com.mycompany.myapp.model.MemberBean;
import com.mycompany.myapp.service.FriendService;
import com.mycompany.myapp.service.MemberServiceImpl;

@Controller
public class FriendAction {

	@Autowired
	private MemberServiceImpl memberService;
	@Autowired
	private FriendService friendService;

	// search friend

	@RequestMapping(value = "/friend_search.do", method = RequestMethod.POST)
	@ResponseBody
	public MemberBean member_friendSearch(@RequestParam("email") String email, Model model) throws Exception {
		
		MemberBean m = memberService.userCheck(email);
		if (m == null)
			return m;
		

		return m;
	}
	@RequestMapping(value = "/friend_accept.do", method = RequestMethod.POST)
	@ResponseBody
	public int member_friendAccept_push(HttpSession session, int status,FriendPush fc, Model model) throws Exception {
		fc.setInvitee((String) session.getAttribute("email"));
		int result =0;
		if(status==1) {
			Map<String, String> m = new HashMap<String, String>();
			m.put("inviter", fc.getInviter());
			m.put("invitee", fc.getInvitee());
			result = friendService.accept(m);
		}
			result = friendService.del(fc);

		return result;
	}

	// add friend
	@RequestMapping(value = "/friend_add.do", method = RequestMethod.POST)
	@ResponseBody
	public int add_friend(@RequestParam("email") String email, HttpSession session, Model model) throws Exception {
		String myEmail = (String) session.getAttribute("email");
		
		if (myEmail.equals(email)) {
			return 100;
		}
		Map<String, String> m = new HashMap<String, String>();
		m.put("inviter", myEmail);
		m.put("invitee", email);

		int result = friendService.checkFriend(m);
		if (result != -1) {
			result =friendService.checkFriendConfirm(m);
			if (result ==1)
				result = friendService.push_confirm(m);
		}
		return result;
	}

	// ?????? ?????? --------------------------------------------------
	// delete friend
	@RequestMapping(value = "/friend_del.do")
	public String delete_friend(HttpServletRequest request, String email, Model model) throws Exception {
		
		String myEmail = (String) request.getSession().getAttribute("email");
		Map<String, String> m = new HashMap<String, String>();
		m.put("my", myEmail);
		m.put("opponent", email);
		int result = friendService.delFriend(m);
		model.addAttribute("result",result);
		return "member/fr_delResult";
	}

	// ?????? ????????? --------------------------------------------------
	@RequestMapping(value = "/friendlist.do")
	public String friendList(HttpSession session, Model model) throws Exception {
		String email = (String) session.getAttribute("email");
//			String email = "1@1.qq";
		List<FriendBean> list = friendService.list(email);
		List<MemberBean> friendList = new ArrayList<MemberBean>();	            
        for(FriendBean fb : list) {
        	if(fb.getInviter().equals(email))
        		friendList.add(memberService.userCheck(fb.getInvitee()));
        	else
        		friendList.add(memberService.userCheck(fb.getInviter()));
        }
        
		model.addAttribute("list", friendList);
        //model.addAttribute("list", list);
		
		//?????? ?????? ?????? ?????? ????????????
		// ?????? ?????? ????????? ?????? ???????????? ???????????????. ????????????.
		List<FriendPush> invitationList = friendService.invite(email);
		model.addAttribute("invite",invitationList);
		
//		//?????? ?????? ?????????
//        List<FriendBean> recommend = friendService.recommend(email);
//        session.setAttribute("fr_recommend",recommend);
        
		return "member/friend";
	}
}
