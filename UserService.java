package com.cybage.service;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.cybage.model.Users;
import com.cybage.controller.UserController;
import com.cybage.model.BookingStatus;
import com.cybage.model.EventBooking;
import com.cybage.model.LoginTemp;
import com.cybage.repository.EventBookingRepository;
import com.cybage.repository.UserRepository;

import ch.qos.logback.classic.Logger;
import net.bytebuddy.utility.RandomString;

@Service
public class UserService {
	public static final int MAX_LOGIN_ATTEMPTS_ALLOWED = 3;
	org.slf4j.Logger logger = LoggerFactory.getLogger(UserService.class);

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EventBookingRepository eventBookingRepository;

	@Autowired
	private JavaMailSender mailSender;

	public Users getUserByEmail(String email) {
		Users user = userRepository.findByEmail(email);
		return user;
	}

	public Users getUserByPassword(String password) {
		Users user = userRepository.findByPassword(password);
		logger.info("user" + user.getUserName() + " retrieved successfully:");
		return user;
	}

	public boolean addUser(Users user) throws UnsupportedEncodingException, MessagingException {

		String randomCode = RandomString.make(64);
		user.setVerificationCode(randomCode);
		user.setAccountStatus(false);
		user.setAccountNonLocked(true);
		logger.info("user:" +user.getUserName()+"added successfully");
		userRepository.save(user);
		return sendVerificationEmail(user);
	}

	private boolean sendVerificationEmail(Users user) throws MessagingException, UnsupportedEncodingException {
		String siteURL = "http://localhost:8080";
		String toAddress = user.getEmail();
		String fromAddress = "Trng2@evolvingsols.com";
		String senderName = "Event Management";
		String subject = "Please verify your registration";

		String content = "<p> Dear " + user.getUserName() + ",</p>";

		content += "<p> Please click the link below to verify your registration:</p>";

		String verifyURL = siteURL + "/user/verify?code=" + user.getVerificationCode();

		content += "<h4> <a href=\"" + verifyURL + "\">VERIFY</a></h4>";
		content += "<p> Thank you <br> Event Management</p>";

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom(fromAddress, senderName);
		helper.setTo(toAddress);
		helper.setSubject(subject);

		helper.setText(content, true);

		mailSender.send(message);
		logger.info("Mail Sent to the user" + user.getUserName() + "successfully:");

		return true;

	}

	public boolean verify(String verificationCode) {

		Users user = userRepository.findByVerificationCode(verificationCode);

		if (user == null || user.isAccountStatus()) {
			return false;

		} else {
			user.setVerificationCode("Account Verified");
			logger.info("account Verified successfully!");
			System.out.println("user!=null");
			user.setAccountStatus(true);
			userRepository.save(user);
			logger.info(" user" + user.getUserName() + "successfully Verified");
			return true;
		}
	}

	public List<Users> getAllUser() {

		return userRepository.findAll();

	}

	public Users getUserById(int id) {
		logger.info("user retrieved by id ");
		return userRepository.findById(id).orElse(null);
	}

	public Users deleteUser(int id) {
		Users user = getUserById(id);
		if (user == null)
			return null;
		userRepository.deleteById(id);
		logger.info(" user" + user.getUserName() + "deleted successfully");
		return new Users();

	}

	public void updateUserDetail(int id, Users user) {
		Users updateUser = userRepository.findById(id).get();

		if (user.getUserName() != null)
			updateUser.setUserName(user.getUserName());

		if (user.getAddress() != null)
			updateUser.setAddress(user.getAddress());

		if (user.getCity() != null)
			updateUser.setCity(user.getCity());

		if (user.getState() != null)
			updateUser.setState(user.getState());

		if (user.getPinCode() != 0)
			updateUser.setPinCode(user.getPinCode());
		logger.info("user detail for the user " + user.getUserName() + "updated successfully");
		userRepository.save(updateUser);

	}

