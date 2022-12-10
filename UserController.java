package com.cybage.controller;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cybage.exception.EmailAlreadyExistException;
import com.cybage.exception.RecordNotFoundException;
import com.cybage.model.Users;
import com.cybage.model.EventBooking;
import com.cybage.model.LoginTemp;
import com.cybage.service.UserService;

@CrossOrigin("http://localhost:4200")
@RestController
@RequestMapping("/user")
public class UserController {



	@Autowired
	private UserService userService;

	@PostMapping("/register")
	public ResponseEntity<String> addUser(@RequestBody Users user)
			throws UnsupportedEncodingException, MessagingException, EmailAlreadyExistException {
		if (userService.getUserByEmail(user.getEmail()) != null)
			throw new EmailAlreadyExistException("An account already exists for this email");
		else
			userService.addUser(user);
		return new ResponseEntity<String>(
				"User registered successfully\nVerification link has been sent to your email.", HttpStatus.CREATED);
	}

	@GetMapping("/getAllUsers")
	public ResponseEntity<List<Users>> getAllUsers() {
		List<Users> userList = userService.getAllUser();
		return new ResponseEntity<List<Users>>(userList, HttpStatus.OK);
	}

	@GetMapping("/byId/{id}")
	public ResponseEntity<Users> getUser(@PathVariable int id) {
		Users user = userService.getUserById(id);
		if (user == null)
			throw new RecordNotFoundException("user not found.");
		return new ResponseEntity<Users>(userService.getUserById(id), HttpStatus.OK);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteUser(@PathVariable int id) {
		Users user = userService.deleteUser(id);
		if (user == null) {
			throw new RecordNotFoundException("User not found.");
		} else {
			return new ResponseEntity<String>("Delete user successfully", HttpStatus.CREATED);
		}
	}

	@PutMapping("/update/profile/{id}")
	public ResponseEntity<String> updateUserDetail(@PathVariable int id, @RequestBody Users user) {
		userService.updateUserDetail(id, user);
		return new ResponseEntity<String>("User updated successfully", HttpStatus.OK);

	}

	@GetMapping("/verify")
	public String verifyUser(@Param("code") String code, Model model) {

		if (userService.verify(code)) {
			return "<div class=\"text-center\">\r\n" + "		<h2>Customer Verification</h2>\r\n"
					+ "		<h4>Congratulations! Your account has been verified.</h4>\r\n"
					+ "		<p><a href=\"http://localhost:4200/login\">Login Now</a></p>\r\n" + "	</div>";
		} else {
			return "<div class=\"text-center\">\r\n" + "		<h2>Customer Verification</h2>\r\n"
					+ "		<h4>Your account was already verified, or the verification code is invalid.</h4>\r\n"
					+ "		<p><a href=\"http://localhost:4200/login\">Login Now</a></p>\r\n" + "	</div>";
		}

	}

	@GetMapping("/login/{email}/{password}")
	public ResponseEntity<LoginTemp> verifyLoginWithPassword(@PathVariable String email,
			@PathVariable String password) {
		LoginTemp verifyLoginWithOTP = userService.verifyLoginWithPassword(email, password);
		return new ResponseEntity<LoginTemp>(verifyLoginWithOTP, HttpStatus.OK);
	}

	@GetMapping("/byemail/{email}")
	public ResponseEntity<Users> getByEmail(@PathVariable String email) {
		Users userss = userService.getUserByEmail(email);
		return new ResponseEntity<Users>(userss, HttpStatus.OK);
	}

	@GetMapping("/user-login/{email}")
	public ResponseEntity<String> loginWithEmail(@PathVariable String email)
			throws UnsupportedEncodingException, MessagingException {
		String loginUserReponse = userService.loginUserByEmail(email);
		return new ResponseEntity<String>(loginUserReponse, HttpStatus.OK);
	}

	@GetMapping("/verify-otp/{email}/{otp}")
	public ResponseEntity<LoginTemp> verifyLoginWithOTPDemo(@PathVariable String email, @PathVariable String otp) {
		LoginTemp verifyLoginWithPassword = userService.verifyLoginWithOTPDemo(email, otp);
		return new ResponseEntity<LoginTemp>(verifyLoginWithPassword, HttpStatus.OK);
	}
	
	@PutMapping("/changepassword/{id}/{old}/{newPassword}")
	ResponseEntity<String> changePassword(@PathVariable int id, @PathVariable String old,@PathVariable String newPassword){
		userService.changePassword(id,old,newPassword);
		return new ResponseEntity<String>("Password update successfully",HttpStatus.OK);
		
	}

	// Book events
	@PostMapping("/bookevent")
	public ResponseEntity<String> bookEvent(@RequestBody EventBooking eventBooking) {

		userService.bookEvent(eventBooking);
		return new ResponseEntity<String>("Event book successfully.....", HttpStatus.CREATED);
	}

	@GetMapping("/booking/byUserId/{userId}")
	public ResponseEntity<List<EventBooking>> getBookedEventByUserId(@PathVariable int userId) {

		List<EventBooking> bookedEventsByUser = userService.getBookedEventByUserId(userId);
		if (bookedEventsByUser == null)
			throw new RecordNotFoundException("event not found.");
		return new ResponseEntity<List<EventBooking>>(userService.getBookedEventByUserId(userId), HttpStatus.OK);
	}

	//System feedback
	
}
