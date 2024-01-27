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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

import static com.cloudbees.assessment.enums.Section.SECTION_A;
import static com.cloudbees.assessment.enums.Section.SECTION_B;

/**
 * @author vaibhav
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class SeatingManagementService {

    /* Static map which stores seat number vs section
    For now lets assume there are 10 seats.
    Seat Number 1 to 5 -> Section A
    Seat Number 6-10 -> Section B
    */
    private static final Map<Integer, Section> SEAT_MAP;
    private static final BigDecimal PRICE_PAID_20 = new BigDecimal(20);

    private final UserRepository userRepository;

    private final ReceiptRepository receiptRepository;

    // Static list to store to vacant seats
    static List<Integer> vacantSeats = new ArrayList<>();

    // Initialize static fields
    static {
        SEAT_MAP = new HashMap<>();
        SEAT_MAP.put(1, SECTION_A);
        SEAT_MAP.put(2, SECTION_A);
        SEAT_MAP.put(3, SECTION_A);
        SEAT_MAP.put(4, SECTION_A);
        SEAT_MAP.put(5, SECTION_A);
        SEAT_MAP.put(6, SECTION_B);
        SEAT_MAP.put(7, SECTION_B);
        SEAT_MAP.put(8, SECTION_B);
        SEAT_MAP.put(9, SECTION_B);
        SEAT_MAP.put(10, SECTION_B);
        for (int i = 1; i <= 10; i++) {
            vacantSeats.add(i);
        }
    }

    public ReceiptResponse allocateSeatToUser(ReceiptSubmitRequest request) throws Exception {
        String email = request.getEmail();
        User user = userRepository.findByEmail(email);

        // Throw exception if the user has already submitted receipt
        if (user != null) {
            throw new CustomCloudBeesException(HttpStatus.UNPROCESSABLE_ENTITY, "This user is already inside the " +
                    "train");
        }
        int totalVacantSeats = vacantSeats.size();
        if (totalVacantSeats == 0) {
            throw new CustomCloudBeesException(HttpStatus.UNPROCESSABLE_ENTITY, "All seats have been filled");
        }
        user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());

        // Pick first vacant seat from the list and assign to user
        Integer seatNumberToBeAssignedToUser = vacantSeats.get(0);
        user.setSeatNumber(seatNumberToBeAssignedToUser);
        user.setSection(SEAT_MAP.get(seatNumberToBeAssignedToUser));

        Receipt receipt = new Receipt(request.getFrom(), request.getTo(), user, request.getPrice() == null ?
                PRICE_PAID_20 : request.getPrice());
        receiptRepository.save(receipt);

        // Remove the seat assigned to user from the list of vacant seats
        vacantSeats.remove(seatNumberToBeAssignedToUser);

        return new ReceiptResponse(receipt.getId(), receipt.getFromStation(), receipt.getToStation(), user,
                receipt.getPrice());

    }

    public ReceiptResponse getUserReceiptDetails(Long userId, String email) throws CustomCloudBeesException {
        if (userId == null && StringUtils.isEmpty(email)) {
            throw new CustomCloudBeesException(HttpStatus.UNPROCESSABLE_ENTITY, "One of user id or email is mandatory");
        }
        User user = userRepository.findByEmailOrId(email, userId);
        if (user == null) {
            throw new CustomCloudBeesException(HttpStatus.NOT_FOUND, "No user found with given details");
        }
        Receipt receipt = user.getReceipt();
        return new ReceiptResponse(receipt.getId(), receipt.getFromStation(), receipt.getToStation(), user,
                receipt.getPrice());
    }

    public List<UserSeatResponse> getUserSeatDetailsBySection(Section section) {
        List<User> usersBySection = userRepository.findBySection(section);
        List<UserSeatResponse> userSeatResponses = new ArrayList<>();
        usersBySection.forEach(u -> {
            userSeatResponses.add(new UserSeatResponse(u.getId(), u.getEmail(), u.getSeatNumber(), u.getSection()));
        });
        return userSeatResponses;
    }

    public void removeUser(Long id) throws CustomCloudBeesException {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            throw new CustomCloudBeesException(HttpStatus.NOT_FOUND, "User not found with given id");
        }
        User user = userOptional.get();
        Integer seatNumber = user.getSeatNumber();
        userRepository.delete(user);

        // Add the seat of removed user to list of vacant seats
        vacantSeats.add(seatNumber);
        log.info("Successfully removed user for id: {}", id);
    }

    public SeatUpdateResponse updateUserSeat(SeatUpdateRequest seatUpdateRequest) throws CustomCloudBeesException {
        User user = userRepository.findById(seatUpdateRequest.getUserId()).orElse(null);
        if (user == null) {
            throw new CustomCloudBeesException(HttpStatus.NOT_FOUND, "User not found with given id");
        }
        Integer existingSeat = user.getSeatNumber();
        Integer newSeat = seatUpdateRequest.getNewSeat();

        // Throw exception if new seat is not vacant
        if (!vacantSeats.contains(newSeat)) {
            throw new CustomCloudBeesException(HttpStatus.BAD_REQUEST, "The given seat is already occupied");
        }
        user.setSeatNumber(newSeat);
        user.setSection(SEAT_MAP.get(newSeat));

        // remove new seat from vacant seats
        vacantSeats.remove(newSeat);

        // add the existing (old) seat of user to vacant seats
        vacantSeats.add(existingSeat);
        userRepository.save(user);
        log.info("Successfully updated user with id: {}", seatUpdateRequest.getUserId());

        return new SeatUpdateResponse(user.getId(), user.getSeatNumber());
    }
}
