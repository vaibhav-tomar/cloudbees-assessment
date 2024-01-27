package com.cloudbees.assessment.service;

import com.cloudbees.assessment.api.request.ReceiptSubmitRequest;
import com.cloudbees.assessment.api.request.SeatUpdateRequest;
import com.cloudbees.assessment.api.response.ReceiptResponse;
import com.cloudbees.assessment.api.response.SeatUpdateResponse;
import com.cloudbees.assessment.api.response.UserSeatResponse;
import com.cloudbees.assessment.entity.Receipt;
import com.cloudbees.assessment.entity.User;
import com.cloudbees.assessment.enums.Section;
import com.cloudbees.assessment.exception.CustomCloudBeesException;
import com.cloudbees.assessment.repository.ReceiptRepository;
import com.cloudbees.assessment.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@SpringBootTest
class SeatingManagementServiceTest {
    @Autowired
    private SeatingManagementService seatingManagementService;
    @MockBean
    private ReceiptRepository receiptRepository;
    @MockBean
    private UserRepository userRepository;


    @Test
    void allocateSeatToUser() throws Exception {
        User user = getMockUser();
        Receipt receipt = getMockReceipt(user);
        ReceiptSubmitRequest receiptSubmitRequest = new ReceiptSubmitRequest(receipt.getFromStation(),
                receipt.getToStation(), user.getFirstName(), user.getLastName(), user.getEmail(), null);

        when(receiptRepository.save(receipt)).thenReturn(receipt);
        ReceiptResponse actual = seatingManagementService.allocateSeatToUser(receiptSubmitRequest);
        assertEquals(receipt.getFromStation(), actual.getFrom());
        assertEquals(user.getEmail(), actual.getUser().getEmail());
    }

    // fail test case when user is already inside train
    @Test
    void allocateSeatToUserFailForUserAlreadyPresent() {
        User user = getMockUser();
        Receipt receipt = getMockReceipt(user);
        ReceiptSubmitRequest receiptSubmitRequest = new ReceiptSubmitRequest(receipt.getFromStation(),
                receipt.getToStation(), user.getFirstName(), user.getLastName(), user.getEmail(), null);

        when(userRepository.findByEmail("abc1@gmail.com")).thenReturn(user);
        assertThrowsExactly(CustomCloudBeesException.class,
                () -> seatingManagementService.allocateSeatToUser(receiptSubmitRequest), "This user is already inside" +
                        " the " +
                        "train");
    }

    // fail case if there are no vacant seats
    @Test
    void allocateSeatToUserFailForNoVacantSeats() {
        User user = getMockUser();
        Receipt receipt = getMockReceipt(user);
        ReceiptSubmitRequest receiptSubmitRequest = new ReceiptSubmitRequest(receipt.getFromStation(),
                receipt.getToStation(), user.getFirstName(), user.getLastName(), user.getEmail(), null);
        List<Integer> vacantSeats = SeatingManagementService.vacantSeats;
        vacantSeats.clear();
        assertThrowsExactly(CustomCloudBeesException.class,
                () -> seatingManagementService.allocateSeatToUser(receiptSubmitRequest), "All seats have been filled");
    }

    @Test
    void getUserReceiptDetails() throws CustomCloudBeesException {
        User user = getMockUser();
        Receipt receipt = getMockReceipt(user);
        user.setReceipt(receipt);
        when(userRepository.findByEmailOrId(anyString(), anyLong())).thenReturn(user);
        ReceiptResponse actual = seatingManagementService.getUserReceiptDetails(anyLong(), anyString());
        assertEquals(receipt.getFromStation(), actual.getFrom());
        assertEquals(user.getEmail(), actual.getUser().getEmail());
        assertEquals(user.getId(), actual.getUser().getId());
    }

    // fail case if user not found
    @Test
    void getUserReceiptDetailsFailForUserNotFound() {
        User user = getMockUser();
        Receipt receipt = getMockReceipt(user);
        user.setReceipt(receipt);
        when(userRepository.findByEmailOrId(anyString(), anyLong())).thenReturn(null);
        assertThrowsExactly(CustomCloudBeesException.class,
                () -> seatingManagementService.getUserReceiptDetails(anyLong(), anyString()));
    }

    //
    @Test
    void getUserSeatDetailsBySection() {
        User user = getMockUser();
        List<User> usersBySection = List.of(user);
        when(userRepository.findBySection(any())).thenReturn(usersBySection);
        List<UserSeatResponse> actual = seatingManagementService.getUserSeatDetailsBySection(any());
        assertEquals(usersBySection.size(), actual.size());
        assertEquals(usersBySection.get(0).getEmail(), actual.get(0).getEmail());
    }

    //
    @Test
    void removeUser() throws CustomCloudBeesException {
        User user = getMockUser();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        seatingManagementService.removeUser(anyLong());
        verify(userRepository, times(1)).delete(user);

    }

    // fail case if user not found while removing user
    @Test
    void removeUserFailIfUserNotFound() {
        User user = getMockUser();
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrowsExactly(CustomCloudBeesException.class, () -> seatingManagementService.removeUser(anyLong()));
    }

    @Test
    void updateUserSeat() throws CustomCloudBeesException {
        User user = getMockUser();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        SeatUpdateResponse actual = seatingManagementService.updateUserSeat(
                new SeatUpdateRequest(user.getId(), 2));
        assertEquals(user.getSeatNumber(), actual.getNewSeat());
    }

    // fail cases if new seat is unavailable while updating user
    @Test
    void updateUserSeatFailCase() {
        User user = getMockUser();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        assertThrows(CustomCloudBeesException.class, () -> seatingManagementService.updateUserSeat(
                new SeatUpdateRequest(user.getId(), 1)));
    }


    private static Receipt getMockReceipt(User user) {
        Receipt receipt = new Receipt(1l, "pune", "delhi", user, new BigDecimal(20));
        return receipt;
    }

    private static User getMockUser() {
        User user = new User();
        user.setId(1l);
        user.setFirstName("vaibhav");
        user.setLastName("tomar");
        user.setEmail("abc1@gmail.com");
        user.setSeatNumber(1);
        user.setSection(Section.SECTION_A);
        List<Integer> vacantSeats = SeatingManagementService.vacantSeats;
        vacantSeats.remove(user.getSeatNumber());
        return user;
    }
}