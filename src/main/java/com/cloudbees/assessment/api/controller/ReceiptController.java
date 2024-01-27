package com.cloudbees.assessment.api.controller;


import com.cloudbees.assessment.api.request.ReceiptSubmitRequest;
import com.cloudbees.assessment.api.request.SeatUpdateRequest;
import com.cloudbees.assessment.api.response.ReceiptResponse;
import com.cloudbees.assessment.api.response.SeatUpdateResponse;
import com.cloudbees.assessment.api.response.UserSeatResponse;
import com.cloudbees.assessment.enums.Section;
import com.cloudbees.assessment.exception.CustomCloudBeesException;
import com.cloudbees.assessment.service.SeatingManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/receipt")
@Slf4j
@RequiredArgsConstructor
public class ReceiptController {

    private final SeatingManagementService seatingManagementService;

    @PostMapping("/submit")
    public ResponseEntity<ReceiptResponse> submitReceipt(@RequestBody @Valid ReceiptSubmitRequest receiptSubmitRequest)
            throws Exception {
        log.info("Received request to submit receipt. request: {}", receiptSubmitRequest);
        return ResponseEntity.ok(seatingManagementService.allocateSeatToUser(receiptSubmitRequest));
    }

    @GetMapping()
    public ResponseEntity<ReceiptResponse> getUserReceiptDetails(@RequestParam(required = false) Long userId,
            @RequestParam(required = false) String email)
            throws CustomCloudBeesException {
        log.info("Received request to get user receipt details for userId: {}, email: {}", userId, email);
        return ResponseEntity.ok(seatingManagementService.getUserReceiptDetails(userId, email));
    }

    @GetMapping("/{section}")
    public ResponseEntity<List<UserSeatResponse>> getUserSeatDetailsBySection(@PathVariable Section section) {
        log.info("Received request to get user seat details by section: {}", section);
        return ResponseEntity.ok(seatingManagementService.getUserSeatDetailsBySection(section));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> removeUser(@PathVariable Long id) throws CustomCloudBeesException {
        log.info("Received request to delete user for id: {}", id);
        seatingManagementService.removeUser(id);
        return ResponseEntity.ok().body("Successfully removed user");
    }

    @PatchMapping()
    public ResponseEntity<SeatUpdateResponse> updateUserSeat(@RequestBody @Valid SeatUpdateRequest seatUpdateRequest)
            throws CustomCloudBeesException {
        log.info("Received request to update user seat. Request: {}", seatUpdateRequest);
        return ResponseEntity.ok(seatingManagementService.updateUserSeat(seatUpdateRequest));
    }
}