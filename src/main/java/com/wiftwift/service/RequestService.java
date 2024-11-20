package com.wiftwift.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wiftwift.entity.Request;
import com.wiftwift.entity.User;
import com.wiftwift.repository.RequestRepository;
import com.wiftwift.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Service
public class RequestService {
    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    public void submitRequest(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        Request request = new Request();
        request.setUser(user);
        request.setSubmittedAt(LocalDateTime.now());
        request.setStatus(Request.RequestStatus.PENDING);
        requestRepository.save(request);
    }

    public Optional<Request> getUserRequest(Long userId) {
        return requestRepository.findByUserId(userId).stream().findFirst(); 
    }

    public List<Request> getAllRequests() {
        return requestRepository.findAll(); 
    }

    public void acceptRequest(Long requestId) {
        Request request = requestRepository.findById(requestId).orElseThrow();
        User user = request.getUser();
        user.getRoles().add(roleService.findByName("ADMIN").get());
        userService.save(user);
        requestRepository.delete(request); 
    }

    public void rejectRequest(Long requestId) {
        requestRepository.deleteById(requestId); 
    }
}