	public String changePassword(int id, String old, String newPassword) {
		Users user= userRepository.findById(id).get();
		System.out.println(old);
		System.out.println(user.getPassword());
		if(user.getPassword().equals(old)) {
			System.out.println(old);
			user.setPassword(newPassword);
			userRepository.save(user);
			logger.info("password for the user: "+user.getUserName()+"updated successfully");
			return "update";
		}
		else {
			System.out.println(old);
			return "incorrectpassword";
		}
	}
	
	public String loginUserByEmail(String email) throws UnsupportedEncodingException, MessagingException {
		Users user = userRepository.findByEmail(email);
		if (userRepository.findByEmail(email) != null) {
			if (user.isAccountStatus() == false) {
				return "Your account is not verified yet.\nCheck your email for verification link.";
			} else if (user.isAccountNonLocked() == false) {
				return "Your account is blocked.";
			}
			String OTP = RandomString.make(8);

			user.setOneTimePassword(OTP);
			user.setOtpRequestedTime(new Date());
			user.setFailedAttempt(3);
			userRepository.save(user);

			boolean isOTPSentSUccessfully = sendOTPEmail(user, OTP);
			if (isOTPSentSUccessfully)
				return "We've sent an One-Time-Password (OTP) " + "to your email " + user.getEmail() + ". "
						+ "Kindly check your email and use the provided OTP to login. ";

		} else {
			return "User not found";
		}
		return null;
	}

	public boolean sendOTPEmail(Users user, String OTP) throws UnsupportedEncodingException, MessagingException {

		String siteURL = "http://localhost:8080";
		String toAddress = user.getEmail();
		String fromAddress = "Trng2@evolvingsols.com";
		String senderName = "Event Management";

		String subject = "Here's your One Time Password (OTP) - Expire in 5 minutes!";

		String content = "<p>Hello " + user.getUserName() + "</p>"
				+ "<p>For security reason, you're required to use the following " + "One Time Password to login:</p>"
				+ "<p><b>" + OTP + "</b></p>" + "<br>" + "<p>Thank you</p>";

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom(fromAddress, senderName);
		helper.setTo(toAddress);
		helper.setSubject(subject);

		helper.setText(content, true);

		mailSender.send(message);

		return true;
	}

	public LoginTemp verifyLoginWithOTPDemo(String email, String otp) {
		Users user = userRepository.findByEmail(email);
		if (user.isAccountNonLocked()) {
			if (user.getOneTimePassword().equals(otp)) {

				user.setOneTimePassword(null);
				user.setFailedAttempt(0);
				user.setOtpRequestedTime(null);
				userRepository.save(user);

				return new LoginTemp("OTP verified successfully.", user, true);
			} else {
				int remainingAttempt;
				if (user.getFailedAttempt() > 0) {
					remainingAttempt = user.getFailedAttempt() - 1;
					user.setFailedAttempt(remainingAttempt);
					userRepository.save(user);

					String msg = "You have entered wrong OTP." + "You have " + (remainingAttempt + 1)
							+ " attempt(s) to enter correct OTP.";
					return new LoginTemp(msg, null, false);

				} else {
					user.setFailedAttempt(0);
					user.setAccountNonLocked(false);
					user.setLockTime(new Date());
					user.setOneTimePassword(null);
					user.setOtpRequestedTime(null);
					userRepository.save(user);
					return new LoginTemp("Your account has been locked due to 3 failed attempts.", null, false);
				}
			}
		} else {
			return new LoginTemp("Your account is blocked.", null, false);
		}

	}

	public LoginTemp verifyLoginWithPassword(String email, String password) {
		Users user = userRepository.findByEmail(email);
			if (user.getPassword().equals(password)) 
			{

				return new LoginTemp("Login Successfull", user, true);
			} 
			else
				return new LoginTemp("Incorrect Password", null, false);
	}
	

	// Booking
	public EventBooking bookEvent(EventBooking eventBooking) {
		eventBooking.setBookingStatus(BookingStatus.PENDING);
		return eventBookingRepository.save(eventBooking);
	}

	public List<EventBooking> getBookedEventByUserId(int userId) {
		return eventBookingRepository.findBookingByUserId(userId);
	}

}